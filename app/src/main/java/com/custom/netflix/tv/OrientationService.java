package com.custom.netflix.tv;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.view.View;
import android.view.WindowManager;

public class OrientationService extends Service {
    private WindowManager windowManager;
    private View invisibleView;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        
        windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

        invisibleView = new View(this);

        int layoutType;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutType = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        }

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                0, 0, // 0 width/height so it's completely invisible
                layoutType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | 
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | 
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                PixelFormat.TRANSPARENT);

        // This is the magic line that forces the entire system into Landscape mode
        params.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;

        try {
            windowManager.addView(invisibleView, params);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (invisibleView != null && windowManager != null) {
            try {
                windowManager.removeView(invisibleView);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
