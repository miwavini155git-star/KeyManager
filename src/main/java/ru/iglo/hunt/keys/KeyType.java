package ru.iglo.hunt.keys;

public enum KeyType {
    CABINET_1("key_cabinet_1", "Ключ от кабинета №1",
            "§7Ключ от стандартного кабинета\n§fДоступ: 1 уровень", 1, 0xFFD700),

    CABINET_2("key_cabinet_2", "Ключ от кабинета №2",
            "§7Ключ от процедурного кабинета\n§fДоступ: 1 уровень", 1, 0xC0C0C0),

    CABINET_3("key_cabinet_3", "Ключ от кабинета №3",
            "§7Ключ от ординаторской\n§fДоступ: 2 уровень", 2, 0x8B4513),

    LIBRARY("key_library", "Ключ от библиотеки",
            "§7Доступ к медицинской литературе\n§fДоступ: 2 уровень\n§eСекция: Архив", 2, 0x964B00),

    SECURITY("key_security", "Ключ от охраны",
            "§7Доступ к посту охраны\n§fДоступ: 3 уровень\n§cТребуется авторизация", 3, 0x000000),

    SERVER_ROOM("key_server", "Ключ от серверной",
            "§7Доступ к ИТ-инфраструктуре\n§fДоступ: 4 уровень\n§cВысокая секретность", 4, 0x00FF00),

    BASEMENT("key_basement", "Ключ от подвала",
            "§7Доступ к техническим помещениям\n§fДоступ: 2 уровень\n§8Внимание: повышенная влажность", 2, 0x4A4A4A),

    MORGUE("key_morgue", "Ключ от морга",
            "§7Доступ в патологоанатомическое отделение\n§fДоступ: 3 уровень\n§8Температура: +4°C", 3, 0xFFFFFF),

    CHIEF_DOCTOR("key_chief", "Ключ от кабинета главврача",
            "§7Доступ в кабинет руководства\n§fДоступ: 5 уровень\n§6Высший приоритет", 5, 0xFF4500),

    ELECTRICAL("key_electrical", "Ключ от щитовой",
            "§7Доступ к электрощитовой\n§fДоступ: 3 уровень\n§eОпасно: высокое напряжение", 3, 0xFFD700),

    MASTER_KEY("key_master", "Мастер-ключ",
            "§7Универсальный доступ\n§fДоступ: MAX уровень\n§6Только для администрации", 10, 0xFF00FF);

    private final String id;
    private final String displayName;
    private final String description;
    private final int accessLevel;
    private final int color;

    KeyType(String id, String displayName, String description, int accessLevel, int color) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.accessLevel = accessLevel;
        this.color = color;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public int getAccessLevel() { return accessLevel; }
    public int getColor() { return color; }

    public static KeyType fromId(String id) {
        for (KeyType type : values()) {
            if (type.id.equals(id)) {
                return type;
            }
        }
        return CABINET_1;
    }

    public String getTextureName() {
        return id + ".png";
    }

    public String getFormattedDisplayName() {
        String colorCode = getColorCode(accessLevel);
        return colorCode + displayName;
    }

    private String getColorCode(int level) {
        switch(level) {
            case 1: return "§7"; // Серый
            case 2: return "§a"; // Зеленый
            case 3: return "§9"; // Синий
            case 4: return "§5"; // Фиолетовый
            case 5: return "§6"; // Золотой
            default: return "§c"; // Красный
        }
    }
}