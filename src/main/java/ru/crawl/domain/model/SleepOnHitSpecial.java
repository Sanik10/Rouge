package ru.crawl.domain.model;

import java.util.concurrent.ThreadLocalRandom;

public final class SleepOnHitSpecial implements EnemySpecial {

    @Override
    public void onAfterEnemyHit(Enemy enemy, Player player, int damageDealt) {
        if (damageDealt <= 0) return;

        // 25–35% по ТЗ, плавно растёт с уровнем (до 35%)
        double p = 0.25 + Math.min(0.10, enemy.getLevel() * 0.005);

        if (ThreadLocalRandom.current().nextDouble() < p) {
            player.applySleep(1);
        }
    }
}
