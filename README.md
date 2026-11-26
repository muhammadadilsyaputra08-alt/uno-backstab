# BACKSTAB! — Android

*Trust Nobody. Play Dirty. Win Anyway.* Game kartu kompetitif 2–6 pemain (First to Zero / Last Standing), implementasi PRD v2.3.

- Kotlin + Jetpack Compose, landscape, tampilan meja 2.5D (aset 2D) mengikuti referensi di `harness/reference_screens/`
- Engine aturan murni Kotlin (`engine/`), diuji di JVM tanpa emulator
- 120 kartu + ikon + ikon launcher dari paket aset (lihat `app/src/main/assets`, `app/src/main/res`)

## Struktur

```
app/src/main/java/com/backstab/game/
  engine/   Models, Deck(120), GameEngine(aturan+validasi), Bot
  ui/       Theme(2400×1080 stage), Components, GameViewModel, GameScreen, Screens
  data/     Prefs, Feedback(haptik/sfx)
harness/    app_map.json, appmap_check.py, card_manifest.json, reference_screens/
docs/       PRD.md, RULES_DECISIONS.md, APP_MAP.md (dihasilkan), CICD.md
.github/    workflows (CI + release), dependabot
```

## Build & uji

```bash
python3 harness/scripts/appmap_check.py   # harness peta app
./gradlew :app:testDebugUnitTest          # aturan + simulasi bot
./gradlew :app:assembleDebug              # APK debug
```
Atau push ke GitHub: Actions membangun APK/AAB dan mengunggahnya sebagai artefak (lihat `docs/CICD.md`).

## Catatan
- Keputusan aturan PRD §18 & asumsi: `docs/RULES_DECISIONS.md`.
- Musik: toggle tersedia, tetapi paket aset tidak berisi audio; efek suara memakai `ToneGenerator`.
- Aset kartu/ikon & font (Poppins, Inter — lisensi OFL) dibawa apa adanya dari paket yang diberikan.
