package Helper;

import android.content.Context;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Toast;

import com.example.android.bluetoothlegatt.SyncActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import DataObjects.DataObject;
import DataObjects.DataObjectComparer;
import DataObjects.Location;
import SQLLocalDatabse.SQLiteDBHelper;

public class SynchronizationService {
    private static final String TAG = "SYNCSERVICE";
    private static final String KEY_SUCCESS = "success";
    private static final String KEY_PM_TEN = "pm_ten";
    private static final String KEY_PM_TWENTY_FIVE = "pm_twenty_five";
    private static final String KEY_MEASUREMENT_DATE = "measurement_date";
    private static final String KEY_LONGITUDE = "longitude";
    private static final String KEY_LATITUDE = "latitude";
    private static final String KEY_ALTITUDE = "altitude";
    private static final String KEY_DATA = "data";
    private static final String KEY_SYNCHRONIZATION_DATE = "synchronization_date";
    private static final String BASE_URL = "http://192.168.0.17/measurements/";
    private static int SUCCESS;
    private SQLiteDBHelper localDb;
    private ArrayList<String> syncDates;
    private String synchronizationDate;
    private String lastSynchronization;
    private int unsynchedValues;

    public SynchronizationService(Context context) {
        Location loc1 = new Location("12.43077537", "51.37791981", "17.8");
        DataObject o1 = new DataObject("10.12", "10.23", "2017-03-03 22:59:36", loc1);
        synchronizationDate = returnTimeStamp();
        localDb = new SQLiteDBHelper(context);
        lastSynchronization = returnLastSynchronization();
        unsynchedValues = returnUnsynchedValues().size();
    }

    private String returnLastSynchronization() {
        HttpJsonParser httpJsonParser = new HttpJsonParser();
        JSONObject jsonObject = httpJsonParser.makeHttpRequest(
                BASE_URL + "list_measurements.php", "POST", null);
        try {
            int success = jsonObject.getInt(KEY_SUCCESS);
            JSONArray measurements;
            if (success == 1) {
                syncDates = new ArrayList<>();
                measurements = jsonObject.getJSONArray(KEY_DATA);
                //Iterate through the response and populate movies list
                for (int i = 0; i < measurements.length(); i++) {
                    JSONObject measurement = measurements.getJSONObject(i);
                    String synchro_date = measurement.getString(KEY_SYNCHRONIZATION_DATE);
                    syncDates.add(synchro_date);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return syncDates.get(syncDates.size() - 1);
    }

    public void synchronizeData(){
        ArrayList<DataObject> unsynchedList = returnUnsynchedValues();

        for(DataObject obj: unsynchedList){
            addDataObject(obj);
        }

        lastSynchronization = returnLastSynchronization();
        Log.d(TAG, "synchronizeData: " + lastSynchronization);
        unsynchedValues = returnUnsynchedValues().size();
        Log.d(TAG, "synchronizeData: " + unsynchedValues);
    }

    private ArrayList<DataObject> returnUnsynchedValues() {
        ArrayList<DataObject> localDbData = localDb.getItems();
        ArrayList<DataObject> unsynchedDataObjects = new ArrayList<>();

        for(DataObject obj: localDbData){
            if(obj.getMeasurementDate().compareTo(lastSynchronization) > 0){
                unsynchedDataObjects.add(obj);
            }
        }

        return unsynchedDataObjects;
    }

    private void addDataObject(DataObject obj) {
        HttpJsonParser httpJsonParser = new HttpJsonParser();
        Map<String, String> httpParams = new HashMap<>();
        //Populating request parameters

        httpParams.put(KEY_PM_TEN, obj.getPmTen());
        httpParams.put(KEY_PM_TWENTY_FIVE, obj.getPmTwentyFive());
        httpParams.put(KEY_MEASUREMENT_DATE, obj.getMeasurementDate());
        httpParams.put(KEY_LONGITUDE, obj.getLocation().getLongitude());
        httpParams.put(KEY_LATITUDE, obj.getLocation().getLatitude());
        httpParams.put(KEY_ALTITUDE, obj.getLocation().getAltitude());
        httpParams.put(KEY_SYNCHRONIZATION_DATE, synchronizationDate);

        Log.d(TAG, "addDataObject: ------------ " + httpParams.size());

        for (int i = 0; i < httpParams.size(); i++) {
            Log.d(TAG, "addDataObject: " + httpParams.get(i));
        }

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

    public int getSuccess() {
        return SUCCESS;
    }

    public String getLastSyncDate(){
        return lastSynchronization;
    }

    private String returnTimeStamp() {
        Calendar cal = Calendar.getInstance();
        Date date = cal.getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = dateFormat.format(date);

        return formattedDate;
    }

    public int getUnsynchedValues() {
        return unsynchedValues;
    }
}