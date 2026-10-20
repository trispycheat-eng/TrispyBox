package com.trispy.engine;

import android.content.Context;
import android.content.Intent;

public class VirtualCore {
    private static VirtualCore instance;
    private Context hostContext;

    public static VirtualCore get() {
        if (instance == null) instance = new VirtualCore();
        return instance;
    }

    public void startup(Context context) {
        this.hostContext = context;
        System.loadLibrary("trispy_native"); // Load our C++ bypass
    }

    public void launchApp(String packageName) {
        try {
            Intent intent = hostContext.getPackageManager().getLaunchIntentForPackage(packageName);
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                hostContext.startActivity(intent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
