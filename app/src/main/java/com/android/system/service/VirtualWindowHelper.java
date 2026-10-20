package com.android.system.service;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.WindowManager;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class VirtualWindowHelper {
    private static final String TAG = "TrispyWindow";
    private static Object unsafeInstance;
    private static Class<?> unsafeClass;

    public static void initUnsafe() {
        try {
            unsafeClass = Class.forName("sun.misc.Unsafe");
            Field theUnsafe = unsafeClass.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            unsafeInstance = theUnsafe.get(null);
            Log.d(TAG, "Unsafe initialized in Helper.");
        } catch (Exception e) {
            Log.e(TAG, "Failed to init Unsafe in Helper", e);
        }
    }

    public static void injectEnvironment(Activity targetActivity, Context baseContext, Intent targetIntent, ComponentName componentName, String targetPkg, Object currentActivityThread, Object realInstrumentation) {
        try {
            Application dummyApp = (Application) unsafeClass.getMethod("allocateInstance", Class.class).invoke(unsafeInstance, Application.class);
            
            // 1. Core Contexts
            forceInjectField(targetActivity, "mBase", baseContext);
            forceInjectField(targetActivity, "mApplication", dummyApp);
            
            // 2. Intent & Component Metadata
            forceInjectField(targetActivity, "mIntent", targetIntent);
            forceInjectField(targetActivity, "mComponent", componentName);
            forceInjectField(targetActivity, "mCallingActivity", componentName);
            forceInjectField(targetActivity, "mCallingPackage", targetPkg);

            // 3. Threads & Handlers
            forceInjectField(targetActivity, "mUiThread", Thread.currentThread());
            forceInjectField(targetActivity, "mMainThread", currentActivityThread);
            forceInjectField(targetActivity, "mHandler", new Handler(Looper.getMainLooper()));
            forceInjectField(targetActivity, "mInstrumentation", realInstrumentation);

            // 4. Fix Locks (Prevents monitor-enter crash)
            forceInjectField(targetActivity, "mActivityLifecycleCallbacksLock", targetActivity);
            forceInjectField(targetActivity, "mActivityLifecycleCallbacks", new ArrayList<Application.ActivityLifecycleCallbacks>());

            // 5. The Magic Window (Prevents getSystemService crash)
            try {
                Class<?> phoneWindowClass = Class.forName("com.android.internal.policy.PhoneWindow");
                Constructor<?> windowConstructor = phoneWindowClass.getConstructor(Context.class);
                Object phoneWindow = windowConstructor.newInstance(baseContext);
                
                WindowManager windowManager = (WindowManager) baseContext.getSystemService(Context.WINDOW_SERVICE);
                
                // Set WindowManager
                forceInjectField(targetActivity, "mWindowManager", windowManager);
                
                // Set Window
                forceInjectField(targetActivity, "mWindow", phoneWindow);
                
                // Set DecorView
                Method getDecorViewMethod = phoneWindowClass.getMethod("getDecorView");
                Object decorView = getDecorViewMethod.invoke(phoneWindow);
                forceInjectField(targetActivity, "mDecor", decorView);
                
                Log.d(TAG, "PhoneWindow and WindowManager injected successfully.");
            } catch (Throwable t) {
                Log.e(TAG, "PhoneWindow injection failed (Expected on Android 11+ without bypass)", t);
            }

        } catch (Exception e) {
            Log.e(TAG, "Environment injection failed", e);
        }
    }

    public static void forceInjectField(Object obj, String fieldName, Object value) {
        try {
            Class<?> clazz = obj.getClass();
            Field field = null;
            while (clazz != null) {
                try {
                    field = clazz.getDeclaredField(fieldName);
                    break;
                } catch (NoSuchFieldException e) {
                    clazz = clazz.getSuperclass();
                }
            }
            
            if (field != null && unsafeInstance != null) {
                Method objectFieldOffsetMethod = unsafeClass.getMethod("objectFieldOffset", Field.class);
                long offset = (Long) objectFieldOffsetMethod.invoke(unsafeInstance, field);
                
                Method putObjectMethod = unsafeClass.getMethod("putObject", Object.class, long.class, Object.class);
                putObjectMethod.invoke(unsafeInstance, obj, offset, value);
            } else {
                Log.d(TAG, "Field not found: " + fieldName);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to inject " + fieldName, e);
        }
    }
}
