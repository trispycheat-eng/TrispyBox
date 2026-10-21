package com.android.system.service;

import android.app.Application;
import android.content.Context;
import top.niunaijun.blackbox.BlackBoxCore;
import top.niunaijun.blackbox.app.configuration.ClientConfiguration;

public class TrispyApp extends Application {
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        
        // Initialize the Virtual Machine Engine with fake device properties
        ClientConfiguration config = new ClientConfiguration.Default() {
            @Override
            public String getDeviceModel() {
                return "SM-S918B";
            }

            @Override
            public String getDeviceBrand() {
                return "samsung";
            }

            @Override
            public String getDeviceManufacturer() {
                return "samsung";
            }
        };

        BlackBoxCore.get().doAttachBaseContext(base, config);
        BlackBoxCore.get().doCreate();
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }
}
