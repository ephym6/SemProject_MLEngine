# PlantID Android — Decision Log

Record a decision here when the team agrees on it. Don't delete old entries.
If a decision changes, mark the old one *superseded* and add a new entry.

## Decided

| # | Date | Decision | Rationale |
|---|---|---|---|
| D1 | 2026-11-18 | **UI toolkit:** Jetpack Compose + Material 3 only (no XML layouts) | Project was generated with Compose; one coherent UI approach |
| D2 | 2026-11-18 | **Application ID / package:** `io.github.ephym6.plantid` | Replaces the `com.example` placeholder; namespace the team controls |
| D3 | 2026-11-18 | **minSdk 26** (Android 8.0) | `java.time` without desugaring; still broad device coverage |
| D4 | 2026-11-18 | **Git flow:** feature branch per task, reviewed, merged locally into `main`; push only on request | Keeps `main` buildable; matches team review workflow |
| D5 | 2026-11-18 | **Architecture (proposed, applied from Sprint 1):** single Activity, MVVM + unidirectional data flow (`ViewModel` → `StateFlow<UiState>`), Navigation Compose with type-safe routes, manual DI container (no Hilt) | Small app; fewer dependencies; easy for the team to explain |
| D6 | 2026-11-18 | **AI boundary:** UI depends only on an `IdentificationService` interface with outcomes `Identified` / `Uncertain` / `Unsupported` / `Failed`. Mock implementation first; real on-device model only in Sprint 7 | UI-first strategy; model can be swapped without UI rewrite |
| D7 | 2026-11-18 | **Image input:** Android Photo Picker + system camera (`TakePicture` via `FileProvider`) | No storage permission and no CAMERA permission required |

### Planned dependencies (added only in the sprint that needs them)

| Sprint | Dependency | Purpose |
|---|---|---|
| 1 | navigation-compose, kotlinx-serialization plugin, lifecycle-viewmodel-compose / runtime-compose | Navigation and ViewModel state |
| 2 | Coil 3 (coil-compose) | Downsampled image display |
| 3 | kotlinx-coroutines-test | ViewModel / mock service tests |
| 4 | kotlinx-serialization-json | Bundled plant/care data |
| 5 | Room + KSP | Local history |
| 7 | LiteRT (TensorFlow Lite) | On-device inference, after the model contract is agreed |

## Open questions

| # | Question | Notes | Owner | Needed by |
|---|---|---|---|---|
| Q1 | **Supported plant classes** | `species_map.json` lists 7 classes; the training notebook mentions a possible extension to 100 classes. The app scope needs to be fixed | AI lead + research lead | Sprint 4 |
| Q2 | **Uncertain / unsupported decision rule** | The current model has no "unknown" class (7-way softmax always picks one). "Unsupported" needs a threshold or out-of-distribution policy. Same-source test confidence is not calibration | AI lead + research lead | Sprint 7 |
| Q3 | **Model export contract** | No `.tflite` yet. Need: file, label order, input size/format (notebook: 224×224 RGB, `preprocess_input` inside the graph), quantisation, model version | AI lead | Sprint 7 |
| Q4 | **Care content and sources** | `care_tips.json` comes from a FloraDB *sample*. It has toxicity fields (not shown unless a reviewed source is approved; one entry is marked "inferred"), fixed watering-day counts (avoid presenting as rigid schedules), raw lux values (need plain-language wording), and no overview/soil/description | Research lead | Sprint 4 (description needed for Sprint 3 result screen) |
| Q5 | **Care data location in app** | Proposal: the app bundles its own reviewed copy under `app/src/main/assets/`; root JSON files stay untouched | Team | Sprint 4 |
| Q6 | **Image retention** | Keep a downscaled copy only when a result is saved? Delete it with the record? Behaviour when the file is missing? | Team | End of Sprint 3 |
| Q7 | **History scope** | Is history part of the UI Prototype milestone? Individual delete? Clear-all? | Team | Sprint 5 |
| Q8 | **Theme** | Proposal: support light + dark, disable Material You dynamic colour so the botanical brand stays consistent | Team | Sprint 1 |
| Q9 | **Branding assets** | App icon/logo; currently template launcher icon | Team | Sprint 1–6 |
| Q10 | **Mock-mode selection** | Proposal: debug-only scenario picker labelled DEMO; release builds never use mock outcomes silently | Team | Sprint 3 |
| Q11 | **Research test protocol** | Separate model-accuracy evaluation from usability evaluation | Research lead + team | Sprint 8 |
