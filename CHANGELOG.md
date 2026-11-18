# Changelog — Glassly

All notable changes to **Glassly** (`com.glassly.app`) are documented in this file.

## [0.01.10] — 2026-11-18 (versionCode 110)

### Added
- **App Identity & Versioning**:
  - Configured single-source-of-truth app identity: `Glassly`, `versionName = "0.01.10"`, `versionCode = 110`, and `applicationId = "com.glassly.app"`.
  - Created custom layered-glass adaptive, round, and monochrome launcher icons across all Android densities (`mdpi` through `xxxhdpi`).
  - Added sub-1-second layered-glass logo fade-and-settle splash overlay on startup.
- **Liquid Glass Optical Shader & Material System**:
  - Built `GlassConfig` central data class holding every optical and motion tunable (refraction strength, rim width, chromatic dispersion, blur radius, specular intensity, noise grain, inner glow, shadow alpha, spring stiffness/damping, and accessibility overrides).
  - Implemented `LIQUID_GLASS_AGSL` custom `RuntimeShader` (API 33+) with rounded-box Signed Distance Field (SDF) edge lensing/refraction, RGB chromatic dispersion along the rim, device-tilt + touch-reactive top-left bevel specular highlight, and fine procedural grain.
  - Implemented `GlassBackdropState` and `Modifier.glassBackdropSource(...)` using `rememberGraphicsLayer()` for zero-recursion background capture and optical sampling.
  - Added `Regular`, `Clear` (with adaptive legibility scrim), and `Tinted` glass variants plus automatic `Reduce Glass` high-contrast tonal fallback.
- **Shared 20-Component Liquid Glass Library**:
  - Built and verified `GlassButton`, `GlassContainer` (SDF smooth-union metaball fusion), `GlassSegmentedControl`, `GlassSwitch`, `GlassSlider`, `GlassStepper`, `GlassSearchField`, `GlassChip`, `GlassBadge`, `GlassCard` + `FlatGlassRow` (enforcing the "Never glass inside glass" rule), `GlassToolbar`, `GlassTabBar`, `GlassMenu`, `GlassAlert`, `GlassBottomSheet`, `GlassActionBar`, `GlassToast`, `GlassLoader`, `GlassScrubber`, and `GlassPicker`.
  - Added interactive **Glass Gallery** component showcase screen accessible from Settings.
- **Complete Feature Set Across All 8 App Areas**:
  - **Settings (10 Sections + Live Preview)**: Interactive top Live Preview card, Appearance, Library, Viewer & Video, Privacy & Security, Storage, Language (instant English / বাংলা switch), Accessibility, Permissions, About (`Glassly Version 0.01.10`), and Reset All Settings confirmation alert expanding from the trigger button.
  - **Library Tab**: Newest photos at the bottom (auto-scrolls to latest on open), `Years / Months / Days / All Photos` glass segmented control, pinch-to-zoom grid density, square vs aspect-ratio toggle, sticky date & location chips, multi-select with floating `GlassActionBar`, Clear-Glass media badges (`LIVE`, duration, `RAW`, `GIF`, Favorite heart), and fast-scroll date scrubber.
  - **Collections Tab & Album Detail**: Pinned collections, user Albums (create, rename, delete, reorder, pin), Smart Collections (`Recents`, `Favorites`, `Videos`, `Selfies`, `Screenshots`), Media Types (`Live`, `RAW`, `GIF`), Utilities (`Hidden`, `Recently Deleted`, `Duplicates`, `Large Files`), and adjustable tile density.
  - **Search Tab**: Thumb-reach bottom `GlassSearchField`, natural phrase search (`"videos last month"`, places, dates, albums, types), smart suggestion chips, and recent search history with clear option.
  - **Photo Viewer & Video Player**: Pinch/double-tap zoom (`1x–4x`), pan, swipe-down to dismiss, swipe-up EXIF Info `GlassBottomSheet`, tap to toggle chrome, bottom filmstrip scrubber, Media3 `ExoPlayer` video controller (`0.5x–2x` speed, mute, loop, PiP), and long-press Live Photo motion playback.
  - **Non-Destructive Photo Editor**: Crop ratios (`Free`, `1:1`, `4:3`, `16:9`, `9:16`), 90° rotate, horizontal/vertical flip, straighten dial, 6 optical adjustment sliders (`Brightness`, `Contrast`, `Saturation`, `Warmth`, `Sharpness`, `Vignette`), 6 filters, freehand markup & text overlay, `Hold to Compare`, `Revert`, and `Save as Copy` / `Overwrite`.
  - **Management & Utilities**: 30-day `Recently Deleted` trash recovery, `Hidden` album protected by a Liquid Glass PIN keypad + Biometric unlock, `Duplicates` merger, `Large Files` storage cleaner, and `Slideshow` with Ken Burns transitions.
