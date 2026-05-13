// Phase 2: DRM Interceptor (The "Legacy Handshake")
const originalRequestMediaKeySystemAccess = navigator.requestMediaKeySystemAccess;
navigator.requestMediaKeySystemAccess = function(keySystem, configurations) {
    console.log("Netflix TV Wrapper: Intercepting DRM Handshake for " + keySystem);
    
    // Force a "Low Security" profile to avoid L1 hardware checks
    if (configurations && configurations.length > 0) {
        configurations.forEach(config => {
            if (config.videoCapabilities) {
                config.videoCapabilities.forEach(cap => {
                    // Force H.264 (avc1) only - Phase 3
                    cap.contentType = 'video/mp4; codecs="avc1.42E01E"'; 
                    cap.robustness = 'SW_SECURE_CRYPTO'; // Tell it we use software crypto
                });
            }
        });
    }
    return originalRequestMediaKeySystemAccess.apply(this, [keySystem, configurations]);
};

// Phase 3: Codec Capability Overwrite
const oldCanPlayType = HTMLVideoElement.prototype.canPlayType;
HTMLVideoElement.prototype.canPlayType = function(type) {
    if (type.includes('avc1') || type.includes('mp4')) return 'probably';
    if (type.includes('hevc') || type.includes('vp9')) return ''; // Hide 4K/HDR codecs
    return oldCanPlayType.call(this, type);
};

// Basic TV Layout fixes
Object.defineProperty(window.screen, 'width', { get: () => 1280 });
Object.defineProperty(window.screen, 'height', { get: () => 720 });

// Hide overlays
setInterval(() => {
    const overlays = document.querySelectorAll('.leaving-so-soon, .sign-out-container, .update-required-overlay, .modal-open-app');
    overlays.forEach(el => el.style.display = 'none');
}, 1000);

console.log("Netflix TV Wrapper: Legacy Handshake Active");
