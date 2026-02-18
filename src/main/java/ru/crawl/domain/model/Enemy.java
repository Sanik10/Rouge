package ru.crawl.domain.model;

public class Enemy {
    public enum Type {
        ZOMBIE,
        VAMPIRE,
        GHOST,
        OGRE,
        MAGIC_SNAKE,
        MIMIC
    }

    private final Type type;
    private final int level;
    private int x, y;
    private final EnemyStats stats;
    private int hp;
    private final EnemySpecial special;
    private Direction direction;
    private int isVisible;
    private int roomIndex;
    private boolean hunting;
    private final int badBlood;

    private boolean disguised;
    private Item.TypeItem disguiseItemType;

    public boolean isDisguised() {
        return type == Type.MIMIC && disguised;
    }

    public Item.TypeItem getDisguiseItemType() {
        return disguiseItemType;
    }

    public void disguiseAs(Item.TypeItem type) {
        if (this.type != Type.MIMIC) return;
        disguised = true;
        disguiseItemType = type;
    }

    public void reveal() {
        if (this.type != Type.MIMIC) return;
        disguised = false;
    }

    public Enemy(Type type, int level, int startX, int startY, int spawnRoomIndex) {
        this.type = type;
        this.level = level;
        this.x = startX;
        this.y = startY;
        this.roomIndex = spawnRoomIndex;
        this.stats = EnemyStatsFactory.statsFor(type, level);
        this.hp = stats.hpMax();
        this.special = EnemySpecialFactory.forType(type, level);
        if (type == Type.MAGIC_SNAKE)
            this.direction = Direction.UP_RIGHT;
        else
            this.direction = Direction.UP;
        this.isVisible = 0;
        this.hunting = false;
        this.badBlood = setBadBlood();

        if (type == Type.MIMIC) {
            this.disguised = true;
            this.disguiseItemType = Item.TypeItem.WEAPON_Gun;
        } else {
            this.disguised = false;
            this.disguiseItemType = null;
        }
    }

    public int setBadBlood() {
        return switch (type) {
            case GHOST -> 3;
            case OGRE, ZOMBIE -> 4;
            case VAMPIRE, MAGIC_SNAKE -> 5;
            case MIMIC -> 1;
        };
    }

    public int getBadBlood() { return badBlood; }

    public boolean isHunting() { return hunting; }

    public void setHunting(boolean value) { hunting = value; }

    public Direction getDirection() { return direction; }

    public void setDirection(Direction dir) { direction = dir; }

    public int getVisible() { return isVisible; }

    public void setVisible(int newInt) { isVisible = newInt; }

    public Type getType() {
        return type;
    }

    public int getLevel() {
        return level;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public EnemyStats getStats() {
        return stats;
    }

    public int getHp() {
        return hp;
    }

    public EnemySpecial getSpecial() {
        return special;
    }

    public void moveTo(int newX, int newY) {
        x = newX;
        y = newY;
    }

    public void takeDamage(int incoming) {
        int actual = special.onBeforeTakeDamage(this, incoming);
        hp = Math.max(0, hp - actual);
    }

    public void tick(GameState state) {
        special.onTick(this, state);
    }

    public boolean isAlive() {
        return hp > 0;
    }

    public int getRoomIndex() {
        return roomIndex;
    }

    public void setRoomIndex(int index) { roomIndex = index; }

    public void setHp(int hp) {
        this.hp = hp;
    }
}