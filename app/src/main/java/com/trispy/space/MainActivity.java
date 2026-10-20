package com.trispy.space;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Color;

import java.util.List;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setBackgroundColor(Color.BLACK);
        layout.setPadding(40, 40, 40, 40);
        
        TextView title = new TextView(this);
        title.setText("TRISPY SPACE\nInstalled Apps");
        title.setTextSize(24);
        title.setTextColor(Color.RED);
        layout.addView(title);

        // Load installed apps (Simple Multi-Space UI)
        List<PackageInfo> packages = getPackageManager().getInstalledPackages(0);
        for (PackageInfo pkg : packages) {
            // Only show user apps, not system apps
            if ((pkg.applicationInfo.flags & android.content.pm.ApplicationInfo.FLAG_SYSTEM) == 0) {
                TextView appItem = new TextView(this);
                appItem.setText("📦 " + pkg.applicationInfo.loadLabel(getPackageManager()).toString());
                appItem.setTextColor(Color.WHITE);
                appItem.setTextSize(18);
                appItem.setPadding(0, 30, 0, 30);
                
                final String packageName = pkg.packageName;
                appItem.setOnClickListener(v -> {
                    Toast.makeText(this, "Injecting " + packageName + " into Space...", Toast.LENGTH_SHORT).show();
                    // Later, this will call VirtualCore to launch inside the sandbox
                });
                
                layout.addView(appItem);
            }
        }
        setContentView(layout);
    }
}
