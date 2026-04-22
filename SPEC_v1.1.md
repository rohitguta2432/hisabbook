# HisabBook v1.1 — Revised Product Specification (All-Android Tier Model)

**Version:** 1.1 | **Date:** 22 Apr 2026 | **Owner:** Rohit
**Change from v1.0:** Tech stack revised so app installs + works on every Android phone (API 24+, 2GB RAM+). Voice-to-JSON quality **tiers up** on better hardware. No device left behind.

---

## 1. Design Constraint

**Principle:** "If 3Cr merchants own it, HisabBook installs on it."

- **Minimum phone:** Android 7 (API 24), 2 GB RAM, 16 GB storage, no NPU.
  Covers JioPhone Next, Redmi 9A/10A/12C, Samsung M01/A05, all SD 4xx/6xx, all Helio/Unisoc chips.
- **Maximum phone:** flagship (Pixel 9 Pro, Galaxy S25 Ultra, 16GB RAM).
- Same APK. Same UX. Feature gating invisible.

---

## 2. Tier Model — one APK, four quality tiers

App detects device on first launch, pins a **Voice Tier**. User sees no tier label — just the quality they get. Tier upgrade happens silently if a hardware condition unlocks (very rare; mostly fixed at install).

| Tier | Phone class | RAM | Storage | STT | Parser | WER (Hinglish noisy) | Entry latency |
|---|---|---|---|---|---|---|---|
| **T0 — Baseline** | 2 GB phones, JioPhone Next, Helio/Unisoc | ≥2 GB | +80 MB | Vosk small-hi-0.22 (42 MB) + ALang (digit hotword) | Hand-written Kotlin rules/regex | 30–45% | 1.5–3 s |
| **T1 — Standard** | 4 GB phones, SD 6xx/7xx, Redmi Note, Galaxy A | ≥4 GB | +220 MB | Vosk big-hi-0.22 (1.5 GB, optional WiFi download) or T0 STT | Same rules + dictionary | 20–30% | 1.5–3 s |
| **T2 — Smart** | 6–8 GB phones, SD 7 Gen 1/8 Gen 1, Pixel 7, Galaxy A5x | ≥6 GB, ≥8 GB internal free | +3.5 GB (on-demand, WiFi-only) | Vosk small + Gemma 4 E2B **text-only** via MediaPipe LLM Inference | LLM classifier over Vosk transcript | 10–15% | 3–5 s |
| **T3 — Flagship** | 8 GB+ phones with Adreno 7xx+/Mali-G7xx+, Pixel 8+/S24+/Fold6 | ≥8 GB | +3.5 GB | Gemma 4 E2B **audio-in** native via MediaPipe (or Gemini Nano via AICore where available) | LLM direct from audio | <10% | 1–2 s |

**Guardrail:** No tier blocks a user. T0 ships full app, full 6 screens, full khata, full WhatsApp summary. T1–T3 are **accuracy upgrades only**.

### How device detection works

One-shot at first launch + cache:

```kotlin
fun resolveTier(): VoiceTier {
    val ramGb = (Runtime.getRuntime().maxMemory() / 1_073_741_824L).toInt()
    val totalRamGb = ActivityManager.MemoryInfo().let { mi ->
        (ctx.getSystemService(ACTIVITY_SERVICE) as ActivityManager).getMemoryInfo(mi)
        (mi.totalMem / 1_073_741_824L).toInt()
    }
    val freeStorageGb = (Environment.getDataDirectory().freeSpace / 1_073_741_824L).toInt()
    val hasAICore = ctx.packageManager.hasSystemFeature("android.software.ai_core")
    val gpuClass = detectGpuClass()  // Adreno 6/7/8, Mali-G5/7/8 via EGL_VENDOR

    return when {
        hasAICore && totalRamGb >= 8 -> VoiceTier.T3_AICORE
        totalRamGb >= 8 && gpuClass >= GpuClass.HIGH && freeStorageGb >= 8 -> VoiceTier.T3_MEDIAPIPE
        totalRamGb >= 6 && freeStorageGb >= 8 -> VoiceTier.T2
        totalRamGb >= 4 -> VoiceTier.T1
        else -> VoiceTier.T0
    }
}
```

User override: Settings → "Voice AI quality" → `Auto | Lite | Smart`. Lite forces T0 regardless of hardware (battery saver).

---

## 3. Revised Tech Stack

