package com.android.system.service;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ScrollView;
import android.widget.TextView;
import android.graphics.Color;

public class CrashActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String error = getIntent().getStringExtra("error");
        ScrollView scroll = new ScrollView(this);
        TextView tv = new TextView(this);
        tv.setText("FREE FIRE CRASHED:\n\n" + error);
        tv.setTextColor(Color.RED);
        tv.setTextSize(12);
        tv.setPadding(20, 20, 20, 20);
        tv.setBackgroundColor(Color.BLACK);
        scroll.addView(tv);
        setContentView(scroll);
    }
}
