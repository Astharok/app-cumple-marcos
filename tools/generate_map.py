"""Genera el fondo cartografico de Europa a partir de Natural Earth."""
import json
from pathlib import Path
from urllib.request import urlretrieve
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "tmp" / "mapdata" / "countries.geojson"
SOURCE_URL = "https://raw.githubusercontent.com/nvkelso/natural-earth-vector/master/geojson/ne_110m_admin_0_countries.geojson"
OUTPUT = ROOT / "src" / "main" / "resources" / "images" / "mapa-europa-preciso.png"
WIDTH, HEIGHT, SCALE = 1200, 1450, 2
# Recorte: desde el este de Francia hasta antes de Kiev, incluyendo Tunez y Helsinki.
MIN_LON, MAX_LON, MIN_LAT, MAX_LAT = 8.0, 29.8, 35.0, 61.5


def project(point):
    lon, lat = point[:2]
    x = (lon - MIN_LON) / (MAX_LON - MIN_LON) * WIDTH * SCALE
    y = (MAX_LAT - lat) / (MAX_LAT - MIN_LAT) * HEIGHT * SCALE
    return round(x), round(y)


def rings(geometry):
    if geometry["type"] == "Polygon":
        yield from geometry["coordinates"]
    elif geometry["type"] == "MultiPolygon":
        for polygon in geometry["coordinates"]:
            yield from polygon


if not SOURCE.exists():
    SOURCE.parent.mkdir(parents=True, exist_ok=True)
    urlretrieve(SOURCE_URL, SOURCE)

data = json.loads(SOURCE.read_text(encoding="utf-8"))
image = Image.new("RGB", (WIDTH * SCALE, HEIGHT * SCALE), "#f4f5f6")
draw = ImageDraw.Draw(image)

for feature in data["features"]:
    geometry = feature.get("geometry")
    if not geometry:
        continue
    for ring in rings(geometry):
        if len(ring) < 3:
            continue
        points = [project(point) for point in ring]
        draw.polygon(points, fill="#d7d9da", outline="#4a4d50", width=2 * SCALE)

image = image.resize((WIDTH, HEIGHT), Image.Resampling.LANCZOS)
OUTPUT.parent.mkdir(parents=True, exist_ok=True)
image.save(OUTPUT, optimize=True)
print(OUTPUT)
