// Spoof screen resolution to 720p for performance
Object.defineProperty(window.screen, 'width', { get: () => 1280 });
Object.defineProperty(window.screen, 'height', { get: () => 720 });

// Spoof tablet properties
Object.defineProperty(navigator, 'maxTouchPoints', { get: () => 10 });
Object.defineProperty(navigator, 'platform', { get: () => 'Linux armv8l' });

// Overwrite 'canPlayType' to force Netflix to use compatible codecs
const oldCanPlayType = HTMLVideoElement.prototype.canPlayType;
HTMLVideoElement.prototype.canPlayType = function(type) {
    if (type.contains('avc1') || type.contains('mp4')) return 'probably';
    return oldCanPlayType.call(this, type);
};

// Force hide annoying sign-out overlays
const hideInterval = setInterval(() => {
    const overlays = document.querySelectorAll('.leaving-so-soon, .sign-out-container, .update-required-overlay');
    overlays.forEach(el => el.style.display = 'none');
}, 1000);

console.log("Netflix TV Wrapper: Tablet Spoofing & Codec Force Active");
