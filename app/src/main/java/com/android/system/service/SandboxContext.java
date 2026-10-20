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

    // FIX OBB PATH: Redirect FF to the real OBB folder
    @Override
    public File getObbDir() {
        File realObb = new File(Environment.getExternalStorageDirectory(), "Android/obb/" + targetPackage);
        if (realObb.exists()) return realObb;
        return super.getObbDir();
    }

    // THE MAGIC GMS & SIGNATURE SPOOFER
    @Override
    public PackageManager getPackageManager() {
        PackageManager basePm = getBaseContext().getPackageManager();
        
        return (PackageManager) Proxy.newProxyInstance(
            PackageManager.class.getClassLoader(),
            new Class<?>[]{PackageManager.class},
            (proxy, method, args) -> {
                // 1. Spoof GMS Package Info
                if (method.getName().equals("getPackageInfo") && args != null && args.length > 0) {
                    String pkg = (String) args[0];
                    if (pkg != null && pkg.contains("com.google.android.gms")) {
                        PackageInfo fakeGms = new PackageInfo();
                        fakeGms.packageName = pkg;
                        fakeGms.versionName = "22.0.0";
                        return fakeGms;
                    }
                }
                
                // 2. Spoof Signature check (Prevents "hacked app" crash)
                if (method.getName().equals("getPackageSignatures") || method.getName().equals("checkSignatures")) {
                    if (method.getName().equals("checkSignatures")) return PackageManager.SIGNATURE_MATCH;
                    return new android.content.pm.Signature[]{ new android.content.pm.Signature("MOCK") };
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
