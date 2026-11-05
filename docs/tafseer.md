# Ayah tafseer

- Reminders include **التفسير** next to **قرأت**. It opens the exact notified ayah in the full-screen reader, without marking it as read.
- Home and saved ayah cards have a **التفسير** button to expand or hide the explanation.
- Full-screen reading always includes the ayah and **التفسير الميسر** in one scrollable view, with a source link and fixed reading controls.
- The reader extends its background behind transparent system bars and selects light/dark system icons from the app theme.

## Source and offline behavior

The app fetches Al-Muyassar over HTTPS from Quran.com's public content endpoint:

`https://api.quran.com/api/v4/tafsirs/ar-tafsir-muyassar/by_ayah/{surah}:{ayah}`

[API documentation](https://api-docs.quran.com/docs/content_apis_versioned/4.0.0/list-ayah-tafsirs/).

The response must identify `ar-tafsir-muyassar` and contain the requested verse key before it is displayed or cached. Formatting HTML is converted to readable text. Successfully fetched explanations are stored privately on the device and remain available offline. A missing connection or service failure leaves the ayah readable and offers **إعادة المحاولة**. No API secret is embedded in the APK.

Instrumentation tests check Arabic/HTML parsing, rejection of mismatched content, and the notification action opening the correct ayah and reader.
