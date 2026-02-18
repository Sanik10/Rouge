package ru.crawl.presentation.view;

import ru.crawl.domain.model.Item;

public final class ItemSprites {

    private static final AsciiSprite POTION = new AsciiSprite(
            "  __   ",
            " (  )  ",
            "  ||   ",
            "  ||   "
    );

    private static final AsciiSprite FOOD = new AsciiSprite(
            " _____ ",
            "/     \\",
            "\\_____/",
            "       "
    );

    private static final AsciiSprite SCROLL = new AsciiSprite(
            " _____ ",
            "/~~~/\\ ",
            "\\___/  ",
            "       "
    );

    private static final AsciiSprite SWORD = new AsciiSprite(
            "  /\\   ",
            "  ||   ",
            "  ||   ",
            " /__\\  "
    );

    private static final AsciiSprite TREASURE = new AsciiSprite(
            " /^^^\\ ",
            "/#####\\",
            "\\_____/",
            "  |_|  "
    );

    private static final AsciiSprite KEY = new AsciiSprite(
            "  __   ",
            " /__\\  ",
            "  ||   ",
            "  ||   "
    );

    private ItemSprites() {}

    public static AsciiSprite get(Item.TypeItem type) {
        return switch (type) {
            case ELIXIR_Strength,
                 ELIXIR_Dexterity,
                 ELIXIR_MaxHP -> POTION;

            case FOOD -> FOOD;

            case SCROLL_Strength,
                 SCROLL_Dexterity,
                 SCROLL_MaxHP -> SCROLL;

            case WEAPON_Sword,
                 WEAPON_Gun -> SWORD;

            case TREASURE -> TREASURE;

            case KEY_RED,
                 KEY_BLUE,
                 KEY_YELLOW -> KEY;
        };
    }
}