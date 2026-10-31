"""Gera as texturas e os icones em pixel art da pagina (site/assets/img/ui/). Arte PROPRIA (ruido de paleta e grades de pixels),
sem nenhum arquivo do Minecraft. Rode: python gensite.py   (precisa do Pillow)."""
import random
from pathlib import Path

from PIL import Image

OUT = Path(__file__).resolve().parents[2] / "site" / "assets" / "img" / "ui"
OUT.mkdir(parents=True, exist_ok=True)


def hexc(h):
    h = h.lstrip("#")
    return tuple(int(h[i:i + 2], 16) for i in (0, 2, 4)) + (255,)


def noise_tile(name, palette, seed, size=16, weights=None, extra=None):
    rnd = random.Random(seed)
    img = Image.new("RGBA", (size, size))
    cols = [hexc(c) for c in palette]
    for y in range(size):
        for x in range(size):
            img.putpixel((x, y), rnd.choices(cols, weights=weights)[0])
    if extra:
        extra(img, rnd)
    img.save(OUT / f"{name}.png")


def grass_top(img, rnd):
    pass


def dirt_with_grass(img, rnd):
    g = [hexc(c) for c in ("#5d9b3a", "#4f8a30", "#6bab44", "#468028")]
    for x in range(16):
        h = 3 + (rnd.random() < 0.5) + (rnd.random() < 0.25)
        for y in range(h):
            img.putpixel((x, y), rnd.choice(g))


def planks(img, rnd):
    dark = hexc("#7a5a2b")
    for y in (3, 7, 11, 15):
        for x in range(16):
            img.putpixel((x, y), dark)
    for y, xs in ((0, 5), (4, 11), (8, 3), (12, 9)):
        for dy in range(3):
            img.putpixel((xs, y + dy), dark)


def stone_bricks(img, rnd):
    mortar = hexc("#6e6e6e")
    for y in (0, 8):
        for x in range(16):
            img.putpixel((x, y), mortar)
    for x in (0, 8):
        for y in range(0, 8):
            img.putpixel((x, y), mortar)
    for x in (4, 12):
        for y in range(8, 16):
            img.putpixel((x, y), mortar)


noise_tile("grass", ["#5d9b3a", "#4f8a30", "#6bab44", "#468028", "#57963a"], 1, weights=[4, 3, 3, 2, 3])
noise_tile("dirt", ["#866043", "#79553a", "#946b4b", "#6b4a31", "#8b6446"], 2, weights=[4, 3, 3, 2, 3])
noise_tile("dirt_grass", ["#866043", "#79553a", "#946b4b", "#6b4a31", "#8b6446"], 3, weights=[4, 3, 3, 2, 3], extra=dirt_with_grass)
noise_tile("stone", ["#7d7d7d", "#8a8a8a", "#707070", "#939393", "#767676"], 4, weights=[4, 3, 3, 2, 3])
noise_tile("planks", ["#a07c43", "#9a7640", "#a98449", "#94703a"], 5, weights=[4, 3, 3, 2], extra=planks)
noise_tile("bricks", ["#7d7d7d", "#858585", "#767676", "#8d8d8d"], 6, weights=[4, 3, 3, 2], extra=stone_bricks)
noise_tile("sky", ["#8ab8ff", "#93bfff", "#82b0f7"], 7, weights=[6, 3, 1])
noise_tile("deepslate", ["#4a4a52", "#52525a", "#42424a", "#58585f"], 8, weights=[4, 3, 3, 2])

PAL = {".": None, "k": "#2b2b2b", "w": "#ffffff", "g": "#9a9a9a", "G": "#5f5f5f", "e": "#17dd62", "E": "#0a8a3a", "y": "#fcdb05", "Y": "#c79a00",
       "o": "#e8832a", "r": "#d9382b", "R": "#8f1f17", "b": "#3a74d6", "c": "#4aedd9", "n": "#8b5a2b", "N": "#5e3b17", "p": "#f4a6c0", "v": "#8a4fd6",
       "s": "#d9d9d9", "h": "#e9c9a0"}

