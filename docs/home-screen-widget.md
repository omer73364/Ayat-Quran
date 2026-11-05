# Home-screen ayah widget

After installing a build with widget support, long-press an empty area on the Android home screen, choose **Widgets**, and add **مقطع من القرآن** under Ayat Quran.

- Tap the widget to open the displayed passage in the reader.
- Tap the refresh icon to choose another passage without changing reading progress.
- Resize the widget horizontally or vertically. Longer verses may be shortened in the widget; tap to read the complete verse.
- Each widget keeps its own verse across process restarts. Android requests a daily update; battery restrictions can delay it.
- Widget colors follow the phone's light/dark mode. All verses are available offline.

The provider uses native RemoteViews and WorkManager, with cached content shown immediately. Tests cover independent saved state, resizing/inflation, and a real AppWidgetHost binding, refresh, and verse-opening flow on Android 10+.
