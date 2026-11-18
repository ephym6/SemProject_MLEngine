# SemProject_MLEngine — PlantID

PlantID is an academic research project that looks at how well a lightweight
on-device classifier can identify a small set of common indoor plants from
smartphone photos, and how usable the care guidance that follows is.

This repository holds both parts of the project:

| Path | Contents |
|---|---|
| `Model_Base.ipynb` | Model training notebook (MobileNetV2 transfer learning on the selected plant classes) |
| `FloraDB_Exploration.ipynb` | Exploration of the FloraDB house-plant care dataset |
| `species_map.json` | Mapping from training class folders to scientific names |
| `care_tips.json` | Care data extracted for the selected species |
| `Mobile-App/` | PlantID Android application (Kotlin, Jetpack Compose) |

## Android app

The Android app is in [`Mobile-App/`](Mobile-App/README.md). Open that folder,
not the repository root, in Android Studio. See the Mobile-App README for setup,
build and test instructions.

The app is being built UI-first with mock identification data. The trained model
has **not** been integrated into the app yet.

## Status

Research prototype under active development. It only supports a limited set of
plant species and is not a general plant identifier.
