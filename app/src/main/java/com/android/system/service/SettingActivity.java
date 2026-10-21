package com.android.system.service;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        SharedPreferences prefs = getSharedPreferences("VM_ROOT", MODE_PRIVATE);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.parseColor("#121212"));
        root.setPadding(40, 60, 40, 40);

        TextView title = new TextView(this);
        title.setText("ROOT & ZYGISK SETTINGS");
        title.setTextColor(Color.WHITE);
        title.setTextSize(20);
        root.addView(title);

        root.addView(createSwitch("Enable Magisk Root (Fake)", prefs, "magisk"));
        root.addView(createSwitch("Enable Zygisk", prefs, "zygisk"));
        root.addView(createSwitch("Hide Root (DenyList)", prefs, "hide_root"));

        setContentView(root);
    }

    private LinearLayout createSwitch(String label, SharedPreferences prefs, String key) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, 30, 0, 30);

        TextView text = new TextView(this);
        text.setText(label);
        text.setTextColor(Color.LTGRAY);
        text.setTextSize(16);
        text.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));
        
        Switch toggle = new Switch(this);
        toggle.setChecked(prefs.getBoolean(key, false));
        toggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(key, isChecked).apply();
            Toast.makeText(this, label + ": " + isChecked, Toast.LENGTH_SHORT).show();
        });

        row.addView(text);
        row.addView(toggle);
        return row;
    }
}
