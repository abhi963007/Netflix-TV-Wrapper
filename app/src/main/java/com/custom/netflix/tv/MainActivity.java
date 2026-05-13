package com.custom.netflix.tv;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private WebView netflixWebView;
    private View splashOverlay;
    private static final String NETFLIX_URL = "https://www.netflix.com";
    private static final String TV_USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        netflixWebView = findViewById(R.id.netflix_webview);
        splashOverlay = findViewById(R.id.splash_overlay);
        
        setupWebView();
        netflixWebView.loadUrl(NETFLIX_URL);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        WebSettings settings = netflixWebView.getSettings();
        
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        settings.setUserAgentString(TV_USER_AGENT);
        settings.setSaveFormData(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        
        // Enable Hardware Acceleration for video
        netflixWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        netflixWebView.setWebChromeClient(new android.webkit.WebChromeClient() {
            @Override
            public void onPermissionRequest(final android.webkit.PermissionRequest request) {
                // Automatically grant permission for Protected Media (DRM)
                for (String res : request.getResources()) {
                    if (res.equals(android.webkit.PermissionRequest.RESOURCE_PROTECTED_MEDIA_ID)) {
                        request.grant(new String[]{res});
                        return;
                    }
                }
                super.onPermissionRequest(request);
            }
        });

        netflixWebView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LOW_PROFILE |
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        netflixWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("intent://") || url.startsWith("market://") || url.startsWith("netflix://")) {
                    return true; // Block redirects to external apps
                }
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                
                // Hide splash screen after delay
                netflixWebView.postDelayed(() -> {
                    if (splashOverlay != null) {
                        splashOverlay.setVisibility(View.GONE);
                    }
                }, 3000);

                injectCustomAssets();
            }
        });

        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(netflixWebView, true);
    }

    private void injectCustomAssets() {
        try {
            // Inject CSS
            java.io.InputStream cssInput = getAssets().open("netflix_tv.css");
            byte[] cssBuffer = new byte[cssInput.available()];
            cssInput.read(cssBuffer);
            cssInput.close();
            String cssEncoded = android.util.Base64.encodeToString(cssBuffer, android.util.Base64.NO_WRAP);
            netflixWebView.evaluateJavascript("var style = document.createElement('style');" +
                    "style.innerHTML = window.atob('" + cssEncoded + "');" +
                    "document.head.appendChild(style);", null);

            // Inject JS (720p Spoofing)
            java.io.InputStream jsInput = getAssets().open("netflix_tv.js");
            byte[] jsBuffer = new byte[jsInput.available()];
            jsInput.read(jsBuffer);
            jsInput.close();
            String jsContent = new String(jsBuffer);
            netflixWebView.evaluateJavascript(jsContent, null);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
                return super.onKeyDown(keyCode, event);
            case KeyEvent.KEYCODE_BACK:
                if (netflixWebView.canGoBack()) {
                    netflixWebView.goBack();
                    return true;
                }
        }
        return super.onKeyDown(keyCode, event);
    }
}
