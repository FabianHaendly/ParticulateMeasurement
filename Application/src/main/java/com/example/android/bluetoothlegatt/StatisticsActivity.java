package com.example.android.bluetoothlegatt;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;

import java.util.ArrayList;

import DataObjects.DataObject;
import SQLDatabse.SQLiteDBHelper;

public class StatisticsActivity extends Activity {

    Button mTodayBtn;
    Button mWeekBtn;
    Button mMonthBtn;
    Button mYearBtn;
    LineChart mLineChart;
    GraphService mGraphService;
    ArrayList<DataObject> mDbData;
    ArrayList<DataObject> mDisplayList;
    FilterService mFilterService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        mTodayBtn = findViewById(R.id.today_btn);
        mWeekBtn = findViewById(R.id.week_btn);
        mMonthBtn = findViewById(R.id.month_btn);
        mYearBtn= findViewById(R.id.year_btn);
        mLineChart = findViewById(R.id.lineChart);
        mFilterService = new FilterService();

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

    private void btnExecute(int period){
        mDisplayList = FilterService.returnFilteredList(mDbData, period);
        mGraphService = new GraphService(mLineChart, mDisplayList);
        mGraphService.initializeStaticGraph();
    }

    private void toastMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
