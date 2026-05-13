package com.custom.netflix.tv;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final int OVERLAY_PERMISSION_REQ_CODE = 1234;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkPermissionAndLaunch();
    }

    private void checkPermissionAndLaunch() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                if (!Settings.canDrawOverlays(this)) {
                    // We need permission to draw the invisible orientation ghost window
                    Toast.makeText(this, "Please grant 'Display over other apps' to force Landscape Mode", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + getPackageName()));
                    startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE);
                    return;
                }
            } catch (Exception e) {
                // Some TVs don't support the overlay settings page; skip and try to launch anyway
                android.util.Log.e("GhostLauncher", "Overlay settings not found: " + e.getMessage());
            }
        }
        
        // Permission is granted or settings page is missing. Start the ghost orientation locker!
        startGhostLauncher();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == OVERLAY_PERMISSION_REQ_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(this)) {
                    startGhostLauncher();
                } else {
                    Toast.makeText(this, "Permission denied. Cannot force Landscape.", Toast.LENGTH_SHORT).show();
                    finish(); // Kill our app if permission denied
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void startGhostLauncher() {
        // 1. Start the invisible Horizontal Overlay Service
        Intent serviceIntent = new Intent(this, OrientationService.class);
        startService(serviceIntent);

        // 2. Launch the official Netflix Mobile app
        try {
            Intent netflixIntent = getPackageManager().getLaunchIntentForPackage("com.netflix.mediaclient");
            if (netflixIntent != null) {
                netflixIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(netflixIntent);
            } else {
                Toast.makeText(this, "Official Netflix App is not installed!", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 3. Immediately close our wrapper app so the user never sees it again
        finish();
    }
}
