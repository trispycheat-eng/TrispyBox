package com.android.system.service;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.util.Log;
import java.lang.reflect.Method;

public class SandboxContext extends ContextWrapper {
    private final String targetPackage;
    private Resources targetResources;

    public SandboxContext(Context base, String targetPackage) {
        super(base);
        this.targetPackage = targetPackage;
        try {
            AssetManager assets = AssetManager.class.newInstance();
            Method addAssetPath = AssetManager.class.getMethod("addAssetPath", String.class);
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

    // SPOOF GOOGLE PLAY SERVICES
    @Override
    public PackageManager getPackageManager() {
        return new PackageManagerWrapper(getBaseContext().getPackageManager()) {
            @Override
            public android.content.pm.PackageInfo getPackageInfo(String packageName, int flags) throws NameNotFoundException {
                // If FF asks for GMS, return a fake PackageInfo
                if (packageName.equals("com.google.android.gms")) {
                    android.content.pm.PackageInfo info = new android.content.pm.PackageInfo();
                    info.packageName = "com.google.android.gms";
                    info.versionName = "22.0.0";
                    return info;
                }
                return super.getPackageInfo(packageName, flags);
            }
        };
    }
}
