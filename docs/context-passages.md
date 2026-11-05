# Reading ayahs in context

Selection now uses passages, rather than isolated randomly selected ayahs. Opening any ayah within a passage opens that same complete range. Each ayah retains its number and original text.

## Boundary source

The baseline is Tanzil's existing rukūʿ section metadata, version 1.0. Attribution: **Tanzil Project, © 2008–2009 Tanzil.info**; the metadata declares the **CC-BY** license.

- [Metadata documentation](https://tanzil.net/docs/Quran_Metadata)
- [Original XML](https://tanzil.net/res/text/metadata/quran-data.xml)

The app derives `assets/passages.json` from those section starts and surah lengths. As an app policy, surahs containing 12 ayahs or fewer are kept in full. These are documented baseline context sections, **not a claim of scholarly review or guaranteed semantic independence**. No AI-generated boundaries are used.

The result is 552 non-overlapping passages covering all 6,236 ayahs exactly once. For example, selecting any ayah of Al-Ma'un shows the full surah, 107:1–7, including the description following 107:4.

To regenerate, download the original XML and run:

```sh
python3 tools/generate_passages.py /path/to/quran-data.xml
```

The generator checks complete coverage against the app's Quran text. Future reviewed boundary changes should retain their source and reviewer information and pass the same coverage checks.

## App behavior and compatibility

- Home, saved passages, full-screen reading, sharing, and widget references show the passage's range.
- Tafseer is fetched and cached for **every ayah** in the passage, with verse headings and at most three concurrent requests.
- Reminders choose whole passages of at most 12 ayahs and 1,200 characters. They never split a longer section to fit. Old scheduled payloads are resolved through the new catalog, including before unlock; an oversized legacy selection is replaced with a deterministic short passage.
- The widget explicitly labels its text as a preview and opens the full passage. Cached legacy verse IDs resolve to their full passage.
- Old bookmarks remain usable. Multiple saved ayahs within one passage appear once; removing the passage removes all its saved constituent IDs.
- Reading counts each constituent ayah once per day, including when the same passage is read from both a reminder and the app. Existing reading history is retained.

Tests check full coverage, contiguous ranges, Al-Ma'un context, reminder size limits, old payloads, grouped serialization, widget display, bookmarks, and reading counts.
