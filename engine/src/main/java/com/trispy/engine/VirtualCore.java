package com.trispy.engine;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class VirtualCore {
    private static VirtualCore instance;
    private Context hostContext;
    private String targetPackage = "com.dts.freefiremax"; // Default FF Max

    public static VirtualCore get() {
        if (instance == null) instance = new VirtualCore();
        return instance;
    }

    public void startup(Context context) {
        this.hostContext = context;
        System.loadLibrary("trispy_native"); // Load our C++ Bypass
        hookActivityThread(); // Hijack the system launcher
    }

    public void setTargetPackage(String pkg) {
        this.targetPackage = pkg;
    }

    public void launchApp() {
        try {
            Intent intent = hostContext.getPackageManager().getLaunchIntentForPackage(targetPackage);
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                hostContext.startActivity(intent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- THE MAGIC: Hooking ActivityThread ---
    private void hookActivityThread() {
        try {
            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            Method currentActivityThreadMethod = activityThreadClass.getDeclaredMethod("currentActivityThread");
            currentActivityThreadMethod.setAccessible(true);
            Object currentActivityThread = currentActivityThreadMethod.invoke(null);

            // Get the mH (Handler) field
            Field mHField = activityThreadClass.getDeclaredField("mH");
            mHField.setAccessible(true);
            Handler mH = (Handler) mHField.get(currentActivityThread);

            // Set our custom callback to intercept messages
            Field mCallbackField = Handler.class.getDeclaredField("mCallback");
            mCallbackField.setAccessible(true);
            
            mCallbackField.set(mH, new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    if (msg.what == 100) { 
                        try {
                            Object obj = msg.obj;
                            Field intentField = obj.getClass().getDeclaredField("intent");
                            intentField.setAccessible(true);
                            Intent intent = (Intent) intentField.get(obj);
                            
                            if (intent != null && intent.getComponent() != null) {
                                String pkg = intent.getComponent().getPackageName();
                                
                                // IF THE SYSTEM IS TRYING TO LAUNCH OUR TARGET APP...
                                if (pkg.equals(targetPackage)) {
                                    // Redirect it to our StubActivity!
                                    intent.setClassName(hostContext, "com.trispy.space.StubActivity");
                                    // Save the original target package so StubActivity knows what to load
                                    intent.putExtra("TRISPY_TARGET_PKG", pkg);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    return false; // Let the system continue processing
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
