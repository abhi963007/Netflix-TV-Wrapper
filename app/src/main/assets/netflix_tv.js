// =============================================
// Netflix TV Wrapper — Browsing Mode Scripts
// Video playback is handled by VideoActivity
// =============================================

// Fix layout dimensions for TV
Object.defineProperty(window.screen, 'width',      { get: () => 1280 });
Object.defineProperty(window.screen, 'height',     { get: () => 720  });
Object.defineProperty(window.screen, 'availWidth', { get: () => 1280 });
Object.defineProperty(window.screen, 'availHeight',{ get: () => 720  });

// Hide all annoying overlay modals using a MutationObserver
// so we catch ones that appear after the page loads
const hideOverlays = () => {
    const selectors = [
        '.leaving-so-soon',
        '.sign-out-container',
        '.update-required-overlay',
        '.modal-open-app',
        '.open-app-container',
        '[data-uia="open-app-dialog"]',
        '[data-uia="leaving-dialog"]'
    ];
    selectors.forEach(sel => {
        document.querySelectorAll(sel).forEach(el => {
            el.style.setProperty('display', 'none', 'important');
        });
    });
};

// Run immediately
hideOverlays();

// Watch for dynamically added overlays
if (document.body) {
    const observer = new MutationObserver(hideOverlays);
    observer.observe(document.body, { childList: true, subtree: true });
} else {
    document.addEventListener('DOMContentLoaded', () => {
        const observer = new MutationObserver(hideOverlays);
        observer.observe(document.body, { childList: true, subtree: true });
    });
}

console.log('[Netflix TV Wrapper] Browse mode active. /watch/ URLs handled by VideoActivity.');
