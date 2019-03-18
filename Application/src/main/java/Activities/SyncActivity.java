package Activities;

import android.app.Activity;
import android.os.StrictMode;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import BLEHelper.bluetoothlegatt.R;

import Services.SynchronizationOpenSenseMapService;
import Services.SynchronizationService;

public class SyncActivity extends Activity {
    private SynchronizationService syncService;
    TextView mHostLabel;
    TextView mLastSync;
    TextView mUnsynchedValues;
    Button mSyncBtn;
    TextView mHostOpenSenseMapUrl;
    TextView mOpenSenseMapSensorID;
    boolean syncBtnEnabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync);

        mHostLabel = findViewById(R.id.host_url);
        mLastSync = findViewById(R.id.tv_last_sync_data);
        mUnsynchedValues = findViewById(R.id.tv_unsynced_values_data);
        mSyncBtn = findViewById(R.id.sync_data_btn);
        mHostOpenSenseMapUrl = findViewById(R.id.host_openSenseMap_url);
        mOpenSenseMapSensorID = findViewById(R.id.openSenseMap_sensor_id);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        initializiePrivateServer();
        initializeOpenSenseMapServer();
    }

    private void initializiePrivateServer() {
        mHostLabel.setText(SynchronizationService.BASE_URL);
        try {
            syncService = new SynchronizationService(this);
            mLastSync.setText(syncService.getLastSyncDate());
            mUnsynchedValues.setText(String.valueOf(syncService.getUnsynchedValues()));
            syncBtnEnabled = true;
        } catch (Exception e) {
            syncBtnEnabled = false;
            Log.d("SYNCACT", "onCreate: EXCEPTION CAUGHT");
        }
    }

    private void initializeOpenSenseMapServer() {
        SynchronizationOpenSenseMapService service = new SynchronizationOpenSenseMapService();
        mHostOpenSenseMapUrl.setText(service.getUrl());
        mOpenSenseMapSensorID.setText(service.getSensorBoxId());
    }


    public void onSyncBtnClick(View view) {
        if (syncBtnEnabled) {
            if (syncService.getUnsynchedValues() != 0) {
                syncService.synchronizeData();

                Log.d("ONSYNCCLICK", "last SYNC: " + syncService.getLastSyncDate());
                mLastSync.setText(syncService.getLastSyncDate());

                Log.d("ONSYNCCLICK", "UNSYNCHED: " + String.valueOf(syncService.getUnsynchedValues()));
                mUnsynchedValues.setText(String.valueOf(syncService.getUnsynchedValues()));
            } else {
                Toast.makeText(SyncActivity.this,
                        "Already up to date!", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(SyncActivity.this,
                    "No connection to Server!", Toast.LENGTH_SHORT).show();
        }
    }

    public void onOpenSenseMapSyncBtnClick(View view) {
    }
}
