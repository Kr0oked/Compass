fastlane documentation
----

# Installation

Make sure you have the latest version of the Xcode command line tools installed:

```sh
xcode-select --install
```

For _fastlane_ installation instructions, see [Installing _fastlane_](https://docs.fastlane.tools/#installing-fastlane)

# Available Actions

## Android

### android test

```sh
[bundle exec] fastlane android test
```

Checks formatting and runs all the tests

### android apk

```sh
[bundle exec] fastlane android apk
```

Build a new APK

### android deploy

```sh
[bundle exec] fastlane android deploy
```

Deploy a new version to the Google Play

### android grab_screen_phone

```sh
[bundle exec] fastlane android grab_screen_phone
```

Grab phone screenshots

### android grab_screen_seven_inch

```sh
[bundle exec] fastlane android grab_screen_seven_inch
```

Grab seven inch screenshots

### android grab_screen_ten_inch

```sh
[bundle exec] fastlane android grab_screen_ten_inch
```

Grab ten inch screenshots

### android grab_screens

```sh
[bundle exec] fastlane android grab_screens
```

Grab all screenshots, booting each screenshot emulator in turn

### android setup_screenshot_emulators

```sh
[bundle exec] fastlane android setup_screenshot_emulators
```

Create the Screenshots_* AVDs on this machine if they don't already exist

### android generate_feature_graphics

```sh
[bundle exec] fastlane android generate_feature_graphics
```

Regenerate featureGraphic.png for every fastlane locale from the app icon, theme colors and translated app name

----

This README.md is auto-generated and will be re-generated every time [_fastlane_](https://fastlane.tools) is run.

More information about _fastlane_ can be found on [fastlane.tools](https://fastlane.tools).

The documentation of _fastlane_ can be found on [docs.fastlane.tools](https://docs.fastlane.tools).
