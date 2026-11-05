"""Preserve QSU semantic-unit links while displaying only whole ayahs."""
import collections
import csv

SOURCE_SHA256 = '68d8171356f8ff018b7c9cf4a909a1b1b7249acd8e9ef67af6c7eb69c7b50a87'


def derive_groups(csv_file, verses):
    ids = {verse['id'] for verse in verses}
    counts = collections.Counter(verse['surahNumber'] for verse in verses)
    linked_boundaries, annotated, indices = set(), set(), set()
    with csv_file.open(encoding='utf-8-sig', newline='') as stream:
        for unit in csv.DictReader(stream):
            chapter, start, start_word = map(int, unit['start_location'].split(':'))
            end_chapter, end, end_word = map(int, unit['end_location'].split(':'))
            if chapter != end_chapter or not (1 <= start <= end <= counts[chapter]):
                raise ValueError(f"Invalid semantic unit {unit['index']}")
            if min(start_word, end_word) < 1 or (start == end and start_word > end_word):
                raise ValueError(f"Invalid word positions in unit {unit['index']}")
            if unit['index'] in indices:
                raise ValueError(f"Duplicate semantic unit {unit['index']}")
            indices.add(unit['index'])
            annotated.update(f'{chapter}-{number}' for number in range(start, end + 1))
            linked_boundaries.update((chapter, number) for number in range(start, end))
    if annotated != ids:
        raise ValueError('Source annotations must cover every bundled ayah')

    groups = []
    for chapter, length in sorted(counts.items()):
        start = 1
        for number in range(1, length + 1):
            if number == length or (chapter, number) not in linked_boundaries:
                groups.append(dict(surah=chapter, startAyah=start, endAyah=number))
                start = number + 1
    covered = [f"{group['surah']}-{number}" for group in groups
               for number in range(group['startAyah'], group['endAyah'] + 1)]
    if len(covered) != len(ids) or set(covered) != ids:
        raise ValueError('Derived groups must cover every ayah exactly once')
    return groups, len(indices)
