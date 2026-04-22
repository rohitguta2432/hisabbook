# Vosk Hindi model drop-in

HisabBook T0 tier uses Vosk small-hi-0.22 (~42 MB) for offline Hindi ASR.

## How to add the model to the debug APK

1. Download from Alphacephei:
   https://alphacephei.com/vosk/models/vosk-model-small-hi-0.22.zip
2. Rename to `vosk-model-small-hi-0.22.zip` and drop it in this folder.
3. Rebuild. Base APK will be ~42 MB heavier, and `ModelInstaller` will unpack
   it to `filesDir/vosk/hi-small/` on first voice use.
4. Open **Settings → Voice AI** — first tap triggers install; subsequent launches
   show "Taiyaar ✓".

## Production path (Play Store)

Don't bundle the zip directly — use Play Asset Delivery (on-demand or fast-follow).
This keeps base AAB small and the model is downloaded by the Play service without
requiring the INTERNET permission in the app manifest.

Wiring PAD is not done yet — see Phase 2 plan.

## Verify post-install

```
adb shell run-as com.hisabbook.app ls files/vosk/hi-small/
# expect: am, conf, graph, ivector, README
```
