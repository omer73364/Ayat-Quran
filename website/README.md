# App website preview

A responsive Arabic landing page for Ayat Quran. Built with plain HTML, CSS, and JavaScript; no build or dependencies are required.

From the repository root, start a local preview with:

```sh
python3 -m http.server 4173 --bind 127.0.0.1 --directory website
```

Open http://127.0.0.1:4173. The download buttons point to the signed v1.1.1 APK in the public GitHub release. Replacing that release asset updates the download without redeploying the website. Screenshots, the app icon, and the Quran font are also bundled locally.

Public website: https://omer73364.github.io/Ayat-Quran/

Create the static deployment directory with `node build.mjs`. GitHub Pages serves the generated static files from the `gh-pages` branch. Build with `node build.mjs` and publish the contents of `dist/` to that branch. The build copies only public website files; signing keys and the Android source tree are never included.
