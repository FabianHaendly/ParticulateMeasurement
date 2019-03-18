package Services;

import android.content.Context;
import android.media.audiofx.Equalizer;
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
import java.util.FormatFlagsConversionMismatchException;

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
    private Context con;

    public SynchronizationOpenSenseMapService() {
    }

    public SynchronizationOpenSenseMapService(Context context) {

        localDb = new SQLiteDBHelper(context);
        ArrayList<MeasurementObject> list = localDb.getItems();
        Log.d(TAG, "SynchronizationOpenSenseMapService: " + list.size());
        con = context;

        try {
            sendPost(list);
        }
        catch(InterruptedException e){
            e.printStackTrace();
        }
    }


    public void sendPost(final ArrayList<MeasurementObject> list) throws InterruptedException {
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
                        MeasurementObject currObject = list.get(i);
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
                            String rfcDateOfCurrentObj = DateFormatterService.returnRFCFormattedDate(currObject.getMeasurementDate());

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

                    conn.disconnect();
                } catch (Exception e) {
                    SyncSuccessMessage = "Error in sychnronizing with openSenseMap";
                    e.printStackTrace();
                }
            }
        });

        thread.start();
        thread.join();

        Toast.makeText(con, SyncSuccessMessage, Toast.LENGTH_LONG).show();
    }

    /*
    Checks if measurement date is is at least 60 minutes younger than the utc0 time from now
     */
    boolean isDateOlderThanAnHour(String measurementDate) throws ParseException {

        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date temp = sdf.parse(measurementDate);
        cal.setTime(temp);
        int measurementMinutes = cal.get(MINUTE);
        int measurementHour = cal.get(HOUR_OF_DAY);

        cal = Calendar.getInstance();
        cal.add(Calendar.HOUR, -1);
        int osmMinutes = cal.get(MINUTE);
        int osmHour = cal.get(HOUR_OF_DAY);

        if (measurementHour < osmHour) {
            // Fall 1: Stunde ist KLEINER als derzeitige Zeit (-1); Minuten sind dann egal
            // Messzeit 12:59 - akt. Zeit 14:01
            // true
            return true;
        } else if (measurementHour <= osmHour && measurementMinutes <= osmMinutes) {
            // Fall 2: Stunde ist KLEINER GLEICH der derzeitigen Zeit (-1); Minuten sind KLEINER GLEICH der jetzigen Zeit
            // Messzeit 13:01 - akt. Zeit 14:01
            // true
            return true;
        } else if (measurementHour <= osmHour && measurementMinutes > osmMinutes) {
            // Fall 3: Stunde ist KLEINER GLEICH der derzeitigen Zeit (-1); Minuten sind GRÖßER als die der jetzigen Zeit
            // Messzeit 12:02 - akt. Zeit 13:01
            // false
            return false;
        } else {
            // Fall 4: Stunde ist GRÖßER der derzeitigen Zeit (-1); Minuten sind egal
            // Messzeit 13:01 - akt. Zeit 13:30
            // false
            return false;
        }
    }

    public String getSensorBoxId() {
        return SENSOR_BOX_ID;
    }

    public String getUrl() {
        return BASE_URL;
    }
}
