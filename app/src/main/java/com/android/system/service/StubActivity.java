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
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Process;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import dalvik.system.DexClassLoader;

/**
 * =========================================================================================
 * TRISPY VIRTUAL SPACE ENGINE - ULTIMATE APP STORE MONOLITH (COMPILE-SAFE)
 * =========================================================================================
 */
public class StubActivity extends Activity {

    private static final String TAG = "TrispyStoreEngine";
    private static final String DEFAULT_TARGET_PACKAGE = "com.dts.freefiremax";
    private static final String FALLBACK_FF_ACTIVITY = "com.dts.freefireth.FFMainActivity";
    
    private String targetPackageName = DEFAULT_TARGET_PACKAGE;
    private String targetActivityClassName = "";
    
    private ApplicationInfo targetAppInfo;
    private ActivityInfo targetActivityInfo;
    private DexClassLoader targetClassLoader;
    private SandboxContext sandboxContext;
    
    // Unchanged: We use Object to bypass the compiler restriction on sun.misc.Unsafe
    private Object unsafeInstance;
    private Class<?> unsafeClass;
    
    private LinearLayout rootMainLayout;
    private LinearLayout storeContainer;
    private TextView logTextView;
    private ScrollView logScrollView;
    private StringBuilder logStringBuilder = new StringBuilder();
    
    private WindowManager windowManager;
    private View floatingPanelView;
    
    private boolean isEngineInitialized = false;
    private boolean isActivityLaunched = false;
    private boolean isUnsafeInitialized = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        buildMainDashboardUI();
        log("=========================================");
        log("INITIALIZING TRISPY APP STORE ENGINE");
        log("=========================================");
        
        installGlobalCrashHandler();
        initUnsafe();
        HardwareSpoofer.spoofHardware();
        captureIntentData();
        
        new Thread(() -> {
            try {
                executeEngineSequence();
            } catch (Throwable t) {
                logFatalError("ENGINE FATAL CRASH", t);
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        destroyFloatingPanel();
        log("Engine Destroyed. Cleaning up VFS...");
    }

    private void buildMainDashboardUI() {
        try {
            rootMainLayout = new LinearLayout(this);
            rootMainLayout.setOrientation(LinearLayout.VERTICAL);
            rootMainLayout.setBackgroundColor(Color.parseColor("#121212"));
            rootMainLayout.setPadding(30, 30, 30, 30);

            LinearLayout topBar = new LinearLayout(this);
            topBar.setOrientation(LinearLayout.HORIZONTAL);
            topBar.setGravity(Gravity.CENTER_VERTICAL);
            topBar.setPadding(0, 0, 0, 20);

            TextView title = new TextView(this);
            title.setText("TRISPY STORE");
            title.setTextColor(Color.parseColor("#FF2200"));
            title.setTextSize(26);
            title.setTypeface(null, Typeface.BOLD);
            topBar.addView(title);

            rootMainLayout.addView(topBar);

            storeContainer = new LinearLayout(this);
            storeContainer.setOrientation(LinearLayout.VERTICAL);
            storeContainer.setBackgroundColor(Color.parseColor("#1E1E1E"));
            storeContainer.setPadding(20, 20, 20, 20);

            LinearLayout ffItem = new LinearLayout(this);
            ffItem.setOrientation(LinearLayout.HORIZONTAL);
            ffItem.setGravity(Gravity.CENTER_VERTICAL);
            ffItem.setPadding(0, 10, 0, 10);

            TextView ffName = new TextView(this);
            ffName.setText("Free Fire MAX");
            ffName.setTextColor(Color.WHITE);
            ffName.setTextSize(18);
            ffName.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f));
            ffItem.addView(ffName);

            Button playBtn = new Button(this);
            playBtn.setText("Install/Play");
            playBtn.setBackgroundColor(Color.parseColor("#FF2200"));
            playBtn.setTextColor(Color.WHITE);
            playBtn.setOnClickListener(v -> {
                if (isEngineInitialized) {
                    try {
                        launchVirtualActivity();
                    } catch (Throwable t) {
                        logFatalError("MANUAL LAUNCH FAILED", t);
                    }
                } else {
                    Toast.makeText(this, "Engine initializing...", Toast.LENGTH_SHORT).show();
                }
            });
            ffItem.addView(playBtn);
            storeContainer.addView(ffItem);

            LinearLayout psItem = new LinearLayout(this);
            psItem.setOrientation(LinearLayout.HORIZONTAL);
            psItem.setGravity(Gravity.CENTER_VERTICAL);
            psItem.setPadding(0, 10, 0, 10);

            TextView psName = new TextView(this);
            psName.setText("Google Play Store");
            psName.setTextColor(Color.WHITE);
            psName.setTextSize(18);
            psName.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f));
            psItem.addView(psName);

