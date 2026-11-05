#!/usr/bin/env python3
"""Derive context ranges from Tanzil metadata (CC-BY), retaining short surahs in full.

Usage: python3 tools/generate_passages.py /path/to/quran-data.xml
Source: https://tanzil.net/res/text/metadata/quran-data.xml
"""
import argparse
import json
from pathlib import Path
import xml.etree.ElementTree as ET

parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('metadata', type=Path)
args = parser.parse_args()
root = Path(__file__).resolve().parents[1]
metadata = ET.parse(args.metadata).getroot()
starts = [(int(item.attrib['sura']), int(item.attrib['aya'])) for item in metadata.find('rukus')]
ranges = []
for surah in metadata.find('suras'):
    number, count = int(surah.attrib['index']), int(surah.attrib['ayas'])
    points = [aya for chapter, aya in starts if chapter == number]
    if count <= 12:
        points = [1]
    assert points and points[0] == 1 and points == sorted(set(points))
    for index, start in enumerate(points):
        end = points[index + 1] - 1 if index + 1 < len(points) else count
        assert start <= end <= count
        ranges.append(dict(surah=number, startAyah=start, endAyah=end))
covered = [f"{item['surah']}-{aya}" for item in ranges for aya in range(item['startAyah'], item['endAyah'] + 1)]
verses = json.loads((root / 'app/src/main/assets/ayahs.json').read_text())
assert len(covered) == len(set(covered)) == len(verses)
assert set(covered) == {item['id'] for item in verses}
(root / 'app/src/main/assets/passages.json').write_text(json.dumps(ranges, ensure_ascii=False, indent=2) + '\n')
print(f'{len(ranges)} passages cover all {len(covered)} ayahs exactly once.')
