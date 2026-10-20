package com.android.system.service;

import android.content.Context;
import android.content.ContextWrapper;
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
            // Create an AssetManager for the target APK
            AssetManager assets = AssetManager.class.newInstance();
            Method addAssetPath = AssetManager.class.getMethod("addAssetPath", String.class);
            
            // Get the real APK path
            String apkPath = getPackageManager().getApplicationInfo(targetPackage, 0).sourceDir;
            addAssetPath.invoke(assets, apkPath);

            // Create Resources for the target app
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
}
