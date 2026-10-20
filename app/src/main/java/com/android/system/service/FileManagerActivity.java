package com.android.system.service;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class FileManagerActivity extends Activity {
    private TextView logView;
    private SandboxContext sandboxContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // We use Free Fire's package as default for the File Manager
        sandboxContext = new SandboxContext(this, "com.dts.freefiremax");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 40, 40, 40);
        layout.setBackgroundColor(Color.BLACK);

        TextView title = new TextView(this);
        title.setText("VM File Manager");
        title.setTextColor(Color.RED);
        title.setTextSize(24);
        layout.addView(title);

        logView = new TextView(this);
        logView.setTextColor(Color.GREEN);
        logView.setTextSize(14);
        logView.setPadding(0, 20, 0, 20);
        layout.addView(logView);

        Button btnImportObb = new Button(this);
        btnImportObb.setText("Import OBB File (FF Max)");
        btnImportObb.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            startActivityForResult(intent, 2001);
        });
        layout.addView(btnImportObb);

        Button btnRefresh = new Button(this);
        btnRefresh.setText("Refresh File List");
        btnRefresh.setOnClickListener(v -> listFiles());
        layout.addView(btnRefresh);

        setContentView(layout);
        listFiles();
    }

    private void listFiles() {
        logView.setText("--- VIRTUAL OBB FOLDER ---\n");
        File obbDir = sandboxContext.getObbDir();
        logView.append("Path: " + obbDir.getAbsolutePath() + "\n\n");

        if (obbDir.exists()) {
            File[] files = obbDir.listFiles();
            if (files != null && files.length > 0) {
                for (File f : files) {
                    logView.append("[FILE] " + f.getName() + " (" + (f.length() / 1024 / 1024) + " MB)\n");
                }
            } else {
                logView.append("(Empty. FF Max will crash without OBB)\n");
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2001 && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            try {
                File dest = new File(sandboxContext.getObbDir(), "main.2018100001.com.dts.freefiremax.obb");
                InputStream is = getContentResolver().openInputStream(uri);
                FileOutputStream fos = new FileOutputStream(dest);
                byte[] buffer = new byte[8192];
                int len;
                while ((len = is.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
                is.close();
                Toast.makeText(this, "OBB Imported Successfully!", Toast.LENGTH_SHORT).show();
                listFiles();
            } catch (Exception e) {
                Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }
}
