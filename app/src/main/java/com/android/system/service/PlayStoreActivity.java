package com.android.system.service;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class PlayStoreActivity extends Activity {
    private static final int PICK_APK_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.parseColor("#121212"));
        root.setPadding(40, 60, 40, 40);
        root.setGravity(Gravity.CENTER_HORIZONTAL);

        TextView title = new TextView(this);
        title.setText("VIRTUAL PLAY STORE");
        title.setTextColor(Color.parseColor("#00FF00"));
        title.setTextSize(24);
        root.addView(title);

        TextView desc = new TextView(this);
        desc.setText("Install APKs directly into the Virtual Machine environment. ACE cannot detect this storage.");
        desc.setTextColor(Color.GRAY);
        desc.setGravity(Gravity.CENTER);
        desc.setPadding(0, 20, 0, 40);
        root.addView(desc);

        Button installBtn = new Button(this);
        installBtn.setText("SELECT APK TO INSTALL");
        installBtn.setBackgroundColor(Color.parseColor("#00FF00"));
        installBtn.setTextColor(Color.BLACK);
        installBtn.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 150));
        installBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("application/vnd.android.package-archive");
            startActivityForResult(intent, PICK_APK_CODE);
        });
        root.addView(installBtn);

        setContentView(root);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_APK_CODE && resultCode == RESULT_OK && data != null) {
            Uri apkUri = data.getData();
            if (apkUri != null) {
                Toast.makeText(this, "Installing into VM... Please wait.", Toast.LENGTH_LONG).show();
                VMEngine.installApkFromUri(this, apkUri);
            }
        }
    }
}
