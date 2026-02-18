package ru.crawl.domain.generation.rogue;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SimpleCorridorHorizontalGenerator implements CorridorHorizontalGenerator {
    public List<CorridorHorizontal> corridorHorizontalGenerator(List<Room> rooms) {
        int[][] near_rooms = {{0, 1}, {1, 2}, {3, 4}, {4, 5}, {6, 7}, {7, 8}};
        return Arrays.stream(near_rooms)
//                .map(pair -> new CorridorHorizontal(rooms, pair[0], pair[1]))
                .map(pair -> new CorridorHorizontal(
                        rooms.get(pair[0]).getRoomRightExit(),
                        rooms.get(pair[1]).getRoomLeftExit(),
                        rooms.get(pair[0]).getRoomX() + rooms.get(pair[0]).getRoomWidth(),
                        rooms.get(pair[1]).getRoomX()))
                .collect(Collectors.toList());
    }
}
