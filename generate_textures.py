#!/usr/bin/env python3
"""
Генератор текстур для больничных ключей
Hospital Key Texture Generator
"""

try:
    from PIL import Image, ImageDraw
except ImportError:
    print("PIL не установлен. Установите: pip install Pillow")
    exit(1)

import os

# Цвета для разных ключей (RGB)
KEY_COLORS = {
    "hospital_key_cabinet1": (192, 192, 192),      # Silver
    "hospital_key_cabinet2": (255, 255, 255),      # White  
    "hospital_key_cabinet3": (139, 69, 19),        # Brown
    "hospital_key_library": (184, 134, 11),        # Dark Goldenrod
    "hospital_key_security": (0, 0, 0),            # Black
    "hospital_key_server": (0, 128, 0),            # Green
    "hospital_key_basement": (74, 74, 74),         # Dark Gray
    "hospital_key_morgue": (240, 240, 240),        # White Gray
    "hospital_key_chief": (255, 69, 0),            # Orange Red
    "hospital_key_electrical": (255, 215, 0),      # Gold
    "hospital_key_master": (255, 0, 255),          # Magenta
    "hospital_key_default": (200, 200, 200),       # Light Gray
    "key_default": (192, 192, 192),                # Silver
}

def create_key_texture(name, color, size=16):
    """Создает текстуру ключа с помощью PIL"""
    # Создаем изображение с прозрачностью
    img = Image.new('RGBA', (size, size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    
    # Рисуем простую форму ключа
    # Стержень ключа
    draw.rectangle([2, 6, 12, 10], fill=color + (255,))
    
    # Голова ключа (круг)
    draw.ellipse([0, 4, 8, 12], fill=color + (255,))
    
    # Зубцы (для разнообразия)
    draw.rectangle([11, 8, 13, 10], fill=color + (255,))
    
    return img

def generate_textures(output_dir):
    """Генерирует все текстуры ключей"""
    os.makedirs(output_dir, exist_ok=True)
    
    for name, color in KEY_COLORS.items():
        texture = create_key_texture(name, color)
        filepath = os.path.join(output_dir, f"{name}.png")
        texture.save(filepath)
        print(f"✓ Создана текстура: {name}.png")
    
    print(f"\n✓ Все {len(KEY_COLORS)} текстур созданы в {output_dir}")

if __name__ == "__main__":
    output_dir = "src/main/resources/assets/hunt/textures/item"
    generate_textures(output_dir)
