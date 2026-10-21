package com.android.system.service;

import android.app.Application;
import android.content.Context;
import top.niunaijun.blackbox.BlackBoxCore;
import top.niunaijun.blackbox.core.env.BEnvironment;

public class TrispyApp extends Application {
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        // Initialize the Virtual Machine Engine before anything else
        BlackBoxCore.get().doAttachBaseContext(base, new top.niunaijun.blackbox.app.configuration.ClientConfiguration.Default());
        BlackBoxCore.get().doCreate();
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }
}
