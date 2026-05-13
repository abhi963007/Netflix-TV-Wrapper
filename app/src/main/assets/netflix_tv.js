// =============================================
// Netflix TV Wrapper — Browsing Mode Scripts
// Playback is handled by Chrome Custom Tab
// =============================================

// Fix screen size for proper layout
Object.defineProperty(window.screen, 'width', { get: () => 1280 });
Object.defineProperty(window.screen, 'height', { get: () => 720 });

// Hide all annoying overlay modals
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

// Run immediately and keep watching for new overlays
hideOverlays();
const observer = new MutationObserver(hideOverlays);
observer.observe(document.body || document.documentElement, {
    childList: true,
    subtree: true
});

console.log('[Netflix TV Wrapper] Browsing mode active. Playback handled by Chrome.');
