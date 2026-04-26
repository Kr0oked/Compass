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

# Build debug APK
./gradlew assembleDebug

# Build release APK (requires signing env vars)
fastlane android apk

# Deploy to Google Play (requires signing env vars + JSON key)
fastlane android deploy

# Run lint
./gradlew lint

# Screenshots via Fastlane (requires device)
fastlane android grab_screen_phone_1
fastlane android grab_screen_phone_2
fastlane android grab_screen_seven_inch_1
fastlane android grab_screen_seven_inch_2
fastlane android grab_screen_ten_inch_1
fastlane android grab_screen_ten_inch_2
```

Fastlane release builds require env vars: `KEYSTORE_FILE`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`.
Google Play deployment additionally requires `ANDROID_JSON_KEY_FILE`.

## Architecture

The app follows MVVM in a single-Activity Compose setup:

- **`CompassApplication`** — Hilt entry point
- **`MainActivity`** — Single Compose activity; registers sensor and location listeners; handles `ACCESS_LOCATION`
  permission workflow
- **`CompassViewModel`** — All UI state via `StateFlow`; debounced settings writes (1 second); loaded from
  `SettingsRepository` on init
- **`ICompassViewModel`** / **`ComposeCompassViewModel`** — Interface + preview implementation used by all Compose
  screens

### Key Packages

| Package     | Responsibility                                                                                                                 |
|-------------|--------------------------------------------------------------------------------------------------------------------------------|
| `data/`     | Immutable data models: `Azimuth`, `CardinalDirection`, `SensorAccuracy`, `LocationStatus`, `AppNightMode`, `AppError`          |
| `settings/` | `DataStoreSettingsRepository` — persists preferences via Jetpack DataStore; migrates from SharedPreferences; injected via Hilt |
| `ui/`       | Jetpack Compose screens: `compass/`, `settings/`, `licenses/`, `theme/`                                                        |
| `util/`     | `MathUtils` — azimuth calculation, magnetic declination, haptic feedback interval helpers                                      |

### Data Flow

Sensor events → `MainActivity` → `CompassViewModel` (StateFlow) → Compose UI

Settings changes in the UI update `CompassViewModel` flows immediately, and are debounced 1 second before being written
to DataStore.

### Location Handling

`MainActivity` uses `requestLocationUpdates` (not `getCurrentLocation`) to acquire a single location fix, then removes
the listener immediately after the first result. Location permission is requested once via `registerForActivityResult`.
The `repeatOnLifecycle(RESUMED)` block re-triggers location handling whenever `trueNorth` changes.

## Tech Stack

- **UI:** Jetpack Compose + Material3, Navigation Compose
- **DI:** Hilt + KSP
- **Persistence:** DataStore Preferences
- **Sensors:** Android `SensorManager` (rotation vector + magnetic field)
- **Build:** AGP 9.1.0, Kotlin 2.3.x, Java 11 toolchain
- **Testing:** JUnit4, Compose UI Test, kotlinx-coroutines-test, Fastlane Screengrab

## Branch Notes

`master` is the main branch used for releases and PRs.

## Translations

Translations are managed via Weblate. Do not manually edit `strings.xml` files in locale-specific resource directories —
changes come in through automated PRs from Weblate.
