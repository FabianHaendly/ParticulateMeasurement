package com.example.android.bluetoothlegatt;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MyActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        MenuItem togBtn = menu.findItem(R.id.menu_bt_switch);
        togBtn.setActionView(R.layout.switch_layout);

        final Switch sw = (Switch) menu.findItem(R.id.menu_bt_switch).getActionView().findViewById(R.id.togBtn);

        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                    Toast.makeText(getBaseContext(), "toggling", Toast.LENGTH_SHORT).show();
                else{
                    Toast.makeText(getBaseContext(), "not toggling", Toast.LENGTH_SHORT).show();
                }
            }
        });

        menu.findItem(R.id.menu_stop).setVisible(false);
        menu.findItem(R.id.menu_refresh).setVisible(false);
        menu.findItem(R.id.menu_scan).setVisible(false);
        menu.findItem(R.id.menu_bt_switch).setVisible(true);



        return true;
    }
}


