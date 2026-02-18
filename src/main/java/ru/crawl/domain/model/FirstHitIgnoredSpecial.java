package ru.crawl.domain.model;

public final class FirstHitIgnoredSpecial implements EnemySpecial {
    private boolean firstHitIgnored = true;

    @Override
    public boolean onBeforePlayerAttack(Enemy enemy, Player player) {
        if (firstHitIgnored) {
            firstHitIgnored = false;
            return false; // первый удар по вампиру всегда промах
        }
        return true;
    }
}
