package com.trispy.space;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.trispy.engine.VirtualCore;
import android.graphics.Color;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 50, 50, 50);
        layout.setBackgroundColor(Color.BLACK);
        
        TextView title = new TextView(this);
        title.setText("TRISPY SPACE\nEngine: ONLINE");
        title.setTextSize(26);
        title.setTextColor(Color.RED);
        layout.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("\nClick below to inject Free Fire Max into the Sandbox.");
        subtitle.setTextColor(Color.WHITE);
        layout.addView(subtitle);
        
        Button launchBtn = new Button(this);
        launchBtn.setText("INJECT & LAUNCH FF MAX");
        launchBtn.setTextSize(18);
        launchBtn.setOnClickListener(v -> {
            Toast.makeText(this, "Hooking ActivityThread...", Toast.LENGTH_SHORT).show();
            VirtualCore.get().setTargetPackage("com.dts.freefiremax");
            VirtualCore.get().launchApp();
        });
        layout.addView(launchBtn);
        
        setContentView(layout);
    }
}
