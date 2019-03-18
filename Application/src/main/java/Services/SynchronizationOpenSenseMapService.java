package Services;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import Entities.MeasurementObject;

public class SynchronizationOpenSenseMapService {
    private static final String TAG = "OpenSenseMapService";

    //SensorBox data
    private static final String SENSOR_BOX_ID = "5c8e80d0922ca900193a229c";
    private static final String SENSOR_PM_TEN_ID = "5c8e80d0922ca900193a229e";
    private static final String SENSOR_PM_TWENTY_FIVE_ID = "5c8e80d0922ca900193a229d";

    //URLs
    private static final String BASE_URL = "https://api.opensensemap.org/boxes/";
    private static final String MULTIPLE_MEASUREMENT_URL = BASE_URL + SENSOR_BOX_ID + "/data";

    //JsonObject params
    private static final String SENSOR_ID = "sensor";
    private static final String VALUE = "value";
    private static final String CREATED_AT = "createdAt";
    private static final String LOCATION = "location";

    private String SyncSuccessMessage = "";

    public SynchronizationOpenSenseMapService(){}

    public SynchronizationOpenSenseMapService(ArrayList<MeasurementObject> list){
        Log.d(TAG, "SynchronizationOpenSenseMapService: " + list.size());

        //sendPost(list);
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
