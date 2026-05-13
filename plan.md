# Netflix Web TV - Implementation Plan

## Objective
Create a custom Android TV "Wrapper" application using a WebView to access Netflix. This bypasses native app certification checks while providing a TV-optimized experience.

## 1. Project Configuration
*   **Package Name:** `com.custom.netflix.tv`
*   **Target Device:** Android TV (Android 9+)
*   **UI Mode:** Leanback (TV Home Screen integration)

## 2. Core Features
### A. WebView Engine
*   **URL:** `https://www.netflix.com`
*   **JavaScript:** Enabled for interactive UI.
*   **DOM Storage:** Enabled for login persistence.
*   **User Agent:** Set to a specific Android TV browser string to trigger the TV layout.

### B. Media & DRM (Critical)
*   **Widevine Support:** Configure the WebView to support encrypted media extensions (EME) for Netflix playback.
*   **Mixed Content:** Allow loading content from secure and non-secure sources if necessary.

### C. TV Navigation (D-Pad)
*   Override `onKeyDown` to handle:
    *   **Up/Down/Left/Right:** Navigation between Netflix tiles.
    *   **OK/Center:** Selection/Playback.
    *   **Back:** Returning to the previous page or exiting.

## 3. UI/UX (TV Optimized)
*   **Leanback Launcher:** Add `<category android:name="android.intent.category.LEANBACK_LAUNCHER" />` to the Manifest.
*   **TV Banner:** 320x180 PNG banner for the home screen.
*   **Full Screen:** Hide status bars and navigation bars for a cinematic experience.

## 4. Implementation Steps
1.  **[ ] Step 1:** Define `AndroidManifest.xml` with TV permissions and Leanback support.
2.  **[ ] Step 2:** Create `MainActivity.java` with the specialized WebView logic.
3.  **[ ] Step 3:** Design the `activity_main.xml` layout (Full-screen WebView).
4.  **[ ] Step 4:** Add TV resources (Banner and Icon) from the original TV APK.
5.  **[ ] Step 5:** Build, Sign, and Deploy to the TV via ADB.

---
**Status:** Planning Completed. Ready for Step 1.
