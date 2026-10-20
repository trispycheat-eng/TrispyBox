package com.android.system.service;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import android.graphics.Color;

public class StubActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        String targetPkg = getIntent().getStringExtra("TRISPY_TARGET_PKG");
        
        TextView tv = new TextView(this);
        tv.setText("VIRTUAL SPACE ENGINE ACTIVE!\n\nIntercepted: " + targetPkg + "\n\nNext: Load Engine & Inject Panel.");
        tv.setTextColor(Color.GREEN);
        tv.setTextSize(18);
        tv.setPadding(50, 50, 50, 50);
        tv.setBackgroundColor(Color.BLACK);
        setContentView(tv);
    }
}
