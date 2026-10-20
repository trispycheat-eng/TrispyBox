package com.trispy.engine;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
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
            hookActivityThread(); 
        } catch (Throwable t) {
            Log.e("TrispySpace", "Hook failed: " + t.getMessage());
        }
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

    private void hookActivityThread() {
        try {
            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            Method currentActivityThreadMethod = activityThreadClass.getDeclaredMethod("currentActivityThread");
            currentActivityThreadMethod.setAccessible(true);
            Object currentActivityThread = currentActivityThreadMethod.invoke(null);

            Field mHField = activityThreadClass.getDeclaredField("mH");
            mHField.setAccessible(true);
            Handler mH = (Handler) mHField.get(currentActivityThread);

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
                                if (pkg.equals(targetPackage)) {
                                    intent.setClassName(hostContext, "com.trispy.space.StubActivity");
                                    intent.putExtra("TRISPY_TARGET_PKG", pkg);
                                }
                            }
                        } catch (Throwable e) {
                            // Silent fail on message processing
                        }
                    }
                    return false; 
                }
            });
        } catch (Throwable e) {
            Log.e("TrispySpace", "Reflection error: " + e.getMessage());
        }
    }
}
