package com.android.system.service;

import android.app.Activity;
import android.app.Application;
import android.app.Instrumentation;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.graphics.Color;
import java.io.File;
import java.lang.reflect.Method;
import dalvik.system.DexClassLoader;

public class StubActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        String targetPkg = getIntent().getStringExtra("TRISPY_TARGET_PKG");
        Log.d("TrispySpace", "Loading: " + targetPkg);

        try {
            ApplicationInfo appInfo = getPackageManager().getApplicationInfo(targetPkg, 0);
            String apkPath = appInfo.sourceDir;
            String nativeLibPath = appInfo.nativeLibraryDir; 
            File dexOutputDir = getDir("dex_" + targetPkg, 0);
            
            // 1. Create ClassLoader
            DexClassLoader classLoader = new DexClassLoader(
                apkPath, dexOutputDir.getAbsolutePath(), nativeLibPath, getClassLoader()
            );
            
            // 2. Setup the Fake Context
            SandboxContext sandboxContext = new SandboxContext(this, targetPkg);
            
            // 3. Load the Game Class
            Intent launchIntent = getPackageManager().getLaunchIntentForPackage(targetPkg);
            String actualActivityName = launchIntent.getComponent().getClassName();
            Class<?> mainActivityClass = classLoader.loadClass(actualActivityName);
            
            // 4. Create Instance
            Activity targetActivity = (Activity) mainActivityClass.newInstance();
            
            // 5. Get the REAL ActivityThread and Instrumentation
            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            Method currentActivityThreadMethod = activityThreadClass.getDeclaredMethod("currentActivityThread");
            currentActivityThreadMethod.setAccessible(true);
            Object realActivityThread = currentActivityThreadMethod.invoke(null);
            
            Method getInstrumentationMethod = activityThreadClass.getDeclaredMethod("getInstrumentation");
            getInstrumentationMethod.setAccessible(true);
            Instrumentation realInstrumentation = (Instrumentation) getInstrumentationMethod.invoke(realActivityThread);

            // 6. Dynamically find the attach method
            Method attachMethod = null;
            for (Method m : Activity.class.getDeclaredMethods()) {
                if (m.getName().equals("attach")) {
                    attachMethod = m;
                    break;
                }
            }
            
            if (attachMethod != null) {
                attachMethod.setAccessible(true);
                Object[] args = new Object[attachMethod.getParameterCount()];
                
                // Fill with REAL objects instead of nulls
                args[0] = sandboxContext; 
                args[1] = realActivityThread; // REAL ActivityThread
                args[2] = realInstrumentation; // REAL Instrumentation
                args[3] = null; // IBinder (can be null for now)
                args[4] = 0;    // int
                args[5] = getApplication(); // Application
                args[6] = launchIntent; // Intent
                args[7] = null; // ActivityInfo
                args[8] = "";   // CharSequence
                args[9] = null; // Activity
                args[10] = "";  // String
                args[11] = null; // Object
                args[12] = null; // Object
                
                for (int i = 13; i < args.length; i++) {
                    args[i] = null;
                }
                
                attachMethod.invoke(targetActivity, args);
            }
            
            // 7. Call onCreate
            Method onCreate = Activity.class.getDeclaredMethod("onCreate", Bundle.class);
            onCreate.setAccessible(true);
            onCreate.invoke(targetActivity, savedInstanceState);
            
            // 8. Show the game!
            setContentView(targetActivity.getWindow().getDecorView());

        } catch (Throwable t) {
            Log.e("TrispySpace", "Virtual load failed", t);
            TextView tv = new TextView(this);
            tv.setText("VIRTUAL SPACE ERROR:\n" + t.toString() + "\n\nIf it says 'Resources', the SandboxContext needs fixing.");
            tv.setTextColor(Color.RED);
            tv.setTextSize(14);
            tv.setPadding(50, 50, 50, 50);
            tv.setBackgroundColor(Color.BLACK);
            setContentView(tv);
        }
    }
}
