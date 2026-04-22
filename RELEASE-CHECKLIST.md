# HisabBook — Release Checklist

Use this before cutting an internal / beta / production build for Play Store.

## 1. Code readiness

- [ ] `./gradlew :app:testDebugUnitTest` passes (32+ tests)
- [ ] `./gradlew :app:lintDebug` clean (or baselined)
- [ ] No `TODO(...)` left in P0 voice/parser/DB paths
- [ ] `LeakCanary` clean on dev device after 10 min of use
- [ ] Manual smoke test on Redmi 9A or equivalent 2-3 GB phone
- [ ] Manual smoke test on flagship (Pixel 8+/S24+) to verify tier resolver T3

## 2. Secrets + signing

- [ ] Upload key (`hisabbook-release.jks`) stored in 1Password + offline drive
- [ ] `local.properties` has `RELEASE_STORE_PASSWORD`, `RELEASE_KEY_ALIAS`, `RELEASE_KEY_PASSWORD`
- [ ] `play-store-key.json` (Play Console service account) in project root, not committed
- [ ] `.gitignore` covers `*.jks`, `*.keystore`, `local.properties`, `play-store-key.json`
- [ ] `signingConfig` wired for release buildType
- [ ] Play App Signing enabled on Play Console

## 3. App bundle + ABI splits

- [ ] `./gradlew :app:bundleRelease` produces `.aab`
- [ ] `bundletool validate` passes
- [ ] `bundle { abi.enableSplit = true; language.enableSplit = true; density.enableSplit = true }` enabled (already set)
- [ ] Universal APK size <40 MB (without Vosk model)
- [ ] Per-ABI download size <25 MB on arm64-v8a

## 4. Vosk model delivery

Pick ONE path before cutting release:

**(a) Bundle inside base APK** (simple but inflates install ≥42 MB):
- [ ] Place `vosk-model-small-hi-0.22.zip` in `app/src/main/assets/models/`
- [ ] Confirm `ModelInstaller.assetAvailable()` returns true
- [ ] Verify unpack on first launch via `adb shell run-as com.hisabbook.app ls files/vosk/hi-small/`

**(b) Play Asset Delivery — on-demand** (recommended, keeps base AAB small):
- [ ] Create `asset_packs/vosk_hi_small` module
- [ ] `DeliveryType` = `on-demand` (or `fast-follow`)
- [ ] `AssetPackManager.fetch(...)` wired in `ModelInstaller`
- [ ] Fallback UX: "Dikkat aayi, settings se phir se try karein"
- [ ] Confirm zero INTERNET permission in final merged manifest

**(c) Side-load** (internal testing only):
- [ ] SAF picker in Settings → Voice AI → long-press → "Model file chunein"

## 5. Privacy + permissions audit

- [ ] Manifest merged output lists ONLY: RECORD_AUDIO, VIBRATE, POST_NOTIFICATIONS, SCHEDULE_EXACT_ALARM, USE_EXACT_ALARM, RECEIVE_BOOT_COMPLETED
- [ ] **NO INTERNET permission** (`grep -r "android.permission.INTERNET" app/build/outputs/apk/`)
- [ ] Privacy policy hosted at `https://hisabbook.in/privacy` (or equivalent)
- [ ] Play Console Data Safety form:
  - Collects: none
  - Shares: none
  - Encryption in transit: N/A (no transit)
  - Data deletion: `ACTION_DELETE` → app wipes `filesDir/` + SQLCipher DB
- [ ] Offline attestation screenshot for store listing

## 6. Content + store listing

- [ ] Hindi-first store listing (Short + Full description)
- [ ] English secondary description
- [ ] 6 screenshots (Home, Voice Confirm, Khata, Summary, Settings, Offline badge)
- [ ] Feature graphic 1024×500 in HisabBook teal palette
- [ ] Category: Finance → Personal finance
- [ ] Target age: 18+
- [ ] Content rating: Everyone
- [ ] Tags: khata, ledger, accounting, offline, cash book

## 7. Rollout + monitoring

- [ ] First release → Internal testing track, 5 testers
- [ ] D1 install→voice-entry funnel instrumented (local counter, no server)
- [ ] Crash reporting: App Quality Insights via Play Console (zero-PII)
- [ ] `targetSdk` matches current Play requirement (35 as of 2026-04)
- [ ] Staged rollout 5% → 25% → 100% over 1 week for production
- [ ] `versionCode` incremented, `versionName` bumped semver

## 8. Legal

- [ ] Play Developer Program Policies review signed off
- [ ] GST registration completed if charging Pro ₹199/year
- [ ] Terms of Service + Refund policy drafted
- [ ] Intellectual property: Stitch designs attribution confirmed
- [ ] Vosk Apache 2.0 license included in in-app About → Open Source

## 9. Post-launch tripwires

- Uninstall rate > 15% first 48h → roll back
- Crash rate > 0.5% → roll back
- 1-star rate > 10% with "app crashes" → hotfix path
- Any "app is calling home" review → audit permissions again
