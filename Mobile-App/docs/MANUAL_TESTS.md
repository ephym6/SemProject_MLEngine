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
