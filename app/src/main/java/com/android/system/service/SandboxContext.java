package com.android.system.service;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.ApplicationInfo;
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

    // --- VIRTUAL FILE SYSTEM REDIRECTION ---
    
    // Helper to get the root of our virtual SD card
    public File getVirtualRoot() {
        File root = new File(getBaseContext().getFilesDir(), "TrispyVM/0");
        if (!root.exists()) root.mkdirs();
        return root;
    }

    @Override
    public File getObbDir() {
        File obb = new File(getVirtualRoot(), "Android/obb/" + targetPackage);
        if (!obb.exists()) obb.mkdirs();
        return obb;
    }

    @Override
    public File getFilesDir() {
        File files = new File(getVirtualRoot(), "data/" + targetPackage + "/files");
        if (!files.exists()) files.mkdirs();
        return files;
    }

    @Override
    public File getCacheDir() {
        File cache = new File(getVirtualRoot(), "data/" + targetPackage + "/cache");
        if (!cache.exists()) cache.mkdirs();
        return cache;
    }

    @Override
    public File getExternalFilesDir(String type) {
        File ext = new File(getVirtualRoot(), "Android/data/" + targetPackage + "/files/" + (type != null ? type : ""));
        if (!ext.exists()) ext.mkdirs();
        return ext;
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
                        android.content.pm.PackageInfo fakeGms = new android.content.pm.PackageInfo();
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
