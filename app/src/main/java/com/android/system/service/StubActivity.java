package com.android.system.service;

import android.app.Activity;
import android.app.ActivityThread;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
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
            
            // 2. Extract Native Libs
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
            
            // 5. Setup Unsafe to bypass ActivityClientRecord constructor
            Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
            Field theUnsafe = unsafeClass.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            sun.misc.Unsafe unsafe = (sun.misc.Unsafe) theUnsafe.get(null);

            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            Method currentActivityThreadMethod = activityThreadClass.getDeclaredMethod("currentActivityThread");
            currentActivityThreadMethod.setAccessible(true);
            Object currentActivityThread = currentActivityThreadMethod.invoke(null);

            Class<?> recordClass = Class.forName("android.app.ActivityThread$ActivityClientRecord");
            Object record = unsafe.allocateInstance(recordClass);
            
            // 6. Set fields using Reflection
            Field intentField = recordClass.getDeclaredField("intent");
            intentField.setAccessible(true);
            intentField.set(record, launchIntent);
            
            Field activityInfoField = recordClass.getDeclaredField("activityInfo");
            activityInfoField.setAccessible(true);
            activityInfoField.set(record, actInfo);

            Field tokenField = recordClass.getDeclaredField("token");
            tokenField.setAccessible(true);
            tokenField.set(record, new android.os.Binder());

            // 7. Call performLaunchActivity
            Method performLaunchActivityMethod = activityThreadClass.getDeclaredMethod("performLaunchActivity", recordClass, null);
            performLaunchActivityMethod.setAccessible(true);
            
            Activity targetActivity = (Activity) performLaunchActivityMethod.invoke(currentActivityThread, record, null);
            
            if (targetActivity != null) {
                showText("SUCCESS!\nActivity Launched: " + targetActivity.getClass().getSimpleName());
                // In the next step, we will get the SurfaceView from targetActivity to actually render it
            } else {
                showText("ERROR: performLaunchActivity returned null.");
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
