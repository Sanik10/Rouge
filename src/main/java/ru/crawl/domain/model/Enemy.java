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

    public Enemy(Type type, int level, int startX, int startY) {
        this.type = type;
        this.level = level;
        this.x = startX;
        this.y = startY;
        this.stats = EnemyStatsFactory.statsFor(type, level);
        this.hp = stats.hpMax();
        this.special = EnemySpecialFactory.forType(type, level);
    }

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

    // геймплейные методы

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
}