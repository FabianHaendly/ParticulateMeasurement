package com.example.android.bluetoothlegatt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import BLEHelper.DeviceScanActivity;

public class MainActivity extends Activity {
    private static String TAG = "New My Code";
    private static final int REQUEST_ENABLE_BT = 99;
    BluetoothAdapter mBluetoothAdapter;
    BluetoothDevice mBtDevice;
    Button mOnOffBtn;
    Button mSearchDevBtn;
    Button mStartMeasurementBtn;
    Button mStatisticsBtn;
    TextView mConnectionInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_view);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        View onOffLayout = findViewById(R.id.switch_btn);
        mOnOffBtn = (Button) onOffLayout.findViewById(R.id.onOff_btn);
        if (mBluetoothAdapter.isEnabled())
            mOnOffBtn.setText("TURN OFF");
        else
            mOnOffBtn.setText("TURN ON");

        mSearchDevBtn = findViewById(R.id.search_devices_btn);
        mConnectionInfo = findViewById(R.id.display_connection_info);
        mStartMeasurementBtn = findViewById(R.id.start_measurement_btn);
        mStatisticsBtn = findViewById(R.id.statistics_btn);

        onOffBtnListen();
        onSearchDevicesBtn();
        onStartMeasurementBtn();
        onStatisticBtn();
    }

    private void onOffBtnListen(){
        mOnOffBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: ON/OFF btn clicked");
                enableBT();
            }
        });
    }

    private void onSearchDevicesBtn(){
        mSearchDevBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: SEARCH btn clicked");
                if(mBluetoothAdapter.isEnabled())
                {
                    //GO TO SCAN
                    startActivityForResult(new Intent(MainActivity.this, DeviceScanActivity.class), 1);
                }
                else{
                    Toast.makeText(getBaseContext(), "Turn on Bluetooth first", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void onStartMeasurementBtn(){
        mStartMeasurementBtn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                if(mBtDevice == null){
                    Toast.makeText(getBaseContext(), "Please connect to a device first", Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent passArgIntent = new Intent(MainActivity.this, MeasurementActivity.class);
                passArgIntent.putExtra("device", mBtDevice);
                startActivity(passArgIntent);
            }
        });
    }

    private void onStatisticBtn(){
        mStatisticsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, StatisticsActivity.class);
                startActivity(intent);
            }
        });
    }

    private final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(mBluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, mBluetoothAdapter.ERROR);

                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG, "onReceive: STATE OFF");
                        mOnOffBtn.setText("TURN ON");
                        mConnectionInfo.setText("Connected: - to Device:");
                        Toast.makeText(getBaseContext(), "Bluetooth turned off", Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG, "onReceive: STATE TURNING OFF");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG, "onReceive: STATE ON");
                        mOnOffBtn.setText("TURN OFF");
                        Toast.makeText(getBaseContext(), "Bluetooth turned on", Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG, "onReceive: STATE TURNING ON");
                        break;
                }
            }
        }
    };

    public void enableBT() {
        if (mBluetoothAdapter == null) {
            Log.d(TAG, "enableDisableBT: Does not have BT capabilities");
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Log.d(TAG, "enableBT: enabling BT");
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBTIntent, REQUEST_ENABLE_BT);

//          Intercepts changes in BT status + Log / ACTION_REQUEST_ENABLE is the state change
            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
//          BroadcastReceiver catches the state change aka ACTION_STATE_CHANGED
            registerReceiver(mBroadcastReceiver1, BTIntent);
        }
        if (mBluetoothAdapter.isEnabled()) {
            Log.d(TAG, "enableBT: disabling BT");
            mBluetoothAdapter.disable();

            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroadcastReceiver1, BTIntent);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        Log.d(TAG, "onActivityResult: CATCHES BT ON OR OFF FROM USER DECISION");
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            Toast.makeText(getBaseContext(), "No measurement possible", Toast.LENGTH_SHORT).show();
            return;
        }

        if (requestCode == 1) {
            if(resultCode == Activity.RESULT_OK){
                String conStatus = data.getStringExtra("resultDevScan");
                mBtDevice = data.getParcelableExtra("device");
                Toast.makeText(getBaseContext(), "Data: " + conStatus + " Device name: " + mBtDevice.getName(), Toast.LENGTH_SHORT).show();
                mConnectionInfo.setText("Connection: true - to Device: " + mBtDevice.getName());
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(getBaseContext(), "Something went wrong ", Toast.LENGTH_SHORT).show();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: called");
        super.onDestroy();
        try {
            unregisterReceiver(mBroadcastReceiver1);
        } catch (IllegalArgumentException e) {
            Log.d(TAG, "onDestroy: Receiver 1 was not registered - " + e.getMessage());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        menu.findItem(R.id.menu_stop).setVisible(false);
        menu.findItem(R.id.menu_refresh).setVisible(false);
        menu.findItem(R.id.menu_scan).setVisible(false);

        return true;
    }
}


