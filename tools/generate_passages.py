#!/usr/bin/env python3
"""Derive whole-ayah context groups from QSU v1 without splitting a semantic unit.

Usage: python3 tools/generate_passages.py /path/to/QSU.csv
Source: https://doi.org/10.17632/v7hyhk7krd.1 (CC BY-NC 3.0).
"""
import argparse
import hashlib
import json
from pathlib import Path
from qsu_boundaries import derive_groups, SOURCE_SHA256

parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('csv_file', type=Path)
args = parser.parse_args()
root = Path(__file__).resolve().parents[1]
if hashlib.sha256(args.csv_file.read_bytes()).hexdigest() != SOURCE_SHA256:
    raise ValueError('Expected the published QSU.csv version 1; review source changes before importing')
verses = json.loads((root / 'app/src/main/assets/ayahs.json').read_text())
groups, units = derive_groups(args.csv_file, verses)
(root / 'app/src/main/assets/passages.json').write_text(json.dumps(groups, ensure_ascii=False, indent=2) + '\n')
print(f'{len(groups)} passages cover all {len(verses)} ayahs, preserving {units} source units.')
