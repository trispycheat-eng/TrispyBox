package com.trispy.space;

import android.app.Application;
import android.util.Log;
import com.trispy.engine.VirtualCore;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        try {
            VirtualCore.get().startup(this);
        } catch (Throwable t) {
            Log.e("TrispySpace", "Startup failed: " + t.getMessage());
        }
    }
}
