package com.example.android.bluetoothlegatt;

import android.app.Activity;
import android.os.StrictMode;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import Helper.SynchronizationService;

public class SyncActivity extends Activity {
    private SynchronizationService syncService;
    TextView mLastSync;
    TextView mUnsynchedValues;
    Button mSyncBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync);

        mLastSync = findViewById(R.id.tv_last_sync_data);
        mUnsynchedValues = findViewById(R.id.tv_unsynced_values_data);
        mSyncBtn = findViewById(R.id.sync_data_btn);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        syncService = new SynchronizationService(this);
        mLastSync.setText(syncService.getLastSyncDate());
        mUnsynchedValues.setText(String.valueOf(syncService.getUnsynchedValues()));
    }


    public void onSyncBtnClick(View view) {
        if(syncService.getUnsynchedValues() != 0) {
            syncService.synchronizeData();

            Log.d("ONSYNCCLICK", "last SYNC: " + syncService.getLastSyncDate());
            mLastSync.setText(syncService.getLastSyncDate());

            Log.d("ONSYNCCLICK", "UNSYNCHED: " + String.valueOf(syncService.getUnsynchedValues()));
            mUnsynchedValues.setText(String.valueOf(syncService.getUnsynchedValues()));
        }
        else {
            Toast.makeText(SyncActivity.this,
                    "Already up to date!", Toast.LENGTH_LONG).show();
        }
    }


    private void checkSynchronization(){
        if (syncService.getSuccess() == 1) {
            //Display success message
            Toast.makeText(SyncActivity.this,
                    "Measurements synchronized", Toast.LENGTH_LONG).show();
            finish();

        } else {
            Toast.makeText(SyncActivity.this,
                    "Some error occurred while synchronizing measurement",
                    Toast.LENGTH_LONG).show();

        }
    }
}
