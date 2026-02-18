package ru.crawl.domain.model;

public interface EnemySpecial {
    default boolean onBeforePlayerAttack(Enemy enemy, Player player) { return true; }

    default int onBeforeTakeDamage(Enemy enemy, int incomingDamage) { return incomingDamage; }

    default void onAfterEnemyHit(Enemy enemy, Player player, int damageDealt) {}

    default void onTick(Enemy enemy, GameState state) {}
}