| Layer | Tech | Notes |
|---|---|---|
| Language | Kotlin 2.1 | — |
| UI | Jetpack Compose + Material 3 | unchanged |
| Min Android | API 24 (was 26 in v1.0) | covers ~98% of active India install base |
| Target / compile | 35 | unchanged |
| **STT — all tiers** | Vosk `vosk-model-small-hi-0.22` (42 MB, bundled in APK assets) | Primary offline STT. Works on all 2GB+ phones. |
| **STT — T1 upgrade** | Vosk `vosk-model-hi-0.22` (1.5 GB, PAD on-demand) | Optional, WiFi-only, user-prompted. Lower WER on clean speech. |
| **LLM — T2/T3** | Gemma 4 E2B int4 (3.2 GB) via **MediaPipe LLM Inference** | Google AI Edge runtime. Text-only on T2, audio-in on T3 where GPU delegate supports it. |
| **LLM — T3 alt** | Gemini Nano via AICore | Auto-detected via `PackageManager.hasSystemFeature`. |
| Parser — all tiers | Hand-written rules: Hindi digits, person tokens, Hinglish verb map | **Shipped on every tier.** LLM output is validated against rules; if rules disagree on amount, UI shows both + asks user. |
| TTS | `android.speech.tts.TextToSpeech` | All tiers. Offline voices vary by OEM; fallback to on-screen text. |
| DB | Room 2.6 + SQLCipher 4.5 | Encrypted. |
| DI | Hilt 2.54 | |
| Asset delivery | Play Asset Delivery (PAD) | Base APK 22 MB (includes Vosk small). Extras on-demand: big-Vosk (1.5 GB), Gemma 4 E2B (3.2 GB). |
| Backup | Encrypted JSON via SAF (`ACTION_CREATE_DOCUMENT`) | Fixed from v1.0 (removed `WRITE_EXTERNAL_STORAGE`). |

