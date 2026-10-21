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
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildHomeScreen();
    }

    private void buildHomeScreen() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.parseColor("#0F0F0F"));
        root.setPadding(20, 40, 20, 20);

        // Top Bar
        LinearLayout topBar = new LinearLayout(this);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setGravity(Gravity.CENTER_VERTICAL);
        
        TextView time = new TextView(this);
        time.setText("TRISPY OS");
        time.setTextColor(Color.WHITE);
        time.setTextSize(16);
        time.setTypeface(null, Typeface.BOLD);
        topBar.addView(time, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f));

        Button settingsBtn = new Button(this);
        settingsBtn.setText("⚙ ZYGISK");
        settingsBtn.setTextColor(Color.parseColor("#FF2200"));
        settingsBtn.setBackgroundColor(Color.TRANSPARENT);
        settingsBtn.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
        topBar.addView(settingsBtn);
        
        root.addView(topBar);

        // Desktop Container
        LinearLayout mainContainer = new LinearLayout(this);
        mainContainer.setOrientation(LinearLayout.VERTICAL);
        mainContainer.setBackgroundColor(Color.parseColor("#1A1A1A"));
        mainContainer.setPadding(20, 20, 20, 20);
        LinearLayout.LayoutParams innerParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.0f);
        innerParams.setMargins(0, 30, 0, 30);
        mainContainer.setLayoutParams(innerParams);
        mainContainer.setBackgroundResource(android.R.drawable.dialog_holo_dark_frame);

        // App Grid
        GridLayout appGrid = new GridLayout(this);
        appGrid.setColumnCount(4);
        appGrid.setAlignmentMode(GridLayout.ALIGN_BOUNDS);
        
        addAppIcon(appGrid, "Play Store", "#00FF00", v -> startActivity(new Intent(this, PlayStoreActivity.class)));
        addAppIcon(appGrid, "Free Fire", "#FF2200", v -> VMEngine.launchApp(this, "com.dts.freefiremax"));
        addAppIcon(appGrid, "Settings", "#555555", v -> startActivity(new Intent(this, SettingsActivity.class)));

        mainContainer.addView(appGrid);
        root.addView(mainContainer);

        // Bottom Dock
        LinearLayout dock = new LinearLayout(this);
        dock.setOrientation(LinearLayout.HORIZONTAL);
        dock.setGravity(Gravity.CENTER);

        Button launchBtn = new Button(this);
        launchBtn.setText("LAUNCH VM");
        launchBtn.setBackgroundColor(Color.parseColor("#FF2200"));
        launchBtn.setTextColor(Color.WHITE);
        launchBtn.setOnClickListener(v -> Toast.makeText(this, "VM is running in background. Use icons above.", Toast.LENGTH_SHORT).show());
        
        dock.addView(launchBtn, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        root.addView(dock);

        setContentView(root);
    }

    private void addAppIcon(GridLayout grid, String name, String colorHex, View.OnClickListener listener) {
        LinearLayout appWrapper = new LinearLayout(this);
        appWrapper.setOrientation(LinearLayout.VERTICAL);
        appWrapper.setGravity(Gravity.CENTER);
        appWrapper.setPadding(10, 10, 10, 10);
        appWrapper.setOnClickListener(listener);

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
        
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        grid.addView(appWrapper, params);
    }
}
