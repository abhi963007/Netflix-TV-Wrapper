// =============================================
// Netflix TV Wrapper — Browsing Mode Scripts
// =============================================

// Fix layout dimensions for TV
Object.defineProperty(window.screen, 'width',      { get: () => 1280 });
Object.defineProperty(window.screen, 'height',     { get: () => 720  });

// 1. INTERCEPT WATCH NAVIGATION
// Netflix uses pushState, which doesn't trigger Android's shouldOverrideUrlLoading.
// We intercept it here and call our AndroidBridge instead.
const checkAndRedirect = (url) => {
    if (url && url.includes('/watch/')) {
        console.log('[Netflix TV] Intercepted watch URL: ' + url);
        if (window.AndroidBridge) {
            window.AndroidBridge.playVideo(url);
            return true;
        }
    }
    return false;
};

// Patch pushState
const originalPushState = history.pushState;
history.pushState = function() {
    const url = arguments[2];
    if (checkAndRedirect(url)) return; // Stop if redirected
    return originalPushState.apply(this, arguments);
};

// Patch replaceState
const originalReplaceState = history.replaceState;
history.replaceState = function() {
    const url = arguments[2];
    if (checkAndRedirect(url)) return;
    return originalReplaceState.apply(this, arguments);
};

// 2. HIDE OVERLAYS
const hideOverlays = () => {
    const selectors = [
        '.leaving-so-soon', '.sign-out-container', '.update-required-overlay',
        '.modal-open-app', '.open-app-container', '[data-uia="open-app-dialog"]'
    ];
    selectors.forEach(sel => {
        document.querySelectorAll(sel).forEach(el => el.style.display = 'none');
    });
};

setInterval(hideOverlays, 1000);
console.log('[Netflix TV] Bridge Active.');
