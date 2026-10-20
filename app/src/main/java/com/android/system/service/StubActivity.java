package com.android.system.service;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.graphics.Color;

public class StubActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        String targetPkg = getIntent().getStringExtra("TRISPY_TARGET_PKG");
        TextView tv = new TextView(this);
        tv.setText("SYSTEM HOOK SUCCESS!\n\nReady to inject:\n" + targetPkg);
        tv.setTextColor(Color.GREEN);
        tv.setTextSize(20);
        tv.setPadding(50, 50, 50, 50);
        setContentView(tv);
    }
}
