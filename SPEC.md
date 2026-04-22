# HisabBook v1.0 — Product Specification

**Version:** 1.0 | **Date:** 22 Apr 2026 | **Owner:** Rohit

## 1. One-Line Idea

Offline Munim for Bharat: An Android app where Indian shopkeepers speak in any language, and it converts voice to cashbook entries instantly. 100% offline, 100% private.

## 2. Problem Statement

- 30Cr+ micro-merchants in India run on cash + udhar but track on paper/memory.
- Khatabook/OkCredit fail where it matters: basements, ghats, villages = no network.
- Typing English is barrier: Users speak "Ramesh ko 200 doodh udhar", not type it.
- Trust deficit: Won't upload customer khata/Aadhaar to cloud.
- Result: Money leakage, forgotten udhar, zero daily P&L visibility.

## 3. Target User

| Persona | Example | Phone | Pain |
|---|---|---|---|
| Primary | Chaiwala, Kirana, Salon, Tailor, Auto driver | Redmi 9A-12C, 3-4GB RAM, ₹6K-12K | Network dead, can't type, scared of cloud |
| Secondary | Sabzi vendor, Electrician, Small farmer | Same | Forgets udhar, fights with customer |
| Not for | CA, GST business, English-first users | — | They have Tally/Zoho |

**Literacy:** Can read numbers + names. May not read full English sentences.
**Languages v1:** Hindi, Hinglish. **v1.1:** Tamil, Telugu, Kannada, Marathi, Bengali.

## 4. Core Principles

1. **Voice-first:** Mic is hero. Typing is fallback only.
2. **Offline-first:** Works 100% on airplane mode after 1-time 1.3GB download.
3. **5-second rule:** Any screen understandable in 5 sec by low-literacy user.
4. **One-hand use:** All primary actions in bottom 40% thumb zone.
5. **Zero PII to cloud:** No login, no signup, no backend. Data never leaves phone.
6. **Trust > Features:** If feature needs internet/login, delete feature.

## 5. MVP Feature List v1.0

| Feature | Description | Offline? | Priority |
|---|---|---|---|
| F1: Voice Entry | Hold mic, speak, get parsed chips: Person, Amount, Type, Item. Swipe to confirm. | Yes | P0 |
| F2: Auto-categorize | LLM converts "Ramesh ko 200 doodh udhar" → `{type: Udhar Diya, person: Ramesh, amount: 200, item: doodh}` | Yes | P0 |
| F3: Customer Khata | Per-person ledger. Auto calc baki. Swipe entry = "Paisa mil gaya". | Yes | P0 |
| F4: Daily Summary | Cards: Bikri, Kharch, Udhar Diya, Jama, Munafa. Share as WhatsApp image. | Yes | P0 |
| F5: 9 PM Nudge | Notification + TTS: "Aaj ka munafa ₹1400. Sunil ka ₹120 baki." | Yes | P0 |
| F6: Offline Badge | Persistent "Offline ✓" top-right. Red "Phone full" if storage <100MB. | Yes | P0 |
| F7: Encrypted DB | Room + SQLCipher. PIN to open app. | Yes | P0 |
| F8: WhatsApp Export | Full khata → encrypted JSON → share via Intent. User controls backup. | Yes | P1 |
| F9: Multi-language | v1: Hindi. v1.1: +4 South langs. UI + STT + TTS + LLM all switch. | Yes | P1 |
| F10: Vosk Fallback | <4GB RAM phones use Vosk Hindi STT + regex. No LLM. | Yes | P1 |

**Not in v1:** GST, Inventory, UPI link, Multi-user, Cloud sync, Graphs.

## 6. User Flows

### Flow 1: First Entry
1. Install → Onboarding 3 slides → "Shuru Karein"
2. Home empty state: "Aaj ka hisab shuru karo. Mic dabao."
3. Hold mic → "Ramesh ko 200 ka doodh udhar diya" → Release
4. See chips: `[₹200] [Udhar Diya] [Ramesh] [doodh]` → Swipe to confirm
5. Toast: "Save ho gaya ✓". Home shows: Baki Udhar ₹200.

### Flow 2: Payment Received
1. Open Khata → Tap "Ramesh" → See baki ₹200
2. Tap mic → "Ramesh se 200 wapas" → Confirm
3. Ramesh khata = ₹0. Home: Baki Udhar ₹0.

### Flow 3: End of Day
1. 9 PM notification triggers.
2. Open Summary → See cards → Tap "WhatsApp pe Bhejo"
3. Image generated → WhatsApp Intent → Send to wife/partner.

## 7. Screen Specs — 6 Screens

**S1: Onboarding**
Components: ViewPager3, 3 slides, FilledButton. Text from `strings_hi.xml`. Skip = none.

**S2: Home Dashboard**
- TopAppBar: Title "HisabBook" + Badge "Offline ✓".
- Grid: 2x2 ElevatedCard 16dp radius. Values: ₹1,50,000 + 1.5 Lakh subtext.
- FAB: ExtendedFloatingActionButton 72dp, icon mic, text "Bolo", bottom-center.
- Chips: 4 FilterChip horizontal scroll.
- Nav: NavigationBar, 4 items: Ghar, Khata, Hisab, Setting.

**S3: Voice Entry**
- States: Idle, Listening, Processing, Confirm, Error.
- Listening: Lottie pulse + "Sun rahe hain..."
- Confirm: 4 editable FilterChip + Swipe-to-confirm component.
- Error: Icon + "Samajh nahi aaya" + Button "Phir se bolo".

**S4: Customer Khata**
- Top: ElevatedCard with Name, Kul Baki, IconButton call, IconButton share.
- List: LazyColumn, SwipeToDismiss endToStart = "Paisa mil gaya".
- Item: Date | Kaam | Rakam | Baki.

