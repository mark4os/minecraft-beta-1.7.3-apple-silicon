import os
from PIL import Image, ImageDraw, ImageFont, ImageFilter

def create_dmg_background(output_path, scale=1):
    width = 660 * scale
    height = 420 * scale

    # Load dirt background
    dirt_tile = Image.open('src/main/resources/gui/background.png').convert('RGBA')
    tile_w, tile_h = dirt_tile.size
    tile_w_scaled, tile_h_scaled = tile_w * 2 * scale, tile_h * 2 * scale
    dirt_scaled = dirt_tile.resize((tile_w_scaled, tile_h_scaled), Image.NEAREST)

    # Tile across background
    bg = Image.new('RGBA', (width, height))
    for x in range(0, width, tile_w_scaled):
        for y in range(0, height, tile_h_scaled):
            bg.paste(dirt_scaled, (x, y))

    # Darken and add vignette / gradient overlay for modern sleek look
    overlay = Image.new('RGBA', (width, height), (0, 0, 0, 0))
    draw_overlay = ImageDraw.Draw(overlay)
    
    # Top and bottom gradient shadows
    for y in range(height):
        # Base darkness 140
        alpha = int(140 + 70 * (y / height))
        draw_overlay.line([(0, y), (width, y)], fill=(18, 18, 24, alpha))
    
    bg = Image.alpha_composite(bg, overlay)

    # Render Minecraft logo
    logo_sheet = Image.open('src/main/resources/title/mclogo.png').convert('RGBA')
    # Left part: (0, 0, 155, 44), Right part: (0, 45, 155, 44)
    part1 = logo_sheet.crop((0, 0, 155, 44))
    part2 = logo_sheet.crop((0, 45, 119, 45 + 44))

    full_logo = Image.new('RGBA', (274, 44), (0, 0, 0, 0))
    full_logo.paste(part1, (0, 0), part1)
    full_logo.paste(part2, (155, 0), part2)

    # Scale logo nicely (e.g. 1.25x or 1.3x)
    logo_scale = 1.2 * scale
    logo_w = int(274 * logo_scale)
    logo_h = int(44 * logo_scale)
    logo_resized = full_logo.resize((logo_w, logo_h), Image.NEAREST)

    logo_x = (width - logo_w) // 2
    logo_y = int(32 * scale)

    # Logo drop shadow
    shadow_offset = int(3 * scale)
    shadow = Image.new('RGBA', (logo_w, logo_h), (0, 0, 0, 0))
    for px in range(logo_w):
        for py in range(logo_h):
            r, g, b, a = logo_resized.getpixel((px, py))
            if a > 50:
                shadow.putpixel((px, py), (0, 0, 0, 160))
    bg.paste(shadow, (logo_x + shadow_offset, logo_y + shadow_offset), shadow)
    bg.paste(logo_resized, (logo_x, logo_y), logo_resized)

    draw = ImageDraw.Draw(bg)

    # Subtitle: Beta 1.7.3 • Apple Silicon Native (arm64)
    font_path = '/System/Library/Fonts/SFNS.ttf'
    if not os.path.exists(font_path):
        font_path = '/System/Library/Fonts/Supplemental/Arial Bold.ttf'

    sub_font = ImageFont.truetype(font_path, int(15 * scale))
    hint_font = ImageFont.truetype(font_path, int(13 * scale))

    sub_text = "Beta 1.7.3  •  Apple Silicon Native (arm64)"
    sub_bbox = draw.textbbox((0, 0), sub_text, font=sub_font)
    sub_w = sub_bbox[2] - sub_bbox[0]
    sub_x = (width - sub_w) // 2
    sub_y = logo_y + logo_h + int(10 * scale)

    # Draw subtitle with shadow
    draw.text((sub_x + 1, sub_y + 1), sub_text, fill=(0, 0, 0, 200), font=sub_font)
    draw.text((sub_x, sub_y), sub_text, fill=(255, 215, 0, 255), font=sub_font) # Gold

    # Draw central stylized arrow between icons (icon centers are at x=170 and x=490, y=210)
    arrow_center_x = width // 2
    arrow_center_y = int(215 * scale)
    arrow_len = int(36 * scale)
    arrow_w = int(14 * scale)

    # Arrow base shadow & shape
    arrow_color = (255, 255, 255, 220)
    arrow_shadow = (0, 0, 0, 150)
    
    for offset_x, offset_y, col in [(scale, scale, arrow_shadow), (0, 0, arrow_color)]:
        ax1 = arrow_center_x - arrow_len + offset_x
        ax2 = arrow_center_x + arrow_len - int(12 * scale) + offset_x
        ay = arrow_center_y + offset_y
        
        # Shaft
        draw.line([(ax1, ay), (ax2, ay)], fill=col, width=int(5 * scale))
        
        # Arrowhead
        p1 = (arrow_center_x + arrow_len + offset_x, arrow_center_y + offset_y)
        p2 = (arrow_center_x + arrow_len - int(18 * scale) + offset_x, arrow_center_y - arrow_w + offset_y)
        p3 = (arrow_center_x + arrow_len - int(18 * scale) + offset_x, arrow_center_y + arrow_w + offset_y)
        draw.polygon([p1, p2, p3], fill=col)

    # Instruction at the bottom
    hint_text = "Drag Minecraft to Applications to install"
    hint_bbox = draw.textbbox((0, 0), hint_text, font=hint_font)
    hint_w = hint_bbox[2] - hint_bbox[0]
    hint_x = (width - hint_w) // 2
    hint_y = int(355 * scale)

    # Hint pill background
    pill_pad_x = int(16 * scale)
    pill_pad_y = int(6 * scale)
    pill_rect = [
        hint_x - pill_pad_x,
        hint_y - pill_pad_y,
        hint_x + hint_w + pill_pad_x,
        hint_y + (hint_bbox[3] - hint_bbox[1]) + pill_pad_y
    ]
    draw.rounded_rectangle(pill_rect, radius=int(12 * scale), fill=(0, 0, 0, 120))
    draw.text((hint_x, hint_y), hint_text, fill=(230, 230, 230, 240), font=hint_font)

    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    bg.save(output_path, 'PNG')
    print(f"Generated {output_path} ({width}x{height})")

if __name__ == '__main__':
    create_dmg_background('build/dmg_assets/background.png', scale=1)
    create_dmg_background('build/dmg_assets/background@2x.png', scale=2)
