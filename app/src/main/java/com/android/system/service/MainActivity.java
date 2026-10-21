package com.android.system.service;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private LinearLayout mainContainer;
    private GridLayout appGrid;
    private LinearLayout topBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildHomeScreen();
    }

    private void buildHomeScreen() {
        // Main Wrapper
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.parseColor("#0F0F0F")); // Deep dark
        root.setPadding(20, 40, 20, 20);

        // Top Bar (Battery, Time, Settings)
        topBar = new LinearLayout(this);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setGravity(Gravity.CENTER_VERTICAL);
        
        TextView time = new TextView(this);
        time.setText("10:45 AM");
        time.setTextColor(Color.WHITE);
        time.setTextSize(16);
        time.setTypeface(null, Typeface.BOLD);
        topBar.addView(time, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f));

        Button settingsBtn = new Button(this);
        settingsBtn.setText("⚙ ZYGISK");
        settingsBtn.setTextColor(Color.parseColor("#FF2200"));
        settingsBtn.setBackgroundColor(Color.TRANSPARENT);
        settingsBtn.setOnClickListener(v -> showRootSettings());
        topBar.addView(settingsBtn);
        
        root.addView(topBar);

        // Main Inner Screen (VPhone Style)
        mainContainer = new LinearLayout(this);
        mainContainer.setOrientation(LinearLayout.VERTICAL);
        mainContainer.setBackgroundColor(Color.parseColor("#1A1A1A"));
        mainContainer.setPadding(20, 20, 20, 20);
        LinearLayout.LayoutParams innerParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.0f);
        innerParams.setMargins(0, 30, 0, 30);
        mainContainer.setLayoutParams(innerParams);
        mainContainer.setBackgroundResource(android.R.drawable.dialog_holo_dark_frame); // borders

        // App Grid
        appGrid = new GridLayout(this);
        appGrid.setColumnCount(4);
        appGrid.setAlignmentMode(GridLayout.ALIGN_BOUNDS);
        
        // Fake Apps for realism
        addAppIcon("Settings", "#555555");
        addAppIcon("Camera", "#0055FF");
        addAppIcon("Gallery", "#FFAA00");
        addAppIcon("Play Store", "#00FF00");

        mainContainer.addView(appGrid);
        root.addView(mainContainer);

        // Bottom Dock (Free Fire and Files)
        LinearLayout dock = new LinearLayout(this);
        dock.setOrientation(LinearLayout.HORIZONTAL);
        dock.setGravity(Gravity.CENTER);
        dock.setPadding(0, 20, 0, 20);

        Button playStoreBtn = new Button(this);
        playStoreBtn.setText("Play Store");
        playStoreBtn.setBackgroundColor(Color.parseColor("#00FF00"));
        playStoreBtn.setTextColor(Color.BLACK);
        playStoreBtn.setOnClickListener(v -> openPlayStore());

        Button ffBtn = new Button(this);
        ffBtn.setText("Free Fire MAX");
        ffBtn.setBackgroundColor(Color.parseColor("#FF2200"));
        ffBtn.setTextColor(Color.WHITE);
        ffBtn.setOnClickListener(v -> launchFreeFire());

        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        btnParams.setMargins(10, 0, 10, 0);
        
        dock.addView(playStoreBtn, btnParams);
        dock.addView(ffBtn, btnParams);
        
        root.addView(dock);

        setContentView(root);
    }

    private void addAppIcon(String name, String colorHex) {
        LinearLayout appWrapper = new LinearLayout(this);
        appWrapper.setOrientation(LinearLayout.VERTICAL);
        appWrapper.setGravity(Gravity.CENTER);
        appWrapper.setPadding(10, 10, 10, 10);

        TextView icon = new TextView(this);
        icon.setText(name.substring(0, 1));
        icon.setTextColor(Color.WHITE);
        icon.setTextSize(24);
        icon.setGravity(Gravity.CENTER);
        icon.setBackgroundColor(Color.parseColor(colorHex));
        icon.setWidth(120);
        icon.setHeight(120);

        TextView label = new TextView(this);
        label.setText(name);
        label.setTextColor(Color.WHITE);
        label.setTextSize(10);
        label.setGravity(Gravity.CENTER);

        appWrapper.addView(icon);
        appWrapper.addView(label);
        
        // Add to Grid
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        appGrid.addView(appWrapper, params);
    }

    private void showRootSettings() {
        // This will be our fake Magisk/Zygisk injector
        Toast.makeText(this, "Opening Zygisk Settings...", Toast.LENGTH_SHORT).show();
        // Next step: Implement root UI
    }

    private void openPlayStore() {
        Toast.makeText(this, "Opening Play Store (Virtual)...", Toast.LENGTH_SHORT).show();
        // Next step: Implement Play Store APK extractor
    }

    private void launchFreeFire() {
        // Next step: Trigger BlackBox VM to launch Free Fire
        Toast.makeText(this, "Booting ACE Bypass & Launching FF...", Toast.LENGTH_LONG).show();
    }
}
