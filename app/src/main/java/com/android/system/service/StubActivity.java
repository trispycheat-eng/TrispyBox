package com.android.system.service;

import android.app.Activity;
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
            android.content.pm.ApplicationInfo appInfo = getPackageManager().getApplicationInfo(targetPkg, 0);
            String apkPath = appInfo.sourceDir;
            
            File dexOutputDir = getDir("dex", 0);
            
            DexClassLoader classLoader = new DexClassLoader(
                apkPath,
                dexOutputDir.getAbsolutePath(),
                null,
                getClassLoader()
            );
            
            Class<?> mainActivityClass = classLoader.loadClass("com.dts.freefiremax.MainActivity");
            
            TextView tv = new TextView(this);
            tv.setText("VIRTUAL SPACE SUCCESS!\n\nLoaded FF Max Engine.\nNext: Start FF Activity inside this space.");
            tv.setTextColor(Color.GREEN);
            tv.setTextSize(18);
            tv.setPadding(50, 50, 50, 50);
            setContentView(tv);

        } catch (Throwable t) {
            Log.e("TrispySpace", "Virtual load failed", t);
            TextView tv = new TextView(this);
            tv.setText("VIRTUAL SPACE ERROR:\n" + t.getMessage());
            tv.setTextColor(Color.RED);
            tv.setPadding(50, 50, 50, 50);
            setContentView(tv);
        }
    }
}
