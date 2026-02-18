package ru.crawl.domain.model;

public final class EnemyStatsFactory {
    private EnemyStatsFactory() {}

    public static EnemyStats statsFor(Enemy.Type type, int levelEnemy) {
        int lvl = Math.max(1, Math.min(21, levelEnemy));

        return switch (type) {
            case ZOMBIE -> {
                int hpMax = 30 + 3 * (lvl - 1);
                double strength = 6 + 0.3 * (lvl - 1);
                double dexterity = 4 + 0.1 * (lvl - 1);
                int hostility = 5; // можно усложнить позже
                yield new EnemyStats(hpMax, strength, dexterity, hostility);
            }
            case VAMPIRE -> {
                int hpMax = 25 + 2 * (lvl - 1);
                double strength = 7 + 0.25 * (lvl - 1);
                double dexterity = 12 + 0.3 * (lvl - 1);
                int hostility = 8;
                yield new EnemyStats(hpMax, strength, dexterity, hostility);
            }
            case GHOST -> {
                int hpMax = 8 + (lvl - 1);
                double strength = 3 + 0.15 * (lvl - 1);
                double dexterity = 14 + 0.2 * (lvl - 1);
                int hostility = 3;
                yield new EnemyStats(hpMax, strength, dexterity, hostility);
            }
            case OGRE -> {
                int hpMax = 40 + 4 * (lvl - 1);
                double strength = 12 + 0.5 * (lvl - 1);
                double dexterity = 5 + 0.1 * (lvl - 1);
                int hostility = 6;
                yield new EnemyStats(hpMax, strength, dexterity, hostility);
            }
            case MAGIC_SNAKE -> {
                int hpMax = 18 + 2 * (lvl - 1);
                double strength = 5 + 0.25 * (lvl - 1);
                double dexterity = 16 + 0.35 * (lvl - 1);
                int hostility = 9;
                yield new EnemyStats(hpMax, strength, dexterity, hostility);
            }
            case MIMIC -> {
                int hpMax = 34 + 3 * (lvl - 1);
                double strength = 4.5 + 0.15 * (lvl - 1);
                double dexterity = 15 + 0.3 * (lvl - 1);
                int hostility = 3;
                yield new EnemyStats(hpMax, strength, dexterity, hostility);
            }
            default -> throw new IllegalArgumentException("Unsupported: " + type);
        };
    }
}
