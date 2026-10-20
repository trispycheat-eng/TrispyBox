package com.android.system.service;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.Looper;
import android.os.Process;
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
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
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
 * TRISPY VIRTUAL SPACE ENGINE - THE ULTIMATE MONOLITH
 * =========================================================================================
 * This engine deeply hooks into Android's PackageManager, Resources, and ActivityThread.
 * It spoofs GMS, fakes OBB paths, extracts native libs, and launches the target Activity.
 * =========================================================================================
 */
public class StubActivity extends Activity {

    // =========================================================================================
    // SECTION 0: GLOBAL CONSTANTS AND VARIABLES
    // =========================================================================================
    private static final String TAG = "TrispyUltimate";
    private static final String DEFAULT_TARGET_PACKAGE = "com.dts.freefiremax";
    private static final String FALLBACK_FF_ACTIVITY = "com.dts.freefireth.FFMainActivity";
    
    private String targetPackageName = DEFAULT_TARGET_PACKAGE;
    private String targetActivityClassName = "";
    
    // UI Components
    private WindowManager windowManager;
    private View floatingPanelView;
    private TextView logTextView;
    private ScrollView logScrollView;
    private StringBuilder logStringBuilder = new StringBuilder();
    
    // Engine Components
    private ApplicationInfo targetAppInfo;
    private ActivityInfo targetActivityInfo;
    private DexClassLoader targetClassLoader;
    private SandboxContext sandboxContext;
    
    // State Flags
    private boolean isEngineInitialized = false;
    private boolean isActivityLaunched = false;
    private boolean isFloatingMenuActive = false;
    
    // Virtual File System Paths
    private File virtualRootDir;
    private File virtualDataDir;
    private File virtualObbDir;

    // =========================================================================================
    // SECTION 1: LIFECYCLE MANAGEMENT
    // =========================================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        initializeDebuggerUI();
        log("=========================================");
        log("INITIALIZING TRISPY ULTIMATE ENGINE");
        log("=========================================");
        
        installGlobalCrashHandler();
        HardwareSpoofer.spoofHardware();
        captureIntentData();
        
        try {
            executeEngineSequence();
        } catch (Throwable t) {
            logFatalError("ENGINE FATAL CRASH", t);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        destroyFloatingPanel();
        log("Engine Destroyed. Cleaning up...");
    }

    // =========================================================================================
    // SECTION 2: THE ON-SCREEN DEBUGGER UI
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

    private void log(final String msg) {
        Log.d(TAG, msg);
        logStringBuilder.append("[*] ").append(msg).append("\n");
        updateDebuggerText();
    }

    private void logError(final String msg) {
        Log.e(TAG, msg);
        logStringBuilder.append("[!] ").append(msg).append("\n");
        updateDebuggerText();
    }

    private void logFatalError(final String header, final Throwable t) {
        Log.e(TAG, header, t);
        logStringBuilder.append("\n[X] ").append(header).append("\n");
        logStringBuilder.append("Reason: ").append(t.toString()).append("\n");
        if (t.getCause() != null) {
            logStringBuilder.append("Cause: ").append(t.getCause().toString()).append("\n");
        }
        updateDebuggerText();
        
        runOnUiThread(() -> {
            if (logTextView != null) logTextView.setTextColor(Color.RED);
        });
    }

    private void updateDebuggerText() {
        runOnUiThread(() -> {
            if (logTextView != null) {
                logTextView.setText(logStringBuilder.toString());
                if (logScrollView != null) {
                    logScrollView.post(() -> logScrollView.fullScroll(View.FOCUS_DOWN));
                }
            }
        });
    }

    // =========================================================================================
    // SECTION 3: CRASH HANDLER
    // =========================================================================================

