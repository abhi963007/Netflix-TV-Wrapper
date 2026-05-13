// Spoof screen resolution to 720p for performance
Object.defineProperty(window.screen, 'width', { get: () => 1280 });
Object.defineProperty(window.screen, 'height', { get: () => 720 });

// ChromeOS / Linux Spoofing
Object.defineProperty(navigator, 'platform', { get: () => 'X11; CrOS x86_64' });

// Force hide annoying sign-out and app-redirect overlays
const hideInterval = setInterval(() => {
    const overlays = document.querySelectorAll('.leaving-so-soon, .sign-out-container, .update-required-overlay, .modal-open-app');
    overlays.forEach(el => el.style.display = 'none');
}, 1000);

console.log("Netflix TV Wrapper: ChromeOS Hybrid Mode Active");
