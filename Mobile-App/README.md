# PlantID — Android App

Android client for the PlantID research prototype: take or pick a photo of a
plant, see the identification result, read its care guide, and revisit past
identifications. All processing is meant to happen on the device, and the app
does not need an account.

> **Current status: Sprint 6 (hardening) — interface ready for model
> integration.** The full flow works with a clearly labelled **demo**
> identifier: photo → preview → identify → result → care guide → save → History.
> **No real model is connected yet** (Sprint 7, see `docs/AI_INTEGRATION.md`).
> Test results and known issues: `docs/TEST_REPORT.md`.

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
domain/plant/              # Plant, PlantRepository, CareRecommendations (rule-based care tips)
data/image/                # PhotoStore: private copy of the selected photo
data/plant/                # reads the bundled assets/plants.json
domain/history/            # HistoryRecord, HistoryRepository
data/history/              # Room database + downscaled photo copies for saved results
data/identification/mock/  # DEMO-only mock identifier, demo scenarios, fictional plants
ui/
├── PlantIdApp.kt          # app shell: bottom bar, navigation host, photo selection
├── navigation/            # type-safe routes, NavHost, navigation helpers
├── theme/                 # colours, typography, shapes, Spacing/Sizes tokens
├── components/            # buttons, cards, status message, loading/empty/error states, branding
├── identify/              # photo source sheet, preview, demo mode picker, identifying + result screens
├── care/                  # care guide screen
├── home/  history/  about/   # one package per screen
```

Planned additions: the real on-device identifier (Sprint 7).

### Saved history

- Saved results live in a Room database (`plantid.db`, schema in
  `app/schemas/`). Each record is a snapshot of what was shown, so old records
  don't change when plant data does.
- Each saved result keeps a downscaled photo copy (≤ 1024 px, no EXIF/location)
  in private storage. It's deleted with the record or by Clear history.
- PlantID's data is excluded from Android backups and device transfer.
- **Database changes** need a version bump and a migration in
  `PlantIdDatabase`. Commit the new schema JSON.
- **Device tests clear the debug app's history** (`HistoryFlowTest`).

### Plant data and care guides

- `app/src/main/assets/plants.json` holds the 7 supported plants (names,
  family, classifier class label, care values) and the source and licence. It
  is **generated**: don't edit it by hand.
- After the AI team changes `species_map.json` or `care_tips.json` (for example,
  adding species), regenerate it from the repository root and commit the result:

  ```bash
  python Mobile-App/tools/sync_plant_data.py
  ```

  `PlantDataSyncTest` (unit tests) fails if the asset is out of date.
- Care tips are **rule-based**: `CareRecommendations` keeps only plausible
  values that exist in the source, and the screen words them with fixed
  templates in `strings.xml`. No advice is generated or invented, and toxicity
  data is never copied into the app (DECISIONS D20).
- Source: FloraDB houseplant care sample, **CC BY-NC 4.0** (non-commercial,
  attribution required). It is credited in the app.

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
- *Identified* always returns the same supported plant (Golden Pothos) from the
  plant data, so the care guide can be shown. *Missing details* returns a
  fictional plant ("Demo Plant B") that has no care guide. It returns no scores.
- Every result it produces shows a **Demo result** banner. Never present these
  results as real predictions or use them as model-accuracy data.

## Documentation

- [`docs/DECISIONS.md`](docs/DECISIONS.md): technical and product decisions, and
  open questions with owners.
- [`docs/MANUAL_TESTS.md`](docs/MANUAL_TESTS.md): on-device checks for flows
  automated tests can't drive (photo picker, camera).
- [`docs/TEST_REPORT.md`](docs/TEST_REPORT.md): Sprint 6 test results, fixes and
  known issues.
- [`docs/AI_INTEGRATION.md`](docs/AI_INTEGRATION.md): what the trained model
  must provide and where it plugs in (Sprint 7).
