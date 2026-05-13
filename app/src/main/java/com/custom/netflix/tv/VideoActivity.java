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
    
    // Official Android TV User-Agent — known to work perfectly with Netflix DRM
    private static final String TV_PLAYER_UA = 
            "Mozilla/5.0 (Linux; Adroid 9; Digital Media Player) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.225 Safari/537.36";

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
        videoWebView.setBackgroundColor(0xFF000000); // Black background
        setContentView(videoWebView);

        String url = getIntent().getStringExtra(EXTRA_URL);
        String cookies = getIntent().getStringExtra(EXTRA_COOKIES);

        // 1. Setup Cookies BEFORE loading URL
        setupCookies(cookies);
        
        // 2. Setup WebView Settings
        setupWebViewSettings();

        // 3. Load the movie
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
        
        // Inject each cookie for both the main domain and the streaming domain
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
        
        // Use the TV Identity for playback
        s.setUserAgentString(TV_PLAYER_UA);

        // Force hardware acceleration for 4K/HD video
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
                // Force autoplay once loaded
                view.evaluateJavascript(
                    "(function() {" +
                    "  var v = document.querySelector('video');" +
                    "  if(v) { v.play(); v.style.width='100%'; v.style.height='100%'; }" +
                    "  document.body.style.backgroundColor = 'black';" +
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
