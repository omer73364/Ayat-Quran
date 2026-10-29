# Ayat Quran identity

An open Quran beneath a pointed arch and a crescent moon. Emerald anchors the identity, ivory keeps the pages clear, and gold highlights the crescent.

## Exports

- `logo.svg` and `logo.png`: Arabic wordmark with the app icon, on transparency. Lettering is outlined so exports do not need an installed font.
- `app-icon.svg`, `app-icon-512.png`, `app-icon-1024.png`: rounded icon for website and promotional use.
- `store-icon-512.png`: opaque, full-bleed square for store submission; rounding is left to the store.
- `mark.svg` and `mark-1024.png`: standalone mark on transparency.
- `preview.png`: identity review sheet.

The Android launcher uses native adaptive foreground and background layers, with a separate monochrome layer for Android 13+ themed icons. The same mark appears on the splash screen and as a white notification icon. Android applies the launcher mask; the foreground does not include baked-in corners.

The website uses `website/assets/app-icon.svg`. The v1.1.1 release includes this identity in the signed APK.

Regenerate with `python3 tools/generate_brand_assets.py`. Requires pycairo and PyGObject with Pango/PangoCairo. The Cairo font and its license are bundled in `website/assets/`.
