# PlantID Android — Manual Test Checklist

Automated tests can't drive other apps (the system photo picker and camera),
so check these by hand on a real device before merging a sprint. Record the
device, Android version, date and result. Use real results only: write
"not tested" rather than leaving a box ticked.

## Sprint 2 — Photo selection and preview

Device: ____________  Android: ____  Build/commit: ________  Tester: ____  Date: ________

| # | Steps | Expected | Result |
|---|---|---|---|
| 2.1 | Home → **Identify a plant** | Sheet shows *Take a photo* and *Choose from photos* | |
| 2.2 | History (empty) → **Identify a plant** | Same sheet opens | |
| 2.3 | Sheet → swipe down / tap outside / Back | Sheet closes, still on the same screen | |
| 2.4 | **Choose from photos** → pick a photo | Brief "Preparing your photo…", then *Check your photo* with the photo, not stretched or cropped | |
| 2.5 | **Choose from photos** → Back out of the picker without picking | Returns to the screen you started on, no error | |
| 2.6 | **Take a photo** → take and confirm a photo | Preview shows it the right way up | |
| 2.7 | **Take a photo** → cancel in the camera app | Returns to the starting screen, no error, no preview | |
| 2.8 | Check permissions: Settings → Apps → PlantID → Permissions | No permissions requested or listed; no permission prompt ever appeared | |
| 2.9 | On preview, **Choose another photo** → pick a different photo | Preview now shows the new photo; Back goes to where you started, not the old preview | |
| 2.10 | On preview, **Choose another photo** → cancel | The original photo is still shown | |
| 2.11 | On preview | **Identify plant** is disabled; the note says identification comes next | |
| 2.12 | Preview → top-bar Back and system Back | Both return to the starting screen; bottom bar visible again | |
| 2.13 | Rotate the device on the preview (if rotation is on) | Same photo still shown | |
| 2.14 | Pick a non-image or damaged file if your picker allows it, or a HEIC photo on Android 8/9 | "Can't open this photo" dialog; OK returns you safely | |
| 2.15 | Camera taken, then background PlantID for a while (or enable Developer options → *Don't keep activities*), then repeat 2.6 | Photo still reaches the preview after returning from the camera | |
| 2.16 | Dark mode and large font size (Settings → Display) | Sheet, dialogs and preview readable, nothing clipped, page scrolls | |
| 2.17 | Gallery after taking a photo in PlantID | The photo is **not** added to the gallery | |

## Sprint 3 — Mock identification flow

Device: ____________  Android: ____  Build/commit: ________  Tester: ____  Date: ________

All outcomes here come from the **demo** identifier. Do not record them as
model results.

| # | Steps | Expected | Result |
|---|---|---|---|
| 3.1 | Choose a photo → preview | **Demo mode** card lists 6 outcomes; *Plant identified* selected by default; **Identify plant** enabled once the photo shows | |
| 3.2 | Demo *Plant identified* → **Identify plant** | "Analyzing your plant…" with the photo, then result: **Demo result** banner, photo + "Demo Plant A" in one card, *Match found*, *About this plant* | |
| 3.3 | Demo *Identified, some details missing* | "Demo Plant B", no scientific name, no *About this plant*, layout intact | |
| 3.4 | Demo *Not confident* | Warning banner "We couldn't confidently identify this plant", **no plant name**, *Try another photo* | |
| 3.5 | Demo *No supported plant* | "No supported plant found", **no plant name**, looks different from 3.4 | |
| 3.6 | Demo *Something goes wrong* → **Try again** | Error panel with Try again and Back to home; Try again shows Analyzing, then the error again (the demo outcome is still *Something goes wrong*) | |
| 3.7 | Demo *Plant identified (slow)* | Loading lasts about 6 s, no fake percentage, then the result | |
| 3.8 | Tap **Identify plant** several times quickly | Only one Analyzing screen; Back returns to the preview once | |
| 3.9 | During Analyzing, press Back | Returns to preview, no result appears later | |
| 3.10 | Result → **Identify another plant** / **Try another photo** → pick a photo | New preview; Back goes to where you started, not the old result | |
| 3.11 | Result → **Back to home** (also when started from History) | Home tab, bottom bar visible | |
| 3.12 | Result → top-bar Back | Returns to the preview with the same photo | |
| 3.13 | Rotate during Analyzing and on the result | Identification is not restarted; same result shown | |
| 3.14 | TalkBack on the result | Banner, status and plant name are read; status isn't conveyed by colour only | |
| 3.15 | Dark mode + large font on each result type | Readable, nothing clipped, page scrolls | |

## Sprint 4 — Care guide

Device: ____________  Android: ____  Build/commit: ________  Tester: ____  Date: ________

Identification results are still **demo** output.

| # | Steps | Expected | Result |
|---|---|---|---|
| 4.1 | Demo *Plant identified* → result | Golden Pothos / *Epipremnum aureum*, **Demo result** banner, **View care guide** button | |
| 4.2 | **View care guide** | Name, scientific name, family; *General guidance* note; Light 500–3,000 lux, Watering about every 7 days (with "not a fixed schedule" note), Temperature 15–30 °C, Humidity around 50 %; Source with CC BY-NC 4.0 | |
| 4.3 | Compare 4.2 with `care_tips.json` for *Epipremnum aureum* | Every number matches the source; nothing extra (no toxicity, no invented advice) | |
| 4.4 | Care guide → Back (top bar and system) | Returns to the same result | |
| 4.5 | Demo *Identified, some details missing* → **View care guide** | "No care guide available"; Back returns to the result | |
| 4.6 | Demo *Not confident* / *No supported plant* / *Something goes wrong* | **No** View care guide button | |
| 4.7 | About tab | Lists 7 supported plants (common + scientific names); *Care information* card credits FloraDB and its licence | |
| 4.8 | Airplane mode, repeat 4.2 | Care guide still loads (offline) | |
| 4.9 | Dark mode + large font on the care guide | Readable, nothing clipped, page scrolls | |
| 4.10 | TalkBack on the care guide | Section titles are read as headings; values and notes are read | |
