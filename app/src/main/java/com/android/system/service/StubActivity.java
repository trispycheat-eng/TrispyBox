package com.android.system.service;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

public class StubActivity extends Activity {
    private WindowManager wm;
    private View logoBtn;
    private static final int OVERLAY_REQ = 1002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        String targetPkg = getIntent().getStringExtra("TRISPY_TARGET_PKG");
        
        // 1. Load the Trispy Native Panel (libcrypto.so)
        try {
            System.loadLibrary("crypto"); 
            Log.d("TrispySpace", "Panel Library Loaded!");
        } catch (Throwable t) {
            Log.e("TrispySpace", "Failed to load panel: " + t.getMessage());
        }

        // 2. Show the Green Screen
        TextView tv = new TextView(this);
        tv.setText("VIRTUAL SPACE ENGINE ACTIVE!\n\nPanel Loaded.\nFloating Icon should appear.");
        tv.setTextColor(Color.GREEN);
        tv.setTextSize(18);
        tv.setPadding(50, 50, 50, 50);
        tv.setBackgroundColor(Color.BLACK);
        setContentView(tv);

        // 3. Check Overlay Permission & Show Icon
        if (Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(this)) {
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, OVERLAY_REQ);
            } catch (Throwable t) {
                Toast.makeText(this, "Overlay permission needed!", Toast.LENGTH_LONG).show();
            }
        } else {
            showFloatingLogo();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OVERLAY_REQ) {
            if (Build.VERSION.SDK_INT >= 23 && Settings.canDrawOverlays(this)) {
                showFloatingLogo();
            } else {
                Toast.makeText(this, "Permission denied. Icon won't show.", Toast.LENGTH_LONG).show();
            }
        }
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
