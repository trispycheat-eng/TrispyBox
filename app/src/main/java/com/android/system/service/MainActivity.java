package com.android.system.service;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.trispy.engine.VirtualCore;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private AppListAdapter adapter;
    private List<PackageInfo> appList = new ArrayList<>();
    private String pendingPackage = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerView = findViewById(R.id.recycler_apps);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        loadInstalledApps();

        adapter = new AppListAdapter(this, appList, pkg -> {
            // 1. Ask for Overlay Permission
            if (Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(this)) {
                pendingPackage = pkg.packageName;
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, 1002);
                return;
            }
            
            // 2. Ask for All Files Access (For OBB)
            if (Build.VERSION.SDK_INT >= 30 && !Environment.isExternalStorageManager()) {
                pendingPackage = pkg.packageName;
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, 1003);
                return;
            }

            // 3. Launch
            launchTarget(pkg.packageName);
        });
        recyclerView.setAdapter(adapter);

        Button settingsBtn = findViewById(R.id.btn_settings);
        settingsBtn.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));

        Button filesBtn = findViewById(R.id.btn_files);
        filesBtn.setOnClickListener(v -> startActivity(new Intent(this, FileManagerActivity.class)));

        FloatingActionButton fabAdd = findViewById(R.id.fab_add);
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            startActivityForResult(intent, 1001);
        });
    }

    private void launchTarget(String pkg) {
        Toast.makeText(this, "Injecting " + pkg + "...", Toast.LENGTH_SHORT).show();
        VirtualCore.get().setTargetPackage(pkg);
        VirtualCore.get().launchApp();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        // If Overlay was granted
        if (requestCode == 1002) {
            if (Build.VERSION.SDK_INT >= 23 && Settings.canDrawOverlays(this)) {
                if (!pendingPackage.isEmpty()) {
                    // Now check for OBB permission
                    if (Build.VERSION.SDK_INT >= 30 && !Environment.isExternalStorageManager()) {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, Uri.parse("package:" + getPackageName()));
                        startActivityForResult(intent, 1003);
                    } else {
                        launchTarget(pendingPackage);
                    }
                }
            } else {
                Toast.makeText(this, "Overlay permission is required!", Toast.LENGTH_LONG).show();
            }
        }

        // If OBB/All Files Access was granted
        if (requestCode == 1003) {
            if (Build.VERSION.SDK_INT >= 30 && Environment.isExternalStorageManager()) {
                if (!pendingPackage.isEmpty()) launchTarget(pendingPackage);
            } else {
                Toast.makeText(this, "All Files Access is required for OBB!", Toast.LENGTH_LONG).show();
            }
        }
        
        // File Picker Result
        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            Uri fileUri = data.getData();
            if (fileUri != null) {
                try {
                    File vmDir = new File(getFilesDir(), "TrispyVM/Apps");
                    if (!vmDir.exists()) vmDir.mkdirs();
                    File destFile = new File(vmDir, "imported_app.apk");
                    InputStream is = getContentResolver().openInputStream(fileUri);
                    FileOutputStream fos = new FileOutputStream(destFile);
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = is.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                    fos.close();
                    is.close();
                    Toast.makeText(this, "File added to VM!", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void loadInstalledApps() {
        List<PackageInfo> packages = getPackageManager().getInstalledPackages(0);
        for (PackageInfo pkg : packages) {
            if ((pkg.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                appList.add(pkg);
            }
        }
    }
}
