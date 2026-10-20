package com.android.system.service;

import android.app.Activity;
import android.app.Application;
import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.TextView;
import android.graphics.Color;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import dalvik.system.DexClassLoader;

public class StubActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        String targetPkg = getIntent().getStringExtra("TRISPY_TARGET_PKG");
        showText("VIRTUAL SPACE INITIALIZING...\nTarget: " + targetPkg);

        try {
            // 1. Get real app info
            ApplicationInfo appInfo = getPackageManager().getApplicationInfo(targetPkg, 0);
            String apkPath = appInfo.sourceDir;
            
            // 2. Extract Native Libs (Fixes UnsatisfiedLinkError)
            File extractedLibs = NativeLibraryExtractor.extractLibs(this, apkPath, targetPkg);
            
            // 3. Setup Sandbox Context and ClassLoader
            SandboxContext sandboxContext = new SandboxContext(this, targetPkg);
            File dexOutputDir = getDir("dex_" + targetPkg, 0);
            DexClassLoader classLoader = new DexClassLoader(
                apkPath, dexOutputDir.getAbsolutePath(), extractedLibs.getAbsolutePath(), getClassLoader()
            );
            
            // 4. Get the real launcher activity
            Intent launchIntent = getPackageManager().getLaunchIntentForPackage(targetPkg);
            String actualActivityName = launchIntent.getComponent().getClassName();
            ActivityInfo actInfo = getPackageManager().getActivityInfo(launchIntent.getComponent(), 0);
            
            // 5. Create the Target Activity instance
            Class<?> mainActivityClass = classLoader.loadClass(actualActivityName);
            Activity targetActivity = (Activity) mainActivityClass.newInstance();

            // 6. Get REAL ActivityThread and Instrumentation (Critical for Unity)
            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            Method currentActivityThreadMethod = activityThreadClass.getDeclaredMethod("currentActivityThread");
            currentActivityThreadMethod.setAccessible(true);
            Object currentActivityThread = currentActivityThreadMethod.invoke(null);
            
            Method getInstrumentationMethod = activityThreadClass.getDeclaredMethod("getInstrumentation");
            getInstrumentationMethod.setAccessible(true);
            Instrumentation realInstrumentation = (Instrumentation) getInstrumentationMethod.invoke(currentActivityThread);

            // 7. Dynamically find the attach method signature (Fixes hidden API errors)
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
                
                // Populate with REAL objects to prevent NullPointerException
                args[0] = sandboxContext; 
                args[1] = currentActivityThread; 
                args[2] = realInstrumentation; 
                args[3] = new Binder(); // IBinder
                args[4] = 0;    
                args[5] = getApplication(); 
                args[6] = launchIntent; 
                args[7] = actInfo; // REAL ActivityInfo
                args[8] = "";   
                args[9] = null; 
                args[10] = "";  
                args[11] = null; 
                args[12] = null; 
                
                for (int i = 13; i < args.length; i++) args[i] = null;
                
                // Attach the context
                attachMethod.invoke(targetActivity, args);
            }
            
            // 8. Call onCreate using Instrumentation
            realInstrumentation.callActivityOnCreate(targetActivity, savedInstanceState);
            
            // 9. Render the game screen!
            setContentView(targetActivity.getWindow().getDecorView());
            showToast("Engine Loaded: " + actualActivityName);

        } catch (Throwable t) {
            Log.e("TrispyVM", "Launch failed", t);
            Throwable cause = t.getCause() != null ? t.getCause() : t;
            showText("VIRTUAL SPACE ERROR:\n\n" + cause.toString() + "\n\nIf this says NullPointer or Resources, Unity blocked the sandbox.");
        }
    }

    private void showText(String msg) {
        TextView tv = new TextView(this);
        tv.setText(msg);
        tv.setTextColor(Color.RED);
        tv.setTextSize(14);
        tv.setPadding(50, 50, 50, 50);
        tv.setBackgroundColor(Color.BLACK);
        setContentView(tv);
    }
    
    private void showToast(String msg) {
        android.widget.Toast.makeText(this, msg, android.widget.Toast.LENGTH_SHORT).show();
    }
                    }
