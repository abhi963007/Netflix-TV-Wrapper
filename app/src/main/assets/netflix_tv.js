// =============================================
// Netflix TV Wrapper — Hybrid Navigation Engine
// Detects /watch/ URLs via pushState interception
// and signals Android to open Chrome Custom Tab
// =============================================

// Fix screen size for proper layout
Object.defineProperty(window.screen, 'width', { get: () => 1280 });
Object.defineProperty(window.screen, 'height', { get: () => 720 });

// -----------------------------------------------
// HYBRID HANDOFF: Intercept pushState navigation
// Netflix uses history.pushState for /watch/ URLs
// so shouldOverrideUrlLoading never fires for them.
// We patch pushState to detect this ourselves.
// -----------------------------------------------
(function() {
    const originalPushState = history.pushState.bind(history);
    history.pushState = function(state, title, url) {
        originalPushState(state, title, url);
        if (url && (url.includes('/watch/') || url.includes('watch?'))) {
            const fullUrl = url.startsWith('http') ? url : 'https://www.netflix.com' + url;
            // Signal Android via window.location change which triggers shouldOverrideUrlLoading
            window.location.href = fullUrl;
        }
    };

    const originalReplaceState = history.replaceState.bind(history);
    history.replaceState = function(state, title, url) {
        originalReplaceState(state, title, url);
        if (url && (url.includes('/watch/') || url.includes('watch?'))) {
            const fullUrl = url.startsWith('http') ? url : 'https://www.netflix.com' + url;
            window.location.href = fullUrl;
        }
    };
})();

// Also catch any anchor clicks to /watch/ links
document.addEventListener('click', function(e) {
    const anchor = e.target.closest('a[href]');
    if (anchor) {
        const href = anchor.getAttribute('href');
        if (href && (href.includes('/watch/') || href.includes('watch?'))) {
            e.preventDefault();
            const fullUrl = href.startsWith('http') ? href : 'https://www.netflix.com' + href;
            window.location.href = fullUrl;
        }
    }
}, true);

// -----------------------------------------------
// Hide all annoying overlay modals
// -----------------------------------------------
const hideOverlays = () => {
    const selectors = [
        '.leaving-so-soon',
        '.sign-out-container',
        '.update-required-overlay',
        '.modal-open-app',
        '.open-app-container',
        '[data-uia="open-app-dialog"]'
    ];
    selectors.forEach(sel => {
        document.querySelectorAll(sel).forEach(el => {
            el.style.setProperty('display', 'none', 'important');
        });
    });
};

hideOverlays();
const observer = new MutationObserver(hideOverlays);
observer.observe(document.body || document.documentElement, {
    childList: true,
    subtree: true
});

console.log('[Netflix TV Wrapper] Hybrid engine active — pushState interception enabled.');
