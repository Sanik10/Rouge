package ru.crawl.domain.model;

import java.util.List;
import java.util.Random;

public final class Combat {
    private Combat() {}

    // шанс попадания: функция от ловкости атакующего и цели
    public static boolean rollHit(double attDex, double defDex, Random rng) {
        double a = Math.max(0.0, attDex);
        double d = Math.max(0.0, defDex);

        double p = (a + 1.0) / (a + d + 2.0);

        // ограничим крайности (чтобы всегда был шанс)
        p = clamp(p, 0.05, 0.95);

        return rng.nextDouble() < p;
    }

    // урон: от силы (позже сюда добавим модификатор оружия)
    public static int rollDamage(double strength, Random rng) {
        double s = Math.max(1.0, strength);
        int base = (int) Math.round(s);

        int min = Math.max(1, base / 2);
        int max = Math.max(min, base);

        return min + rng.nextInt(max - min + 1);
    }

    // Атака игрока по врагу (контакт шагом)
    public static void playerAttacks(Player player, Enemy enemy, Random rng, List<String> log, GameStats gameStats) {
        if (!enemy.getSpecial().onBeforePlayerAttack(enemy, player)) {
            gameStats.incMiss();
            log.add("You miss!");
            return;
        }

        boolean hit = rollHit(player.getDex(), enemy.getStats().dexterity(), rng);
        if (!hit) {
            gameStats.incMiss();
            log.add("You miss!");
            return;
        }

        gameStats.incHit();

        int intended = rollDamage(player.getStrength(), rng);

        int hpBefore = enemy.getHp();
        enemy.takeDamage(intended);
        int dealt = hpBefore - enemy.getHp();

        if (dealt > 0) {
            int maxBefore = player.getMaxHp();
            boolean wasSleeping = player.isSleeping();

            enemy.getSpecial().onAfterEnemyHit(enemy, player, dealt);

            if (player.getMaxHp() < maxBefore) {
                log.add("Your vitality is drained!");
            }
            if (!wasSleeping && player.isSleeping()) {
                log.add("You fall asleep!");
            }
        } else {
            log.add("You deal no damage");
        }

        if (!enemy.isAlive()) log.add(enemy.getType() + " dies!");
    }

    // Атака врага по игроку (используем в enemiesTurn, чтобы заработали drain/sleep)
    public static void enemyAttacks(Enemy enemy, Player player, Random rng, List<String> log) {
        boolean hit = rollHit(enemy.getStats().dexterity(), player.getDex(), rng);
        if (!hit) {
            log.add(enemy.getType() + " misses you");
            return;
        }

        int dmg = rollDamage(enemy.getStats().strength(), rng);

        int hpBefore = player.getHp();
        player.takeDamage(dmg);
        int dealt = hpBefore - player.getHp();

        log.add(enemy.getType() + " hits you for " + dealt);

        if (dealt > 0) {
            enemy.getSpecial().onAfterEnemyHit(enemy, player, dealt);
        }

        if (!player.isAlive()) log.add("You died!");
    }

    private static double clamp(double v, double lo, double hi) {
        return Math.max(lo, Math.min(hi, v));
    }
}
