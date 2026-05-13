package com.custom.netflix.tv;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabColorSchemeParams;
import androidx.browser.customtabs.CustomTabsClient;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.browser.customtabs.CustomTabsServiceConnection;
import androidx.browser.customtabs.CustomTabsSession;

public class MainActivity extends AppCompatActivity {

    private WebView netflixWebView;
    private View splashOverlay;
    private CustomTabsClient customTabsClient;
    private CustomTabsSession customTabsSession;

    private static final String NETFLIX_URL = "https://www.netflix.com";

    // Modern Linux Desktop — passes "Update Required" check, avoids "Open in App"
    private static final String TV_USER_AGENT =
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        netflixWebView = findViewById(R.id.netflix_webview);
        splashOverlay = findViewById(R.id.splash_overlay);

        // Pre-warm Chrome for faster playback handoff
        warmupChrome();

        setupWebView();
        netflixWebView.loadUrl(NETFLIX_URL);
    }

    /**
     * Pre-warms the Chrome browser in the background so that when we
     * hand off a /watch/ URL, it opens almost instantly.
     */
    private void warmupChrome() {
        try {
            CustomTabsClient.bindCustomTabsService(this, "com.android.chrome",
                    new CustomTabsServiceConnection() {
                        @Override
                        public void onCustomTabsServiceConnected(ComponentName name, CustomTabsClient client) {
                            customTabsClient = client;
                            customTabsClient.warmup(0L);
                            customTabsSession = customTabsClient.newSession(null);
                        }

                        @Override
                        public void onServiceDisconnected(ComponentName name) {
                            customTabsClient = null;
                            customTabsSession = null;
                        }
                    });
        } catch (Exception e) {
            // Chrome not available — will fall back to default browser
            e.printStackTrace();
        }
    }

    /**
     * Opens a Netflix /watch/ URL in Chrome Custom Tab.
     * Chrome has full Widevine DRM support, so playback will work.
     */
    private void openInChrome(String url) {
        // Netflix red color scheme for the Chrome tab
        CustomTabColorSchemeParams colorParams = new CustomTabColorSchemeParams.Builder()
                .setToolbarColor(0xFF000000)           // Black toolbar
                .setNavigationBarColor(0xFF000000)     // Black nav bar
                .build();

        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        if (customTabsSession != null) {
            builder = new CustomTabsIntent.Builder(customTabsSession);
        }

        CustomTabsIntent customTabsIntent = builder
                .setDefaultColorSchemeParams(colorParams)
                .setColorScheme(CustomTabsIntent.COLOR_SCHEME_DARK)
                .setShowTitle(false)
                .setUrlBarHidingEnabled(true)
                .build();

        // Sync cookies from WebView to Chrome so user stays logged in
        syncCookiesToChrome(url);

        customTabsIntent.launchUrl(this, Uri.parse(url));
    }

    /**
     * Copies the Netflix session cookies from our WebView into
     * Chrome's cookie store so the user doesn't have to log in again.
     */
    private void syncCookiesToChrome(String url) {
        try {
            CookieManager cm = CookieManager.getInstance();
            String cookies = cm.getCookie(url);
            if (cookies != null) {
                // Cookies are automatically shared via the system CookieManager
                // when both WebView and Chrome Custom Tabs use the same profile
                cm.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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

        // Hardware acceleration for smooth scrolling
        netflixWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        // Grant protected media permission (still useful for thumbnails/previews)
        netflixWebView.setWebChromeClient(new android.webkit.WebChromeClient() {
            @Override
            public void onPermissionRequest(final android.webkit.PermissionRequest request) {
                for (String res : request.getResources()) {
                    if (res.equals(android.webkit.PermissionRequest.RESOURCE_PROTECTED_MEDIA_ID)) {
                        request.grant(new String[]{res});
                        return;
                    }
                }
                super.onPermissionRequest(request);
            }
        });

        // Full-screen immersive mode
        netflixWebView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LOW_PROFILE |
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        // === THE HYBRID ENGINE ===
        netflixWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // Block external app redirects
                if (url.startsWith("intent://") || url.startsWith("market://") || url.startsWith("netflix://")) {
                    return true;
                }

                // HYBRID HANDOFF: Detect /watch/ URLs and open in Chrome
                if (url.contains("/watch/") || url.contains("watch?v=")) {
                    openInChrome(url);
                    return true; // Don't load in WebView
                }

                return false; // Everything else loads normally in WebView
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                // Hide splash screen after page loads
                netflixWebView.postDelayed(() -> {
                    if (splashOverlay != null) {
                        splashOverlay.setVisibility(View.GONE);
                    }
                }, 3000);

                // Inject our cinematic CSS and JS
                injectCustomAssets();
            }
        });

        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(netflixWebView, true);
    }

    private void injectCustomAssets() {
        try {
            // Inject CSS (cinematic styles, red glow, etc.)
            java.io.InputStream cssInput = getAssets().open("netflix_tv.css");
            byte[] cssBuffer = new byte[cssInput.available()];
            cssInput.read(cssBuffer);
            cssInput.close();
            String cssEncoded = android.util.Base64.encodeToString(cssBuffer, android.util.Base64.NO_WRAP);
            netflixWebView.evaluateJavascript("var style = document.createElement('style');" +
                    "style.innerHTML = window.atob('" + cssEncoded + "');" +
                    "document.head.appendChild(style);", null);

            // Inject JS (overlay hiding, layout fixes)
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
