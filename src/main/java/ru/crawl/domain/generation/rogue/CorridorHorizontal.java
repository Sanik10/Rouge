package ru.crawl.domain.generation.rogue;

import java.util.List;
import java.util.Random;

public class CorridorHorizontal {
    protected int left_side;
    protected int right_side;
    protected int center;
    protected int start;
    protected int finish;

    public CorridorHorizontal(int firstSide, int secondSide, int start, int finish) {
        this.left_side = firstSide;
        this.right_side = secondSide;
        this.center = ((finish - start) / 2) + start;
        this.start = start;
        this.finish = finish;
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
}
