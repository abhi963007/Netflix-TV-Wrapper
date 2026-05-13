package com.custom.netflix.tv;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class VideoActivity extends Activity {

    private WebView videoWebView;

    public static final String EXTRA_URL     = "video_url";
    public static final String EXTRA_COOKIES = "video_cookies";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // True full-screen — no status bar, no navigation bar
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

        // Full-screen WebView — no layout file needed
        videoWebView = new WebView(this);
        videoWebView.setLayoutParams(new android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT));
        videoWebView.setBackgroundColor(0xFF000000);
        setContentView(videoWebView);

        String url     = getIntent().getStringExtra(EXTRA_URL);
        String cookies = getIntent().getStringExtra(EXTRA_COOKIES);

        // Restore cookies from the browsing session so we stay logged in
        if (cookies != null && !cookies.isEmpty()) {
            CookieManager cm = CookieManager.getInstance();
            cm.setAcceptCookie(true);
            cm.setAcceptThirdPartyCookies(videoWebView, true);
            // Inject each individual cookie
            for (String cookie : cookies.split(";")) {
                cm.setCookie("https://www.netflix.com", cookie.trim());
            }
            cm.flush();
        }

        setupVideoWebView();

        if (url != null) {
            videoWebView.loadUrl(url);
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupVideoWebView() {
        WebSettings settings = videoWebView.getSettings();

        // === USE THE DEVICE'S OWN NATIVE UA ===
        // This is the key: we do NOT spoof anything here.
        // The device's real WebView UA tells Netflix exactly what
        // Widevine level this hardware supports, so DRM works natively.
        // We only append a hint that we accept HD video.
        String nativeUA = settings.getUserAgentString();
        // Keep native UA — don't override it

        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);

        // Critical for DRM: allow saved passwords / form data
        settings.setSaveFormData(true);

        // Hardware acceleration for smooth video
        videoWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        // Grant ALL media permissions automatically
        videoWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                // Grant all — video, audio, protected media (Widevine)
                request.grant(request.getResources());
            }

            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                // Handle full-screen video playback
                setContentView(view);
            }

            @Override
            public void onHideCustomView() {
                setContentView(videoWebView);
            }
        });

        videoWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // Stay within Netflix — block external redirects
                if (url.startsWith("intent://") ||
                    url.startsWith("market://") ||
                    url.startsWith("netflix://")) {
                    return true;
                }
                // If redirected to login, go back to MainActivity
                if (!url.contains("netflix.com")) {
                    finish();
                    return true;
                }
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // Inject minimal JS to auto-start and clean up the video UI
                view.evaluateJavascript(
                    "(function() {" +
                    "  document.body.style.background='#000';" +
                    "  var videos = document.querySelectorAll('video');" +
                    "  videos.forEach(function(v) { v.play(); });" +
                    "})();", null);
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (videoWebView.canGoBack()) {
                videoWebView.goBack();
            } else {
                finish(); // Return to MainActivity
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onPause() {
        super.onPause();
        videoWebView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        videoWebView.onResume();
    }

    @Override
    protected void onDestroy() {
        if (videoWebView != null) {
            videoWebView.stopLoading();
            videoWebView.destroy();
        }
        super.onDestroy();
    }
}