            Button openPsBtn = new Button(this);
            openPsBtn.setText("Open Store");
            openPsBtn.setBackgroundColor(Color.parseColor("#333333"));
            openPsBtn.setTextColor(Color.WHITE);
            openPsBtn.setOnClickListener(v -> openPlayStore());
            psItem.addView(openPsBtn);
            storeContainer.addView(psItem);

            rootMainLayout.addView(storeContainer);

            View spacer = new View(this);
            spacer.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 40));
            rootMainLayout.addView(spacer);

            TextView logTitle = new TextView(this);
            logTitle.setText("VM LOGS:");
            logTitle.setTextColor(Color.GRAY);
            logTitle.setTextSize(12);
            rootMainLayout.addView(logTitle);

            logScrollView = new ScrollView(this);
            logScrollView.setBackgroundColor(Color.BLACK);
            logScrollView.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    0,
                    1.0f
            ));

            LinearLayout logContainer = new LinearLayout(this);
            logContainer.setOrientation(LinearLayout.VERTICAL);
            logContainer.setPadding(20, 20, 20, 20);

            logTextView = new TextView(this);
            logTextView.setTextColor(Color.GREEN);
            logTextView.setTextSize(11);
            logTextView.setTypeface(Typeface.MONOSPACE);

            logContainer.addView(logTextView);
            logScrollView.addView(logContainer);
            rootMainLayout.addView(logScrollView);

            setContentView(rootMainLayout);
        } catch (Throwable t) {
            Log.e(TAG, "Failed to build Dashboard UI", t);
        }
    }

    private void openPlayStore() {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("market://details?id=" + targetPackageName));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            log("Opened Play Store for " + targetPackageName);
        } catch (Exception e) {
            logError("Play Store app not found. Opening browser instead.");
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=" + targetPackageName));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } catch (Exception ex) {
                logError("Failed to open browser: " + ex.getMessage());
            }
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
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        Log.e(TAG, sw.toString());

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

    private void installGlobalCrashHandler() {
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            logFatalError("UNCAUGHT EXCEPTION", throwable);
            try {
                File crashFile = new File(getFilesDir(), "trispy_store_crash_log.txt");
                FileOutputStream fos = new FileOutputStream(crashFile, true);
                PrintWriter pw = new PrintWriter(fos);
                pw.println("\n--- CRASH AT " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date()) + " ---");
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

    private void initUnsafe() {
        try {
            unsafeClass = Class.forName("sun.misc.Unsafe");
            Field theUnsafe = unsafeClass.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            unsafeInstance = theUnsafe.get(null);
            isUnsafeInitialized = true;
            log("Unsafe memory engine initialized.");
        } catch (Exception e) {
            isUnsafeInitialized = false;
            logError("Failed to init Unsafe: " + e.getMessage());
        }
    }

    private void executeEngineSequence() throws Exception {
        if (isEngineInitialized) return;
        
        log("Beginning Engine Sequence...");
        resolveApplicationInfo();
        setupVirtualFileSystem();
        extractNativeLibraries();
        resolveTargetActivity();
        setupEnvironment();
        launchVirtualActivity();
        showFloatingPanel();
        
        isEngineInitialized = true;
        log("=========================================");
        log("ENGINE INITIALIZATION COMPLETE");
        log("=========================================");
    }

    private void resolveApplicationInfo() throws PackageManager.NameNotFoundException {
        log("Fetching ApplicationInfo...");
        targetAppInfo = getPackageManager().getApplicationInfo(targetPackageName, 0);
        if (targetAppInfo == null) throw new RuntimeException("ApplicationInfo is null. App not installed?");
        log("APK: " + targetAppInfo.sourceDir);
        log("Native Lib Dir: " + targetAppInfo.nativeLibraryDir);
    }

    private void setupVirtualFileSystem() {
        log("Setting up Virtual File System (VFS)...");
        File virtualRootDir = new File(getFilesDir(), "TrispyVM");
        File virtualDataDir = new File(virtualRootDir, "Data/" + targetPackageName);
        File virtualObbDir = new File(virtualRootDir, "Obb/" + targetPackageName);
        
        if (!virtualRootDir.exists()) virtualRootDir.mkdirs();
        if (!virtualDataDir.exists()) virtualDataDir.mkdirs();
        if (!virtualObbDir.exists()) virtualObbDir.mkdirs();
        
        log("VFS Data Dir: " + virtualDataDir.getAbsolutePath());
        log("VFS Obb Dir: " + virtualObbDir.getAbsolutePath());
    }

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

    private void launchVirtualActivity() throws Exception {
        if (isActivityLaunched) return;
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
        
        Activity targetActivity;
        
        // Use Unsafe via Reflection if available, otherwise fallback to newInstance
        if (isUnsafeInitialized) {
            Method allocateInstanceMethod = unsafeClass.getMethod("allocateInstance", Class.class);
            targetActivity = (Activity) allocateInstanceMethod.invoke(unsafeInstance, mainActivityClass);
        } else {
            targetActivity = (Activity) mainActivityClass.newInstance();
        }

        Intent targetIntent = new Intent(Intent.ACTION_MAIN);
        targetIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        ComponentName componentName = new ComponentName(targetPackageName, targetActivityClassName);
        targetIntent.setComponent(componentName);
        targetIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        if (isUnsafeInitialized) {
            try {
                log("Injecting Unity metadata via Unsafe...");
                injectFieldUnsafe(targetActivity, "mIntent", targetIntent);
                injectFieldUnsafe(targetActivity, "mComponent", componentName);
                injectFieldUnsafe(targetActivity, "mCallingActivity", componentName);
                injectFieldUnsafe(targetActivity, "mCallingPackage", targetPackageName);
            } catch (Exception e) {
                log("Unsafe injection partially failed.");
            }
        }

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
        
        isActivityLaunched = true;
        log("GAME ENGINE RENDERED!");
    }

    private void injectFieldUnsafe(Object obj, String fieldName, Object value) {
        try {
            Class<?> clazz = obj.getClass();
            Field field = null;
            while (clazz != null) {
                try {
                    field = clazz.getDeclaredField(fieldName);
                    break;
                } catch (NoSuchFieldException e) {
                    clazz = clazz.getSuperclass();
                }
            }
            
            if (field != null && unsafeInstance != null) {
                Method objectFieldOffsetMethod = unsafeClass.getMethod("objectFieldOffset", Field.class);
                long offset = (Long) objectFieldOffsetMethod.invoke(unsafeInstance, field);
                
                Method putObjectMethod = unsafeClass.getMethod("putObject", Object.class, long.class, Object.class);
                putObjectMethod.invoke(unsafeInstance, obj, offset, value);
            }
        } catch (Exception e) {
            // Ignore
        }
    }

    private void showFloatingPanel() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
                logError("No overlay permission.");
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

    public static class HardwareSpoofer {
        public static void spoofHardware() {
            try {
                setBuildField("MODEL", "SM-S928B");
                setBuildField("BRAND", "samsung");
                setBuildField("MANUFACTURER", "samsung");
                setBuildField("DEVICE", "e3q");
                setBuildField("FINGERPRINT", "samsung/e3qxeea/e3q:14/UP1A.231005.007/S928BXXU1AWLM:user/release-keys");
            } catch (Exception e) {}
        }

        private static void setBuildField(String fieldName, String value) {
            try {
                Field field = Build.class.getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(null, value);
                Field field2 = Build.VERSION.class.getDeclaredField(fieldName);
                field2.setAccessible(true);
                field2.set(null, value);
            } catch (Exception e) {}
        }
    }

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
            } catch (Exception e) {}
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
        public PackageManager getPackageManager() {
            PackageManager basePm = getBaseContext().getPackageManager();
            return (PackageManager) Proxy.newProxyInstance(
                PackageManager.class.getClassLoader(),
                new Class<?>[]{PackageManager.class},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        String methodName = method.getName();
                        
                        if (methodName.equals("getLaunchIntentForPackage") && args != null && args.length > 0) {
                            String pkg = (String) args[0];
                            if (pkg != null && pkg.equals(targetPkg)) {
                                Intent intent = new Intent(Intent.ACTION_MAIN);
                                intent.addCategory(Intent.CATEGORY_LAUNCHER);
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
