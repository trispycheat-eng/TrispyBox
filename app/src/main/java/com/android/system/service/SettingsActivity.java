package com.android.system.service;

import android.os.Bundle;
import android.widget.Switch;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("VM Settings");
        }

        Switch rootSwitch = findViewById(R.id.switch_root);
        Switch magiskSwitch = findViewById(R.id.switch_magisk);
        TextView logText = findViewById(R.id.txt_logs);

        // Load settings
        rootSwitch.setChecked(getSharedPreferences("vm_settings", MODE_PRIVATE).getBoolean("root", true));
        magiskSwitch.setChecked(getSharedPreferences("vm_settings", MODE_PRIVATE).getBoolean("magisk", true));

        // Save settings
        rootSwitch.setOnCheckedChangeListener((v, isChecked) -> {
            getSharedPreferences("vm_settings", MODE_PRIVATE).edit().putBoolean("root", isChecked).apply();
            logText.append("\n[+] Root status: " + isChecked);
        });

        magiskSwitch.setOnCheckedChangeListener((v, isChecked) -> {
            getSharedPreferences("vm_settings", MODE_PRIVATE).edit().putBoolean("magisk", isChecked).apply();
            logText.append("\n[+] Magisk status: " + isChecked);
        });

        // Initial Logs
        logText.append("[*] Trispy VM Engine Started\n");
        logText.append("[*] Spoofer Active (SM-S928B)\n");
        logText.append("[*] ACE Thread Freezer Running\n");
        logText.append("[*] Ready to inject panel.\n");
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
