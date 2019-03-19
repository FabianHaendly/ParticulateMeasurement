package Services;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import Database.SQLiteDBHelper;
import Entities.MeasurementObject;

import static java.util.Calendar.HOUR_OF_DAY;
import static java.util.Calendar.MINUTE;

public class SynchronizationOpenSenseMapService {
    private static final String TAG = "OpenSenseMapService";

    //SensorBox data
    private static final String SENSOR_BOX_ID = "5c8f789b922ca9001982b1e8";
    private static final String SENSOR_PM_TEN_ID = "5c8f789b922ca9001982b1ea";
    private static final String SENSOR_PM_TWENTY_FIVE_ID = "5c8f789b922ca9001982b1e9";

    //URLs
    private static final String BASE_URL = "https://api.opensensemap.org/boxes/";
    private static final String MULTIPLE_MEASUREMENT_URL = BASE_URL + SENSOR_BOX_ID + "/data";

    //JsonObject params
    private static final String SENSOR_ID = "sensor";
    private static final String VALUE = "value";
    private static final String CREATED_AT = "createdAt";
    private static final String LOCATION = "location";

    private String SyncSuccessMessage = "";
    private SQLiteDBHelper localDb;
    private Context context;
    ArrayList<MeasurementObject> measurementObjects;

    public SynchronizationOpenSenseMapService(Context context) {

        localDb = new SQLiteDBHelper(context);
        measurementObjects = localDb.getItems();
        this.context = context;

    }


    public void sendPost() throws InterruptedException {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    URL url = new URL(MULTIPLE_MEASUREMENT_URL);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                    conn.setRequestProperty("Accept", "application/json");
                    conn.setDoOutput(true);
                    conn.setDoInput(true);

                    JSONArray multipleMeasurements = new JSONArray();

                    if (getNumOfUnsynchedValues() > 0) {

                        for (int i = 0; i < measurementObjects.size(); i++) {
                            MeasurementObject currObject = measurementObjects.get(i);
                            String measurementDate = currObject.getMeasurementDate();

                            if (isDateOlderThanAnHour(measurementDate)) {
                                JSONArray locationCoords = new JSONArray();
                                JSONObject jsonObjectPmTen = new JSONObject();
                                JSONObject jsonObjectPmTwentyFive = new JSONObject();

                                //fill location array
                                if (currObject.getLocation().getLatitude() != "0.0" && currObject.getLocation().getLongitude() != "0.0") {
                                    locationCoords.put(Double.valueOf(currObject.getLocation().getLatitude()));
                                    locationCoords.put(Double.valueOf(currObject.getLocation().getLongitude()));
                                    locationCoords.put(Double.valueOf(currObject.getLocation().getAltitude()));
                                }
                                String rfcDateOfCurrentObj = DateService.getRFCFormattedDate(currObject.getMeasurementDate());

                                //PM10 object
                                jsonObjectPmTen.put(SENSOR_ID, SENSOR_PM_TEN_ID);
                                jsonObjectPmTen.put(VALUE, currObject.getPmTen());
                                jsonObjectPmTen.put(CREATED_AT, rfcDateOfCurrentObj);
                                if (locationCoords.length() > 0) {
                                    jsonObjectPmTen.put(LOCATION, (Object) locationCoords);
                                }
                                //PM25 object
                                jsonObjectPmTwentyFive.put(SENSOR_ID, SENSOR_PM_TWENTY_FIVE_ID);
                                jsonObjectPmTwentyFive.put(VALUE, currObject.getPmTwentyFive());
                                jsonObjectPmTwentyFive.put(CREATED_AT, rfcDateOfCurrentObj);
                                if (locationCoords.length() > 0) {
                                    jsonObjectPmTwentyFive.put(LOCATION, (Object) locationCoords);
                                }

                                multipleMeasurements.put(jsonObjectPmTen);
                                multipleMeasurements.put(jsonObjectPmTwentyFive);
                            }
                        }

                        Log.i("JSON", multipleMeasurements.toString());

                        if (multipleMeasurements.length() > 0) {
                            DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                            os.writeBytes(multipleMeasurements.toString());
                            os.flush();
                            os.close();
                        }

                        Log.i("STATUS", String.valueOf(conn.getResponseCode()));
                        Log.i("MSG", conn.getResponseMessage());
                        SyncSuccessMessage = multipleMeasurements.length() + " elements have been synchronized";

                        // Saving Sync Date
                        FileService fs = new FileService(context, 1);
                        fs.saveLatestSyncDate(DateService.getCurrentDateAndTime());
                        Log.d(TAG, "Saved to " + context.getFilesDir() + "/");

                        conn.disconnect();
                    } else {
                        SyncSuccessMessage = "Everything up to date";
                    }
                } catch (Exception e) {
                    SyncSuccessMessage = "Error in sychnronizing with openSenseMap";
                    e.printStackTrace();
                }
            }
        });

        thread.start();
        thread.join();

        Toast.makeText(context, SyncSuccessMessage, Toast.LENGTH_LONG).show();
    }

    public String getLastOsmSync() {
        FileService fs = new FileService(context, 1);
        return fs.getLastSyncDate();
    }

    public int getNumOfUnsynchedValues() throws ParseException {
        ArrayList<MeasurementObject> list = localDb.getItems();

//        Log.d(TAG, "getNumOfUnsynchedValues: DB ITEM FROM OSM " + list.size());

        list = getRelevantMeasurements(list);

//        Log.d(TAG, "getNumOfUnsynchedValues: RELEVANT " + list.size());

        return list.size();
    }

    private ArrayList<MeasurementObject> getRelevantMeasurements(ArrayList<MeasurementObject> list) throws ParseException {

        ArrayList<MeasurementObject> filteredList = new ArrayList<>();
        FileService fs = new FileService(context, 1);
        String lastOsmSync = fs.getLastSyncDate();

        for (int i = 0; i < list.size(); i++) {
            String mD = list.get(i).getMeasurementDate();

            if (mD.compareTo(lastOsmSync) > 0 && isDateOlderThanAnHour(mD)) {
                Log.d(TAG, "getRelevantMeasurements: ITEM ADDED UNSYCHED OSM");
                filteredList.add(list.get(i));
            }
        }

        return filteredList;
    }

    /*
    Checks if measurement date is is at least 60 minutes younger than the utc0 time from now
     */
    boolean isDateOlderThanAnHour(String measurementDate) throws ParseException {

        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        cal = Calendar.getInstance();
        cal.add(Calendar.HOUR, -1);



        // if measurementdate < utc+0 -> measurement is valid for sync
        if (measurementDate.compareTo(sdf.format(cal.getTime())) < 0) {

            Log.d(TAG, "TRUE - DATE TO CHECK: " + measurementDate + " is < " + "CURRENT UTC+0: " + sdf.format(cal.getTime()));
            return true;
        }

        Log.d(TAG, "FALSE - DATE TO CHECK: " + measurementDate + " is > " + "CURRENT UTC+0: " + sdf.format(cal.getTime()));

        return false;
    }

    public String getSensorBoxId() {
        return SENSOR_BOX_ID;
    }

    public String getUrl() {
        return BASE_URL;
    }
}
