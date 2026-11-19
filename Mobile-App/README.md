# PlantID — Android App

Android client for the PlantID research prototype: take or pick a photo of a
plant, see the identification result, read its care guide, and revisit past
identifications. All processing is meant to happen on the device, and the app
does not need an account.

> **Current status: Sprint 3 (mock identification).** You can take or choose a
> photo, preview it and run a **demo** identification that shows each result
> type: identified, uncertain, unsupported and error. All results come from a
> clearly labelled mock: **no real model is connected** (Sprint 7). Care guide
> (Sprint 4) and saved history (Sprint 5) are not built yet.

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
PlantIdApplication.kt      # owns the AppContainer
MainActivity.kt            # sets the theme and PlantIdApp
di/AppContainer.kt         # manual dependency container
domain/identification/     # IdentificationService contract and outcome types
data/image/                # PhotoStore: private copy of the selected photo
data/identification/mock/  # DEMO-only mock identifier, demo scenarios, fictional plants
ui/
├── PlantIdApp.kt          # app shell: bottom bar, navigation host, photo selection
├── navigation/            # type-safe routes, NavHost, navigation helpers
├── theme/                 # colours, typography, shapes, Spacing/Sizes tokens
├── components/            # buttons, cards, status message, loading/empty/error states, branding
├── identify/              # photo source sheet, preview, demo mode picker, identifying + result screens
├── home/  history/  about/   # one package per screen
```

Planned additions: plant and care content (Sprint 4), local history
(Sprint 5), the real on-device identifier (Sprint 7).

### Photos and permissions

PlantID requests **no runtime permissions**. Photos come from the Android
Photo Picker (access to the chosen photo only) or the system camera app, which
writes into PlantID's cache through a FileProvider. The newest photo is kept as
a temporary private copy; nothing is uploaded. See `docs/DECISIONS.md` D12.

### UI conventions

- Use `MaterialTheme.colorScheme` / `typography` / `shapes`, `PlantIDTheme.extendedColors`,
  and the `Spacing` and `Sizes` tokens. Don't hard-code colours or dimensions in screens.
- Build screens from the shared components in `ui/components/`. Every component and
  screen has `@PreviewLightDark` previews.
- All user-facing text goes in `res/values/strings.xml`.
- Icons: Material Symbols Rounded as vector drawables in `res/drawable/ic_*.xml`.
- Screens take state and callbacks only. They never receive the `NavController`.

### Mock identification (demo only)

The UI only talks to the `IdentificationService` interface. Until the real
model is integrated, `MockIdentificationService` is the only implementation:

- It **does not analyse the photo** (it only checks the file exists) and
  returns the outcome chosen in the preview's **Demo mode** card: identified,
  identified with missing details, not confident, no supported plant, error,
  or slow (about 6 s).
- Its plants are **fictional** ("Demo Plant A"). It returns no scores and no
  plant facts.
- Every result it produces shows a **Demo result** banner. Never present these
  results as real predictions or use them as model-accuracy data.

## Documentation

- [`docs/DECISIONS.md`](docs/DECISIONS.md): technical and product decisions, and
  open questions with owners.
- [`docs/MANUAL_TESTS.md`](docs/MANUAL_TESTS.md): on-device checks for flows
  automated tests can't drive (photo picker, camera).
