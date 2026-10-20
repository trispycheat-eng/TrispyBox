package com.android.system.service;

import android.app.Activity;
import android.util.Log;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class RealmeBypass {
    private static final String TAG = "TrispyRealme";

    public static void inject(Activity activity) {
        try {
            // Realme/ColorOS uses a custom IActivityExt interface inside Activity
            Class<?> extClass = Class.forName("android.app.IActivityExt");
            
            // Create a dummy Proxy that does nothing when checkViewCacheThread() is called
            Object dummyExt = Proxy.newProxyInstance(
                extClass.getClassLoader(),
                new Class<?>[]{extClass},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) {
                        Class<?> retType = method.getReturnType();
                        if (retType == boolean.class) return false;
                        if (retType == int.class) return 0;
                        if (retType == long.class) return 0L;
                        return null;
                    }
                }
            );

            // Inject it into the Activity
            VirtualWindowHelper.forceInjectField(activity, "mActivityExt", dummyExt);
            Log.d(TAG, "Realme IActivityExt bypass injected successfully.");
        } catch (ClassNotFoundException e) {
            Log.d(TAG, "Not a Realme device. Skipping IActivityExt bypass.");
        } catch (Exception e) {
            Log.e(TAG, "Failed to inject Realme bypass", e);
        }
    }
}
