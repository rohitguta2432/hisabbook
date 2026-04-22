# HisabBook v1.0 — B + C Validation Report

**Date:** 2026-04-22
**Scope:** Track B (tech-risk spike) + Track C (Compose skeleton)

---

## B. Tech-Risk Validation — Spec Claims vs Reality

| # | Spec Claim | Status | Reality |
|---|---|---|---|
| 1 | ML Kit GenAI Prompt API does audio → JSON | **WRONG** | Prompt API is **text + image** multimodal, not audio. Audio only via separate `SpeechRecognition` GenAI API (text-out). No single-call audio→JSON path. |
| 2 | Gemma **4** E2B (int4, 1.3GB) | **WRONG (name)** | Correct model is **Gemma 3n E2B** (mobile-efficient, PLE caching, audio/vision/text). "Gemma 4" exists in preview banner but has no released E2B on-device SKU. |
| 3 | AICore hosts Gemma on Pixel/Galaxy flagships | **WRONG (mix-up)** | AICore = **Gemini Nano only**. Gemma ships via **MediaPipe LLM Inference / AI Edge**, app-bundled or side-loaded. Different runtime. |
| 4 | AICore reaches target phones (Redmi 9A/12C) | **CONFIRMED (excluded)** | AICore = Tensor G3+ / SD 8 Gen 3 class. Redmi 9A (Helio G25) and Redmi 12C (Helio G85) → **never eligible**. Coverage of sub-₹10K Indian cohort ≈ **0%**. |
| 5 | 25 tok/s for 2B int4 on SD 439/680 | **WRONG** | MediaPipe benchmarks: ~20-30 tok/s only on SD 8 Gen 2/3. SD 7 Gen 1 drops to 4-8 tok/s. SD 439/680 with no NPU → **<2 tok/s**, likely OOM on 3-4GB RAM. |
| 6 | Vosk 0.3.45 Hindi 50MB | **PARTIALLY WRONG** | Latest = `vosk-model-small-hi-0.22` (42MB). Published WER 20-25% on clean read speech. Real-world WER on noisy code-mix shopkeeper speech: **30-45%**. |
| 7 | MediaPipe LLM audio input | **PARTIALLY WRONG** | Audio listed as Gemma 3n capability but **not exposed in Android LLM Inference task API** yet. CPU-only/experimental. |

**Verdict:** On-device LLM audio-in path on Redmi-class phones = **not shippable** as specified. Eligible cohort on stated target hardware ≈ 0%.

### Recommended Architecture (replaces Spec §8 AI row + F10)

1. **Tier 0 — All phones (3GB+ RAM, offline-first):**
   - **STT:** Vosk `small-hi-0.22` (42MB, app-bundled). RAM ~300MB runtime.
   - **Parser:** hand-written Kotlin rules/regex over Hindi digits, amount/person/item heuristics. No LLM.
   - Covers 100% of install base. No download friction.

2. **Tier 1 — Flagship (AICore-eligible, Pixel 8+/S24+/S25/Fold6):**
   - `FEATURE_AICORE` + RAM ≥ 8GB + SoC allowlist.
   - Gemini Nano via AICore for harder utterances (multi-clause, code-mix).
   - Gated behind a single feature flag.

3. **Tier 2 — Cloud fallback (opt-in only, violates Principle 5 unless explicitly user-initiated):**
   - Gemini 2.5 Flash-Lite on ambiguous/failed parses.
   - Send **transcript text only**, never audio.
   - Off by default. User toggles in Settings with clear consent copy.

4. **Telemetry (local-only):** record SoC/RAM buckets + parser success rate to size future Tier-1 rollout. Zero PII.

### Other Spec Corrections Surfaced

- **F8 `WRITE_EXTERNAL_STORAGE`** — deprecated since API 30. Use SAF / `ACTION_CREATE_DOCUMENT`. Already removed from manifest in C scaffold.
- **F5 9 PM nudge** — need `SCHEDULE_EXACT_ALARM` + `RECEIVE_BOOT_COMPLETED`. Added to manifest.
- **EntryType** — spec missed `UDHAR_LIYA` / `UDHAR_CHUKAYA` (shop owes supplier). Added to `Models.kt`.
- **D1 retention >40%** — industry median 20-25% on low-end Android. Recommend tracking **D7** as primary.
- **Backup restore flow** — export defined, import absent. Phone loss = data loss without it.

