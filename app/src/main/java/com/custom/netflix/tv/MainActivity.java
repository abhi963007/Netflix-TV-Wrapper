package com.custom.netflix.tv;

import android.annotation.SuppressLint;
import android.content.Intent;
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

    // Linux Desktop UA — passes "Update Required", avoids "Open in App"
    private static final String BROWSE_UA = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36";

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
        settings.setUserAgentString(BROWSE_UA);
        settings.setSaveFormData(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);

        // JS Bridge: lets JavaScript call back into Android
        // when Netflix's pushState navigation goes to /watch/
        netflixWebView.addJavascriptInterface(new NetflixBridge(), "AndroidBridge");

        netflixWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        // Grant preview/trailer DRM permissions (full DRM handled in VideoActivity)
        netflixWebView.setWebChromeClient(new android.webkit.WebChromeClient() {
            @Override
            public void onPermissionRequest(final android.webkit.PermissionRequest request) {
                request.grant(request.getResources());
            }
        });

        // Full-screen immersive
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
                // Block external app redirects
                if (url.startsWith("intent://") ||
                        url.startsWith("market://") ||
                        url.startsWith("netflix://")) {
                    return true;
                }

                /* 
                // HYBRID HANDOFF DISABLED: Trailers are working in main WebView!
                if (url.contains("netflix.com/watch/") || url.contains("netflix.com/watch?")) {
                    launchVideoPlayer(url);
                    return true;
                }
                */

                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                // Hide splash after page loads
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

    /**
     * Launches VideoActivity with the /watch/ URL and current session cookies.
     * VideoActivity uses the device's native UA so Widevine DRM works properly.
     */
    private void launchVideoPlayer(String url) {
        // Flush cookies to disk first
        CookieManager.getInstance().flush();

        // Get session cookies to pass to VideoActivity
        String cookies = CookieManager.getInstance().getCookie("https://www.netflix.com");

        Intent intent = new Intent(this, VideoActivity.class);
        intent.putExtra(VideoActivity.EXTRA_URL, url);
        intent.putExtra(VideoActivity.EXTRA_COOKIES, cookies);
        startActivity(intent);
    }

    private void injectCustomAssets() {
        try {
            // Inject CSS (cinematic styles)
            java.io.InputStream cssInput = getAssets().open("netflix_tv.css");
            byte[] cssBuffer = new byte[cssInput.available()];
            cssInput.read(cssBuffer);
            cssInput.close();
            String cssEncoded = android.util.Base64.encodeToString(cssBuffer, android.util.Base64.NO_WRAP);
            netflixWebView.evaluateJavascript(
                    "var style = document.createElement('style');" +
                            "style.innerHTML = window.atob('" + cssEncoded + "');" +
                            "document.head.appendChild(style);",
                    null);

            // Inject JS (overlay hiding + pushState intercept)
            java.io.InputStream jsInput = getAssets().open("netflix_tv.js");
            byte[] jsBuffer = new byte[jsInput.available()];
            jsInput.read(jsBuffer);
            jsInput.close();
            netflixWebView.evaluateJavascript(new String(jsBuffer), null);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if (netflixWebView.canGoBack()) {
                    netflixWebView.goBack();
                    return true;
                }
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * JavaScript Bridge: This is called from our netflix_tv.js
     * when it detects a /watch/ URL navigation.
     */
    public class NetflixBridge {
        @android.webkit.JavascriptInterface
        public void playVideo(final String url) {
            runOnUiThread(() -> {
                // Bridge disabled to keep playback in the main window
                android.util.Log.d("NetflixBridge", "Trailers work! Staying in main window.");
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        netflixWebView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        netflixWebView.onPause();
    }
}
