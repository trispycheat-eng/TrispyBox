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
import java.lang.reflect.Method;

public class StubActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        String targetPkg = getIntent().getStringExtra("TRISPY_TARGET_PKG");
        Log.d("TrispySpace", "Virtual Space Started for: " + targetPkg);

        try {
            ApplicationInfo appInfo = getPackageManager().getApplicationInfo(targetPkg, 0);
            String apkPath = appInfo.sourceDir;
            String nativeLibPath = appInfo.nativeLibraryDir; 
            
            File dexOutputDir = getDir("dex", 0);
            
            // 1. Create ClassLoader
            DexClassLoader classLoader = new DexClassLoader(
                apkPath,
                dexOutputDir.getAbsolutePath(),
                nativeLibPath,
                getClassLoader()
            );
            
            // 2. Load the target class
            Intent launchIntent = getPackageManager().getLaunchIntentForPackage(targetPkg);
            String actualActivityName = launchIntent.getComponent().getClassName();
            Class<?> mainActivityClass = classLoader.loadClass(actualActivityName);
            
            // 3. Try to launch it using Reflection
            // This is where VirtualApp magic happens. We instantiate the Activity.
            Activity targetActivity = (Activity) mainActivityClass.newInstance();
            
            // We need to manually attach the context to the target Activity
            // This requires calling hidden Android APIs via reflection
            Method attachMethod = Activity.class.getDeclaredMethod("attach",
                    android.content.Context.class,
                    Class.forName("android.app.ActivityThread"),
                    Class.forName("android.app.Instrumentation"),
                    android.os.IBinder.class,
                    int.class,
                    android.app.Application.class,
                    Intent.class,
                    Class.forName("android.content.pm.ActivityInfo"),
                    CharSequence.class,
                    Activity.class,
                    String.class,
                    Object.class,
                    Object.class
            );
            attachMethod.setAccessible(true);
            
            // Get ActivityThread and Instrumentation
            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            Method currentActivityThread = activityThreadClass.getDeclaredMethod("currentActivityThread");
            currentActivityThread.setAccessible(true);
            Object activityThread = currentActivityThread.invoke(null);
            
            Method getInstrumentation = activityThreadClass.getDeclaredMethod("getInstrumentation");
            getInstrumentation.setAccessible(true);
            Object instrumentation = getInstrumentation.invoke(activityThread);
            
            // Attach the context
            attachMethod.invoke(targetActivity, this, activityThread, instrumentation, null, 0, getApplication(), launchIntent, null, "", null, null, null, null);
            
            // 4. Call onCreate to start the game
            Method onCreateMethod = Activity.class.getDeclaredMethod("onCreate", Bundle.class);
            onCreateMethod.setAccessible(true);
            onCreateMethod.invoke(targetActivity, savedInstanceState);
            
            // Set the game's view to our screen
            setContentView(targetActivity.getWindow().getDecorView());

        } catch (Throwable t) {
            Log.e("TrispySpace", "Virtual load failed", t);
            TextView tv = new TextView(this);
            tv.setText("VIRTUAL SPACE ERROR:\n" + t.getMessage() + "\n\nThis is normal on Android 11+ without full Binder Hooks.");
            tv.setTextColor(Color.RED);
            tv.setTextSize(16);
            tv.setPadding(50, 50, 50, 50);
            tv.setBackgroundColor(Color.BLACK);
            setContentView(tv);
        }
    }
}
