package com.android.system.service;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import dalvik.system.DexClassLoader;

/**
 * =========================================================================================
 * TRISPY SPACE ENGINE - GOD TIER STUB ACTIVITY
 * =========================================================================================
 * This class is the core of the Virtual Space. It intercepts the launch of the target
 * application (Free Fire Max), extracts its native libraries, builds a custom ClassLoader
 * that supports Split APKs, fakes the Google Play Services (GMS) environment, and uses
 * deep Android Reflection to attach the target Activity to our Host process.
 *
 * It includes an on-screen debugger so the user can see exactly what the engine is doing
 * in real-time, preventing silent crashes.
 * =========================================================================================
 */
public class StubActivity extends Activity {

    // =========================================================================================
    // GLOBAL VARIABLES & CONSTANTS
    // =========================================================================================
    private static final String TAG = "TrispyEngine_GodTier";
    private static final String DEFAULT_TARGET_PACKAGE = "com.dts.freefiremax";
    private static final String FALLBACK_FF_ACTIVITY = "com.dts.freefireth.FFMainActivity";

    private String targetPackageName = DEFAULT_TARGET_PACKAGE;
    private String targetActivityClassName = "";
    
    private WindowManager windowManager;
    private View floatingPanel;
    private TextView logTextView;
    private StringBuilder logBuilder = new StringBuilder();
    private ScrollView logScrollView;

    private ApplicationInfo targetAppInfo;
    private ActivityInfo targetActivityInfo;
    private DexClassLoader targetClassLoader;
    private SandboxContext sandboxContext;

    private boolean isInitialized = false;
    private boolean isEngineRunning = false;

    // =========================================================================================
    // LIFECYCLE METHODS
    // =========================================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 1. Setup the On-Screen Debugger
        initializeDebuggerUI();
        logMessage("========================================");
        logMessage("INITIALIZING TRISPY VIRTUAL SPACE ENGINE");
        logMessage("========================================");

        // 2. Capture Intent Data
        captureIntentData();

        // 3. Start the Engine Sequence
        try {
            startEngineSequence();
        } catch (Throwable t) {
            logFatalError("CRITICAL ENGINE FAILURE", t);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        destroyFloatingPanel();
        logMessage("Engine Destroyed.");
    }

    // =========================================================================================
    // SECTION 1: ON-SCREEN DEBUGGER (UI)
    // =========================================================================================

    private void initializeDebuggerUI() {
        try {
            logScrollView = new ScrollView(this);
            logScrollView.setBackgroundColor(Color.BLACK);
            
            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(40, 40, 40, 40);
            
            logTextView = new TextView(this);
            logTextView.setTextColor(Color.GREEN);
            logTextView.setTextSize(11);
            logTextView.setTypeface(android.graphics.Typeface.MONOSPACE);
            
            layout.addView(logTextView);
            logScrollView.addView(layout);
            setContentView(logScrollView);
        } catch (Throwable t) {
            Log.e(TAG, "Failed to init debug UI", t);
        }
    }

    private void logMessage(final String msg) {
        Log.d(TAG, msg);
        logBuilder.append("[*] ").append(msg).append("\n");
        updateDebuggerText();
    }

    private void logError(final String msg) {
        Log.e(TAG, msg);
        logBuilder.append("[!] ").append(msg).append("\n");
        updateDebuggerText();
    }

    private void logFatalError(final String header, final Throwable t) {
        Log.e(TAG, header, t);
        logBuilder.append("\n[X] ").append(header).append("\n");
        logBuilder.append("Reason: ").append(t.toString()).append("\n");
        if (t.getCause() != null) {
            logBuilder.append("Cause: ").append(t.getCause().toString()).append("\n");
        }
        updateDebuggerText();
        
        // Turn text red on fatal error
        runOnUiThread(() -> {
            if (logTextView != null) logTextView.setTextColor(Color.RED);
        });
    }

    private void updateDebuggerText() {
        runOnUiThread(() -> {
            if (logTextView != null) {
                logTextView.setText(logBuilder.toString());
                // Auto scroll to bottom
                if (logScrollView != null) {
                    logScrollView.post(() -> logScrollView.fullScroll(View.FOCUS_DOWN));
                }
            }
        });
    }

    // =========================================================================================
    // SECTION 2: INTENT & CONTEXT RESOLUTION
    // =========================================================================================

    private void captureIntentData() {
        Intent intent = getIntent();
        if (intent != null) {
            String pkg = intent.getStringExtra("TRISPY_TARGET_PKG");
            if (pkg != null && !pkg.isEmpty()) {
                targetPackageName = pkg;
            }
        }
        logMessage("Target Package: " + targetPackageName);
    }

