package ru.crawl.domain.model;

public final class FirstHitIgnoredSpecial implements EnemySpecial {
    private boolean firstHitIgnored = true;

    @Override
    public int onBeforeTakeDamage(Enemy enemy, int incomingDamage) {
        if (firstHitIgnored) {
            firstHitIgnored = false;
            return 0;
        }
        return incomingDamage;
    }
}
