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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Immersive Fullscreen
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

        videoWebView = new WebView(this);
        videoWebView.setBackgroundColor(0xFF000000);
        setContentView(videoWebView);

        String url = getIntent().getStringExtra(EXTRA_URL);
        String cookies = getIntent().getStringExtra(EXTRA_COOKIES);

        injectCookies(cookies);
        setupVideoWebView();

        if (url != null) videoWebView.loadUrl(url);
        else finish();
    }

    private void injectCookies(String cookieString) {
        if (cookieString == null) return;
        CookieManager cm = CookieManager.getInstance();
        cm.setAcceptCookie(true);
        for (String cookie : cookieString.split(";")) {
            cm.setCookie("https://www.netflix.com", cookie.trim());
        }
        cm.flush();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupVideoWebView() {
        WebSettings s = videoWebView.getSettings();
        
        // === PURE NATIVE IDENTITY ===
        // We use the real system UA here. Bitmovin test proved this works!
        // No spoofing = Perfect DRM Handshake.
        s.setUserAgentString(null); 

        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        
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
                // Auto-play script
                view.evaluateJavascript(
                    "(function() {" +
                    "  var v = document.querySelector('video');" +
                    "  if(v) v.play();" +
                    "  document.body.style.backgroundColor = 'black';" +
                    "})();", null);
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
