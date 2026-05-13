package com.custom.netflix.tv;

import android.annotation.SuppressLint;
import android.app.Activity;
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

public class VideoActivity extends Activity {

    public static final String EXTRA_URL     = "video_url";
    public static final String EXTRA_COOKIES = "video_cookies";

    private WebView videoWebView;
    
    // Windows 10 Chrome UA — Most stable Netflix Cadmium player, highly forgiving with DRM
    private static final String PLAYER_UA = 
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Keep screen alive
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Fullscreen setup
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

        videoWebView = new WebView(this);
        // We temporarily remove the forced black background so we can see errors
        // videoWebView.setBackgroundColor(0xFF000000); 
        setContentView(videoWebView);

        String url = getIntent().getStringExtra(EXTRA_URL);
        String cookies = getIntent().getStringExtra(EXTRA_COOKIES);

        setupCookies(cookies);
        setupWebViewSettings();

        if (url != null) {
            videoWebView.loadUrl(url);
        } else {
            finish();
        }
    }

    private void setupCookies(String cookieString) {
        if (cookieString == null) return;
        CookieManager cm = CookieManager.getInstance();
        cm.setAcceptCookie(true);
        cm.setAcceptThirdPartyCookies(videoWebView, true);
        
        String[] domains = {"https://www.netflix.com", "https://netflix.com", "https://www.nflxext.com"};
        for (String cookie : cookieString.split(";")) {
            for (String domain : domains) {
                cm.setCookie(domain, cookie.trim());
            }
        }
        cm.flush();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebViewSettings() {
        WebSettings s = videoWebView.getSettings();
        
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        
        s.setUserAgentString(PLAYER_UA);

        videoWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        videoWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(PermissionRequest request) {
                request.grant(request.getResources());
            }
        });

        videoWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                // Try to auto-play, but DO NOT hide the page background
                view.evaluateJavascript(
                    "(function() {" +
                    "  var v = document.querySelector('video');" +
                    "  if(v) { v.play(); }" +
                    "})();", null);
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish(); // Close player and go back to browse
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
