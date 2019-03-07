package com.example.android.bluetoothlegatt;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import SQLDatabse.SQLiteDBHelper;

public class StatisticsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        SQLiteDBHelper db = new SQLiteDBHelper(this);


    }
}
