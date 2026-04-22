# HisabBook

Offline Munim for Bharat. Voice-first cashbook for Indian shopkeepers.
Speak in Hindi/Hinglish → parsed entry → encrypted offline khata. No cloud, no signup, no INTERNET permission.

## Status

**v0 UI skeleton — Apr 2026.** All 8 Compose screens ported from Stitch, navigation wired, mock data, app lock via BiometricPrompt.
Voice pipeline (Vosk + rules), Room DB, encrypted storage — next phase.

## Stack

- Kotlin 2.1 + Compose Material 3
- Gradle 8.9, AGP 8.7, JDK 17
- `minSdk` 26, `targetSdk` 35
- Hilt + Navigation-Compose + DataStore
- Biometric (`BiometricPrompt` + DEVICE_CREDENTIAL)
- Planned: Vosk small-hi-0.22 + SQLCipher + Gemma 4 E2B tier (6GB+ RAM)

## Build

```
cd android
./gradlew :app:assembleDebug
```

APK at `android/app/build/outputs/apk/debug/app-debug.apk`.

## Screens

1. Onboarding (3-slide carousel)
2. App Lock (biometric / phone PIN)
3. Home Dashboard
4. Khata List (customers + suppliers tabs)
5. Customer Khata (per-person ledger, swipe-to-settle)
6. Add Person
7. Voice Entry (Listening / Processing / Confirm / Error states)
8. Manual Entry (type fallback)
9. Daily Summary (WhatsApp share)
10. Settings (App Lock toggle, Dark Mode, Backup)

## Docs

- [SPEC.md](SPEC.md) — v1.0 spec
- [SPEC_v1.1.md](SPEC_v1.1.md) — revised tier model (T0-T3, all Android phones)
- [VALIDATION_REPORT.md](VALIDATION_REPORT.md) — Gemma 4 / AICore / Vosk reality check

## License

TBD
