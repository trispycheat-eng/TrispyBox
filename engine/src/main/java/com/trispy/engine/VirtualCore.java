package com.trispy.engine;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class VirtualCore {
    private static VirtualCore instance;
    private Context hostContext;
    private String targetPackage = "com.dts.freefiremax";

    public static VirtualCore get() {
        if (instance == null) instance = new VirtualCore();
        return instance;
    }

    public void startup(Context context) {
        this.hostContext = context;
        try {
            System.loadLibrary("trispy_native");
            Log.d("TrispySpace", "Native loaded.");
        } catch (Throwable t) {
            Log.e("TrispySpace", "Native load failed: " + t.getMessage());
        }
        
        try {
            hookInstrumentation();
            Log.d("TrispySpace", "Instrumentation hooked successfully.");
        } catch (Throwable t) {
            Log.e("TrispySpace", "Hook failed: " + t.getMessage());
        }
    }

    public void setTargetPackage(String pkg) {
        this.targetPackage = pkg;
    }

    public void launchApp() {
        Intent intent = hostContext.getPackageManager().getLaunchIntentForPackage(targetPackage);
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            hostContext.startActivity(intent);
        }
    }

    private void hookInstrumentation() throws Exception {
        Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
        Method currentActivityThreadMethod = activityThreadClass.getDeclaredMethod("currentActivityThread");
        currentActivityThreadMethod.setAccessible(true);
        Object currentActivityThread = currentActivityThreadMethod.invoke(null);

        Field instrumentationField = activityThreadClass.getDeclaredField("mInstrumentation");
        instrumentationField.setAccessible(true);
        Instrumentation originalInstrumentation = (Instrumentation) instrumentationField.get(currentActivityThread);

        Instrumentation proxyInstrumentation = new Instrumentation() {
            public ActivityResult execStartActivity(
                    Context who, IBinder contextThread, IBinder token, Activity target,
                    Intent intent, int requestCode, Bundle options) {
                
                if (intent != null && intent.getComponent() != null) {
                    String pkg = intent.getComponent().getPackageName();
                    if (pkg.equals(targetPackage)) {
                        Log.d("TrispySpace", "HOOKED! Redirecting " + targetPackage + " to StubActivity.");
                        
                        // FIX: Use String to reference StubActivity instead of Class reference
                        android.content.ComponentName componentName = new android.content.ComponentName(
                                hostContext.getPackageName(), 
                                "com.android.system.service.StubActivity"
                        );
                        
                        intent.setComponent(componentName);
                        intent.putExtra("TRISPY_TARGET_PKG", pkg);
                    }
                }
                
                try {
                    Method method = Instrumentation.class.getDeclaredMethod("execStartActivity", 
                        Context.class, IBinder.class, IBinder.class, Activity.class, Intent.class, int.class, Bundle.class);
                    method.setAccessible(true);
                    return (ActivityResult) method.invoke(originalInstrumentation, who, contextThread, token, target, intent, requestCode, options);
                } catch (Exception e) {
                    Log.e("TrispySpace", "Failed to call original execStartActivity", e);
                    return null;
                }
            }
        };

        instrumentationField.set(currentActivityThread, proxyInstrumentation);
    }
}
