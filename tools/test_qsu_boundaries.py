"""Boundary integrity tests; no network or Android runtime required."""
import csv
from pathlib import Path
import tempfile
import unittest
from qsu_boundaries import derive_groups


class SemanticBoundaryTest(unittest.TestCase):
    def derive(self, locations, count):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / 'units.csv'
            with path.open('w', newline='') as stream:
                writer = csv.writer(stream)
                writer.writerow(['index', 'start_location', 'end_location'])
                writer.writerows((index, start, end) for index, (start, end) in enumerate(locations))
            return derive_groups(path, [dict(id=f'1-{i}', surahNumber=1) for i in range(1, count + 1)])[0]

    def test_links_touching_different_parts_of_one_ayah_are_preserved_together(self):
        result = self.derive([('1:1:1', '1:1:3'), ('1:2:2', '1:3:1'), ('1:3:2', '1:4:3')], 4)
        self.assertEqual([dict(surah=1, startAyah=1, endAyah=1), dict(surah=1, startAyah=2, endAyah=4)], result)

    def test_more_than_five_ayahs_remain_whole(self):
        self.assertEqual([dict(surah=1, startAyah=1, endAyah=6)], self.derive([('1:1:1', '1:6:3')], 6))

    def test_missing_annotations_are_rejected_instead_of_assuming_independence(self):
        with self.assertRaises(ValueError):
            self.derive([('1:1:1', '1:1:3')], 2)

    def test_cross_surah_units_are_rejected(self):
        with self.assertRaises(ValueError):
            self.derive([('1:1:1', '2:1:3')], 1)

    def test_reversed_word_positions_are_rejected(self):
        with self.assertRaises(ValueError):
            self.derive([('1:1:3', '1:1:1')], 1)


if __name__ == '__main__':
    unittest.main()
