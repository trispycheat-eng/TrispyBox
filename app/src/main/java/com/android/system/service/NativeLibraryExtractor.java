package com.android.system.service;

import android.content.Context;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class NativeLibraryExtractor {
    public static File extractLibs(Context context, String apkPath, String targetPackage) {
        File libDir = new File(context.getDir("vm_libs", Context.MODE_PRIVATE), targetPackage);
        if (!libDir.exists()) libDir.mkdirs();

        try {
            ZipFile zip = new ZipFile(apkPath);
            java.util.Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String name = entry.getName();
                
                // Extract only arm64-v8a or armeabi-v7a .so files
                if (name.startsWith("lib/arm64-v8a/") || name.startsWith("lib/armeabi-v7a/")) {
                    File outFile = new File(libDir, new File(name).getName());
                    if (!outFile.exists()) {
                        InputStream is = zip.getInputStream(entry);
                        FileOutputStream fos = new FileOutputStream(outFile);
                        byte[] buffer = new byte[8192];
                        int len;
                        while ((len = is.read(buffer)) > 0) fos.write(buffer, 0, len);
                        fos.close();
                        is.close();
                        Log.d("TrispyVM", "Extracted: " + outFile.getName());
                    }
                }
            }
            zip.close();
        } catch (Exception e) {
            Log.e("TrispyVM", "Lib extraction failed: " + e.getMessage());
        }
        return libDir;
    }
}
