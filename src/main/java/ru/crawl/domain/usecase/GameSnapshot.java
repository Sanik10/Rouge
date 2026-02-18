package ru.crawl.domain.usecase;

import ru.crawl.domain.model.*;

import java.util.List;

public record GameSnapshot(
        int width,
        int height,

        char[][] map,
        char[][] fog,
        List<Enemy> enemies,
        List<Item> items,
        List<Inventory> inventories,
        List<ScrollEffect> scrollEffects,

        Player player,
        Direction facing,

        int turnNumber,
        int levelIndex,

        String targetName,
        Integer targetHp,
        Integer targetMaxHp,

        List<String> log
) {
    public GameSnapshot {
        log = List.copyOf(log);
    }
}
