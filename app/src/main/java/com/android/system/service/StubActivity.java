package com.android.system.service;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class StubActivity extends AppCompatActivity {
    private WindowManager wm;
    private View logoBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        String targetPkg = getIntent().getStringExtra("TRISPY_TARGET_PKG");
        
        try {
            System.loadLibrary("crypto"); 
            Log.d("TrispySpace", "Panel Library Loaded!");
        } catch (Throwable t) {
            Log.e("TrispySpace", "Failed to load panel: " + t.getMessage());
        }

        TextView tv = new TextView(this);
        tv.setText("VIRTUAL SPACE ENGINE ACTIVE!\n\nPanel Loaded.\nFloating Icon should appear.");
        tv.setTextColor(Color.GREEN);
        tv.setTextSize(18);
        tv.setPadding(50, 50, 50, 50);
        tv.setBackgroundColor(Color.BLACK);
        setContentView(tv);

        showFloatingLogo();
    }

    private void showFloatingLogo() {
        try {
            wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
            int type = Build.VERSION.SDK_INT >= 26
                    ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                    : WindowManager.LayoutParams.TYPE_PHONE;

            TextView logo = new TextView(this);
            logo.setText("T");
            logo.setTextColor(Color.WHITE);
            logo.setTextSize(24);
            logo.setGravity(Gravity.CENTER);
            logo.setBackgroundColor(Color.parseColor("#FF2200"));

            final WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                    120, 120, type,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
            lp.gravity = Gravity.TOP | Gravity.LEFT;
            lp.x = 50;
            lp.y = 300;

            logo.setOnClickListener(v -> {
                Toast.makeText(this, "Panel Clicked!", Toast.LENGTH_SHORT).show();
            });

            wm.addView(logo, lp);
            logoBtn = logo;
        } catch (Throwable t) {
            Log.e("TrispySpace", "Overlay failed: " + t.getMessage());
            Toast.makeText(this, "Overlay Crash: " + t.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (wm != null && logoBtn != null) {
                wm.removeView(logoBtn);
            }
        } catch (Throwable t) {}
    }
}
