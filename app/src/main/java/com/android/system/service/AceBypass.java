package com.android.system.service;

import android.os.Build;
import java.lang.reflect.Field;

public class AceBypass {
    
    public static void initialize() {
        spoofHardware();
    }

    private static void spoofHardware() {
        try {
            // Fake everything ACE looks for to identify a VM
            setBuildField("MODEL", "SM-S918B");
            setBuildField("BRAND", "samsung");
            setBuildField("MANUFACTURER", "samsung");
            setBuildField("FINGERPRINT", "samsung/dm3qxxx/dm3q:13/TP1A.220624.014/S918BXXU1AWA6:user/release-keys");
            setBuildField("HARDWARE", "qcom");
            setBuildField("PRODUCT", "dm3qxxx");
            setBuildField("BOARD", "kalama");
            setBuildField("DEVICE", "dm3q");
            setBuildField("DISPLAY", "TP1A.220624.014");
        } catch (Exception e) {}
    }

    private static void setBuildField(String fieldName, String value) {
        try {
            Field field = Build.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(null, value);
        } catch (Exception e) {}
    }
}
