package ru.crawl.domain.generation.rogue;

import java.util.List;

public interface CorridorVerticalGenerator {
    List<CorridorVertical> corridorVerticalGenerator(List<Room> rooms);
}
