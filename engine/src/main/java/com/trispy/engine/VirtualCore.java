package com.trispy.engine;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

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
    }

    public void setTargetPackage(String pkg) {
        this.targetPackage = pkg;
    }

    public void launchApp() {
        try {
            // 1. Get the real launch intent for Free Fire
            Intent intent = hostContext.getPackageManager().getLaunchIntentForPackage(targetPackage);
            
            if (intent != null) {
                // 2. HIJACK IT! Change the target to our StubActivity
                intent.setClassName(hostContext, "com.android.system.service.StubActivity");
                
                // 3. Tell StubActivity which app it's supposed to load
                intent.putExtra("TRISPY_TARGET_PKG", targetPackage);
                
                // 4. Launch it
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                hostContext.startActivity(intent);
            }
        } catch (Exception e) {
            Log.e("TrispySpace", "Launch failed: " + e.getMessage());
        }
    }
}
