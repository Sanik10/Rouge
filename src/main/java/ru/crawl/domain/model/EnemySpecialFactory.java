package ru.crawl.domain.model;

import java.util.ArrayList;
import java.util.List;

public final class EnemySpecialFactory {
    private EnemySpecialFactory() {}

    public static EnemySpecial forType(Enemy.Type type, int level) {
        List<EnemySpecial> list = new ArrayList<>();

        switch (type) {
            case VAMPIRE -> {
                list.add(new FirstHitIgnoredSpecial());
                // TODO: добавить VampireDrainMaxHpSpecial
            }
            case MAGIC_SNAKE -> {
                // TODO: добавить SleepOnHitSpecial (25–35%)
            }
            default -> { }
        }

        if (list.isEmpty()) return new NoSpecial();
        if (list.size() == 1) return list.get(0);
        return new CompositeEnemySpecial(list);
    }
}
