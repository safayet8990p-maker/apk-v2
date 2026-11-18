# Glassly (`com.glassly.app`) — Version 0.01.10

**Glassly** is a flagship Android Gallery & Photo Editor built with Kotlin and Jetpack Compose that recreates Apple's iOS 26 **Liquid Glass** optical design language and the iOS 26 Photos experience.

---

## Plain-Language Summary for the Owner

### What Was Built
1. **True Liquid Glass Visuals**: Every floating bar, button, switch, slider, menu, sheet, and alert bends and refracts the colorful photos behind it, with a glowing rim highlight that shifts as you touch controls or tilt your phone.
2. **Proving-Ground Settings Screen (10 Sections + Live Preview)**: Features a real-time **Live Liquid Glass Preview** card at the top, plus controls for Theme (System/Light/Dark), 6 Accent Colors, Glass Style (`Regular`, `Clear`, `Tinted`), Glass Intensity, Reduce Glass, Grid Density, Video Playback, Privacy Lock, Storage Cleaners, instant **English / বাংলা (Bangla)** language switching, Accessibility, and `Version 0.01.10` in About.
3. **Complete iOS 26 Photos Experience**:
   - **Library**: Opens at your newest photos at the bottom, switches smoothly between `Years`, `Months`, `Days`, and `All Photos`, supports pinch-to-zoom grid sizing, sticky date & location headers, multi-select action bars, and Clear-Glass media badges (`LIVE`, video duration, `RAW`, `GIF`, Favorite heart).
   - **Collections & Search**: Pinned collections, custom albums (create, rename, reorder, delete), smart folders, and a thumb-reach bottom glass search bar that understands places, dates, and natural phrases like `"videos last month"`.
   - **Viewer, Video Player & Non-Destructive Editor**: Pinch/double-tap zoom, swipe up for camera EXIF & GPS info, swipe down to close, full video speed/loop/PiP controls, and a full photo editor (Crop, Straighten dial, Brightness/Contrast/Saturation/Warmth/Sharpness/Vignette glass sliders, Filters, Markup ink, Text, Hold-to-Compare, Revert, and Save as Copy).
   - **Privacy & Storage Utilities**: 30-day Recently Deleted trash, Hidden Album locked behind a Liquid Glass PIN keypad (`2626` default) and Biometrics, Duplicate Photo Merger, Large Files Cleaner, and Ken Burns Slideshow.

---

## How `GlassConfig` Works

All optical and motion parameters live in a single immutable data class (`com.example.ui.theme.GlassConfig`) provided across the app via `LocalGlassConfig`:

- `variant`: `REGULAR` (balanced frosted lens), `CLEAR` (high transparency with automatic dark text-legibility scrim), or `TINTED` (saturated with the chosen accent color).
- `intensity` (`0.15f..1.0f`): Scales refraction strength, blur radius, and specular bevel brightness simultaneously in real time.
- `refractionStrength` & `chromaticDispersion`: Controls how strongly the AGSL `RuntimeShader` (`LIQUID_GLASS_AGSL`) bends background pixels along the rounded-rectangle Signed Distance Field (SDF) boundary and separates RGB channels at the rim.
- `blurRadiusDp`: Controls the `RenderEffect.createBlurEffect` radius applied to the recorded `GraphicsLayer` backdrop.
- `specularIntensity`: Controls the top-left bevel highlight that responds to both finger press coordinates and smoothed device gravity/accelerometer tilt.
- `reduceGlass`, `reduceMotion`, `highContrastGlass`, `largerTouchTargets`: Instant accessibility overrides.

---

## Honest List of Differences from Real iOS 26 Liquid Glass (Android Platform Limits)

1. **API Level Shader Capabilities**:
   - **Android 13+ (API 33+)**: Runs the full custom AGSL `RuntimeShader` (`LIQUID_GLASS_AGSL`) with real-time per-pixel SDF edge lensing, chromatic dispersion, specular bevel lighting, and procedural grain over `RenderEffect` blur.
   - **Android 12 (API 31–32)**: `RuntimeShader` is not available in Android 12; Glassly automatically falls back to hardware `RenderEffect` blur + canvas-drawn bevel specular highlights and adaptive tint.
   - **Below Android 12 / Reduce Glass Enabled**: Falls back to high-contrast frosted tonal surfaces.
2. **Window-Level Dialog Backdrop Capture**:
   - In-window overlays (`GlassToolbar`, `GlassTabBar`, `GlassActionBar`, `GlassSegmentedControl`, `GlassMenu`, `GlassScrubber`, `GlassCard`) refract the live `GraphicsLayer` photo canvas directly underneath them.
   - System-window dialogs (`Dialog`-hosted `GlassAlert` and `GlassBottomSheet`) render in a separate Android window surface, so they combine translucent optical gradients, concentric rim lighting, and backdrop scrims.

---

## Setup & Build Instructions

1. Open the project in Android Studio or AI Studio Build.
2. Build and run the `app` configuration (`gradle assembleDebug`) or run unit/Robolectric verification tests (`gradle :app:testDebugUnitTest`).
3. No external API keys are required—all media indexing, EXIF parsing, Room persistence, and AGSL shaders run 100% on-device.
