package com.android.system.service;

import android.app.Activity;
import android.app.Application;
import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.TextView;
import android.graphics.Color;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import dalvik.system.DexClassLoader;

public class StubActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        String targetPkg = getIntent().getStringExtra("TRISPY_TARGET_PKG");
        showText("VIRTUAL SPACE INITIALIZING...\nTarget: " + targetPkg);

        try {
            ApplicationInfo appInfo = getPackageManager().getApplicationInfo(targetPkg, 0);
            String apkPath = appInfo.sourceDir;
            
            File extractedLibs = NativeLibraryExtractor.extractLibs(this, apkPath, targetPkg);
            
            SandboxContext sandboxContext = new SandboxContext(this, targetPkg);
            File dexOutputDir = getDir("dex_" + targetPkg, 0);
            DexClassLoader classLoader = new DexClassLoader(
                apkPath, dexOutputDir.getAbsolutePath(), extractedLibs.getAbsolutePath(), getClassLoader()
            );
            
            // --- FIX: ROBUST ACTIVITY FINDER ---
            Intent launchIntent = getPackageManager().getLaunchIntentForPackage(targetPkg);
            ComponentName componentName = null;
            
            if (launchIntent != null && launchIntent.getComponent() != null) {
                componentName = launchIntent.getComponent();
            } else {
                // Fallback: Query PackageManager manually
                Intent queryIntent = new Intent(Intent.ACTION_MAIN);
                queryIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                queryIntent.setPackage(targetPkg);
                List<ResolveInfo> resolveInfos = getPackageManager().queryIntentActivities(queryIntent, 0);
                if (!resolveInfos.isEmpty()) {
                    ActivityInfo activityInfo = resolveInfos.get(0).activityInfo;
                    componentName = new ComponentName(activityInfo.packageName, activityInfo.name);
                }
            }
            
            if (componentName == null) {
                showText("ERROR: Could not find Launcher Activity for " + targetPkg);
                return;
            }

            String actualActivityName = componentName.getClassName();
            ActivityInfo actInfo = getPackageManager().getActivityInfo(componentName, 0);
            
            // Create the new Intent explicitly
            launchIntent = new Intent();
            launchIntent.setComponent(componentName);
            
            // --- CONTINUE WITH THE LAUNCH ---
            Class<?> mainActivityClass = classLoader.loadClass(actualActivityName);
            Activity targetActivity = (Activity) mainActivityClass.newInstance();

            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            Method currentActivityThreadMethod = activityThreadClass.getDeclaredMethod("currentActivityThread");
            currentActivityThreadMethod.setAccessible(true);
            Object currentActivityThread = currentActivityThreadMethod.invoke(null);
            
            Method getInstrumentationMethod = activityThreadClass.getDeclaredMethod("getInstrumentation");
            getInstrumentationMethod.setAccessible(true);
            Instrumentation realInstrumentation = (Instrumentation) getInstrumentationMethod.invoke(currentActivityThread);

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
                
                args[0] = sandboxContext; 
                args[1] = currentActivityThread; 
                args[2] = realInstrumentation; 
                args[3] = new Binder(); // IBinder
                args[4] = 0;    
                args[5] = getApplication(); 
                args[6] = launchIntent; 
                args[7] = actInfo;
                args[8] = "";   
                args[9] = null; 
                args[10] = "";  
                args[11] = null; 
                args[12] = null; 
                
                for (int i = 13; i < args.length; i++) args[i] = null;
                
                attachMethod.invoke(targetActivity, args);
            }
            
            realInstrumentation.callActivityOnCreate(targetActivity, savedInstanceState);
            
            setContentView(targetActivity.getWindow().getDecorView());
            showToast("Engine Loaded: " + actualActivityName);

        } catch (Throwable t) {
            Log.e("TrispyVM", "Launch failed", t);
            Throwable cause = t.getCause() != null ? t.getCause() : t;
            showText("VIRTUAL SPACE ERROR:\n\n" + cause.toString());
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