    private void startEngineSequence() throws Exception {
        // Step 1: Get Application Info
        resolveApplicationInfo();
        
        // Step 2: Extract Native Libraries (Prevents UnsatisfiedLinkError)
        extractNativeLibraries();
        
        // Step 3: Resolve the target Activity
        resolveTargetActivity();
        
        // Step 4: Setup ClassLoader & Sandbox Context
        setupEnvironment();
        
        // Step 5: Launch the Activity
        launchVirtualActivity();
        
        // Step 6: Show Floating Panel
        showFloatingPanel();
        
        isEngineRunning = true;
        logMessage("========================================");
        logMessage("ENGINE STARTED SUCCESSFULLY");
        logMessage("========================================");
    }

    // =========================================================================================
    // SECTION 3: APPLICATION INFO RESOLVER
    // =========================================================================================

    private void resolveApplicationInfo() throws PackageManager.NameNotFoundException {
        logMessage("Fetching ApplicationInfo for: " + targetPackageName);
        targetAppInfo = getPackageManager().getApplicationInfo(targetPackageName, 0);
        if (targetAppInfo == null) {
            throw new RuntimeException("ApplicationInfo is null. Is the game installed?");
        }
        logMessage("APK Path: " + targetAppInfo.sourceDir);
        logMessage("Native Lib Path: " + targetAppInfo.nativeLibraryDir);
    }

    // =========================================================================================
    // SECTION 4: NATIVE LIBRARY EXTRACTOR (God-Tier)
    // =========================================================================================