ICONS = {
    "mic": ["....kkkk....", "...kggggk...", "...kggggk...", "...kgwggk...", "...kggggk...", ".k.kggggk.k.", ".k..kkkk..k.", ".kk......kk.", "..kkkkkkkk..", ".....kk.....", ".....kk.....", "...kkkkkk..."],
    "bubble": ["............", ".kkkkkkkkkk.", "kwwwwwwwwwwk", "kwkkwkkwkkwk", "kwwwwwwwwwwk", "kwkkkkkkkwwk", "kwwwwwwwwwwk", ".kwwwwwwwwk.", "..kkkkwwkk..", "......kwk...", ".......kk...", "............"],
    "book": ["............", "..kkkkkkkk..", ".kbbbbbbbbk.", ".kbwwwwwwbk.", ".kbwkkkkwbk.", ".kbwwwwwwbk.", ".kbwkkkkwbk.", ".kbwwwwwwbk.", ".kbbbbbbbbk.", ".kNNNNNNNNk.", "..kkkkkkkk..", "............"],
    "arrow": ["............", ".....kk.....", "....kyyk....", "...kyyyyk...", "..kyyyyyyk..", ".kkkkyykkkk.", "....kyyk....", "....kyyk....", "....kyyk....", "....kyyk....", "....kkkk....", "............"],
    "emerald": ["............", "....kkkk....", "...keeeek...", "..keweeeEk..", ".keweeeeEEk.", ".keeeeeeEEk.", ".keeeeeEEEk.", "..keeeEEEk..", "...keEEEk...", "....kkkk....", "............", "............"],
    "shield": ["............", ".kkkkkkkkkk.", ".kbbbkkbbbk.", ".kbbbkkbbbk.", ".kkkkkkkkkk.", ".kbbbkkbbbk.", ".kbbbkkbbbk.", "..kbbkkbbk..", "...kbkkbk...", "....kbbk....", ".....kk.....", "............"],
    "bone": ["............", "..kk....kk..", ".kwwk..kwwk.", ".kwwkkkkwwk.", "..kwwwwwwk..", "...kwwwwk...", "..kwwwwwwk..", ".kwwkkkkwwk.", ".kwwk..kwwk.", "..kk....kk..", "............", "............"],
    "heart": ["............", "..kkk..kkk..", ".krrrkkrrrk.", "krrwrrrrrrRk", "krwrrrrrrrRk", "krrrrrrrrrRk", ".krrrrrrrRk.", "..krrrrrRk..", "...krrrRk...", "....krRk....", ".....kk.....", "............"],
    "bolt": ["............", "......kkkk..", ".....kyyyk..", "....kyyyk...", "...kyyyk....", "..kyyyyyyk..", "...kkkyyk...", "......kyk...", ".....kyk....", ".....kk.....", "............", "............"],
    "skull": ["............", "..kkkkkkkk..", ".kwwwwwwwwk.", "kwwwwwwwwwwk", "kwkkwwwwkkwk", "kwkkwwwwkkwk", "kwwwwkkwwwwk", ".kwwwkkwwwk.", "..kwkwkwkk..", "..kkkkkkkk..", "............", "............"],
    "bubbles": ["............", ".kkkkkkk....", "kwwwwwwwk...", "kwkkwkkwk...", "kwwwwwwwk.kk", ".kkkwkkkkkyk", "...kkkkykkyk", "......kyyyyk", "......kykkyk", ".......kkkk.", "............", "............"],
    "tag": ["............", "..kkkkkkkk..", ".kyyyyyyyyk.", ".kykkkkkkyk.", ".kykyyyykyk.", ".kykkkkkkyk.", ".kyyyyyyyyk.", "..kYYYYYYk..", "...kkkkkk...", "............", "............", "............"],
    "eye": ["............", "............", "...kkkkkk...", "..kwwwwwwk..", ".kwwkbbkwwk.", "kwwwkbkkwwwk", ".kwwkbbkwwk.", "..kwwwwwwk..", "...kkkkkk...", "............", "............", "............"],
    "lock": ["............", "...kkkkkk...", "..kgggggk...", "..kg....gk..", "..kg....gk..", ".kkkkkkkkkk.", ".kyyyyyyyyk.", ".kyyykkyyyk.", ".kyyykkyyyk.", ".kyyyyyyyyk.", ".kkkkkkkkkk.", "............"],
    "feather": ["............", ".........kk.", "........kwwk", ".......kwwwk", "......kwwwk.", ".....kwwwk..", "....kwwwk...", "...kwwwk....", "..kwwwk.....", ".kkwwk......", "kk.kk.......", "............"],
    "star": ["............", ".....kk.....", ".....kyk....", "....kyyyk...", "kkkkkyyyykkk", ".kyyyyyyyyk.", "..kyyyyyyk..", "...kyyyyk...", "..kyyykyyk..", "..kyykkkyyk.", ".kkkk..kkkk.", "............"],
}
for name, rows in ICONS.items():
    img = Image.new("RGBA", (12, 12), (0, 0, 0, 0))
    for y, row in enumerate(rows):
        assert len(row) == 12, (name, y, row)
        for x, ch in enumerate(row):
            if PAL[ch]:
                img.putpixel((x, y), hexc(PAL[ch]))
    img.save(OUT / f"icon-{name}.png")

# favicon: estrela de 12x12 ampliada
Image.open(OUT / "icon-star.png").resize((48, 48), Image.NEAREST).save(OUT.parent.parent / "favicon.png")
print("ok:", len(list(OUT.glob("*.png"))), "imagens em", OUT)
