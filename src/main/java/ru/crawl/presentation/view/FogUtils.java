package ru.crawl.presentation.view;

import ru.crawl.domain.model.Player;

public final class FogUtils {

    private FogUtils() {}

    public static void changeFog(char[][] map, char[][] fog, int x, int y, Player player) {
        char ch = fog[y][x];

        if (ch != '.' && ch != '▓') {
            player.setRoomIndex(ch - 48);
            player.addVisitedRooms(ch - 48);
        } else if (ch == '.') {
            player.setRoomIndex(-1);
        }

        java.util.function.BiPredicate<Integer,Integer> isFloorOrDoor = (yy, xx) -> {
            char c = map[yy][xx];
            return c == '.' || c == '>' || c == 'R' || c == 'B' || c == 'Y';
        };

        fog[y][x] = (fog[y][x] >= '0' && fog[y][x] <= '9') ? fog[y][x] : '.';

        if (y + 1 < map.length && isFloorOrDoor.test(y + 1, x) && fog[y + 1][x] == '▓')
            fog[y + 1][x] = '.';
        if (x + 1 < map[0].length && isFloorOrDoor.test(y, x + 1) && fog[y][x + 1] == '▓')
            fog[y][x + 1] = '.';
        if (y - 1 >= 0 && isFloorOrDoor.test(y - 1, x) && fog[y - 1][x] == '▓')
            fog[y - 1][x] = '.';
        if (x - 1 >= 0 && isFloorOrDoor.test(y, x - 1) && fog[y][x - 1] == '▓')
            fog[y][x - 1] = '.';
    }
}