**S5: Daily Summary**
- LazyColumn of ElevatedCard. Munafa card: green bg. Nuksaan: yellow bg, never red.
- Button: FilledButton + WhatsApp icon.

**S6: Settings**
- LazyColumn of ListItem. Language uses RadioGroup. Model status shows download progress.

**Global:** Min text 18sp, min touch 48dp, corner 16dp, contrast 4.5:1. All icons have labels.

## 8. Tech Stack

| Layer | Tech | Reason |
|---|---|---|
| Language | Kotlin 2.0 | Official, best ML Kit support |
| UI | Jetpack Compose 1.7, Material 3 | Fast, declarative, one-hand easy |
| AI | ML Kit GenAI Prompt API + Gemma 4 E2B int4 1.3GB | Audio→JSON, all Indic langs, 1 model |
| ASR Fallback | Vosk 0.3.45, Hindi model 50MB | For 3GB RAM phones, text-only |
| TTS | `android.speech.tts.TextToSpeech` | Free, offline, 12 langs |
| DB | Room 2.6 + SQLCipher 4.5 | Encrypted, ACID, 0 backend |
| DI | Hilt 2.51 | Google standard |
| Async | Kotlin Coroutines + Flow | Native |
| Nav | Navigation-Compose 2.8 | Type-safe |
| Assets | Play Asset Delivery on-demand | Keeps base APK 18MB |
| Backup | Encrypted JSON via `ACTION_SEND` | User controls, no server |

## 9. Data Model v1

```kotlin
@Entity(tableName = "persons")
data class Person(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String, // "Ramesh"
    val phone: String?, // "+919999"
    val type: PersonType, // CUSTOMER, SUPPLIER
    val balancePaise: Long // 20000 = ₹200.00
)

@Entity(tableName = "entries")
data class Entry(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val type: EntryType, // BIKRI, KHARCH, UDHAR_DIYA, UDHAR_WAPAS
    val personId: String?, // nullable for generic kharch
    val amountPaise: Long,
    val item: String, // "doodh", "chai patti"
    val note: String?, // "kal ka"
    val langCode: String, // "hi", "ta"
    val createdAt: Long, // epoch millis
    val source: Source // VOICE_LLM, VOICE_VOSK, MANUAL
)

enum class EntryType { BIKRI, KHARCH, UDHAR_DIYA, UDHAR_WAPAS }
```

**Money = `Long` paise** to avoid float errors. 1 Rupee = 100 paise.

## 10. AI Prompt Spec

**Gemma 4 E2B Prompt:**
```
You are HisabBook parser. Convert Indian shopkeeper speech to JSON.
Rules: Output JSON only. Keys: type, person, amount, item, note.
type enum: [Bikri, Kharch, Udhar Diya, Udhar Wapas].
amount: integer rupees, no paise. If unclear, amount=0.
person: proper noun. If missing, person=null.
item: product/service. note: extra info like "kal ka".
Example: "Ramesh ko 200 ka doodh udhar diya kal ka" → {"type":"Udhar Diya","person":"Ramesh","amount":200,"item":"doodh","note":"kal ka"}
```

Audio: Input = 16kHz PCM bytes. Output = JSON string.

**Vosk Fallback Regex** for `"Ramesh 200 doodh"`:
```
Pattern: (?<person>[A-Za-z]+)?\s*(?<amount>\d+)\s*(?<item>\w+)?
type = if contains "udhar|diya" → UDHAR_DIYA else KHARCH
```

## 11. Non-Functional Requirements

| Metric | Target | How |
|---|---|---|
| Latency | Mic release → chips <2s | Gemma E2B 25 tok/s. JSON ≈ 20 tokens |
| Accuracy | WER <15% in 70dB noise | E2B multimodal trained on noise |
| RAM | Peak <2GB on 4GB phone | int4 + kv_cache int8 + unload on bg |
| Battery | 100 entries = <5% drain | Unload model after 5s idle |
| Thermal | 20 entries no throttle | Set `LLM_PERF_FAST` + cooldown |
| APK Size | Base 18MB | Play Asset Delivery for model |
| Crash Rate | <0.5% | No network calls to crash |
| Privacy | 0 PII leaves device | Audit: no INTERNET permission |

## 12. Permissions

`RECORD_AUDIO`, `VIBRATE`, `POST_NOTIFICATIONS`, `WRITE_EXTERNAL_STORAGE` for export.
**Explicitly NOT requested:** `INTERNET`, `READ_CONTACTS`, `CAMERA`. Trust signal.

## 13. Success Metrics v1

- **D1 Retention:** >40%. If voice works, they come back tomorrow.
- **Entries/User/Day:** >8. Means replacing paper.
- **Crash-free:** >99.5%. Low-end phones must not crash.
- **WER:** <15% on Hindi. Else uninstall.
- **NPS:** "Would you recommend to other dukandaar?" >50.

## 14. Go-To-Market

- **Free:** 100 entries/month, 1 business, Hindi only.
- **Pro ₹199/year:** Unlimited, 3 businesses, all languages, PDF export.
- **CAC:** ₹0. Viral via WhatsApp summary image with "Made with HisabBook" footer.
- **Distribution:** YouTube shorts in Hindi: "Dukaan ka hisab bina internet".

## 15. Risks + Mitigation

| Risk | Mitigation |
|---|---|
| Gemma 4 E2B not on 30% phones | ML Kit checks. If no, fallback Vosk. Don't block install. |
| 1.3GB download kills funnel | Show video demo before download. "Ek baar download, zindagi bhar free". |
| LLM hallucinates amount | Show chip. User must swipe confirm. Never auto-save. |
| Govt ban on AI apps | No server = no ban surface. It's a calculator. |
