package Activities;

import android.app.Activity;
import android.os.StrictMode;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;

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
    TextView mOSMLastSyncData;
    TextView mOsmUnsychedValuesData;
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
        mOSMLastSyncData = findViewById(R.id.osm_last_sync_data);
        mOsmUnsychedValuesData = findViewById(R.id.osm_unsyched_values_data);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        initializiePrivateServer();
        try {
            initializeOpenSenseMapServer();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void initializiePrivateServer() {
        mHostLabel.setText(SynchronizationService.BASE_URL);
        try {
            syncService = new SynchronizationService(this);
            mLastSync.setText(syncService.getLastSynchronization());
            mUnsynchedValues.setText(String.valueOf(String.valueOf(syncService.getNumOfUnsychedValues())));
            syncBtnEnabled = true;
        } catch (Exception e) {
            syncBtnEnabled = false;
            Log.d("SYNCACT", "onCreate: EXCEPTION CAUGHT");
            e.printStackTrace();
        }
    }

    private void initializeOpenSenseMapServer() throws ParseException {
        SynchronizationOpenSenseMapService service = new SynchronizationOpenSenseMapService(this);
        mHostOpenSenseMapUrl.setText(service.getUrl());
        mOpenSenseMapSensorID.setText(service.getSensorBoxId());
        mOSMLastSyncData.setText(service.getLastOsmSync());
        mOsmUnsychedValuesData.setText(String.valueOf(service.getNumOfUnsynchedValues()*2));
    }


    public void onSyncBtnClick(View view) throws ParseException {
        if (syncBtnEnabled) {
            if (syncService.getNumOfUnsychedValues() != 0) {
                syncService.synchronizeData();
                mLastSync.setText(syncService.getLastSynchronization());
                mUnsynchedValues.setText(String.valueOf(syncService.getNumOfUnsychedValues()));

                Toast.makeText(SyncActivity.this,
                        "Synchronized successfully!", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(SyncActivity.this,
                        "Everything up to date", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(SyncActivity.this,
                    "No connection to Server!", Toast.LENGTH_SHORT).show();
        }
    }

    public void onOpenSenseMapSyncBtnClick(View view) throws InterruptedException, ParseException {
        SynchronizationOpenSenseMapService service = new SynchronizationOpenSenseMapService(this);
        service.sendPost();
        mOSMLastSyncData.setText(service.getLastOsmSync());
        mOsmUnsychedValuesData.setText(String.valueOf(service.getNumOfUnsynchedValues()));
    }
}
