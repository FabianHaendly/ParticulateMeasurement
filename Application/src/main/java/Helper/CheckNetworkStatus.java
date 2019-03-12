package Helper;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;


public class CheckNetworkStatus {

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        boolean check = activeNetworkInfo != null && activeNetworkInfo.isConnected();

        Log.d("NETWORK", "isNetworkAvailable: " + check);

        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}