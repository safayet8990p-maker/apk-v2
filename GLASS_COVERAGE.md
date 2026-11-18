# Liquid Glass Coverage & Verification Matrix — Glassly v0.01.10

Every floating control, bar, button, menu, sheet, alert, slider, switch, chip, field, toast, and picker in **Glassly** uses the shared Liquid Glass component library (`Modifier.liquidGlass` + `Modifier.glassPressBounce`). Main photo/video content remains unblurred, and rows inside glass cards or sheets use flat tonal fills (`FlatGlassRow`) so glass is never nested inside glass.

| Screen | Control / Component | Uses Glass? | Variant | Pressed Bounce | Dark Mode | Reduce Glass Fallback | TalkBack & 48dp | Status |
| :--- | :--- | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| **Global Navigation** | `GlassTabBar` (Library / Collections / Settings capsule) | Yes | Regular | Yes (1.06x + tick) | Yes | Yes | Yes | **PASS** |
| **Global Navigation** | Detached Search Orb in `GlassContainer` | Yes | Regular / Tinted | Yes (1.06x + tick) | Yes | Yes | Yes | **PASS** |
| **Global Feedback** | `GlassToast` floating top capsule | Yes | Regular | Yes | Yes | Yes | Yes | **PASS** |
| **Settings** | Collapsing `GlassToolbar` + Glass Gallery button | Yes | Regular | Yes | Yes | Yes | Yes | **PASS** |
| **Settings** | Top `LiveGlassPreviewCard` floating lens + `GlassButton` | Yes | Active Variant | Yes | Yes | Yes | Yes | **PASS** |
| **Settings** | 10 Section `GlassCard` containers (with `FlatGlassRow` inside) | Yes | Regular | N/A (Container) | Yes | Yes | Yes | **PASS** |
| **Settings** | Theme, Variant & Language `GlassSegmentedControl` | Yes | Regular + Lens Thumb | Yes | Yes | Yes | Yes | **PASS** |
| **Settings** | Glass Intensity `GlassSlider` (rainbow refractive thumb) | Yes | Clear Droplet Thumb | Yes (1.18x drag) | Yes | Yes | Yes | **PASS** |
| **Settings** | All 15 `GlassSwitch` toggles | Yes | Droplet Pill Thumb | Yes (1.08x + tick) | Yes | Yes | Yes | **PASS** |
| **Settings** | Grid Columns & Trash Retention `GlassStepper` | Yes | Regular | Yes | Yes | Yes | Yes | **PASS** |
| **Settings** | Sort Order & Video Speed `GlassPicker` | Yes | Regular / Tinted | Yes | Yes | Yes | Yes | **PASS** |
| **Settings** | Reset All Settings `GlassButton` + expanding `GlassAlert` | Yes | Danger / Regular | Yes | Yes | Yes | Yes | **PASS** |
| **Library** | Top `GlassToolbar` + `Select` `GlassButton` + More `GlassButton` | Yes | Regular / Tinted | Yes | Yes | Yes | Yes | **PASS** |
| **Library** | Expanding Library Options `GlassMenu` | Yes | Regular (Thickened) | Yes | Yes | Yes | Yes | **PASS** |
| **Library** | Sticky Date & Location `GlassChip` | Yes | Regular | Yes | Yes | Yes | Yes | **PASS** |
| **Library** | Permission Access `PermissionGlassBanner` + `GlassButton` | Yes | Regular + Tinted | Yes | Yes | Yes | Yes | **PASS** |
| **Library** | Thumbnail `GlassBadge` (`LIVE`, duration, `RAW`, `GIF`, Heart) | Yes | Clear + Scrim | N/A | Yes | Yes | Yes | **PASS** |
| **Library** | Fast-Scroll Date Bubble `GlassScrubber` | Yes | Regular | Yes | Yes | Yes | Yes | **PASS** |
| **Library** | `Years / Months / Days / All` `GlassSegmentedControl` | Yes | Regular + Lens Thumb | Yes | Yes | Yes | Yes | **PASS** |
| **Library** | Multi-select `GlassActionBar` (Share/Fav/Album/Hide/Delete) | Yes | Regular (Thickened) | Yes | Yes | Yes | Yes | **PASS** |
| **Collections** | Collapsing `GlassToolbar` + `+` `GlassButton` + Density Menu | Yes | Regular / Tinted | Yes | Yes | Yes | Yes | **PASS** |
| **Collections** | Long-press Album `GlassMenu` (Pin/Rename/Reorder/Delete) | Yes | Regular (Thickened) | Yes | Yes | Yes | Yes | **PASS** |
| **Collections** | Smart Groups, Media Types & Utilities `GlassCard`s | Yes | Regular | Yes | Yes | Yes | Yes | **PASS** |
| **Collections** | Create / Rename / Delete Album `GlassAlert`s | Yes | Regular (Thickened) | Yes | Yes | Yes | Yes | **PASS** |
| **Album Detail** | `GlassToolbar` + Play Slideshow `GlassButton` + `GlassPicker` | Yes | Regular / Tinted | Yes | Yes | Yes | Yes | **PASS** |
| **Search** | Bottom Thumb-Reach `GlassSearchField` | Yes | Regular | Yes | Yes | Yes | Yes | **PASS** |
| **Search** | Smart Suggestion `GlassChip`s & Recent Searches `GlassCard` | Yes | Regular / Tinted | Yes | Yes | Yes | Yes | **PASS** |
| **Photo Viewer** | Top `GlassToolbar` + Zoom/LIVE `GlassChip` + More `GlassMenu` | Yes | Regular / Tinted | Yes | Yes | Yes | Yes | **PASS** |
| **Photo Viewer** | Bottom `GlassActionBar` (Share, Favorite, Info, Edit, Delete) | Yes | Regular (Thickened) | Yes | Yes | Yes | Yes | **PASS** |
| **Photo Viewer** | Swipe-Up EXIF Metadata `GlassBottomSheet` (`FlatGlassRow` inside) | Yes | Regular (Thickened) | Yes | Yes | Yes | Yes | **PASS** |
| **Video Player** | `VideoGlassControllerBar` (Play, `GlassSlider`, Speed, Loop, PiP) | Yes | Regular / Tinted | Yes | Yes | Yes | Yes | **PASS** |
| **Photo Editor** | Top `GlassToolbar` + Hold `Compare` + `Revert` + `Save` | Yes | Regular / Tinted | Yes | Yes | Yes | Yes | **PASS** |
| **Photo Editor** | Bottom Editor Sheet + `GlassSlider` + Filter `GlassChip`s | Yes | Regular / Tinted | Yes | Yes | Yes | Yes | **PASS** |
| **Photo Editor** | Mode `GlassSegmentedControl` (`Adjust / Crop / Filters / Markup`) | Yes | Regular + Lens Thumb | Yes | Yes | Yes | Yes | **PASS** |
| **Recently Deleted** | `GlassToolbar` + Restore/Delete `GlassButton`s + `GlassAlert` | Yes | Regular / Danger | Yes | Yes | Yes | Yes | **PASS** |
| **Hidden Album** | Liquid Glass PIN Dots + 12-button `GlassButton` Keypad + Biometric | Yes | Icon Circle / Tinted | Yes | Yes | Yes | Yes | **PASS** |
| **Duplicates & Large** | `GlassToolbar` + Comparison `GlassCard`s + Merge/Clean Buttons | Yes | Regular / Tinted | Yes | Yes | Yes | Yes | **PASS** |
| **Slideshow** | Auto-hiding `GlassToolbar` + Play/Pause & Speed `GlassPicker` | Yes | Regular / Tinted | Yes | Yes | Yes | Yes | **PASS** |
| **Glass Gallery** | Debug screen verifying all 20 components in every state | Yes | Regular/Clear/Tinted | Yes | Yes | Yes | Yes | **PASS** |
