package com.example.android.bluetoothlegatt;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;

public class GraphDataActivity extends Activity {
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
        mPm10 = intent.getFloatExtra("pm10", (float) 0.0);
        mPm25 = intent.getFloatExtra("pm25", (float) 0.0);

        lineChart = (LineChart) findViewById(R.id.lineChart);
        lineChart = returnLineChartWithFormatting(lineChart, 21);

        yPmTen = new ArrayList<>();
        yPmTwentyFive = new ArrayList<>();

        dataSets = new ArrayList<>();

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
}
