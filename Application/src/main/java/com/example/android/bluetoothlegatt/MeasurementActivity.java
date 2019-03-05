package com.example.android.bluetoothlegatt;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MeasurementActivity extends Activity {
    private static String TAG = "MEASUREMENT: ";
    TextView mTvDeviceName;
    BluetoothDevice mBtDevice;
    private BluetoothLeService mBluetoothLeService;
    private boolean mConnected = false;
    private TextView mTvConnectionStaus;
    private final String PM_PATTERN = "PM(10|25)[0-9]+.[0-9]{2}";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_measurement);

        Intent intent = getIntent();
        mTvDeviceName = findViewById(R.id.tvDeviceName);
        mBtDevice = intent.getParcelableExtra("device");
        mTvDeviceName.setText(mBtDevice.getName());
        mTvConnectionStaus = findViewById(R.id.tvConnectionStaus);


    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mBtDevice.getAddress());
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

    }


    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                updateConnectionState(R.string.connected);
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                updateConnectionState(R.string.disconnected);
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                //displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                //displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            }
        }
    };


    private void displayData(String data) {
        if (data != null) {

            ArrayList<String> allMatches = new ArrayList<>();
            Matcher matches = Pattern.compile(PM_PATTERN).matcher(data);
            while (matches.find()) {
                allMatches.add(matches.group());
            }

            String pm10 = "";
            String pm25 = "";

            ArrayList<String> finalValues = new ArrayList<>();

            for (int i = 0; i < allMatches.size(); i++) {
                finalValues.add(allMatches.get(i).replaceAll("PM(10|25)", ""));
            }

            if (finalValues.size() == 2) {
                pm10 = finalValues.get(0);
                pm25 = finalValues.get(1);
            }

            data = "PM10: " + pm10 + " - PM25: " + pm25;

            //mDataField.setText(data);
        }
    }

    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTvConnectionStaus.setText(resourceId);
            }
        });
    }
}
