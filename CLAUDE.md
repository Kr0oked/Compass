# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Simple compass app for Android (Kotlin + Jetpack Compose), published on F-Droid and Google Play.
Package: `com.bobek.compass`.

## Build & Test Commands

```bash
# Unit tests
./gradlew test

# Instrumented tests (requires connected device/emulator)
./gradlew connectedAndroidTest

# Run all tests, auto-creating/booting/shutting down a Test_Phone AVD for the instrumented ones
bundle exec fastlane android test

# Build debug APK
./gradlew assembleDebug

# Build release APK (requires signing env vars)
bundle exec fastlane android apk

# Deploy to Google Play (requires signing env vars + JSON key)
fastlane android deploy

# Run lint
./gradlew lint

# Screenshots via Fastlane; each lane grabs a light (1.png) and dark (2.png) shot
bundle exec fastlane android grab_screens               # creates Screenshots_* AVDs if missing, boots each in turn
bundle exec fastlane android setup_screenshot_emulators # just (re-)create the Screenshots_* AVDs, without grabbing screenshots
bundle exec fastlane android grab_screen_phone          # requires a connected/already-running device
bundle exec fastlane android grab_screen_seven_inch
bundle exec fastlane android grab_screen_ten_inch

# Regenerate fastlane/metadata/android/*/images/featureGraphic.png from the app icon,
# app/src/main/res/values/colors.xml theme colors, and each locale's translated app name.
# Requires python3-pillow (built with libraqm). Needed fonts are downloaded automatically
# on first run into fastlane/.fonts-cache/ (git-ignored), so a network connection is
# needed the first time only.
bundle exec fastlane android generate_feature_graphics
```

