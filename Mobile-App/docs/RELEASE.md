# PlantID Android — Release Build

How to build the signed app the study uses, and what to check before giving it
to anyone (DECISIONS D44).

## 1. Create the signing key (once)

The key identifies the app. Keep it safe: if it's lost, installed copies can't be
updated with a new build. **Never commit the key or its passwords.** `*.jks`,
`*.keystore` and `keystore.properties` are git-ignored.

From `Mobile-App/`:

```bash
keytool -genkeypair -v -keystore plantid-release.jks -alias plantid \
  -keyalg RSA -keysize 2048 -validity 10000
```

`keytool` comes with the JDK (Android Studio's is in `<Android Studio>/jbr/bin`).
Choose strong passwords when asked, and keep a backup of the `.jks` file and the
passwords somewhere private (not in the repository).

Then create `Mobile-App/keystore.properties`:

```properties
storeFile=plantid-release.jks
storePassword=<the store password>
keyAlias=plantid
keyPassword=<the key password>
```

`storeFile` is relative to `Mobile-App/`. Without this file, release builds still
work but are **unsigned** (they can't be installed).

## 2. Build

From `Mobile-App/` (`gradlew.bat` on Windows):

```bash
./gradlew testDebugUnitTest          # must pass first
./gradlew assembleRelease            # signed APK for testers' phones
./gradlew bundleRelease              # App Bundle (.aab), only for a store upload
```

| Output | Path |
|---|---|
| APK | `app/build/outputs/apk/release/app-release.apk` |
| App Bundle | `app/build/outputs/bundle/release/app-release.aab` |

Release builds are optimised and shrunk with R8. The APK holds LiteRT's native
code for all four CPU types (about 27 MB). An App Bundle lets a store deliver
only one.

**Before each build given to testers,** raise `versionCode` (and `versionName`
if you like) in `app/build.gradle.kts`, so phones accept it as an update.

## 3. Install

- Over USB: `adb install -r app/build/outputs/apk/release/app-release.apk`
- Or send the APK file. Testers allow "Install unknown apps" for the app they
  open it with (for example Files or Drive) when Android asks.

A release build can't be installed over a debug build. Uninstall the debug app
first (this also removes its saved history).

## 4. Check before handing it out

R8 can break things that only show in release builds, so test the **installed
release APK**, not just the debug build:

- [ ] Run `MANUAL_TESTS.md` Sprint 8 on the release APK (identify, care guide,
      save, history, export, About).
- [ ] **Privacy review**: the About privacy text still matches the app:
  - [ ] no permissions requested (`aapt2 dump permissions app-release.apk`
        lists none besides the AndroidX-internal signature permission);
  - [ ] no internet use (no `INTERNET` permission);
  - [ ] photos: temporary copy of the current photo; saved results keep a
        smaller copy without location data; nothing uploaded;
  - [ ] backups and device transfer exclude PlantID's data;
  - [ ] the CSV export happens only when the user chooses it, without photos.
- [ ] `docs/TEST_REPORT.md` updated with the build, devices and results.