**Base APK size: 22 MB** (was spec'd 18). 4 MB added for Vosk small asset. Install is one tap, one download, works offline immediately after.

---

## 4. What Each Tier Actually Does On Same Utterance

Utterance: *"Ramesh ko do sau ka doodh udhar diya"*

### T0 (Redmi 9A, 2GB)
1. Vosk small → transcript `"ramesh ko do sau ka dudh udhar diya"` (missed nasal)
2. Rule parser scans:
   - Person = first capitalized/known noun → `Ramesh`
   - Amount = Hindi digit map `do sau → 200`, scan for ₹/rupee tokens
   - Kaam = verb map `udhar diya → UDHAR_DIYA`
   - Item = remainder noun → `dudh` → fuzzy-match dict → `doodh`
3. Chips shown. User confirms or edits.
4. Save. 1.8 s end-to-end.

### T1 (Redmi 12C, 4GB)
Same path, bigger Vosk model (if downloaded). Transcript cleaner → rules hit more. WER drops.

### T2 (Galaxy A55, 6GB)
1. Vosk small → transcript.
2. Gemma 4 E2B text path: `{"type":"UDHAR_DIYA","person":"Ramesh","amount":200,"item":"doodh"}`
3. Rule parser runs in parallel, produces same JSON.
4. If disagree → chips show both for 1 tap. If agree → auto-confirm chips. 3 s.

### T3 (Pixel 9, 12GB)
1. Gemma 4 E2B direct audio → JSON (skip Vosk).
2. Rule parser runs in parallel on background Vosk (silent verify).
3. Chips shown. 1.2 s.

**Important:** T0 and T3 produce the **same UX, same chips, same confirmation flow.** Only latency + accuracy differ.

---

## 5. Feature List — v1.1 (revised priority)

| # | Feature | Min Tier | Priority | Notes |
|---|---|---|---|---|
| F1 | Voice entry with chip confirmation | T0 | P0 | Core. Vosk+rules always works. |
| F2 | Hindi digit parser (do sau, teen hazaar) | T0 | P0 | Standalone library, no deps. |
| F3 | Hinglish verb map (udhar diya/liya, bech diya, kharch) | T0 | P0 | 40 verbs covers 95%. |
| F4 | Customer khata with swipe-to-settle | T0 | P0 | |
| F5 | Daily summary + WhatsApp share image | T0 | P0 | `FileProvider` + `ACTION_SEND`. |
| F6 | 9 PM nudge | T0 | P0 | `SCHEDULE_EXACT_ALARM` + `BOOT_COMPLETED`. |
| F7 | Encrypted DB (SQLCipher) + PIN | T0 | P0 | PIN recovery via printed QR (export key on setup). |
| F8 | Encrypted backup/restore via SAF | T0 | P0 | Restore is P0 — spec v1.0 missed. |
| F9 | Manual entry (type) fallback | T0 | P0 | One form, 4 fields. For mic-denied + low-WER cases. |
| F10 | Edit/delete entry | T0 | P0 | Long-press → edit/delete. v1.0 missed. |
| F11 | Supplier type (UDHAR_LIYA, UDHAR_CHUKAYA) | T0 | P0 | Shop owes sabzi-wala too. |
| F12 | Person disambiguation (3 Rameshes) | T0 | P0 | Phone-number link + "last seen" subtitle. |
| F13 | Big Vosk model download (accuracy boost) | T1 | P1 | WiFi-only, user-prompted. |
| F14 | Gemma text classifier tier | T2 | P1 | Auto if RAM ≥6GB + free storage. |
| F15 | Gemma audio-native tier | T3 | P1 | Auto if flagship. |
| F16 | Multi-language (Tamil, Telugu, Kannada, Marathi, Bengali) | T0 | P2 | Vosk has all; rules need translation. v1.2. |

---

## 6. Storage Budget

| Tier | Base APK | On-device always | Optional downloads | Total on-device |
|---|---|---|---|---|
| T0 | 22 MB | 22 MB | — | 22 MB |
| T1 | 22 MB | 22 MB | Vosk big 1.5 GB | 1.52 GB |
| T2 | 22 MB | 22 MB | Gemma 4 E2B 3.2 GB | 3.22 GB |
| T3 | 22 MB | 22 MB | Gemma 4 E2B 3.2 GB | 3.22 GB (or 0 extra if AICore-hosted) |

**T0 user never sees a download prompt.** T1–T3 get a one-time dialog: *"Aawaz ki quality badhao? 1.5 GB download, WiFi par."* Default = No.

---

## 7. Non-Functional — revised

| Metric | T0 | T1 | T2 | T3 |
|---|---|---|---|---|
| Mic release → chips | 1.5–3 s | 1.5–3 s | 3–5 s | 1–2 s |
| Peak RAM during entry | 350 MB | 1.8 GB | 4 GB | 5 GB |
| WER (Hinglish noisy) | 30–45% | 20–30% | 10–15% | <10% |
| Battery / 100 entries | 2% | 3% | 6% | 4% |
| APK base size | 22 MB | 22 MB | 22 MB | 22 MB |
| Crash-free | >99.5% | >99.5% | >99% | >99% |

---

## 8. Risks — revised

| Risk | Mitigation |
|---|---|
| T0 WER 30-45% makes voice feel broken | Chips are editable. Manual entry (F9) is always one tap away. Onboarding sets expectation: "Aap chahen to type bhi kar sakte hain." |
| Gemma 4 E2B needs 3.2 GB — most target phones lack storage | Only T2/T3 tiers prompt download. T0 never sees it. No funnel damage. |
| Tier detection mis-classifies → OOM | Each tier runs a 5-second benchmark on first use; if crash, demote tier and log. Never prompt user. |
| "Ek baar download" model-download dialog feels scary | Show size + "WiFi par hoga, phone par hi rahega, internet band rakhenge" copy. |
| Rule parser false-positive on amount | LLM (T2+) validates; if mismatch, show both, user picks. On T0, chips are always editable before save. |
| AICore absent on flagship → falls to T2 | Fine. Gemma via MediaPipe is T3-quality too. |

---

## 9. Open Decisions

- **D1:** Default for T1 big-Vosk prompt — Auto / Ask / Never? (recommend Ask on first WiFi event.)
- **D2:** T2/T3 Gemma prompt — Settings-only (opt-in) or first-time nudge? (recommend Settings-only for v1.1, upsell in v1.2.)
- **D3:** Pro paywall — gate on features or language? (recommend: T0 always free, Pro unlocks multi-business + PDF + extra languages.)

---

## 10. Build status

Scaffold [android/](android/) already compiles for `minSdk 26`. Drop to `minSdk 24` + wire Vosk + rule parser in next phase.

**Phase 1 next (all T0, all offline):**
1. Bundle Vosk `small-hi-0.22` in `app/src/main/assets`
2. `AudioRecord` → PCM → Vosk → `Flow<String>` transcript
3. Kotlin rule parser module (`parser/RulesParser.kt`) — 40 verbs, Hindi digit map
4. Wire real VoiceEntryScreen to pipeline
5. Room DAO + encrypted DB
6. Manual entry form
7. Field test on 1 Redmi 9A

Phase 1 gives full T0 MVP. Phases 2-4 add T1/T2/T3 tiers without touching T0 code.
