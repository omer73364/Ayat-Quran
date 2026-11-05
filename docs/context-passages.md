# Short ayahs with their linked context

All 6,236 ayahs remain available. Opening an ayah displays its source-derived context group, which is usually one ayah, sometimes two to five, and occasionally longer when the source links more verses. The app adds no separate “full context” button and never applies a fixed five-ayah cut.

## Source and derivation

Boundaries are derived from **Quran Semantic Units (QSU), version 1**, published 22 June 2026 by **Abdelraouf Abdelmonem Hawash, Tamer Medhat, Mahmoud Shams, and Nora Mahmoud**.

- [Dataset and methodology](https://data.mendeley.com/datasets/v7hyhk7krd/1)
- DOI: [10.17632/v7hyhk7krd.1](https://doi.org/10.17632/v7hyhk7krd.1)
- License: [CC BY-NC 3.0](https://creativecommons.org/licenses/by-nc/3.0/). The derived boundary data retains this noncommercial license; code licensing is separate. Attribution and the full license text are bundled in the APK and linked in Settings.

The source annotates word-level units using stopping/resuming conventions. The app displays full ayahs. Consequently, whenever any source unit crosses an ayah boundary, those ayahs are joined. If different source units connect different parts of the same ayah to its neighbors, the entire connected group is retained. This avoids breaking a source unit when expanding it to whole ayahs. The existing bundled Quran text is unchanged; QSU contributes boundaries only.

The adaptation produces **4,567 non-overlapping groups**:

| Ayahs per group | Groups |
| --- | ---: |
| 1 | 3,636 |
| 2–5 | 885 |
| More than 5 | 46 |

Examples: Al-Fatihah has groups 1:1, 1:2–4, 1:5, and 1:6–7. Al-Ma'un has 107:1, 107:2–3, and 107:4–7. An-Nas stays 114:1–6. The 46 longer groups are retained by explicit product decision; see [the exception report](short-context-feasibility.md).

This is a reproducible adaptation of the source annotations, not a new scholarly review or a guarantee that each passage can be interpreted without further context.

## Regeneration and validation

Download `QSU.csv` from the dataset's version-1 files, then run:

```sh
python3 tools/generate_passages.py /path/to/QSU.csv
python3 tools/analyze_semantic_units.py /path/to/QSU.csv
python3 -m unittest discover -s tools -p test_qsu_boundaries.py
```

The generator pins the published source SHA-256, requires annotations for every bundled ayah, checks locations and source indices, and verifies that the derived ranges cover every ayah exactly once. It preserves cross-ayah links without truncation. No AI-generated semantic boundaries or short-surah overrides are used.

## App behavior and compatibility

- Home, saved passages, full-screen reading, sharing, reminders, and widgets use the same boundaries and references.
- Tafseer is fetched and cached for every ayah in a group, with verse headings and at most three concurrent requests.
- Every group is eligible for reminders, including longer exceptions. Old scheduled payloads resolve to the group containing their stored ayah ID, including before unlock; a different passage is never substituted because of length.
- Android may visually clip notification/widget previews. Tapping opens the complete mapped group, with no separate context-expansion action.
- Existing bookmark IDs remain saved and resolve through the new mapping. Duplicate bookmarks within a current group appear once. Older canonical IDs may now resolve to a smaller group under the finer boundaries.
- Reading counts each constituent ayah once per day. Existing reading history is retained; a repeated ayah reached through overlapping old history and a current group is not counted again.
