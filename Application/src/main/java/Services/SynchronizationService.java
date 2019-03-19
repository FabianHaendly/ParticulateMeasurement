package Services;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import Entities.MeasurementObject;
import Helper.HttpJsonParser;
import Database.SQLiteDBHelper;

public class SynchronizationService {
    private static final String TAG = "SYNCSERVICE";
    private static final String KEY_SUCCESS = "success";
    private static final String KEY_PM_TEN = "pm_ten";
    private static final String KEY_PM_TWENTY_FIVE = "pm_twenty_five";
    private static final String KEY_MEASUREMENT_DATE = "measurement_date";
    private static final String KEY_LONGITUDE = "longitude";
    private static final String KEY_LATITUDE = "latitude";
    private static final String KEY_ALTITUDE = "altitude";
    private static final String KEY_SENSOR_ID = "sensor_id";
    private static final String KEY_DATA = "data";
    public static final String BASE_URL = "http://192.168.0.17/measurements/";
    private static int SUCCESS;
    private Context context;
    private SQLiteDBHelper localDb;

    public SynchronizationService(Context context, SQLiteDBHelper db) throws Exception {
        this.context = context;

        if(isURLReachable(context)) {
            Log.d(TAG, "SynchronizationService: URL IS REACHABLE");
            localDb = db;
        }
        else{
            Toast.makeText(context, "Server is not reachable", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "SynchronizationService: URL IS NOT REACHABLE");
            throw new Exception(BASE_URL + " is not reachable");
        }
    }

    static public boolean isURLReachable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            try {
                URL url = new URL(BASE_URL);
                HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
                urlc.setConnectTimeout(1 * 1000);
                urlc.connect();
                if (urlc.getResponseCode() == 200) {        // 200 = "OK" code (http connection is fine).
                    Log.d("Connection", "Success !");
                    return true;
                } else {
                    return false;
                }
            } catch (MalformedURLException e1) {
                return false;
            } catch (IOException e) {
                return false;
            }
        }
        return false;
    }

    public String getLastSynchronization(){
        FileService fs = new FileService( context,2);
        return fs.getLastSyncDate();
    }

    public void synchronizeData() throws ParseException {
        ArrayList<MeasurementObject> dbdata = localDb.getItems();

        for(MeasurementObject obj: getRelevantMeasurements(dbdata)){
            addDataObject(obj);
        }

        FileService fs = new FileService(context, 2);
        fs.saveLatestSyncDate(DateService.getCurrentDateAndTime());
    }

    public int getNumOfUnsychedValues() throws ParseException {
        ArrayList<MeasurementObject> list = getRelevantMeasurements(localDb.getItems());
        Log.d(TAG, "#relevantList: " + list.size());

        return list.size();
    }

    private ArrayList<MeasurementObject> getRelevantMeasurements(ArrayList<MeasurementObject> list) throws ParseException {

        ArrayList<MeasurementObject> filteredList = new ArrayList<>();
        FileService fs = new FileService(context, 2);
        String lastOsmSync = fs.getLastSyncDate();

        for (int i = 0; i < list.size(); i++) {
            String mD = list.get(i).getMeasurementDate();

            if (mD.compareTo(lastOsmSync) > 0 && mD.compareTo(DateService.getCurrentDateAndTime()) < 0) {
                filteredList.add(list.get(i));
            }
        }

        return filteredList;
    }

    private void addDataObject(MeasurementObject obj) {
        HttpJsonParser httpJsonParser = new HttpJsonParser();
        Map<String, String> httpParams = new HashMap<>();
        //Populating request parameters

        httpParams.put(KEY_PM_TEN, obj.getPmTen());
        httpParams.put(KEY_PM_TWENTY_FIVE, obj.getPmTwentyFive());
        httpParams.put(KEY_MEASUREMENT_DATE, obj.getMeasurementDate());
        httpParams.put(KEY_LONGITUDE, obj.getLocation().getLongitude());
        httpParams.put(KEY_LATITUDE, obj.getLocation().getLatitude());
        httpParams.put(KEY_ALTITUDE, obj.getLocation().getAltitude());
        httpParams.put(KEY_SENSOR_ID, obj.getSensorId());

        JSONObject jsonObject = httpJsonParser.makeHttpRequest(
                BASE_URL + "add_measurement.php", "POST", httpParams);

        try {
            SUCCESS = jsonObject.getInt(KEY_SUCCESS);
        } catch (JSONException e) {
            SUCCESS = 0;
            e.printStackTrace();
        } catch (NullPointerException e) {
            SUCCESS = 0;
            e.printStackTrace();
        }
    }

    /*

    MySQL table

   CREATE TABLE IF NOT EXISTS `measurements` (
  `measurement_id` int(11) NOT NULL AUTO_INCREMENT,
  `pm_ten` varchar(10) NOT NULL,
  `pm_twenty_five` varchar(10) NOT NULL,
  `measurement_date` datetime NOT NULL,
  `longitude` varchar(50) NOT NULL,
  `latitude` varchar(50) NOT NULL,
  `altitude` varchar(50) NOT NULL,
  `sensor_id` int(11) NOT NULL,
  PRIMARY KEY (`measurement_id`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=1;
*/
}