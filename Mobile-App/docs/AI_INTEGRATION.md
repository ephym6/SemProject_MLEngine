# PlantID — AI Integration Boundary (for Sprint 7)

What the Android app needs before the trained model can replace the demo
identifier, and exactly where it plugs in. Written at the end of Sprint 6.

> **Status:** the app uses `MockIdentificationService` (demo only). No model is
> integrated. Anything below marked *(to confirm)* comes from reading
> `Model_Base.ipynb` and must be confirmed by the AI lead with the exported
> model. It is not an established fact about the final model.

## 1. The contract the model must fulfil

The whole UI depends on one interface:

```kotlin
interface IdentificationService {
    val modelInfo: ModelInfo                       // name, version, isDemo = false
    suspend fun identify(photo: File): IdentificationOutcome
}
```

`IdentificationOutcome` is one of:

| Outcome | Meaning | UI |
|---|---|---|
| `Identified(model, classId, plant, scores)` | A supported class passed the agreed decision rule | Photo + name, care guide, save |
| `Uncertain(model, scores)` | The model returned something, but the rule says it isn't confident | Warning, **no species name** |
| `Unsupported(model)` | No acceptable supported class (other plant, not a plant) | Info, no plant |
| `Failed(model, reason)` | Couldn't process or infer (`PhotoUnavailable`, `ModelUnavailable`, `ProcessingFailed`, `TimedOut`) | Error + Try again |

Rules for the real implementation:

- **Main-safe.** Decode, preprocess and run inference off the main thread
  (`Dispatchers.Default`), and load the model once.
- **Never throw, never fake.** Return `Failed` on any problem. Never fall back to
  a successful-looking result.
- **No interpretation in the UI.** The UI shows no confidence numbers. `scores`
  are carried for diagnostics and evaluation only, until Q2 decides otherwise.
- **`modelInfo.isDemo = false`** and a real `version`, so results and saved
  history record which model produced them.

## 2. Mapping model output to plants

The bundled catalog (`app/src/main/assets/plants.json`, generated from
`species_map.json` + `care_tips.json`) is keyed by **class label**
(e.g. `Epipremnum_aureum`):

```
model output index i  →  class label  →  PlantCatalog.byClassLabel(label)  →  PlantSummary
```

- *(to confirm)* The notebook builds labels as
  `class_to_idx = {c: i for i, c in enumerate(sorted(species_map.keys()))}`, so
  output index *i* is the *i*-th class label in **sorted order**. Ship an
  explicit `labels.txt` (one label per line, in output order) with the model
  instead of relying on this.
- A label with no catalog entry means the app data and model are out of sync.
  Treat it as `Failed(ProcessingFailed)` and fix it by rerunning
  `python Mobile-App/tools/sync_plant_data.py`. Never show such a label as a
  plant.
- **More species later:** retrain, update `species_map.json` / `care_tips.json`,
  rerun the sync script, and ship the new model + labels. No app code changes.

## 3. What the AI lead must deliver (spec §8.2)

| # | Item | Notes |
|---|---|---|
| 1 | Approved model choice | Must match what the research report evaluates (spec §8.3) |
| 2 | Mobile model file | e.g. `plantid.tflite` (LiteRT / TensorFlow Lite) |
| 3 | `labels.txt` | Class labels in output order |
| 4 | Input spec | *(to confirm)* 224 × 224 × 3 RGB. The notebook applies `mobilenet_v2.preprocess_input` **inside** the model graph, so the app would feed raw 0–255 float values. Confirm after export (and whether the model is quantised) |
| 5 | Output spec | *(to confirm)* one softmax vector over the classes. Note the model has **no "unknown" class**: softmax always picks one |
| 6 | Decision rule (Q2) | How scores map to Identified / Uncertain / Unsupported, with research justification. Not invented by the app team |
| 7 | Model version string | Stored with every saved result |
| 8 | Test images with expected labels | Include photos from a **different source** than training (the notebook notes same-source data only) |
| 9 | Licence of training data | Check before distribution |

## 4. Where the code goes (Sprint 7)

| Piece | Location |
|---|---|
| Model + labels | `app/src/main/assets/model/` |
| Preprocessing (decode with EXIF rotation, resize, RGB) | `data/identification/model/` (never in UI composables) |
| `ModelIdentificationService : IdentificationService` | `data/identification/model/` |
| Decision rule (scores → outcome) | Pure Kotlin, unit-tested against the agreed thresholds |
| Wiring | `di/AppContainer.kt`: `identificationService` and `demoScenarios` |

The swap in `AppContainer` is the only app change outside the new package. That
is guarded by `ArchitectureTest`: only `di/` may reference concrete
implementations such as the mock.

After the swap:

- `demoScenarios` becomes `null`, so the Demo mode picker, demo banners and
  Home's demo note disappear automatically.
- Decide whether debug builds keep a way to select the mock (spec: "preserve a
  development/test way to exercise mock outcomes where useful"). Release builds
  must not use it.
- Set the identification timeout (`IdentificationViewModel.TIMEOUT_MILLIS`, now
  a provisional 30 s) from **measured** on-device inference time.
- Check any new runtime dependency against the project's Kotlin 2.2.10
  compiler (see DECISIONS D13/D28).

## 5. How it will be tested

- JVM: decision rule at and around each threshold, output → label → plant
  mapping, unknown label → `Failed`, preprocessing dimensions.
- Device: the existing UI and flow tests run unchanged against the contract.
  Add a test that a known test image reaches the expected label.
- Measure and record inference latency and memory on the team's phones.
- Keep model-accuracy results separate from usability results (spec §8).