Fastlane release builds require env vars: `KEYSTORE_FILE`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`.
Google Play deployment additionally requires `ANDROID_JSON_KEY_FILE`.

## Architecture

The app follows MVVM in a single-Activity Compose setup:

- **`CompassApplication`** — Hilt entry point
- **`MainActivity`** — Single Compose activity; hosts `AppViewModel` and `CompassViewModel`; registers sensor and
  location listeners; handles `ACCESS_LOCATION` permission workflow. Per rotation-vector event it only remaps the
  raw matrix to the current display rotation (`MathUtils.remappedRotationMatrix`) and forwards it; no azimuth math
  or regime logic lives here
- **`AppViewModel`** — Night mode preference via `StateFlow`; reads from `SettingsRepository`
- **`CompassViewModel`** — Combines device rotation, the true-north setting, and location into a `CompassReading`
  via `CompassReadingCalculator.next` (see [Compass Regimes](#compass-regimes-rose--sighting--hint)); exposes that
  plus sensor/settings state via `StateFlow`, loaded from `SettingsRepository` on init
- **`ICompassViewModel`** / **`ComposeCompassViewModel`** — Interface + preview implementation used by all Compose
  screens

### Key Packages

| Package     | Responsibility                                                                                                                                                                                                |
|-------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `data/`     | Immutable data models: `Azimuth`, `CardinalDirection`, `CompassReading`, `CompassRegime`, `SensorAccuracy`, `LocationStatus`, `AppNightMode`, `AppError`                                                      |
| `settings/` | `DataStoreSettingsRepository` — persists preferences via Jetpack DataStore; migrates from SharedPreferences; injected via Hilt                                                                                |
| `ui/`       | Jetpack Compose screens: `compass/`, `settings/`, `licenses/`, `theme/`                                                                                                                                       |
| `util/`     | `MathUtils` — rotation-matrix remap, magnetic declination, haptic-feedback interval math. `CompassReadingCalculator` — pure-Kotlin azimuth/tilt/regime derivation, unit-tested without any Android dependency |

### Data Flow

Rotation-vector sensor events → `MainActivity` remaps them to a device-to-world matrix → `CompassViewModel` combines
that with the true-north setting and location, and runs `CompassReadingCalculator.next` through a `scan` (so regime
hysteresis can see the previous reading) → `CompassReading` `StateFlow` → Compose UI.

Settings changes are debounced 1 second before being written to DataStore.

### Location Handling

`MainActivity` uses `requestLocationUpdates` (not `getCurrentLocation`) to acquire a single location fix, then removes
the listener immediately after the first result. Location permission is requested once via `registerForActivityResult`.
The `repeatOnLifecycle(RESUMED)` block re-triggers location handling whenever `trueNorth` changes.

## Compass Regimes (rose / sighting / hint)

The compass has three visualizations, chosen purely from **tilt**: the angle of the screen normal away from
straight up (0° = flat, screen up; 90° = upright/edge-on; 180° = flat, screen down). Selection and its hysteresis
live entirely in `CompassReadingCalculator.nextRegime`, keyed only on the current tilt and the previous regime
(no time debounce, no broader state machine), so a fast face-down flip jumps `ROSE` → `HINT` directly, skipping
`SIGHTING`.

| Regime     | Screen orientation | Shows                                                          | Enter threshold                    | Exit threshold                                |
|------------|--------------------|----------------------------------------------------------------|------------------------------------|-----------------------------------------------|
| `ROSE`     | roughly horizontal | The rotating compass rose, top-edge bearing                    | (initial regime)                   | tilt > 70° → `SIGHTING`                       |
| `SIGHTING` | roughly vertical   | Linear sighting strip, bearing of the phone's back (device −Z) | tilt > 70° from `ROSE`             | tilt < 52° → `ROSE`; tilt > 130° → `HINT`     |
| `HINT`     | roughly face-down  | Frozen, dimmed rose + "hold your phone level" message          | tilt > 130° from `SIGHTING`/`ROSE` | tilt < 110° → `SIGHTING`; tilt < 52° → `ROSE` |

Rationale: rendering the rose as an in-plane rotation implicitly assumes "screen faces up". Drawing it face-down
would need a mirrored rose, so `HINT` is the deliberate alternative rather than a sensor limitation. This also
fixed a pre-existing bug where the rose visibly swapped east/west when carried through face-down, since the phone
now passes `ROSE` → `SIGHTING` → `HINT` instead of the rose spinning 180° in-plane.

`CompassReading` fields:

| Field             | Meaning                                                                   |
|-------------------|---------------------------------------------------------------------------|
| `azimuth`         | `ROSE` bearing: phone's top edge, declination applied                     |
| `sightingBearing` | `SIGHTING` bearing: phone's back (device −Z), declination applied         |
| `tilt`            | 0–180°, drives regime selection                                           |
| `roll`            | rotation about the screen normal; computed but not yet surfaced in the UI |
| `regime`          | `ROSE` / `SIGHTING` / `HINT`, post-hysteresis                             |
| `reliable`        | gates haptic feedback only, see below                                     |

Both bearings are always populated regardless of the active regime, since `CompassDisplay`'s crossfade needs both
ready at every transition.

Other current design decisions:

- **`reliable` only gates haptics.** It's `false` whenever the active regime's reference axis is within ~17.5° of
  vertical, or unconditionally in `HINT`. It has no visual effect; `HINT` is the only visibly "degraded" state.
- **Haptics are hoisted and regime-agnostic.** `CompassHapticFeedback` (in `CompassHaptics.kt`) just ticks every 2°
  as a given `Azimuth` sweeps past a mark while `enabled`; `CompassDisplay` feeds it whichever bearing (`azimuth` or
  `sightingBearing`) matches the current regime, gated on `reliable` and briefly suppressed (~250 ms, tracked via
  `regimeSettling`) right after any regime change, since the active bearing basis jumps (top edge vs. back of phone).
- **`CompassDisplay` crossfades the three widgets** (250 ms `Crossfade`) and freezes the rose's last azimuth
  (`frozenRoseAzimuth`) for the `HINT` visual so it doesn't visibly keep spinning while hidden.
- **No settings toggle.** Sighting mode always replaces the rose when the phone is held upright; nothing was added
  to the Settings screen.
- **`CompassScreen` uses one layout for every orientation and regime.** The compass display fills the whole content
  area and stays centered; `DeclinationText` and `LocationSection` float over its bottom-start/bottom-end corners
  (`CornerInfo`). Neither the display's size nor the two texts' positions change across regimes or on rotation;
  only the widget inside the display crossfades.
- **The sighting strip forces LTR** regardless of app locale, since it's a physical instrument and degrees must
  always increase clockwise.
- **Screen orientation is not special-cased for `SIGHTING`.** `MainContent` sets `requestedOrientation` purely from
  the `screenOrientationLocked` setting, the same as every other regime. An earlier attempt froze orientation while
  `SIGHTING` was active (Android's orientation detector is unstable near-vertical), but `SCREEN_ORIENTATION_LOCKED`
  locks once and stops re-evaluating, so it also blocked deliberate rotations, not just detector jitter. Reverted.
- **Test tags:** `TestConstants.COMPASS_ROSE`, `COMPASS_STRIP`. `HINT` has no tag; assert it via its visible text
  (`R.string.compass_hold_level`).
- **Non-goals:** a full 3D tilted rose, camera-passthrough sighting, a user-facing inclinometer/pitch readout, and
  mirroring the rose for face-down (that's what `HINT` replaces) were all considered and rejected. A roll indicator
  is deferred; `CompassReading.roll` is computed but currently unused in the UI.

## Tech Stack

- **UI:** Jetpack Compose + Material3, Navigation Compose
- **DI:** Hilt + KSP
- **Persistence:** DataStore Preferences
- **Sensors:** Android `SensorManager` (rotation vector + magnetic field)
- **Build:** AGP 9.x, Kotlin 2.3.x, Java 11 toolchain
- **Testing:** JUnit4, Compose UI Test, kotlinx-coroutines-test, Fastlane Screengrab

## Branch Notes

`master` is the main branch used for releases and PRs.
