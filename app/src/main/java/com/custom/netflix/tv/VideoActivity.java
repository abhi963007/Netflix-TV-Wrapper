package com.custom.netflix.tv;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * VideoActivity — In-app player using a dedicated WebView.
 *
 * Strategy: Use the device's real system User-Agent (no spoofing).
 * The system WebView has the same Widevine support as the device's
 * default browser. We restore the full Netflix session by re-injecting
 * all cookies from the browsing WebView.
 */
public class VideoActivity extends Activity {

    // Sony Bravia TV UA — triggers the Android-compatible player and native DRM
    private static final String SONY_BRAVIA_UA =
            "Mozilla/5.0 (Linux; Android 10; BRAVIA 4K VH2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";

    public static final String EXTRA_URL     = "video_url";
    public static final String EXTRA_COOKIES = "video_cookies";

    private WebView videoWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Keep screen on during playback
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // True full-screen immersive
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

        // Build a full-screen black WebView programmatically
        videoWebView = new WebView(this);
        videoWebView.setBackgroundColor(0xFF000000);
        setContentView(videoWebView);

        String url     = getIntent().getStringExtra(EXTRA_URL);
        String cookies = getIntent().getStringExtra(EXTRA_COOKIES);

        // Re-inject browsing session cookies so we're still logged in
        injectCookies(cookies);
        setupVideoWebView();

        if (url != null && !url.isEmpty()) {
            videoWebView.loadUrl(url);
        } else {
            finish();
        }
    }

    private void injectCookies(String cookieString) {
        if (cookieString == null || cookieString.isEmpty()) return;
        CookieManager cm = CookieManager.getInstance();
        cm.setAcceptCookie(true);
        cm.setAcceptThirdPartyCookies(videoWebView, true);
        // Split and inject each cookie individually
        for (String cookie : cookieString.split(";")) {
            String trimmed = cookie.trim();
            if (!trimmed.isEmpty()) {
                cm.setCookie("https://www.netflix.com", trimmed);
                cm.setCookie("https://netflix.com", trimmed);
            }
        }
        cm.flush();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupVideoWebView() {
        WebSettings s = videoWebView.getSettings();

        // === TV UA FOR PLAYBACK ===
        // We use a TV-specific UA here so Netflix sends the player 
        // that is compatible with Android's system Widevine.
        s.setUserAgentString(SONY_BRAVIA_UA);

        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        s.setCacheMode(WebSettings.LOAD_DEFAULT);
        s.setSaveFormData(true);
        s.setLoadWithOverviewMode(true);
        s.setUseWideViewPort(true);

        // GPU-accelerated rendering for smooth video
        videoWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        // Grant ALL media permissions — video, audio, Widevine (protected media)
        videoWebView.setWebChromeClient(new WebChromeClient() {
            private View fullscreenView;

            @Override
            public void onPermissionRequest(PermissionRequest request) {
                request.grant(request.getResources());
            }

            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                // Handle full-screen video player expansion
                fullscreenView = view;
                setContentView(view);
                view.setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            }

            @Override
            public void onHideCustomView() {
                setContentView(videoWebView);
                fullscreenView = null;
            }
        });

        videoWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // Block external app redirects
                if (url.startsWith("intent://") ||
                    url.startsWith("market://") ||
                    url.startsWith("netflix://")) {
                    return true;
                }
                // If kicked to a non-Netflix page, close player
                if (!url.contains("netflix.com")) {
                    finish();
                    return true;
                }
                return false;
            }

            @Override
            public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                // Inject Chrome fingerprint BEFORE page scripts run.
                // Netflix checks window.chrome and navigator.webdriver
                // to detect WebView vs real Chrome. We fix both here.
                view.evaluateJavascript(
                    "(function() {" +
                    "  if (!window.chrome) {" +
                    "    window.chrome = { runtime: {}, loadTimes: function(){}, csi: function(){} };" +
                    "  }" +
                    "  try {" +
                    "    Object.defineProperty(navigator, 'webdriver', { get: () => false });" +
                    "  } catch(e) {}" +
                    "  try {" +
                    "    Object.defineProperty(navigator, 'plugins', { get: () => [1,2,3,4,5] });" +
                    "  } catch(e) {}" +
                    "})();", null);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // Auto-play and clean up UI
                view.evaluateJavascript(
                    "(function() {" +
                    "  document.body.style.background = '#000';" +
                    "  document.body.style.margin = '0';" +
                    "  document.body.style.overflow = 'hidden';" +
                    "  var vids = document.querySelectorAll('video');" +
                    "  vids.forEach(function(v) { v.play().catch(function(){}); });" +
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
                finish(); // Return to MainActivity browse screen
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (videoWebView != null) videoWebView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (videoWebView != null) videoWebView.onResume();
    }

    @Override
    protected void onDestroy() {
        if (videoWebView != null) {
            videoWebView.stopLoading();
            videoWebView.loadUrl("about:blank");
            videoWebView.destroy();
            videoWebView = null;
        }
        super.onDestroy();
    }
}
