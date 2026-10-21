package com.android.system.service;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.provider.Settings;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

public class PanelManager {
    private static View floatingIcon;
    private static WindowManager windowManager;
    private static boolean isPanelLoaded = false;

    public static void startPanel(Activity activity) {
        if (isPanelLoaded) return;
        isPanelLoaded = true;

        // Load your crypto file
        try {
            System.loadLibrary("crypto"); 
            Toast.makeText(activity, "Crypto Lib Loaded", Toast.LENGTH_SHORT).show();
        } catch (UnsatisfiedLinkError e) {
            Toast.makeText(activity, "Crypto Load Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        // Show floating panel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(activity)) {
            return;
        }

        windowManager = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
        
        Button iconBtn = new Button(activity);
        iconBtn.setText("K");
        iconBtn.setBackgroundColor(Color.parseColor("#FF2200"));
        iconBtn.setTextColor(Color.WHITE);

        int type = Build.VERSION.SDK_INT >= 26
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE;

        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                150, 150, type,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = 100;
        params.y = 200;

        iconBtn.setOnTouchListener(new View.OnTouchListener() {
            private int initialX, initialY;
            private float initialTouchX, initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        windowManager.updateViewLayout(v, params);
                        return true;
                    case MotionEvent.ACTION_UP:
                        Toast.makeText(activity, "Opening Crypto Panel...", Toast.LENGTH_SHORT).show();
                        return true;
                }
                return false;
            }
        });

        windowManager.addView(iconBtn, params);
        floatingIcon = iconBtn;
    }
}
