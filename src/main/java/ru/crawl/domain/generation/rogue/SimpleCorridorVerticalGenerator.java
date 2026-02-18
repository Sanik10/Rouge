package ru.crawl.domain.generation.rogue;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SimpleCorridorVerticalGenerator implements CorridorVerticalGenerator {
    public List<CorridorVertical> corridorVerticalGenerator(List<Room> rooms) {
        int[][] near_rooms = {{0, 3}, {1, 4}, {2, 5}, {3, 6}, {4, 7}, {5, 8}};
        return Arrays.stream(near_rooms)
//                .map(pair -> new CorridorHorizontal(rooms, pair[0], pair[1]))
                .map(pair -> new CorridorVertical(
                        rooms.get(pair[0]).getRoomBottomExit(),
                        rooms.get(pair[1]).getRoomTopExit(),
                        rooms.get(pair[0]).getRoomY() + rooms.get(pair[0]).getRoomHeight(),
                        rooms.get(pair[1]).getRoomY()))
                .collect(Collectors.toList());
    }
}
