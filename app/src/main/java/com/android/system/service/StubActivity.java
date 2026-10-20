package com.android.system.service;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.os.Binder;
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
            
            DexClassLoader classLoader = new DexClassLoader(
                apkPath, dexOutputDir.getAbsolutePath(), nativeLibPath, getClassLoader()
            );
            
            SandboxContext sandboxContext = new SandboxContext(this, targetPkg);
            
            Intent launchIntent = getPackageManager().getLaunchIntentForPackage(targetPkg);
            String actualActivityName = launchIntent.getComponent().getClassName();
            Class<?> mainActivityClass = classLoader.loadClass(actualActivityName);
            
            Activity targetActivity = (Activity) mainActivityClass.newInstance();
            
            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            Method currentActivityThreadMethod = activityThreadClass.getDeclaredMethod("currentActivityThread");
            currentActivityThreadMethod.setAccessible(true);
            Object realActivityThread = currentActivityThreadMethod.invoke(null);
            
            Method getInstrumentationMethod = activityThreadClass.getDeclaredMethod("getInstrumentation");
            getInstrumentationMethod.setAccessible(true);
            Instrumentation realInstrumentation = (Instrumentation) getInstrumentationMethod.invoke(realActivityThread);

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
                
                // Get the REAL ActivityInfo
                ActivityInfo actInfo = getPackageManager().getActivityInfo(launchIntent.getComponent(), 0);
                // Create a dummy IBinder
                Binder dummyBinder = new Binder();
                
                args[0] = sandboxContext; 
                args[1] = realActivityThread; 
                args[2] = realInstrumentation; 
                args[3] = dummyBinder; // Real IBinder
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
                
                try {
                    attachMethod.invoke(targetActivity, args);
                } catch (Throwable t) {
                    // UNPACK THE REAL ERROR
                    Throwable cause = t.getCause() != null ? t.getCause() : t;
                    showError("Attach Failed (Real Cause):\n\n" + cause.toString());
                    return;
                }
            }
            
            Method onCreate = Activity.class.getDeclaredMethod("onCreate", Bundle.class);
            onCreate.setAccessible(true);
            try {
                onCreate.invoke(targetActivity, savedInstanceState);
            } catch (Throwable t) {
                Throwable cause = t.getCause() != null ? t.getCause() : t;
                showError("onCreate Failed (Real Cause):\n\n" + cause.toString());
                return;
            }
            
            setContentView(targetActivity.getWindow().getDecorView());

        } catch (Throwable t) {
            showError("Outer Error:\n" + t.toString());
        }
    }

    private void showError(String msg) {
        TextView tv = new TextView(this);
        tv.setText(msg);
        tv.setTextColor(Color.RED);
        tv.setTextSize(14);
        tv.setPadding(50, 50, 50, 50);
        tv.setBackgroundColor(Color.BLACK);
        setContentView(tv);
    }
}
