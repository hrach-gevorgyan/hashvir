"""Render the GitHub social preview and the README header.

Uses the app's own bundled Armenian font and its own island palette, so the banner is made
of the same pieces as the app rather than being a separate piece of art.

Run: python tools/make_banner.py
Requires: pip install pillow
"""
import math
import os

from PIL import Image, ImageDraw, ImageFont

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..')
FONT = os.path.join(ROOT, 'app/src/main/res/font/noto_sans_armenian_black.ttf')
FONT_BOLD = os.path.join(ROOT, 'app/src/main/res/font/noto_sans_armenian_bold.ttf')
LOGO = os.path.join(ROOT, 'app/src/main/res/mipmap-xxxhdpi/ic_launcher.png')
OUT = os.path.join(ROOT, 'docs')

SAND = (253, 243, 224)
SAND_DARK = (232, 207, 166)
SEA = (95, 189, 191)
PALM = (92, 158, 82)
PALM_DARK = (62, 122, 68)
TRUNK = (169, 123, 79)
SUN = (255, 209, 102)
INK = (46, 42, 40)
SOFT = (107, 101, 96)
COCONUT = (138, 90, 59)


def blend(base, layer, alpha):
    return tuple(int(b + (l - b) * alpha) for b, l in zip(base, layer))


def palm(draw, x, y, height, lean):
    top = (x + lean * height * 0.26, y - height)
    draw.line([(x, y), top], fill=blend(SAND, TRUNK, 0.30), width=int(height * 0.070))
    for angle in (-118, -72, -26, 22, 68):
        rad = math.radians(angle)
        length = height * 0.52
        tip = (top[0] + math.cos(rad) * length, top[1] + math.sin(rad) * length)
        mid = (top[0] + math.cos(rad) * length * 0.5 - math.sin(rad) * height * 0.13,
               top[1] + math.sin(rad) * length * 0.5 + math.cos(rad) * height * 0.13)
        draw.polygon([top, mid, tip], fill=blend(SAND, PALM, 0.26))
        draw.line([top, tip], fill=blend(SAND, PALM_DARK, 0.20), width=int(height * 0.018))
    for dx, dy in ((-0.05, 0.07), (0.06, 0.09)):
        r = height * 0.055
        cx, cy = top[0] + height * dx, top[1] + height * dy
        draw.ellipse([cx - r, cy - r, cx + r, cy + r], fill=blend(SAND, COCONUT, 0.34))


def banner(width, height, path, title_scale=1.0):
    img = Image.new('RGB', (width, height), SAND)
    d = ImageDraw.Draw(img)

    # Sun.
    r = height * 0.26
    d.ellipse([width - r * 1.5, -r * 0.55, width + r * 0.5, r * 1.45], fill=blend(SAND, SUN, 0.30))

    # Sea band and shoreline.
    sea_top = int(height * 0.775)
    d.rectangle([0, sea_top, width, sea_top + height * 0.06], fill=blend(SAND, SEA, 0.20))
    for row in range(2):
        y = sea_top + height * 0.018 + row * height * 0.028
        x = -40 + row * 60
        while x < width:
            d.arc([x, y - 10, x + 90, y + 10], 200, 340, fill=blend(SAND, SEA, 0.45), width=3)
            x += 150

    # Sand.
    d.rectangle([0, int(height * 0.845), width, height], fill=blend(SAND, SAND_DARK, 0.38))

    # Kept to the very edges and faint, so they frame the type instead of crowding it.
    palm(d, width * 0.018, height * 1.0, height * 0.46, lean=-1)
    palm(d, width * 0.985, height * 1.0, height * 0.38, lean=1)

    # Logo first, so the text column can be measured against it.
    logo = Image.open(LOGO).convert('RGBA')
    size = int(height * 0.46)
    logo = logo.resize((size, size), Image.LANCZOS)
    # Round the corners so it sits on the sand as a card rather than a pasted square.
    mask = Image.new('L', (size, size), 0)
    ImageDraw.Draw(mask).rounded_rectangle([0, 0, size - 1, size - 1], radius=int(size * 0.22), fill=255)
    logo_x, logo_y = int(width * 0.72), int(height * 0.50 - size / 2)
    shadow = Image.new('L', (size + 20, size + 20), 0)
    ImageDraw.Draw(shadow).rounded_rectangle(
        [10, 14, size + 9, size + 17], radius=int(size * 0.22), fill=40)
    img.paste(Image.new('RGB', shadow.size, SOFT), (logo_x - 10, logo_y - 10), shadow)
    img.paste(logo, (logo_x, logo_y), mask)

    # Type, in a column that stops well short of the logo.
    title = ImageFont.truetype(FONT, int(height * 0.175 * title_scale))
    sub = ImageFont.truetype(FONT_BOLD, int(height * 0.062 * title_scale))
    small = ImageFont.truetype(FONT_BOLD, int(height * 0.042 * title_scale))

    left = int(width * 0.075)
    d.text((left, height * 0.42), 'Հաշվի՛ր', font=title, fill=INK, anchor='ls')
    d.text((left, height * 0.535), 'Armenian counting game for toddlers',
           font=sub, fill=INK, anchor='ls')
    d.text((left, height * 0.625), 'Numbers 1–10   ·   No ads   ·   No tracking',
           font=small, fill=SOFT, anchor='ls')
    d.text((left, height * 0.695), 'Zero permissions   ·   Works offline',
           font=small, fill=SOFT, anchor='ls')

    os.makedirs(OUT, exist_ok=True)
    img.save(path, optimize=True)
    print(path, img.size)


if __name__ == '__main__':
    # GitHub social preview is shown at 1280x640.
    banner(1280, 640, os.path.join(OUT, 'social-preview.png'))
    # A wider, shorter strip for the top of the README.
    banner(1280, 400, os.path.join(OUT, 'banner.png'), title_scale=1.25)