---

## C. Compose Skeleton — What Was Built

Buildable Android Studio project at `hisabbook/android/`.

### Stack

- Kotlin 2.1, Compose BOM 2024.12, Material3
- AGP 8.7.3, Gradle 8.9, JDK 17
- Hilt 2.54 + KSP, Navigation-Compose 2.8.5
- Room 2.6 + DataStore 1.1 (wired, not yet used)
- `compileSdk 35`, `minSdk 26`, `targetSdk 35`

### Files

```
android/
  settings.gradle.kts
  build.gradle.kts
  gradle.properties
  gradlew + gradle/wrapper/
  local.properties (sdk.dir)
  app/
    build.gradle.kts
    proguard-rules.pro
    src/main/
      AndroidManifest.xml          (no INTERNET, record_audio, alarms)
      java/com/hisabbook/app/
        HisabBookApplication.kt    (@HiltAndroidApp)
        MainActivity.kt
        data/
          model/Models.kt          (Person, Entry, EntryType+LIYA/CHUKAYA, toRupeesString)
          mock/MockData.kt         (Ramesh + 3 entries)
        ui/
          theme/Color.kt           (full Bharat Ledger palette)
          theme/Type.kt            (18sp body min, Noto Sans)
          theme/Theme.kt
          components/OfflineBadge.kt
          navigation/HisabBookNavGraph.kt  (5 routes + bottom nav)
          screens/
            onboarding/OnboardingScreen.kt
            home/HomeScreen.kt
            voice/VoiceEntryScreen.kt
            khata/CustomerKhataScreen.kt
            summary/DailySummaryScreen.kt
            settings/SettingsScreen.kt
      res/
        values/strings.xml         (all Hindi/Hinglish strings)
        values/colors.xml
        values/themes.xml
        drawable/ic_launcher_foreground.xml
        mipmap-anydpi-v26/ic_launcher.xml
```

### Build Status

```
./gradlew :app:assembleDebug
BUILD SUCCESSFUL in 1m 11s
app-debug.apk  19 MB
```

Warnings (non-blocking):
- `Icons.Filled.MenuBook` deprecated → use `Icons.AutoMirrored.Filled.MenuBook` (fixed in Nav, still 1 usage in Onboarding).
- `window.statusBarColor` deprecated — minor, Theme.kt.

### What's wired

- All 6 Stitch screens as Compose.
- Bottom nav (Ghar / Khata / Hisab / Setting) on 4 main tabs.
- Mic FAB ("Bolo") on Home + Khata → navigates to VoiceEntry.
- Onboarding → Home with popUpTo clear.
- Close/Confirm on VoiceEntry → popBack.
- Mock data populates Khata screen.

### What's NOT wired (v0 stubs — next phases)

- No Room DAO yet (models are POJOs).
- No ViewModel per screen (next phase).
- Voice FAB is mock — no mic permission ask, no recording, no Vosk.
- WhatsApp share button does nothing.
- Settings toggles are local-state only.
- PIN / biometric on app open — not implemented.
- No Hindi digit parser.

---

## Decision Point Before Phase 1

Before writing any more code, pick:

**D1. Voice path commitment (biggest lever).**
- (a) **Vosk + rules only** — guaranteed offline, works on all phones, slower dev but ships. Recommend.
- (b) **Vosk + cloud (opt-in)** — better accuracy on hard utterances, needs consent flow, breaks "no INTERNET" moat unless gated.
- (c) **AICore tier for flagships only** — future-safe, but flagship users aren't your ICP.

**D2. Retention metric.**
- (a) D1 >40% as primary (aspirational).
- (b) D7 >25% as primary + D1 as secondary (realistic).

**D3. First field test.**
- (a) Skeleton as-is to 3 shopkeepers this week — pure UI feedback.
- (b) Wait until Vosk wired (~5 days) — test the actual value prop.

My recommendation: **D1=a, D2=b, D3=b**. Don't test voice UX without voice working — fake mic = wasted trip.

---

## Files saved

- `/home/t0266li/Documents/hisabbook/SPEC.md` — full spec
- `/home/t0266li/Documents/hisabbook/VALIDATION_REPORT.md` — this file
- `/home/t0266li/Documents/hisabbook/android/` — buildable Compose project
- `/home/t0266li/Documents/hisabbook/android/app/build/outputs/apk/debug/app-debug.apk` — 19MB APK
