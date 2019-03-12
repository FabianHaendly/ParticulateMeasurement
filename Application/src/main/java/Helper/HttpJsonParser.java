package Helper;

import android.net.Uri;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Map;

public class HttpJsonParser {
    private static String TAG = "JSONPARSER";

    static InputStream is = null;
    static JSONObject jObj = null;
    static String json = "";
    HttpURLConnection urlConnection = null;

    // function get json from url
    // by making HTTP POST or GET method
    public JSONObject makeHttpRequest(String url, String method,
                                      Map<String, String> params) {

        Log.d(TAG, "Method Parameters: " + url);
        Log.d(TAG, "Method Parameters: " + method);

        try {
            Uri.Builder builder = new Uri.Builder();
            URL urlObj;
            String encodedParams = "";
            if (params != null) {
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    builder.appendQueryParameter(entry.getKey(), entry.getValue());
                }

                //?year=1999&genre=rrrrr&rating=5&movie_name=aaaaaa
                //works: ?pm_ten=10.0&altitude=17.8&pm_twenty_five=10.0&measurement_date=2017-03-03%2022%3A59%3A36&latitude=51.37791981&longitude=12.43077537
                Log.d(TAG, "makeHttpRequest: " + builder.toString());

            }
            if (builder.build().getEncodedQuery() != null) {
                encodedParams = builder.build().getEncodedQuery();
            }

            Log.d(TAG, "ENCODED PARAMETERS " + encodedParams);


            urlObj = new URL(url);

            //works: http://192.168.0.17/measurements/add_measurement.php
            Log.d(TAG, "URL: " + url);

            urlConnection = (HttpURLConnection) urlObj.openConnection();
            urlConnection.setRequestMethod(method);
            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            urlConnection.setRequestProperty("Content-Length", String.valueOf(encodedParams.getBytes().length));
            urlConnection.getOutputStream().write(encodedParams.getBytes());

            Log.d("POST!!!!", "makeHttpRequest: " + urlConnection.toString());

            urlConnection.connect();
            is = urlConnection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();
            json = sb.toString();

            //{"success":1,"message":"Movie Successfully Added"}
            //TODO
            Log.d(TAG, "JSON: " + json);

            jObj = new JSONObject(json);


        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            Log.e("JSON Parser", "Error parsing data " + e.toString());
        } catch (Exception e) {
            Log.e("Exception", "Error parsing data " + e.toString());
        }

        // return JSON String
        return jObj;

    }
}