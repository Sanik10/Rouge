package ru.crawl.domain.model;

public final class VampireDrainMaxHpSpecial implements EnemySpecial {

    @Override
    public void onAfterEnemyHit(Enemy enemy, Player player, int damageDealt) {
        if (damageDealt <= 0) return;

        int amount = 1 + enemy.getLevel() / 7;
        player.reduceMaxHp(amount);
    }
}
