package com.android.system.service;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.graphics.Color;
import java.io.File;
import dalvik.system.DexClassLoader;

public class StubActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        String targetPkg = getIntent().getStringExtra("TRISPY_TARGET_PKG");
        Log.d("TrispySpace", "Virtual Space Started for: " + targetPkg);

        try {
            // 1. Get the real app info
            ApplicationInfo appInfo = getPackageManager().getApplicationInfo(targetPkg, 0);
            String apkPath = appInfo.sourceDir;
            String nativeLibPath = appInfo.nativeLibraryDir; // CRITICAL FOR UNITY GAMES
            
            // 2. Setup the isolated folder
            File dexOutputDir = getDir("dex", 0);
            
            // 3. Create the ClassLoader with Native Libs support
            DexClassLoader classLoader = new DexClassLoader(
                apkPath,
                dexOutputDir.getAbsolutePath(),
                nativeLibPath,
                getClassLoader()
            );
            
            // 4. Dynamically find the REAL launcher activity (No more guessing!)
            Intent launchIntent = getPackageManager().getLaunchIntentForPackage(targetPkg);
            String actualActivityName = launchIntent.getComponent().getClassName();
            
            // 5. Load the exact class
            Class<?> mainActivityClass = classLoader.loadClass(actualActivityName);
            
            // Show success
            TextView tv = new TextView(this);
            tv.setText("VIRTUAL SPACE SUCCESS!\n\nLoaded: " + actualActivityName + "\nNext: Start Activity using Reflection.");
            tv.setTextColor(Color.GREEN);
            tv.setTextSize(18);
            tv.setPadding(50, 50, 50, 50);
            tv.setBackgroundColor(Color.BLACK);
            setContentView(tv);

        } catch (Throwable t) {
            Log.e("TrispySpace", "Virtual load failed", t);
            TextView tv = new TextView(this);
            tv.setText("VIRTUAL SPACE ERROR:\n" + t.getMessage());
            tv.setTextColor(Color.RED);
            tv.setPadding(50, 50, 50, 50);
            tv.setBackgroundColor(Color.BLACK);
            setContentView(tv);
        }
    }
}
