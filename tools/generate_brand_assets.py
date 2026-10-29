"""Render the vector brand assets with PyGObject (PangoCairo) and pycairo."""
from pathlib import Path
import ctypes
import re
import math
import cairo
import gi

gi.require_version('Pango', '1.0')
gi.require_version('PangoCairo', '1.0')
from gi.repository import Pango, PangoCairo

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / 'docs/branding'
OUT.mkdir(exist_ok=True)
GREEN, CREAM, GOLD = '#173F33', '#F7F5EE', '#C6A363'
PATHS = [
    dict(d='M38 43V39C38 31 45 28 54 23C63 28 70 31 70 39V43', fill='none', stroke=GOLD, **{'stroke-width':'2.4','stroke-linecap':'round','stroke-linejoin':'round'}),
    dict(d='M57 29C51 27 46 32 47 37C48 43 55 45 60 40C55 41 51 38 51 34C51 32 54 30 57 29Z', fill=GOLD),
    dict(d='M52.5 49C45 43 36 40 28 42V69C37 68 45 71 52.5 76Z', fill=CREAM),
    dict(d='M55.5 49C63 43 72 40 80 42V69C71 68 63 71 55.5 76Z', fill='#E3E8DA'),
    dict(d='M34 49C39 49 44 51 47 53M34 56C39 56 44 58 47 60M34 63C39 63 44 65 47 67M74 49C69 49 64 51 61 53M74 56C69 56 64 58 61 60M74 63C69 63 64 65 61 67', fill='none', stroke=GREEN, **{'stroke-width':'1.6','stroke-linecap':'round'}),
]
def path_markup(paths):
    return ''.join('<path '+ ' '.join(f'{k}="{v}"' for k,v in p.items())+'/>' for p in paths)
def svg(paths, background=False):
    return '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 108 108">'+(f'<rect width="108" height="108" rx="24" fill="{GREEN}"/>' if background else '')+path_markup(paths)+'</svg>\n'
icon = svg(PATHS, True)
mark = svg(PATHS)
(OUT/'app-icon.svg').write_text(icon)
(OUT/'mark.svg').write_text(mark)

def color(c, value):
    c.set_source_rgb(*(int(value[i:i+2],16)/255 for i in (1,3,5)))
def draw(c, background=False):
    if background:
        c.new_sub_path()
        for x,y,start in [(84,24,-90),(84,84,0),(24,84,90),(24,24,180)]:
            c.arc(x,y,24,math.radians(start),math.radians(start+90))
        c.close_path();color(c,GREEN);c.fill()
    for p in PATHS:
        tokens=re.findall(r'[A-Z]|-?\d+(?:\.\d+)?',p['d']);i=0
        while i<len(tokens):
            cmd=tokens[i];i+=1
            count={'M':2,'L':2,'C':6,'V':1,'Z':0}[cmd]
            args=list(map(float,tokens[i:i+count]));i+=count
            if cmd=='M':c.move_to(*args)
            elif cmd=='L':c.line_to(*args)
            elif cmd=='C':c.curve_to(*args)
            elif cmd=='V':c.line_to(c.get_current_point()[0],args[0])
            else:c.close_path()
        if p['fill']!='none':color(c,p['fill']);c.fill_preserve()
        if 'stroke' in p:
            color(c,p['stroke']);c.set_line_width(float(p['stroke-width']))
            c.set_line_cap(cairo.LINE_CAP_ROUND);c.set_line_join(cairo.LINE_JOIN_ROUND);c.stroke()
        c.new_path()
def render(output, size, background=False, full_bleed=False):
    surface=cairo.ImageSurface(cairo.FORMAT_ARGB32,size,size)
    c=cairo.Context(surface)
    if full_bleed:color(c,GREEN);c.paint()
    c.scale(size/108,size/108);draw(c,background)
    surface.write_to_png(str(output))
render(OUT/'app-icon-1024.png',1024,True)
render(OUT/'app-icon-512.png',512,True)
render(OUT/'store-icon-512.png',512,True,True)
render(OUT/'mark-1024.png',1024)

