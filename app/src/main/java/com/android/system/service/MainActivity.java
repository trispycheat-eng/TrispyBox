package com.android.system.service;

import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.trispy.engine.VirtualCore;
import android.graphics.Color;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 50, 50, 50);
        layout.setBackgroundColor(Color.BLACK);
        
        TextView title = new TextView(this);
        title.setText("SYSTEM SERVICE\nEngine: ONLINE");
        title.setTextSize(26);
        title.setTextColor(Color.RED);
        layout.addView(title);

        Button launchBtn = new Button(this);
        launchBtn.setText("INJECT & LAUNCH FF MAX");
        launchBtn.setTextSize(18);
        launchBtn.setOnClickListener(v -> {
            try {
                VirtualCore.get().setTargetPackage("com.dts.freefiremax");
                VirtualCore.get().launchApp();
            } catch (Throwable t) {
                Toast.makeText(this, "Failed: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
        layout.addView(launchBtn);
        
        setContentView(layout);
    }
}
