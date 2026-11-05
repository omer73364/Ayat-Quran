"""Render the widget-picker sample with the same bundled Quran font as the app.
Run with /usr/bin/python3 (requires PyGObject, PangoCairo, and pycairo).
"""
import ctypes
import json
from pathlib import Path
import cairo
import gi

gi.require_version('Pango', '1.0')
gi.require_version('PangoCairo', '1.0')
from gi.repository import Pango, PangoCairo

root = Path(__file__).resolve().parents[1]
fontconfig = ctypes.CDLL('libfontconfig.so.1')
fontconfig.FcConfigGetCurrent.restype = ctypes.c_void_p
fontconfig.FcConfigAppFontAddFile.argtypes = [ctypes.c_void_p, ctypes.c_char_p]
assert fontconfig.FcConfigAppFontAddFile(fontconfig.FcConfigGetCurrent(), str(root / 'app/src/main/res/font/kfgqpc.ttf').encode())
surface = cairo.ImageSurface(cairo.FORMAT_ARGB32, 660, 180)
canvas = cairo.Context(surface)
canvas.set_source_rgba(1, 1, 1, 1)
layout = PangoCairo.create_layout(canvas)
font = Pango.FontDescription('KFGQPC Uthmanic Script HAFS')
font.set_absolute_size(72 * Pango.SCALE)
layout.set_font_description(font)
layout.set_text(json.loads((root / 'app/src/main/assets/ayahs.json').read_text())[0]['textArabic'], -1)
ink, _ = layout.get_pixel_extents()
canvas.move_to((660 - ink.width) / 2 - ink.x, (180 - ink.height) / 2 - ink.y)
PangoCairo.show_layout(canvas, layout)
output = root / 'app/src/main/res/drawable-nodpi/widget_verse_preview.png'
output.parent.mkdir(exist_ok=True)
surface.write_to_png(str(output))
