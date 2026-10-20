package com.trispy.engine;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
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

        java.lang.reflect.Field instrumentationField = activityThreadClass.getDeclaredField("mInstrumentation");
        instrumentationField.setAccessible(true);
        Instrumentation originalInstrumentation = (Instrumentation) instrumentationField.get(currentActivityThread);

        Instrumentation proxyInstrumentation = new Instrumentation() {
            @Override
            public ActivityResult execStartActivity(
                    Context who, IBinder contextThread, IBinder token, Activity target,
                    Intent intent, int requestCode, Bundle options) {
                
                if (intent != null && intent.getComponent() != null) {
                    String pkg = intent.getComponent().getPackageName();
                    if (pkg.equals(targetPackage)) {
                        Log.d("TrispySpace", "INTERCEPTED LAUNCH! Redirecting to Sandbox.");
                        intent.setClassName(hostContext, "com.android.system.service.StubActivity");
                        intent.putExtra("TRISPY_TARGET_PKG", pkg);
                    }
                }
                return super.execStartActivity(who, contextThread, token, target, intent, requestCode, options);
            }
        };

        instrumentationField.set(currentActivityThread, proxyInstrumentation);
    }
}