    private void extractNativeLibraries() {
        logMessage("Starting Native Library Extraction...");
        
        File libDir = new File(getFilesDir(), "trispy_vm_libs/" + targetPackageName);
        if (!libDir.exists()) {
            boolean created = libDir.mkdirs();
            logMessage("Lib Dir Created: " + created);
        }

        String[] targetAbis = {"arm64-v8a"}; // Free Fire Max is strictly 64-bit

        try (ZipFile zip = new ZipFile(targetAppInfo.sourceDir)) {
            Enumeration<? extends ZipEntry> entries = zip.entries();
            
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String entryName = entry.getName();
                
                // Check if the entry is a native lib for our target ABI
                if (entryName.startsWith("lib/")) {
                    boolean isTargetAbi = false;
                    for (String abi : targetAbis) {
                        if (entryName.startsWith("lib/" + abi + "/")) {
                            isTargetAbi = true;
                            break;
                        }
                    }
                    
                    if (isTargetAbi && entryName.endsWith(".so")) {
                        String fileName = entryName.substring(entryName.lastIndexOf('/') + 1);
                        File outFile = new File(libDir, fileName);
                        
                        if (!outFile.exists()) {
                            logMessage("Extracting: " + fileName);
                            try (InputStream is = zip.getInputStream(entry);
                                 FileOutputStream fos = new FileOutputStream(outFile)) {
                                byte[] buffer = new byte[8192];
                                int length;
                                while ((length = is.read(buffer)) > 0) {
                                    fos.write(buffer, 0, length);
                                }
                            }
                        }
                    }
                }
            }
            logMessage("Native Library Extraction Complete.");
        } catch (Throwable t) {
            logError("Failed to extract native libs: " + t.getMessage());
        }
    }

    // =========================================================================================
    // SECTION 5: ACTIVITY RESOLVER (God-Tier Fallbacks)
    // =========================================================================================

    private void resolveTargetActivity() {
        logMessage("Resolving Target Activity...");

        // --- FALLBACK 1: getLaunchIntentForPackage ---
        try {
            Intent intent = getPackageManager().getLaunchIntentForPackage(targetPackageName);
            if (intent != null && intent.getComponent() != null) {
                targetActivityClassName = intent.getComponent().getClassName();
                logMessage("Resolved via Intent: " + targetActivityClassName);
                return;
            }
        } catch (Throwable t) {
            logError("Intent resolution failed.");
        }

        // --- FALLBACK 2: queryIntentActivities ---
        try {
            Intent queryIntent = new Intent(Intent.ACTION_MAIN);
            queryIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            queryIntent.setPackage(targetPackageName);
            List<ResolveInfo> resolveInfos = getPackageManager().queryIntentActivities(queryIntent, 0);
            
            if (resolveInfos != null && !resolveInfos.isEmpty()) {
                ActivityInfo info = resolveInfos.get(0).activityInfo;
                if (info != null && info.name != null) {
                    targetActivityClassName = info.name;
                    logMessage("Resolved via Query: " + targetActivityClassName);
                    return;
                }
            }
        } catch (Throwable t) {
            logError("Query resolution failed.");
        }

        // --- FALLBACK 3: Hardcoded for Free Fire Max ---
        if (targetPackageName.contains("com.dts.freefiremax") || 
            targetPackageName.contains("com.dts.freefireth")) {
            targetActivityClassName = FALLBACK_FF_ACTIVITY;
            logMessage("Resolved via Hardcoded: " + targetActivityClassName);
            return;
        }

        // --- FALLBACK 4: Ultimate crash prevention ---
        if (targetActivityClassName == null || targetActivityClassName.isEmpty()) {
            throw new RuntimeException("Could not resolve Activity for " + targetPackageName);
        }
    }

    // =========================================================================================
    // SECTION 6: ENVIRONMENT SETUP (ClassLoader & Context)
    // =========================================================================================

    private void setupEnvironment() throws Exception {
        logMessage("Setting up Sandbox Context & ClassLoader...");

        // 1. Build the DexPath (Handling Split APKs)
        StringBuilder dexPathBuilder = new StringBuilder(targetAppInfo.sourceDir);
        if (targetAppInfo.splitSourceDirs != null && targetAppInfo.splitSourceDirs.length > 0) {
            for (String split : targetAppInfo.splitSourceDirs) {
                dexPathBuilder.append(":").append(split);
                logMessage("Detected Split APK: " + split);
            }
        }
        String finalDexPath = dexPathBuilder.toString();

        // 2. Create the SandboxContext (Spoofs GMS, Paths, etc.)
        sandboxContext = new SandboxContext(this, targetPackageName);

        // 3. Create the DexClassLoader
        File dexOutputDir = getDir("trispy_dex_opt", Context.MODE_PRIVATE);
        File libDir = new File(getFilesDir(), "trispy_vm_libs/" + targetPackageName);
        
        targetClassLoader = new DexClassLoader(
                finalDexPath,
                dexOutputDir.getAbsolutePath(),
                libDir.getAbsolutePath(),
                getClassLoader()
        );

        // 4. Fetch ActivityInfo
        ComponentName component = new ComponentName(targetPackageName, targetActivityClassName);
        targetActivityInfo = getPackageManager().getActivityInfo(component, 0);
    }

    // =========================================================================================
    // SECTION 7: THE REFLECTION HOOK (God-Tier Launch)
    // =========================================================================================

    private void launchVirtualActivity() throws Exception {
        logMessage("Initiating Reflection Hook...");

        // 1. Get ActivityThread
        Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
        Method currentActivityThreadMethod = activityThreadClass.getDeclaredMethod("currentActivityThread");
        currentActivityThreadMethod.setAccessible(true);
        Object currentActivityThread = currentActivityThreadMethod.invoke(null);

        // 2. Get Instrumentation
        Method getInstrumentationMethod = activityThreadClass.getDeclaredMethod("getInstrumentation");
        getInstrumentationMethod.setAccessible(true);
        Instrumentation realInstrumentation = (Instrumentation) getInstrumentationMethod.invoke(currentActivityThread);

        // 3. Instantiate Target Activity
        logMessage("Loading Class: " + targetActivityClassName);
        Class<?> mainActivityClass = targetClassLoader.loadClass(targetActivityClassName);
        Activity targetActivity = (Activity) mainActivityClass.newInstance();

        // 4. Create Fake Intent
        Intent targetIntent = new Intent();
        targetIntent.setComponent(new ComponentName(targetPackageName, targetActivityClassName));

        // 5. Find and Invoke Attach
        Method attachMethod = findAttachMethod();
        if (attachMethod != null) {
            attachMethod.setAccessible(true);
            Object[] args = buildAttachArguments(attachMethod, currentActivityThread, realInstrumentation, targetIntent);
            logMessage("Invoking attach()...");
            attachMethod.invoke(targetActivity, args);
        }

        // 6. Call onCreate via Instrumentation
        logMessage("Invoking onCreate()...");
        realInstrumentation.callActivityOnCreate(targetActivity, null);

        // 7. Set the Content View
        logMessage("Setting Content View...");
        setContentView(targetActivity.getWindow().getDecorView());
        logMessage("SUCCESS! Engine should be rendering.");
    }

    private Method findAttachMethod() {
        for (Method m : Activity.class.getDeclaredMethods()) {
            if (m.getName().equals("attach")) {
                return m;
            }
        }
        return null;
    }

    private Object[] buildAttachArguments(Method attachMethod, Object activityThread, Instrumentation instrumentation, Intent intent) {
        int paramCount = attachMethod.getParameterCount();
        Object[] args = new Object[paramCount];

        // Fill standard parameters (Android 5 to 10)
        args[0] = sandboxContext;       // Context
        args[1] = activityThread;       // ActivityThread
        args[2] = instrumentation;      // Instrumentation
        args[3] = new Binder();         // IBinder
        args[4] = 0;                    // int
        args[5] = getApplication();     // Application
        args[6] = intent;               // Intent
        args[7] = targetActivityInfo;   // ActivityInfo
        args[8] = "";                   // CharSequence
        args[9] = null;                 // Activity (Parent)
        args[10] = "";                  // String
        args[11] = null;                // Object
        args[12] = null;                // Object

        // For newer Android versions (Android 11+), nullify extras
        for (int i = 13; i < paramCount; i++) {
            args[i] = null;
        }

        return args;
    }

    // =========================================================================================
    // SECTION 8: FLOATING PANEL
    // =========================================================================================

    private void showFloatingPanel() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
                logError("Overlay permission missing.");
                return;
            }

            windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
            int type = Build.VERSION.SDK_INT >= 26
                    ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                    : Win
