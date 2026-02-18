package ru.crawl.domain.generation.rogue;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SimpleRoomGenerator implements RoomGenerator {
    @Override
    public List<Room> generateRooms() {
        return IntStream.range(0, 9)
                .mapToObj(Room::startCoordinate)
                .collect(Collectors.toCollection(ArrayList::new));
    }
}