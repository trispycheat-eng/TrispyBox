package com.trispy.space;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.trispy.engine.VirtualCore;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 50, 50, 50);
        
        TextView title = new TextView(this);
        title.setText("TRISPY SPACE\nMulti-App Engine");
        title.setTextSize(28);
        title.setTextColor(android.graphics.Color.RED);
        layout.addView(title);
        
        Button launchBtn = new Button(this);
        launchBtn.setText("Launch Free Fire (Test)");
        launchBtn.setOnClickListener(v -> {
            VirtualCore.get().launchApp("com.dts.freefiremax");
        });
        layout.addView(launchBtn);
        
        setContentView(layout);
    }
}
