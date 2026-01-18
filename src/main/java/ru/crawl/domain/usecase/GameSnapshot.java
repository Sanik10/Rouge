package ru.crawl.domain.usecase;

import java.util.List;

public record GameSnapshot(
    int playerX,
    int playerY,
    int turnNumber,
    List<String> log
) {
    public GameSnapshot {
        log = List.copyOf(log);
    }
}
