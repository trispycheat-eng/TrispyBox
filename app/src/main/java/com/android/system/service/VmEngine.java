package com.android.system.service;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import top.niunaijun.blackbox.BlackBoxCore;
import top.niunaijun.blackbox.entity.AppConfig;

public class VMEngine {
    private static final int USER_ID = 0;

    public static void launchApp(Context context, String packageName) {
        try {
            // Check if app is installed in the VM
            if (!BlackBoxCore.get().isInstalled(packageName, USER_ID)) {
                Toast.makeText(context, "App not installed in VM yet.", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = BlackBoxCore.get().getLaunchIntent(packageName, USER_ID);
            if (intent != null) {
                context.startActivity(intent);
            } else {
                Toast.makeText(context, "Failed to get launch intent.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(context, "Launch Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public static void installApkFromUri(Context context, Uri uri) {
        try {
            // Copy APK to internal storage
            File apkFile = new File(context.getCacheDir(), "target.apk");
            InputStream is = context.getContentResolver().openInputStream(uri);
            FileOutputStream fos = new FileOutputStream(apkFile);
            byte[] buffer = new byte[8192];
            int len;
            while ((len = is.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }
            fos.close();
            is.close();

            // Install into BlackBox
            top.niunaijun.blackbox.entity.pm.InstallResult result = 
                    BlackBoxCore.get().installPackageAsUser(apkFile.getAbsolutePath(), USER_ID);
            
            if (result.success) {
                Toast.makeText(context, "Installed successfully: " + result.packageName, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(context, "Install failed: " + result.error, Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
