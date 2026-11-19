# PlantID — Android App

Android client for the PlantID research prototype: take or pick a photo of a
plant, see the identification result, read its care guide, and revisit past
identifications. All processing is meant to happen on the device, and the app
does not need an account.

> **Current status: Sprint 1 (design system and navigation).** The app has its
> theme (light/dark), shared UI components, and a bottom-navigation shell with
> placeholder Home, History and About screens. There's no photo selection, no
> identification (mock or real) and no saved history yet.

## Requirements

- Android Studio (current stable)
- Android SDK Platform 37 (install it from the SDK Manager)
- JDK: Gradle provisions the daemon toolchain (JDK 25) on its own through the
  foojay resolver, and Android Studio's bundled JDK also works
- An emulator or physical device running Android 8.0 (API 26) or newer

## Open and run

1. In Android Studio choose **File → Open** and select this `Mobile-App/`
   folder, not the repository root.
2. Wait for Gradle sync to finish.
3. Select a device and press **Run**.

## Command line

Run from `Mobile-App/` (use `gradlew.bat` on Windows cmd/PowerShell):

```bash
./gradlew assembleDebug              # build the debug APK
./gradlew testDebugUnitTest          # JVM unit tests
./gradlew connectedDebugAndroidTest  # instrumented + Compose UI tests (needs a device/emulator)
./gradlew installDebug               # install on a connected device
```

The debug APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

## Project facts

| Item | Value |
|---|---|
| Application ID / namespace | `io.github.ephym6.plantid` |
| minSdk / targetSdk / compileSdk | 26 / 37 / 37 |
| Language / UI | Kotlin, Jetpack Compose, Material 3 |
| Build | Android Gradle Plugin 9.5 (built-in Kotlin), Gradle wrapper 9.6, version catalog in `gradle/libs.versions.toml` |

## Code structure

Single `app` module. Current packages under `io.github.ephym6.plantid`:

```
MainActivity.kt            # sets the theme and PlantIdApp
ui/
├── PlantIdApp.kt          # app shell: bottom bar + navigation host
├── navigation/            # type-safe routes, NavHost, tab navigation helper
├── theme/                 # colours, typography, shapes, Spacing/Sizes tokens
├── components/            # buttons, cards, status message, loading/empty/error states, branding
├── home/  history/  about/   # one package per screen
```

Planned additions: `domain/` (Plant, CareInfo, IdentificationOutcome,
IdentificationService), `data/` (plant content, local history, mock
identification) and `di/` (manual dependency container).

### UI conventions

- Use `MaterialTheme.colorScheme` / `typography` / `shapes`, `PlantIDTheme.extendedColors`,
  and the `Spacing` and `Sizes` tokens. Don't hard-code colours or dimensions in screens.
- Build screens from the shared components in `ui/components/`. Every component and
  screen has `@PreviewLightDark` previews.
- All user-facing text goes in `res/values/strings.xml`.
- Icons: Material Symbols Rounded as vector drawables in `res/drawable/ic_*.xml`.
- Screens take state and callbacks only. They never receive the `NavController`.

The UI only talks to an `IdentificationService` interface. Until the model is
integrated, a **mock** implementation returns clearly labelled demo outcomes.
They are not real predictions.

## Documentation

- [`docs/DECISIONS.md`](docs/DECISIONS.md): technical and product decisions, and
  open questions with owners.
