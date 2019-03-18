package Services;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;

import Database.SQLiteDBHelper;
import Entities.MeasurementObject;

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

    public SynchronizationOpenSenseMapService(){}

    public SynchronizationOpenSenseMapService(Context context){

        localDb = new SQLiteDBHelper(context);
        ArrayList<MeasurementObject> list = localDb.getItems();
        Log.d(TAG, "SynchronizationOpenSenseMapService: " + list.size());

        sendPost(list);
    }


    public void sendPost(final ArrayList<MeasurementObject> list) {
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

                    for (int i = 0; i < list.size(); i++) {
                        JSONArray locationCoords = new JSONArray();
                        JSONObject jsonObjectPmTen = new JSONObject();
                        JSONObject jsonObjectPmTwentyFive = new JSONObject();
                        MeasurementObject currObject = list.get(i);

                        //fill location array
                        locationCoords.put(Double.valueOf(currObject.getLocation().getLatitude()));
                        locationCoords.put(Double.valueOf(currObject.getLocation().getLongitude()));
                        locationCoords.put(Double.valueOf(currObject.getLocation().getAltitude()));

                        String openSenseDateFormat = DateFormatterService.returnRFCFormattedDate(currObject.getMeasurementDate());

                        //PM10 object
                        jsonObjectPmTen.put(SENSOR_ID, SENSOR_PM_TEN_ID);
                        jsonObjectPmTen.put(VALUE, currObject.getPmTen());
                        jsonObjectPmTen.put(CREATED_AT, openSenseDateFormat);
                        jsonObjectPmTen.put(LOCATION, (Object)locationCoords);

                        //PM25 object
                        jsonObjectPmTwentyFive.put(SENSOR_ID, SENSOR_PM_TWENTY_FIVE_ID);
                        jsonObjectPmTwentyFive.put(VALUE, currObject.getPmTwentyFive());
                        jsonObjectPmTwentyFive.put(CREATED_AT, openSenseDateFormat);
                        jsonObjectPmTwentyFive.put(LOCATION, (Object)locationCoords);

                        multipleMeasurements.put(jsonObjectPmTen);
                        multipleMeasurements.put(jsonObjectPmTwentyFive);
                    }

                    Log.i("JSON", multipleMeasurements.toString());
                    DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                    os.writeBytes(multipleMeasurements.toString());

                    os.flush();
                    os.close();

                    Log.i("STATUS", String.valueOf(conn.getResponseCode()));
                    Log.i("MSG", conn.getResponseMessage());
                    SyncSuccessMessage = String.valueOf(conn.getResponseCode()) + conn.getResponseMessage();

                    conn.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
    }

    public String getSyncSuccessMessage() {
        return SyncSuccessMessage;
    }

    public String getSensorBoxId(){
        return SENSOR_BOX_ID;
    }

    public String getUrl(){
        return BASE_URL;
    }
}