    private void installGlobalCrashHandler() {
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            logFatalError("UNCAUGHT EXCEPTION", throwable);
            
            try {
                File crashFile = new File(getFilesDir(), "trispy_crash_log.txt");
                FileOutputStream fos = new FileOutputStream(crashFile, true);
                PrintWriter pw = new PrintWriter(fos);
                pw.println("\n--- CRASH AT " + System.currentTimeMillis() + " ---");
                throwable.printStackTrace(pw);
                pw.close();
                fos.close();
            } catch (IOException e) {
                Log.e(TAG, "Failed to write crash log", e);
            }
            
            try { Thread.sleep(2000); } catch (Exception e) {}
            Process.killProcess(Process.myPid());
            System.exit(1);
        });
    }

    // =========================================================================================
    // SECTION 4: INTENT CAPTURE
    // =========================================================================================

    private void captureIntentData() {
        Intent intent = getIntent();
        if (intent != null) {
            String pkg = intent.getStringExtra("TRISPY_TARGET_PKG");
            if (pkg != null && !pkg.isEmpty()) {
                targetPackageName = pkg;
            }
        }
        log("Target Package: " + targetPackageName);
    }

    // =========================================================================================
    // SECTION 5: ENGINE EXECUTION SEQUENCE
    // =========================================================================================

    private void executeEngineSequence() throws Exception {
        if (isEngineInitialized) return;
        
        resolveApplicationInfo();
        setupVirtualFileSystem();
        extractNativeLibraries();
        resolveTargetActivity();
        setupEnvironment();
        launchVirtualActivity();
        showFloatingPanel();
        
        isEngineInitialized = true;
        log("=========================================");
        log("ENGINE STARTED SUCCESSFULLY");
        log("=========================================");
    }

    private void resolveApplicationInfo() throws PackageManager.NameNotFoundException {
        log("Fetching ApplicationInfo...");
        targetAppInfo = getPackageManager().getApplicationInfo(targetPackageName, 0);
        if (targetAppInfo == null) {
            throw new RuntimeException("ApplicationInfo is null. Is the game installed?");
        }
        log("APK: " + targetAppInfo.sourceDir);
        log("Native Lib Dir: " + targetAppInfo.nativeLibraryDir);
    }

    // =========================================================================================
    // SECTION 6: VIRTUAL FILE SYSTEM (VFS)
    // =========================================================================================

    private void setupVirtualFileSystem() {
        log("Setting up Virtual File System (VFS)...");
        
        virtualRootDir = new File(getFilesDir(), "TrispyVM");
        virtualDataDir = new File(virtualRootDir, "Data/" + targetPackageName);
        virtualObbDir = new File(virtualRootDir, "Obb/" + targetPackageName);
        
        if (!virtualRootDir.exists()) virtualRootDir.mkdirs();
        if (!virtualDataDir.exists()) virtualDataDir.mkdirs();
        if (!virtualObbDir.exists()) virtualObbDir.mkdirs();
        
        log("VFS Data Dir: " + virtualDataDir.getAbsolutePath());
        log("VFS Obb Dir: " + virtualObbDir.getAbsolutePath());
    }

    // =========================================================================================
    // SECTION 7: NATIVE LIBRARY EXTRACTION
    // =========================================================================================

    private void extractNativeLibraries() {
        log("Extracting Native Libraries from APK...");
        File libDir = new File(getFilesDir(), "TrispyVM/Libs/" + targetPackageName);
        if (!libDir.exists()) libDir.mkdirs();

        try (ZipFile zip = new ZipFile(targetAppInfo.sourceDir)) {
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String entryName = entry.getName();
                
                if (entryName.startsWith("lib/arm64-v8a/") && entryName.endsWith(".so")) {
                    String fileName = entryName.substring(entryName.lastIndexOf('/') + 1);
                    File outFile = new File(libDir, fileName);
                    
                    if (!outFile.exists()) {
                        try (InputStream is = zip.getInputStream(entry);
                             FileOutputStream fos = new FileOutputStream(outFile)) {
                            byte[] buffer = new byte[8192];
                            int len;
                            while ((len = is.read(buffer)) > 0) {
                                fos.write(buffer, 0, len);
                            }
                        }
                        log("Extracted: " + fileName);
                    }
                }
            }
            log("Native Libraries Extracted Successfully.");
        } catch (Throwable t) {
            logError("Failed to extract libs: " + t.getMessage());
        }
    }

    // =========================================================================================
    // SECTION 8: ACTIVITY RESOLVER
    // =========================================================================================

    private void resolveTargetActivity() {
        log("Resolving Target Activity...");

        try {
            Intent intent = getPackageManager().getLaunchIntentForPackage(targetPackageName);
            if (intent != null && intent.getComponent() != null) {
                targetActivityClassName = intent.getComponent().getClassName();
                log("Resolved via Intent: " + targetActivityClassName);
                return;
            }
        } catch (Throwable t) { logError("Intent resolution failed."); }

        try {
            Intent queryIntent = new Intent(Intent.ACTION_MAIN);
            queryIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            queryIntent.setPackage(targetPackageName);
            List<ResolveInfo> resolveInfos = getPackageManager().queryIntentActivities(queryIntent, 0);
            if (resolveInfos != null && !resolveInfos.isEmpty()) {
                ActivityInfo info = resolveInfos.get(0).activityInfo;
                if (info != null && info.name != null) {
                    targetActivityClassName = info.name;
                    log("Resolved via Query: " + targetActivityClassName);
                    return;
                }
            }
        } catch (Throwable t) { logError("Query resolution failed."); }

        if (targetPackageName.contains("freefire")) {
            targetActivityClassName = FALLBACK_FF_ACTIVITY;
            log("Resolved via Hardcoded: " + targetActivityClassName);
            return;
        }

        if (targetActivityClassName == null || targetActivityClassName.isEmpty()) {
            throw new RuntimeException("Could not resolve Activity for " + targetPackageName);
        }
    }

    // =========================================================================================
    // SECTION 9: ENVIRONMENT SETUP (ClassLoader & Context)
    // =========================================================================================

    private void setupEnvironment() throws Exception {
        log("Setting up Sandbox Context...");
        sandboxContext = new SandboxContext(this, targetPackageName);

        log("Building DexClassLoader (Split APK Support)...");
        StringBuilder dexPathBuilder = new StringBuilder(targetAppInfo.sourceDir);
        if (targetAppInfo.splitSourceDirs != null && targetAppInfo.splitSourceDirs.length > 0) {
            for (String split : targetAppInfo.splitSourceDirs) {
                dexPathBuilder.append(":").append(split);
                log("Added Split APK: " + split);
            }
        }
        
        File dexOutputDir = getDir("trispy_opt_dex", Context.MODE_PRIVATE);
        File libDir = new File(getFilesDir(), "TrispyVM/Libs/" + targetPackageName);
        
        targetClassLoader = new DexClassLoader(
                dexPathBuilder.toString(),
                dexOutputDir.getAbsolutePath(),
                libDir.getAbsolutePath(),
                getClassLoader()
        );

        ComponentName component = new ComponentName(targetPackageName, targetActivityClassName);
        targetActivityInfo = getPackageManager().getActivityInfo(component, 0);
    }

    // =========================================================================================
    // SECTION 10: THE REFLECTION HOOK (ActivityThread Hijack)
    // =========================================================================================

    private void launchVirtualActivity() throws Exception {
        log("Initiating ActivityThread Reflection Hook...");

        Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
        Method currentActivityThreadMethod = activityThreadClass.getDeclaredMethod("currentActivityThread");
        currentActivityThreadMethod.setAccessible(true);
        Object currentActivityThread = currentActivityThreadMethod.invoke(null);

        Method getInstrumentationMethod = activityThreadClass.getDeclaredMethod("getInstrumentation");
        getInstrumentationMethod.setAccessible(true);
        Instrumentation realInstrumentation = (Instrumentation) getInstrumentationMethod.invoke(currentActivityThread);

        log("Loading Game Class: " + targetActivityClassName);
        Class<?> mainActivityClass = targetClassLoader.loadClass(targetActivityClassName);
        Activity targetActivity = (Activity) mainActivityClass.newInstance();

        Intent targetIntent = new Intent();
        targetIntent.setComponent(new ComponentName(targetPackageName, targetActivityClassName));

        Method attachMethod = null;
        for (Method m : Activity.class.getDeclaredMethods()) {
            if (m.getName().equals("attach")) {
                attachMethod = m;
                break;
            }
        }

        if (attachMethod != null) {
            attachMethod.setAccessible(true);
            int paramCount = attachMethod.getParameterCount();
            Object[] args = new Object[paramCount];
            
            args[0] = sandboxContext;
            args[1] = currentActivityThread;
            args[2] = realInstrumentation;
            args[3] = new Binder();
            args[4] = 0;
            args[5] = getApplication();
            args[6] = targetIntent;
            args[7] = targetActivityInfo;
            args[8] = "";
            args[9] = null;
            args[10] = "";
            args[11] = null;
            args[12] = null;
            
            for (int i = 13; i < paramCount; i++) {
                args[i] = null;
            }

            log("Invoking attach()...");
            attachMethod.invoke(targetActivity, args);
        }

        log("Invoking onCreate()...");
        realInstrumentation.callActivityOnCreate(targetActivity, null);

        log("Setting Content View (Rendering Game)...");
        setContentView(targetActivity.getWindow().getDecorView());
        log("GAME ENGINE RENDERED!");
    }

    // =========================================================================================
    // SECTION 11: FLOATING PANEL
    // =========================================================================================

    private void showFloatingPanel() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
                logError("No overlay permission. Floating menu disabled.");
                return;
            }

            windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
            int type = Build.VERSION.SDK_INT >= 26
                    ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                    : WindowManager.LayoutParams.TYPE_PHONE;

            TextView logo = new TextView(this);
            logo.setText("T");
            logo.setTextColor(Color.WHITE);
            logo.setTextSize(24);
            logo.setGravity(Gravity.CENTER);
            logo.setBackgroundColor(Color.parseColor("#FF2200"));

            final WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                    120, 120, type,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
            lp.gravity = Gravity.TOP | Gravity.LEFT;
            lp.x = 50;
            lp.y = 300;

            logo.setOnClickListener(v -> Toast.makeText(this, "Panel Clicked!", Toast.LENGTH_SHORT).show());

            windowManager.addView(logo, lp);
            floatingPanelView = logo;
            log("Floating Menu Active.");
        } catch (Throwable t) {
            logError("Overlay failed: " + t.getMessage());
        }
    }

    private void destroyFloatingPanel() {
        try {
            if (windowManager != null && floatingPanelView != null) {
                windowManager.removeView(floatingPanelView);
            }
        } catch (Throwable t) {}
    }

    // =========================================================================================
    // SECTION 12: HARDWARE SPOOFER
    // =========================================================================================

    public static class HardwareSpoofer {
        public static void spoofHardware() {
            try {
                setBuildField("MODEL", "SM-S928B");
                setBuildField("BRAND", "samsung");
                setBuildField("MANUFACTURER", "samsung");
                setBuildField("DEVICE", "e3q");
                setBuildField("PRODUCT", "e3qxeea");
                setBuildField("FINGERPRINT", "samsung/e3qxeea/e3q:14/UP1A.231005.007/S928BXXU1AWLM:user/release-keys");
                setBuildField("HARDWARE", "qcom");
                setBuildField("HOST", "21DJF80MZA");
            } catch (Exception e) {
                Log.e(TAG, "Hardware spoof failed", e);
            }
        }

        private static void setBuildField(String fieldName, String value) {
            try {
                Field field = Build.class.getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(null, value);
                Field field2 = Build.VERSION.class.getDeclaredField(fieldName);
                field2.setAccessible(true);
                field2.set(null, value);
            } catch (Exception e) {
                // Ignore
            }
        }
    }

    // =========================================================================================
    // SECTION 13: SANDBOX CONTEXT (GMS & VFS SPOOFER)
    // =========================================================================================

    public static class SandboxContext extends ContextWrapper {
        private final String targetPkg;
        private Resources targetRes;

        public SandboxContext(Context base, String targetPkg) {
            super(base);
            this.targetPkg = targetPkg;
            try {
                AssetManager assets = AssetManager.class.newInstance();
                Method addAssetPath = AssetManager.class.getMethod("addAssetPath", String.class);
                String apkPath = base.getPackageManager().getApplicationInfo(targetPkg, 0).sourceDir;
                addAssetPath.invoke(assets, apkPath);
                Resources hostRes = base.getResources();
                targetRes = new Resources(assets, hostRes.getDisplayMetrics(), hostRes.getConfiguration());
            } catch (Exception e) {
                Log.e(TAG, "SandboxContext Res load failed", e);
            }
        }

        @Override
        public String getPackageName() {
            return targetPkg;
        }

        @Override
        public Resources getResources() {
            return targetRes != null ? targetRes : super.getResources();
        }

        @Override
        public File getObbDir() {
            File obb = new File(getBaseContext().getFilesDir(), "TrispyVM_Obb/" + targetPkg);
            if (!obb.exists()) obb.mkdirs();
            return obb;
        }

        @Override
        public File getFilesDir() {
            File files = new File(getBaseContext().getFilesDir(), "TrispyVM_Data/" + targetPkg);
            if (!files.exists()) files.mkdirs();
            return files;
        }

        @Override
        public PackageManager getPackageManager() {
            PackageManager basePm = getBaseContext().getPackageManager();
            return (PackageManager) Proxy.newProxyInstance(
                PackageManager.class.getClassLoader(),
                new Class<?>[]{PackageManager.class},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        String methodName = method.getName();
                        
                        // Fix: Provide a valid Launch Intent if FF asks for it
                        if (methodName.equals("getLaunchIntentForPackage") && args != null && args.length > 0) {
                            String pkg = (String) args[0];
                            if (pkg != null && pkg.equals(targetPkg)) {
                                Intent intent = new Intent();
                                intent.setComponent(new ComponentName(targetPkg, "com.dts.freefireth.FFMainActivity"));
                                return intent;
                            }
                        }

                        if (methodName.equals("getPackageInfo") && args != null && args.length > 0) {
                            String pkg = (String) args[0];
                            if (pkg != null && pkg.contains("com.google.android.gms")) {
                                PackageInfo fakeGms = new PackageInfo();
                                fakeGms.packageName = pkg;
                                fakeGms.versionName = "22.0.0";
                                return fakeGms;
                            }
                        }
                        
                        if (methodName.equals("checkSignatures")) {
                            return PackageManager.SIGNATURE_MATCH;
                        }
                        
                        try {
                            return method.invoke(basePm, args);
                        } catch (InvocationTargetException e) {
                            throw e.getCause();
                        }
                    }
                }
            );
        }
    }
}
