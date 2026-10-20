package com.android.system.service;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Bundle;
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
            Toast.makeText(this, "Injecting " + pkg.packageName + "...", Toast.LENGTH_SHORT).show();
            VirtualCore.get().setTargetPackage(pkg.packageName);
            VirtualCore.get().launchApp();
        });
        recyclerView.setAdapter(adapter);

        Button settingsBtn = findViewById(R.id.btn_settings);
        settingsBtn.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
        });

        // THE SIDE BUTTON LOGIC
        FloatingActionButton fabAdd = findViewById(R.id.fab_add);
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*"); // Let user pick APK or any file
            startActivityForResult(intent, 1001);
        });
    }

    private void loadInstalledApps() {
        List<PackageInfo> packages = getPackageManager().getInstalledPackages(0);
        for (PackageInfo pkg : packages) {
            if ((pkg.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                appList.add(pkg);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == Activity.RESULT_OK && data != null) {
            Uri fileUri = data.getData();
            if (fileUri != null) {
                try {
                    // Create a folder in the app's private storage to simulate the VM drive
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

                    Toast.makeText(this, "File added to VM successfully!", Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Toast.makeText(this, "Failed to import: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}
