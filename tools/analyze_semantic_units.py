#!/usr/bin/env python3
"""Inspect QSU word-level units as whole-ayah context groups; never truncate a unit.

Usage: python3 tools/analyze_semantic_units.py /path/to/QSU.csv
Source: https://doi.org/10.17632/v7hyhk7krd.1 (CC BY-NC 3.0).
This is an analysis tool; it does not update app assets.
"""
import argparse
import collections
import json
from pathlib import Path
from qsu_boundaries import derive_groups

parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('csv_file', type=Path)
args = parser.parse_args()
root = Path(__file__).resolve().parents[1]
verses = json.loads((root / 'app/src/main/assets/ayahs.json').read_text())
groups, unit_count = derive_groups(args.csv_file, verses)
exceptions = [group for group in groups if group['endAyah'] - group['startAyah'] + 1 > 5]
sizes = collections.Counter(group['endAyah'] - group['startAyah'] + 1 for group in groups)
report = [
    '# Short-context source feasibility', '',
    'Source: [QSU v1, DOI 10.17632/v7hyhk7krd.1](https://data.mendeley.com/datasets/v7hyhk7krd/1).',
    'Authors: Abdelraouf Abdelmonem Hawash, Tamer Medhat, Mahmoud Shams, Nora Mahmoud. License: CC BY-NC 3.0.', '',
    'These are calculations from the downloaded annotations, not a new scholarly review.',
    'Method: join adjacent ayahs whenever any source unit spans their boundary. This preserves every linked unit when displaying full ayahs, including units that start or end partway through an ayah.', '',
    f'- Source units inspected: {unit_count:,}.',
    f'- Whole-ayah groups: {len(groups):,}, covering all {len(verses):,} ayahs.',
    f'- Groups containing one ayah: {sizes[1]:,}.',
    f'- Groups containing two to five ayahs: {sum(sizes[n] for n in range(2, 6)):,}.',
    f'- Groups exceeding five ayahs: {len(exceptions)}.', '',
    'Al-Ma’un: 107:1, 107:2–3, 107:4–7. An-Nas: 114:1–6.', '',
    'The app preserves all source-linked groups, including the 46 groups longer than five ayahs. These exceptions were explicitly accepted. The derived dataset retains the source noncommercial license.', '',
    '## Preserved longer groups', '',
]
report += [f"- {group['surah']}:{group['startAyah']}–{group['endAyah']} ({group['endAyah'] - group['startAyah'] + 1} ayahs)" for group in exceptions]
output = root / 'docs/short-context-feasibility.md'
output.write_text('\n'.join(report) + '\n')
print(f'{len(groups)} groups; {len(exceptions)} exceed five ayahs. Report: {output}')
