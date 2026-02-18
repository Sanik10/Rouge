package ru.crawl.datalayer.json.saveData;

import ru.crawl.domain.model.Direction;

public class SaveEnemy {
    public enum Type {
        ZOMBIE,
        VAMPIRE,
        GHOST,
        OGRE,
        MAGIC_SNAKE,
        MIMIC
    }

    public SaveEnemy.Type type;
    public  int level;
    public int x, y;
    public int hp;
    public Direction direction;
    public int isVisible;
    public int roomIndex;

}
