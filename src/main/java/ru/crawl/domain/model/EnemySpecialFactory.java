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
                list.add(new VampireDrainMaxHpSpecial());
            }
            case MAGIC_SNAKE -> {
                list.add(new SleepOnHitSpecial());
            }
            default -> { }
        }

        if (list.isEmpty()) return new NoSpecial();
        if (list.size() == 1) return list.get(0);
        return new CompositeEnemySpecial(list);
    }
}
