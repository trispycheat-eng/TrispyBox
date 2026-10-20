package com.android.system.service;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
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
            ApplicationInfo appInfo = getPackageManager().getApplicationInfo(targetPkg, 0);
            String apkPath = appInfo.sourceDir;
            
            File extractedLibs = NativeLibraryExtractor.extractLibs(this, apkPath, targetPkg);
            
            SandboxContext sandboxContext = new SandboxContext(this, targetPkg);
            File dexOutputDir = getDir("dex_" + targetPkg, 0);
            DexClassLoader classLoader = new DexClassLoader(
                apkPath, dexOutputDir.getAbsolutePath(), extractedLibs.getAbsolutePath(), getClassLoader()
            );
            
            Intent launchIntent = getPackageManager().getLaunchIntentForPackage(targetPkg);
            ActivityInfo actInfo = getPackageManager().getActivityInfo(launchIntent.getComponent(), 0);
            
            Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
            Field theUnsafe = unsafeClass.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            Object unsafeInstance = theUnsafe.get(null);
            Method allocateInstanceMethod = unsafeClass.getMethod("allocateInstance", Class.class);

            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            Method currentActivityThreadMethod = activityThreadClass.getDeclaredMethod("currentActivityThread");
            currentActivityThreadMethod.setAccessible(true);
            Object currentActivityThread = currentActivityThreadMethod.invoke(null);

            Class<?> recordClass = Class.forName("android.app.ActivityThread$ActivityClientRecord");
            Object record = allocateInstanceMethod.invoke(unsafeInstance, recordClass);
            
            Field intentField = recordClass.getDeclaredField("intent");
            intentField.setAccessible(true);
            intentField.set(record, launchIntent);
            
            Field activityInfoField = recordClass.getDeclaredField("activityInfo");
            activityInfoField.setAccessible(true);
            activityInfoField.set(record, actInfo);

            Field tokenField = recordClass.getDeclaredField("token");
            tokenField.setAccessible(true);
            tokenField.set(record, new android.os.Binder());

            // --- FIXED METHOD REFLECTION ---
            Method performLaunchActivityMethod = null;
            for (Method m : activityThreadClass.getDeclaredMethods()) {
                if (m.getName().equals("performLaunchActivity")) {
                    performLaunchActivityMethod = m;
                    break;
                }
            }
            
            if (performLaunchActivityMethod != null) {
                performLaunchActivityMethod.setAccessible(true);
                
                // Handle Android version differences (1 arg vs 2 args)
                Object[] args;
                if (performLaunchActivityMethod.getParameterCount() == 1) {
                    args = new Object[]{record};
                } else {
                    args = new Object[]{record, null};
                }

                Activity targetActivity = (Activity) performLaunchActivityMethod.invoke(currentActivityThread, args);
                
                if (targetActivity != null) {
                    showText("SUCCESS!\nActivity Launched: " + targetActivity.getClass().getSimpleName());
                } else {
                    showText("ERROR: performLaunchActivity returned null.");
                }
            } else {
                showText("ERROR: performLaunchActivity method not found.");
            }

        } catch (Throwable t) {
            Log.e("TrispyVM", "Launch failed", t);
            Throwable cause = t.getCause() != null ? t.getCause() : t;
            showText("VM ERROR (Real Cause):\n" + cause.toString());
        }
    }

    private void showText(String msg) {
        TextView tv = new TextView(this);
        tv.setText(msg);
        tv.setTextColor(Color.GREEN);
        tv.setTextSize(16);
        tv.setPadding(50, 50, 50, 50);
        tv.setBackgroundColor(Color.BLACK);
        setContentView(tv);
    }
}
