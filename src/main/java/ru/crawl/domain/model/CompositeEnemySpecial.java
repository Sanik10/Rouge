package ru.crawl.domain.model;

import java.util.List;

public final class CompositeEnemySpecial implements EnemySpecial {
    private final List<EnemySpecial> specials;

    public CompositeEnemySpecial(List<EnemySpecial> specials) {
        this.specials = List.copyOf(specials);
    }

    @Override
    public boolean onBeforePlayerAttack(Enemy enemy, Player player) {
        for (EnemySpecial s : specials) {
            if (!s.onBeforePlayerAttack(enemy, player)) return false;
        }
        return true;
    }

    @Override
    public int onBeforeTakeDamage(Enemy enemy, int incomingDamage) {
        int dmg = incomingDamage;
        for (EnemySpecial s : specials) dmg = s.onBeforeTakeDamage(enemy, dmg);
        return dmg;
    }

    @Override
    public void onAfterEnemyHit(Enemy enemy, Player player, int damageDealt) {
        for (EnemySpecial s : specials) s.onAfterEnemyHit(enemy, player, damageDealt);
    }

    @Override
    public void onTick(Enemy enemy, GameState state) {
        for (EnemySpecial s : specials) s.onTick(enemy, state);
    }
}
