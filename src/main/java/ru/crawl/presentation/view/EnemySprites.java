package ru.crawl.presentation.view;

import ru.crawl.domain.model.Enemy;

public final class EnemySprites {

    private static final AsciiSprite ZOMBIE = new AsciiSprite(
            "  ___  ",
            " (x_x) ",
            " /|_|\\ ",
            "  / \\  ",
            " /   \\ "
    );

    private static final AsciiSprite VAMPIRE = new AsciiSprite(
            "  /\\   ",
            " (oVo) ",
            " /|_|\\ ",
            " /^^^\\ ",
            "  / \\  "
    );

    private static final AsciiSprite GHOST = new AsciiSprite(
            "  __   ",
            " (.. ) ",
            " /  \\  ",
            " \\__/  ",
            "  ..   "
    );

    private static final AsciiSprite OGRE = new AsciiSprite(
            " [==]  ",
            " (OO)  ",
            " /||\\  ",
            " /  \\  ",
            " /__\\  "
    );

    private static final AsciiSprite SNAKE = new AsciiSprite(
            "  ~~   ",
            " ~  ~  ",
            "  ~~   ",
            "   ~   ",
            "  ~~   "
    );

    private static final AsciiSprite MIMIC = new AsciiSprite(
            " [__]  ",
            " |MM|  ",
            " |MM|  ",
            " |__|  ",
            "  ||   "
    );

    private EnemySprites() {}

    public static AsciiSprite get(Enemy.Type type) {
        return switch (type) {
            case ZOMBIE      -> ZOMBIE;
            case VAMPIRE     -> VAMPIRE;
            case GHOST       -> GHOST;
            case OGRE        -> OGRE;
            case MAGIC_SNAKE -> SNAKE;
            case MIMIC       -> MIMIC;
        };
    }
}