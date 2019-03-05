package com.example.android.bluetoothlegatt;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MeasurementActivity extends Activity {
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

    LineChart lineChart;
    Button mAddBtn;
    ArrayList<Entry> yPmTen;
    LineDataSet dataSetTen;
    ArrayList<Entry> yPmTwentyFive;
    LineDataSet dataSetTwentyFive;
    ArrayList<ILineDataSet> dataSets;
    private float mPm10;
    private float mPm25;


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

        //Properties for graph
        yPmTen = new ArrayList<>();
        yPmTwentyFive = new ArrayList<>();
        dataSets = new ArrayList<>();

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
    }

    private void doGraphStuff() {
        lineChart = (LineChart) findViewById(R.id.lineChart);
        lineChart = returnLineChartWithFormatting(lineChart, 21);

        if (yPmTen.size() > 20 && yPmTwentyFive.size() > 20) {

            yPmTen = returnBufferedList(yPmTen);
            yPmTwentyFive = returnBufferedList(yPmTwentyFive);

            Log.d("BUFFER", "onClick: Ten: " + yPmTen.size());
        }



        yPmTen.add(new Entry(yPmTen.size(), mPm10));
        yPmTwentyFive.add(new Entry(yPmTwentyFive.size(), mPm25));

        dataSetTen = returnLineDataset(yPmTen, "PM 10", false, Color.RED);
        dataSetTwentyFive = returnLineDataset(yPmTwentyFive, "PM 25", false, Color.GREEN);

        lineChart.setData(new LineData(dataSets));

        dataSets.clear();
        lineChart.clearValues();
        dataSets.add(dataSetTen);
        dataSets.add(dataSetTwentyFive);
        lineChart.setData(new LineData(dataSets));

        Log.d("Onclick", "onClick: Size 10: " + yPmTen.size() + "Size 25: " + yPmTwentyFive.size());
    }


    private ArrayList<Entry> returnBufferedList(ArrayList<Entry> arr) {
        ArrayList<Entry> newArr = new ArrayList<>();

        for (int i = 0; i < arr.size() - 1; i++) {
            Entry prevEntry = arr.get(i);
            Entry currentEntry = arr.get(i + 1);

            newArr.add(new Entry(prevEntry.getX(), currentEntry.getY()));
        }

        for (int i = 0; i < arr.size(); i++) {
            Log.d("TAG", "ELEMENT at: " + i + "   X: " + arr.get(i).getX() + "   Y: " + arr.get(i).getY());
        }
        return newArr;
    }

    private LineDataSet returnLineDataset(ArrayList<Entry> set, String label, boolean drawingValues, int color) {
        LineDataSet dataSet = new LineDataSet(set, label);
        dataSet.setDrawValues(drawingValues);
        dataSet.setColor(color);
        dataSet.setDrawCircles(false);

        return dataSet;
    }

    private LineChart returnLineChartWithFormatting(LineChart lineChart, int range) {
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);

        //Makes description disappear
        Description desc = new Description();
        desc.setText("");
        lineChart.setDescription(desc);

        lineChart.getAxisLeft().setEnabled(true); //show y-axis at left
        lineChart.getAxisRight().setEnabled(false); //hide y-axis at right

        lineChart.setDragEnabled(false);
        lineChart.setTouchEnabled(false);

        lineChart.setVisibleXRangeMaximum(range);

        return lineChart;
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

            Log.d(TAG, "displayData: DISPLAYDATA IS CALLED");
            
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

            mPm10 = Float.valueOf(pm10);
            mPm25 = Float.valueOf(pm25);

            doGraphStuff();
        }
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
