package com.android.system.service;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Environment;
import android.util.Log;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;

public class SandboxContext extends ContextWrapper {
    private final String targetPackage;
    private Resources targetResources;

    public SandboxContext(Context base, String targetPackage) {
        super(base);
        this.targetPackage = targetPackage;
        try {
            AssetManager assets = AssetManager.class.newInstance();
            java.lang.reflect.Method addAssetPath = AssetManager.class.getMethod("addAssetPath", String.class);
            String apkPath = getPackageManager().getApplicationInfo(targetPackage, 0).sourceDir;
            addAssetPath.invoke(assets, apkPath);

            Resources hostRes = getBaseContext().getResources();
            targetResources = new Resources(assets, hostRes.getDisplayMetrics(), hostRes.getConfiguration());
        } catch (Exception e) {
            Log.e("TrispySpace", "Failed to load resources: " + e.getMessage());
        }
    }

    @Override
    public String getPackageName() {
        return targetPackage;
    }

    @Override
    public Resources getResources() {
        return targetResources != null ? targetResources : super.getResources();
    }

    // --- AUTOMATIC REAL OBB PATH ---
    @Override
    public File getObbDir() {
        // Point directly to the real folder on the phone
        File realObb = new File(Environment.getExternalStorageDirectory(), "Android/obb/" + targetPackage);
        if (!realObb.exists()) {
            realObb.mkdirs(); // Create it if missing
        }
        return realObb;
    }

    @Override
    public File getFilesDir() {
        File files = new File(Environment.getExternalStorageDirectory(), "Android/data/" + targetPackage + "/files");
        if (!files.exists()) files.mkdirs();
        return files;
    }

    // --- GMS SPOOFER ---
    @Override
    public PackageManager getPackageManager() {
        PackageManager basePm = getBaseContext().getPackageManager();
        return (PackageManager) Proxy.newProxyInstance(
            PackageManager.class.getClassLoader(),
            new Class<?>[]{PackageManager.class},
            (proxy, method, args) -> {
                if (method.getName().equals("getPackageInfo") && args != null && args.length > 0) {
                    String pkg = (String) args[0];
                    if (pkg != null && pkg.contains("com.google.android.gms")) {
                        PackageInfo fakeGms = new PackageInfo();
                        fakeGms.packageName = pkg;
                        fakeGms.versionName = "22.0.0";
                        return fakeGms;
                    }
                }
                try {
                    return method.invoke(basePm, args);
                } catch (InvocationTargetException e) {
                    throw e.getCause();
                }
            }
        );
    }
}
