package com.example.android.bluetoothlegatt;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.app.Activity;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import DataObjects.DataObject;
import DataObjects.Location;


public class MeasurementActivity extends Activity {
    //Measurement Activity Member
    private static String TAG = "MEASUREMENT_ACTIVITY: ";
    TextView mTvDeviceName;
    TextView mPmTenValue;
    TextView mPmTwentyFiveValue;
    BluetoothDevice mBtDevice;
    private BluetoothLeService mBluetoothLeService;
    private TextView mTvConnectionStatus;
    Button mStartMeasurementBtn;
    boolean mMeasurementStarted = false;
    private final String PM_PATTERN = "PM(10|25)[0-9]+.[0-9]{2}";
    LineChart mLineChart;
    GraphService mGraphService;

    //Location Activity Member
    LocationManager locationManager;
    LocationListener locationListener;
    private DataObject mDataObject;
    private Location mLocation;
    float mLongitude;
    float mLatitude;
    float mAltitude;
    String mTimeStamp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_measurement);

        Intent intent = getIntent();
        mTvDeviceName = findViewById(R.id.tvDeviceName);
        mBtDevice = intent.getParcelableExtra("device");
        mTvDeviceName.setText(mBtDevice.getName());
        mTvConnectionStatus = findViewById(R.id.tvConnectionStatus);
        mStartMeasurementBtn = findViewById(R.id.startMeasurementBtn);

        mPmTenValue = findViewById(R.id.tvPmTenValue);
        mPmTwentyFiveValue = findViewById(R.id.tvPmTwentyFiveValue);

        mLineChart = findViewById(R.id.lineChart);
        mGraphService = new GraphService(mLineChart);

        mStartMeasurementBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMeasurementStarted == false) {
                    mMeasurementStarted = true;
                    mStartMeasurementBtn.setText(R.string.stop_measure);
                } else {
                    mMeasurementStarted = false;
                    mStartMeasurementBtn.setText(R.string.start_measure);
                }

                if (mMeasurementStarted) {
                    Intent gattServiceIntent = new Intent(MeasurementActivity.this, BluetoothLeService.class);
                    bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
                } else {
                    Log.d(TAG, "onClick: STOP MEASUREMENT ");
                }
            }
        });

        mLocation = new Location((float) 0.0, (float) 0.0, (float) 0.0);

        locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(android.location.Location location) {

                if (location == null) {
                    mLocation = new Location((float) 0.0, (float) 0.0, (float) 0.0);
                    Log.d(TAG, "onLocationChanged: GOOGLES LOCATION WAS NULL");
                } else {
                    mLongitude = (float) location.getLongitude();
                    mLatitude = (float) location.getLatitude();
                    mAltitude = (float) location.getAltitude();
                    mLocation = new Location(mLongitude, mLatitude, mAltitude);
                    Log.d(TAG, "onLocationChanged: LOCATION SET");
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                Log.d(TAG, "onStatusChanged: STATUS CHANGE");
            }

            @Override
            public void onProviderEnabled(String provider) {
                Log.d(TAG, "onProviderEnabled: PROVIDER ENABLED");
            }

            @Override
            public void onProviderDisabled(String provider) {
                Log.d(TAG, "onProviderDisabled: PROVIDER DISABLED");
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

            return;
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5, locationListener);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5, locationListener);
            }
        }
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

    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mBtDevice.getAddress());
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }


    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                updateConnectionState("Connected");
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                updateConnectionState("Disconnected");
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                //displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            }
        }
    };


    private void displayData(String data) {
        if (data != null) {

            if (!mMeasurementStarted) {
                mPmTenValue.setText("   -   ");
                mPmTwentyFiveValue.setText("   -   ");
                return;
            }

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

            mPmTenValue.setText(pm10);
            mPmTwentyFiveValue.setText(pm25);

            if (pm10 != "" && pm25 != "") {
                mGraphService.initializeGraph(Float.valueOf(pm10), Float.valueOf(pm25));
                mTimeStamp = returnTimeStamp();
            }

            mDataObject = new DataObject(Float.valueOf(pm10), Float.valueOf(pm25), mTimeStamp, mLocation);
            Log.d(TAG, "DATAOBJECT: " + "PM10: " + pm10 + " PM25: " + pm25 + " Date: " + mTimeStamp);
            Log.d(TAG, "Location: Longitude: " + mLocation.getLongitude() + " Latitude: " + mLocation.getLatitude() + " Altitude: " + mLocation.getAltitude());
        }
    }

    private String returnTimeStamp() {
        Calendar cal = Calendar.getInstance();
        Date date = cal.getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = dateFormat.format(date);

        return formattedDate;
    }

    private void updateConnectionState(final String state) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTvConnectionStatus.setText(state);
            }
        });
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
}
