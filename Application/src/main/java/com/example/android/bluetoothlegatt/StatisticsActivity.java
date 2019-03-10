package com.example.android.bluetoothlegatt;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import DataObjects.DataObject;
import SQLDatabse.SQLiteDBHelper;

public class StatisticsActivity extends Activity {
    private static String TAG = "DISPLAYLIST: ";

    Button mTodayBtn;
    Button mWeekBtn;
    Button mMonthBtn;
    Button mYearBtn;
    LineChart mLineChart;
    GraphService mGraphService;
    ArrayList<DataObject> mDbData;
    ArrayList<DataObject> mDisplayList;
    FilterService mFilterService;

    //    Statistic values
    TextView mValues;
    TextView mDistance;
    TextView mMaxPm10;
    TextView mMaxPm25;
    TextView mAvgPm10;
    TextView mAvgPm25;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        mTodayBtn = findViewById(R.id.today_btn);
        mWeekBtn = findViewById(R.id.week_btn);
        mMonthBtn = findViewById(R.id.month_btn);
        mYearBtn = findViewById(R.id.year_btn);
        mLineChart = findViewById(R.id.lineChart);
        mFilterService = new FilterService();

        mValues = findViewById(R.id.kv_values_data);
        mDistance = findViewById(R.id.kv_distance_data);
        mMaxPm10 = findViewById(R.id.kv_maxPmTen_data);
        mMaxPm25 = findViewById(R.id.kv_maxPmTwentyFive_data);
        mAvgPm10 = findViewById(R.id.kv_avgPmTen_data);
        mAvgPm25 = findViewById(R.id.kv_avgPmTwentyFive_data);

        SQLiteDBHelper db = new SQLiteDBHelper(this);
        mDbData = db.getItems();
    }

    public void onTodayBtnClick(View view) {
        btnExecute(FilterService.PERIOD_CURRENT_DAY);
    }

    public void onWeekBtnClick(View view) {
        btnExecute(FilterService.PERIOD_CURRENT_WEEK);
    }

    public void onMonthBtnClick(View view) {
        btnExecute(FilterService.PERIOD_CURRENT_MONTH);
    }

    public void onYearBtnClick(View view) {
        btnExecute(FilterService.PERIOD_CURRENT_YEAR);
    }

    private void btnExecute(int period) {
        mDisplayList = FilterService.returnFilteredList(mDbData, period);
        Log.d(TAG, "" + mDisplayList.size());
        if (mDisplayList.size() > 0) {
            mGraphService = new GraphService(mLineChart, mDisplayList);
            mGraphService.initializeStaticGraph();
            setStatisticValues(mDisplayList);
        } else toastMessage("No values for this selection!");
    }

    private void setStatisticValues(ArrayList<DataObject> list) {
        ArrayList<DataObject> maxes = returnMaxPmObject(list);
        ArrayList<String> avgs = returnAveragePmValues(list);

        mValues.setText(String.valueOf(list.size()));
        mDistance.setText(returnTraveledDistance(list));
        mMaxPm10.setText(maxes.get(0).getPmTen());
        mMaxPm25.setText(maxes.get(1).getPmTwentyFive());
        mAvgPm10.setText(avgs.get(0));
        mAvgPm25.setText(avgs.get(1));
    }

    private String returnTraveledDistance(ArrayList<DataObject> list) {

        double distanceSum = 0.0;
        double lat1, lat2, lon1, lon2, distance;

        for(int i=0; i<list.size()-1;i++) {
            lat1 = Double.valueOf(list.get(i).getLocation().getLatitude());
            lat2 = Double.valueOf(list.get(i+1).getLocation().getLatitude());
            lon1 = Double.valueOf(list.get(i).getLocation().getLongitude());
            lon2 = Double.valueOf(list.get(i+1).getLocation().getLongitude());

            distance = distanceHaversine(lon1, lat1, lon2, lat2);
            distanceSum+=distance;
        }

        Log.d(TAG, "returnTraveledDistance: " + distanceSum);

        return String.valueOf(distanceSum);
    }

    private double distanceHaversine(double lon1, double lat1, double lon2, double lat2) {

        final int R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c; // convert to meters

        distance = Math.pow(distance, 2);

        return Math.sqrt(distance);
    }

    private ArrayList<DataObject> returnMaxPmObject(ArrayList<DataObject> list) {
        DataObject maxPmTenObj = list.get(0);
        DataObject maxPmTwentyFiveObj = list.get(0);
        float maxPmTen = Float.valueOf(maxPmTenObj.getPmTen());
        float maxPmTwentyFive = Float.valueOf(maxPmTwentyFiveObj.getPmTen());

        for (int i = 1; i < list.size(); i++) {
            float currentPmTen = Float.valueOf(list.get(i).getPmTen());
            float currentPmTwentyFive = Float.valueOf(list.get(i).getPmTwentyFive());

//            Log.d(TAG, "returnMaxPmObject: CURRENT PM10: " + currentPmTen);
//            Log.d(TAG, "returnMaxPmObject: CURRENT PM25: " + currentPmTwentyFive);

            if (currentPmTen > maxPmTen) {
                maxPmTen = currentPmTen;
                maxPmTenObj = list.get(i);
            }
            if (currentPmTwentyFive > maxPmTwentyFive) {
                maxPmTwentyFive = currentPmTwentyFive;
                maxPmTwentyFiveObj = list.get(i);
            }
        }

        ArrayList<DataObject> result = new ArrayList<>();
        result.add(maxPmTenObj);
        result.add(maxPmTwentyFiveObj);

        return result;
    }

    private ArrayList<String> returnAveragePmValues(ArrayList<DataObject> list) {
        float sumPmTen = 0.0f;
        float sumPmTwentyFive = 0.0f;

        for (int i = 0; i < list.size(); i++) {
            sumPmTen += Float.valueOf(list.get(i).getPmTen());
            sumPmTwentyFive += Float.valueOf(list.get(i).getPmTwentyFive());
        }

        String avgPmTen = String.valueOf(sumPmTen / list.size()).substring(0, 4);
        String avgPmTwentyFive = String.valueOf(sumPmTwentyFive / list.size()).substring(0, 4);

//        Log.d(TAG, "returnAveragePmValues: PM10 AVG: " + avgPmTen);
//        Log.d(TAG, "returnAveragePmValues: PM25 AVG: " + mAvgPm25);

        ArrayList<String> avgs = new ArrayList<>();
        avgs.add(avgPmTen);
        avgs.add(avgPmTwentyFive);

        return avgs;
    }

    private void toastMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
