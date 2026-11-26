# CI/CD

Workflow: `.github/workflows/android.yml` (setiap push/PR) dan `release.yml` (tag `v*`).

| Job | Isi | Gagal bila |
|---|---|---|
| `harness` | `harness/scripts/appmap_check.py` — peta layar, 120 kartu, aset, ikon, konstanta PRD | peta/aset/deck tidak konsisten |
| `unit-tests` | `./gradlew :app:testDebugUnitTest` (aturan engine + simulasi bot) | test gagal |
| `lint` | `./gradlew :app:lintDebug` (laporan diunggah) | temuan error lint |
| `build` | `assembleDebug`, `assembleRelease`, `bundleRelease` → artefak APK/AAB | kompilasi gagal |

## Menandatangani rilis (opsional)

Tambahkan secret repositori: `KEYSTORE_BASE64` (`base64 -w0 release.jks`), `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`.
Tanpa secret, APK release dihasilkan **tidak bertanda tangan**; debug APK selalu bisa dipasang.

## Lokal

```bash
python3 harness/scripts/appmap_check.py        # harness peta app
./gradlew :app:testDebugUnitTest               # test engine
./gradlew :app:assembleDebug                   # app/build/outputs/apk/debug/app-debug.apk
```
