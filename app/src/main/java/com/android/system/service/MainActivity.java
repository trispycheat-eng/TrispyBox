package com.android.system.service;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.trispy.engine.VirtualCore;
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
