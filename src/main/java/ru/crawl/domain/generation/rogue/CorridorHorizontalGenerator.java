package ru.crawl.domain.generation.rogue;

import java.util.List;

public interface CorridorHorizontalGenerator {
    List<CorridorHorizontal> corridorHorizontalGenerator(List<Room> rooms);
}
