package ru.crawl.domain.generation.rogue;

import java.util.List;
import java.util.Random;

public class CorridorVertical {
    private int left_side;
    protected int right_side;
    protected int center;
    protected int start;
    protected int finish;

//    CorridorVertical(List<Room> rooms, int room_left, int room_right) {
    public CorridorVertical(int firstSide, int secondSide, int start, int finish) {
        this.left_side = firstSide;
        this.right_side = secondSide;
        this.center = ((finish - start) / 2) + start;
        this.start = start;
        this.finish = finish;
//        Random random = new Random();
//        this.left_side = random.nextInt(rooms.get(room_left).getRoomWidth() - 1) +
//                rooms.get(room_left).getRoomX();
//        this.right_side = random.nextInt(rooms.get(room_right).getRoomWidth() - 1) +
//                rooms.get(room_right).getRoomX();
//        this.center = ((rooms.get(room_right).getRoomY() -
//                rooms.get(room_left).getRoomY() -
//                rooms.get(room_left).getRoomHeight()) / 2) +
//                rooms.get(room_left).getRoomY() + rooms.get(room_left).getRoomHeight();
//        this.start = rooms.get(room_left).getRoomY() + rooms.get(room_left).getRoomHeight();
//        this.finish = rooms.get(room_right).getRoomY();
    }

    public int getLeft_side() {
        return left_side;
    }

    public int getRight_side() {
        return right_side;
    }

    public int getCenter() {
        return center;
    }

    public int getStart() {
        return start;
    }

    public int getFinish() {
        return finish;
    }

//    public void setLeftSide(int newValue) {
//        this.left_side = newValue;
//    }
}