# Outline the Arabic wordmark so standalone exports do not rely on installed fonts.
fc = ctypes.CDLL('libfontconfig.so.1')
fc.FcConfigGetCurrent.restype=ctypes.c_void_p
fc.FcConfigAppFontAddFile.argtypes=[ctypes.c_void_p,ctypes.c_char_p]
assert fc.FcConfigAppFontAddFile(fc.FcConfigGetCurrent(),str(ROOT/'website/assets/cairo.ttf').encode())
logo = cairo.SVGSurface(str(OUT/'logo.svg'), 480, 180)
c = cairo.Context(logo)
c.save();c.translate(314,18);c.scale(144/108,144/108);draw(c,True);c.restore()
layout=PangoCairo.create_layout(c)
layout.set_font_description(Pango.FontDescription('Cairo SemiBold 45'))
layout.set_text('آيات قرآنية',-1)
_, logical=layout.get_pixel_extents()
c.set_source_rgb(23/255,63/255,51/255)
c.move_to(280-logical.width,(180-logical.height)/2-logical.y)
PangoCairo.layout_path(c,layout);c.fill()
logo.finish()
s=cairo.ImageSurface(cairo.FORMAT_ARGB32,960,360);ctx=cairo.Context(s);ctx.scale(2,2)
ctx.save();ctx.translate(314,18);ctx.scale(144/108,144/108);draw(ctx,True);ctx.restore()
ctx.set_source_rgb(23/255,63/255,51/255)
ctx.move_to(280-logical.width,(180-logical.height)/2-logical.y)
PangoCairo.layout_path(ctx,layout);ctx.fill()
s.write_to_png(str(OUT/'logo.png'))

# The Android foreground shares the exact SVG paths and adaptive-icon safe area.
def android_vector(paths,width='108dp',viewport=108,monochrome=False):
    parts=[]
    for p in paths:
        if monochrome and p is PATHS[-1]:continue
        attrs={'android:pathData':p['d'],'android:fillColor':'#00000000' if p.get('fill')=='none' else ('#FFFFFF' if monochrome else p['fill'])}
        if 'stroke' in p:
            attrs.update({'android:strokeColor':'#FFFFFF' if monochrome else p['stroke'],'android:strokeWidth':p['stroke-width'],'android:strokeLineCap':'round','android:strokeLineJoin':'round'})
        parts.append('    <path '+' '.join(f'{k}="{v}"' for k,v in attrs.items())+' />')
    return '<?xml version="1.0" encoding="utf-8"?>\n<vector xmlns:android="http://schemas.android.com/apk/res/android" android:width="'+width+'" android:height="'+width+'" android:viewportWidth="'+str(viewport)+'" android:viewportHeight="'+str(viewport)+'">\n'+'\n'.join(parts)+'\n</vector>\n'
res=ROOT/'app/src/main/res'
(res/'drawable/ic_ayat_foreground.xml').write_text(android_vector(PATHS))
(res/'drawable/ic_ayat_monochrome.xml').write_text(android_vector(PATHS,monochrome=True))
notification=android_vector(PATHS,width='24dp',viewport=72,monochrome=True).replace('    <path','    <group android:translateX="-18" android:translateY="-18">\n    <path',1).replace('</vector>','    </group>\n</vector>')
(res/'drawable/ic_notification.xml').write_text(notification)
(ROOT/'website/assets/app-icon.svg').write_text(icon)
print('Created SVG and PNG logo exports, adaptive foreground, monochrome mark, and notification icon.')

# Review sheet on the app's ivory surface, with no external image dependencies.
board=cairo.ImageSurface(cairo.FORMAT_ARGB32,1400,700);bc=cairo.Context(board)
color(bc,CREAM);bc.paint()
def label(text,x,y,size=16,ink=GREEN):
    l=PangoCairo.create_layout(bc);l.set_font_description(Pango.FontDescription('Cairo '+str(size)))
    l.set_text(text,-1);color(bc,ink);bc.move_to(x,y);PangoCairo.show_layout(bc,l)
label('AYAT QURAN  /  BRAND IDENTITY',64,38,14)
label('APP ICON',130,565,12)
label('ARABIC LOGOTYPE',650,440,12)
bc.save();bc.translate(90,135);bc.scale(390/108,390/108);draw(bc,True);bc.restore()
bc.save();bc.translate(1190,220);bc.scale(144/108,144/108);draw(bc,True);bc.restore()
color(bc,GREEN);bc.move_to(1156-logical.width,220+(144-logical.height)/2-logical.y)
PangoCairo.layout_path(bc,layout);bc.fill()
for x,ink,name in [(650,GREEN,'Emerald'),(855,GOLD,'Gold'),(1060,'#E3E8DA','Sage')]:
    color(bc,ink);bc.rectangle(x,520,165,50);bc.fill();label(name,x,585,12)
board.write_to_png(str(OUT/'preview.png'))
