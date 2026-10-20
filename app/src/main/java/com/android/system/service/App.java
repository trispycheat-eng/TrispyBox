package com.android.system.service;

import android.app.Application;
import android.content.Intent;
import android.os.Process;
import android.util.Log;
import com.trispy.engine.VirtualCore;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            Log.e("TrispySpace", "CRASH: " + Log.getStackTraceString(throwable));
            Intent intent = new Intent(this, CrashActivity.class);
            intent.putExtra("error", Log.getStackTraceString(throwable));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            Process.killProcess(Process.myPid());
            System.exit(1);
        });

        try {
            VirtualCore.get().startup(this);
        } catch (Throwable t) {
            Log.e("TrispySpace", "Startup failed: " + t.getMessage());
        }
    }
}
