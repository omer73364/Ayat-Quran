# App website preview

A responsive Arabic landing page for Ayat Quran. Built with plain HTML, CSS, and JavaScript; no build or dependencies are required.

From the repository root, start a local preview with:

```sh
python3 -m http.server 4173 --bind 127.0.0.1 --directory website
```

Open http://127.0.0.1:4173. The download buttons serve the verified, production-signed v1.1.1 APK bundled in `assets/`, so downloads do not require GitHub access. Screenshots, the app icon, and the Quran font are also bundled locally.

Public website: https://ayat-quran.goodash35.chatgpt.site/

Create the static deployment directory with `node build.mjs`. Hosting identity is recorded in `.openai/hosting.json`. The build copies only public website files; signing keys and the Android source tree are never included.
