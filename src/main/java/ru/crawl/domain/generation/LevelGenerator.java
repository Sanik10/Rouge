package ru.crawl.domain.generation;

import ru.crawl.domain.generation.rogue.*;
import ru.crawl.domain.model.*;

import java.util.*;

public final class LevelGenerator {

    public static final int WORLD_W = 120;
    public static final int WORLD_H = 60;

    public record Level(
            char[][] map,
            char[][] fog,
            int stairsX,
            int stairsY,
            int startRoomIndex,
            List<Room> rooms,
            List<CorridorHorizontal> hor,
            List<CorridorVertical> ver,
            List<Enemy> enemies,
            List<Item> items,
            List<Inventory> inventories
    ) {}

    public Level generate(int levelIndex, long seed, int balance) {
        Random rnd = new Random(seed);

        RoomGenerator roomGen = new SimpleRoomGenerator();
        CorridorHorizontalGenerator hGen = new SimpleCorridorHorizontalGenerator();
        CorridorVerticalGenerator vGen = new SimpleCorridorVerticalGenerator();

        List<Room> rooms = roomGen.generateRooms();
        List<CorridorHorizontal> hor = hGen.corridorHorizontalGenerator(rooms);
        List<CorridorVertical> ver = vGen.corridorVerticalGenerator(rooms);

        char[][] map = new char[WORLD_H][WORLD_W];
        char[][] fog = new char[WORLD_H][WORLD_W];
        fill(map);
        fill(fog);

        carveRooms(map, rooms);
        carveHor(map, hor);
        carveVer(map, ver);

        List<Integer>[] graph = buildGraphFromGeometry(rooms, hor, ver);

        int startRoomIndex = rnd.nextInt(rooms.size());
        System.out.println("chosen startRoom=" + startRoomIndex);

        BfsResult bfs = bfsRooms(graph, startRoomIndex);
        int endRoomIndex = chooseEndRoom(bfs.dist);

        List<Item> items = new ArrayList<>();
        placeLocksAndKeys(map, rooms, hor, ver, graph, bfs, startRoomIndex, endRoomIndex, rnd, items);

        System.out.println("===== MAP =====");
        for (int y = 0; y < WORLD_H; y++) {
            for (int x = 0; x < WORLD_W; x++) {
                System.out.print(map[y][x]);
            }
            System.out.println();
        }
        System.out.println("===============");

        // туман после расстановки комнат/дверей
        carveFog(fog, rooms);

        // ставим лестницу в центре конечной комнаты
        Room end = rooms.get(endRoomIndex);
        int sx = end.getRoomX() + end.getRoomWidth() / 2;
        int sy = end.getRoomY() + end.getRoomHeight() / 2;
        map[sy][sx] = '>';

        List<Enemy> enemies = spawnEnemies(map, rooms, startRoomIndex, levelIndex, rnd);
        items.addAll(createItems(map, rooms, startRoomIndex, levelIndex, rnd, balance));

        List<Inventory> inventories = new ArrayList<>();

        return new Level(map, fog, sx, sy, startRoomIndex, rooms, hor, ver, enemies, items, inventories);
    }

    private record BfsResult(int[] dist, int[] parent) {}

    private static List<Item> createItems(char[][] map,
                                          List<Room> rooms,
                                          int startRoomIndex,
                                          int levelIndex,
                                          Random rnd,
                                          int balance) {
        List<Item> result = new ArrayList<>();
        Set<Long> occupied = new HashSet<>();

        int maxPerRoom = Math.min(5, 1 + levelIndex / 7);
        for (int i = 1; i < rooms.size(); i++) {
            if (i == startRoomIndex) continue;
            Room r = rooms.get(i);
            int count = rnd.nextInt(maxPerRoom + 1);
            for (int k = 0; k < count + balance; k++) {
                for (int attempt = 0; attempt < 30; attempt++) {
                    int x = r.getRoomX() + 1 + rnd.nextInt(Math.max(1, r.getRoomWidth() - 2));
                    int y = r.getRoomY() + 1 + rnd.nextInt(Math.max(1, r.getRoomHeight() - 2));

                    if (!inBounds(map, x, y)) continue;
                    if (map[y][x] != '.') continue;

                    long key = (((long) x) << 32) ^ (y & 0xffffffffL);
                    if (occupied.contains(key)) continue;

                    int item = rnd.nextInt(9);
                    int param = (rnd.nextInt(10) + 1) + levelIndex;
                    if (balance > 0) {
                        item = 0;
                        balance--;
                    }
                    switch (item) {
                        case 0: result.add(new Item(Item.TypeItem.FOOD, x, y, param, r.getRoomNumber())); break;
                        case 1: result.add(new Item(Item.TypeItem.ELIXIR_Strength, x, y, param, r.getRoomNumber())); break;
                        case 2: result.add(new Item(Item.TypeItem.ELIXIR_Dexterity, x, y, param, r.getRoomNumber())); break;
                        case 3: result.add(new Item(Item.TypeItem.ELIXIR_MaxHP, x, y, param, r.getRoomNumber())); break;
                        case 4: result.add(new Item(Item.TypeItem.SCROLL_Strength, x, y, param, r.getRoomNumber())); break;
                        case 5: result.add(new Item(Item.TypeItem.SCROLL_Dexterity, x, y, param, r.getRoomNumber())); break;
                        case 6: result.add(new Item(Item.TypeItem.SCROLL_MaxHP, x, y, param, r.getRoomNumber())); break;
                        case 7: result.add(new Item(Item.TypeItem.WEAPON_Sword, x, y, param, r.getRoomNumber())); break;
                        case 8: result.add(new Item(Item.TypeItem.WEAPON_Gun, x, y, param, r.getRoomNumber())); break;
                    }
                    occupied.add(key);
                    break;
                }
            }
        }
        return result;
    }

    private static List<Enemy> spawnEnemies(char[][] map,
                                            List<Room> rooms,
                                            int startRoomIndex,
                                            int levelIndex,
                                            Random rnd) {
        List<Enemy> result = new ArrayList<>();
        Set<Long> occupied = new HashSet<>();

        int maxPerRoom = Math.min(3, 1 + levelIndex / 7); // 1..3
        for (int i = 0; i < rooms.size(); i++) {
            if (i == startRoomIndex) continue;
            Room r = rooms.get(i);
            int count = rnd.nextInt(maxPerRoom + 1); // 0..maxPerRoom

            for (int k = 0; k < count; k++) {
                for (int attempt = 0; attempt < 30; attempt++) {
                    int x = r.getRoomX() + 1 + rnd.nextInt(Math.max(1, r.getRoomWidth() - 2));
                    int y = r.getRoomY() + 1 + rnd.nextInt(Math.max(1, r.getRoomHeight() - 2));

                    if (!inBounds(map, x, y)) continue;
                    if (map[y][x] != '.') continue;

                    long key = (((long) x) << 32) ^ (y & 0xffffffffL);
                    if (occupied.contains(key)) continue;

                    Enemy.Type type = rollEnemyType(levelIndex, rnd);
                    result.add(new Enemy(type, levelIndex, x, y, r.getRoomNumber()));
                    occupied.add(key);
                    break;
                }
            }
        }
        return result;
    }

    private static Enemy.Type rollEnemyType(int level, Random rnd) {
        int p = rnd.nextInt(100);
        if (level <= 5) {
            if (p < 55) return Enemy.Type.ZOMBIE;
            if (p < 80) return Enemy.Type.GHOST;
            if (p < 95) return Enemy.Type.VAMPIRE;
            return Enemy.Type.MIMIC;
        }
        if (level <= 12) {
            if (p < 35) return Enemy.Type.ZOMBIE;
            if (p < 60) return Enemy.Type.VAMPIRE;
            if (p < 80) return Enemy.Type.MAGIC_SNAKE;
            if (p < 95) return Enemy.Type.OGRE;
            return Enemy.Type.MIMIC;
        }
        if (p < 25) return Enemy.Type.VAMPIRE;
        if (p < 50) return Enemy.Type.OGRE;
        if (p < 75) return Enemy.Type.MAGIC_SNAKE;
        if (p < 90) return Enemy.Type.GHOST;
        return Enemy.Type.MIMIC;
    }

    private static void fill(char[][] map) {
        for (int y = 0; y < map.length; y++)
            for (int x = 0; x < map[0].length; x++)
                map[y][x] = '▓';
    }

    private static void carveRooms(char[][] map, List<Room> rooms) {
        for (Room r : rooms) {
            for (int y = r.getRoomY(); y < r.getRoomY() + r.getRoomHeight(); y++) {
                for (int x = r.getRoomX(); x < r.getRoomX() + r.getRoomWidth(); x++) {
                    if (inBounds(map, x, y)) map[y][x] = '.';
                }
            }
        }
        rooms.forEach(room -> {
            for (int i = room.getRoomY(); i < room.getRoomY() + room.getRoomHeight(); i++) {
                map[i][room.getRoomX() - 1] = '║';
                map[i][room.getRoomX() + room.getRoomWidth()] = '║';
            }
            for (int i = room.getRoomX(); i < room.getRoomX() + room.getRoomWidth(); i++) {
                map[room.getRoomY() - 1][i] = '═';
                map[room.getRoomY() + room.getRoomHeight()][i] = '═';
            }
            map[room.getRoomY() - 1][room.getRoomX() - 1] = '╔';
            map[room.getRoomY() - 1][room.getRoomX() + room.getRoomWidth()] = '╗';
            map[room.getRoomY() + room.getRoomHeight()][room.getRoomX() - 1] = '╚';
            map[room.getRoomY() + room.getRoomHeight()][room.getRoomX() + room.getRoomWidth()] = '╝';
        });
    }

    private static void carveHor(char[][] map, List<CorridorHorizontal> list) {
        for (CorridorHorizontal c : list) {
            int start = c.getStart(), finish = c.getFinish(), center = c.getCenter();
            int left = c.getLeft_side(), right = c.getRight_side();

            for (int x = start; x < finish; x++) {
                if (x == center) {
                    while (left != right) {
                        if (inBounds(map, x, left)) map[left][x] = '.';
                        left = (left < right) ? left + 1 : left - 1;
                    }
                }
                if (inBounds(map, x, left)) map[left][x] = '.';
            }
        }
    }

    private static void carveVer(char[][] map, List<CorridorVertical> list) {
        for (CorridorVertical c : list) {
            int start = c.getStart(), finish = c.getFinish(), center = c.getCenter();
            int left = c.getLeft_side(), right = c.getRight_side();

            for (int y = start; y < finish; y++) {
                if (y == center) {
                    while (left != right) {
                        if (inBounds(map, left, y)) map[y][left] = '.';
                        left = (left < right) ? left + 1 : left - 1;
                    }
                }
                if (inBounds(map, left, y)) map[y][left] = '.';
            }
        }
    }

    private static void carveFog(char[][] fog, List<Room> rooms) {
        rooms.forEach(room -> {
            char ch = (char) ('0' + room.getRoomNumber());
            for (int y = room.getRoomY() - 1; y < room.getRoomY() + room.getRoomHeight() + 1; y++)
                for (int x = room.getRoomX() - 1; x < room.getRoomX() + room.getRoomWidth() + 1; x++)
                    fog[y][x] = ch;
        });
    }

    private static boolean inBounds(char[][] map, int x, int y) {
        return y >= 0 && y < map.length && x >= 0 && x < map[0].length;
    }

    @SuppressWarnings("unchecked")
    private static List<Integer>[] buildGraphFromGeometry(List<Room> rooms,
                                                          List<CorridorHorizontal> hor,
                                                          List<CorridorVertical> ver) {
        int n = rooms.size();
        List<Integer>[] graph = new ArrayList[n];
        for (int i = 0; i < n; i++) graph[i] = new ArrayList<>();

        // Горизонтальные коридоры
        for (CorridorHorizontal c : hor) {
            java.util.Set<Integer> touched = new java.util.HashSet<>();

            int startX = c.getStart();
            int finishX = c.getFinish();

            // Левая ветка коридора (ряд left_side)
            int y1 = c.getLeft_side();
            for (int i = 0; i < rooms.size(); i++) {
                if (touchesRoomHoriz(rooms.get(i), y1, startX, finishX)) {
                    touched.add(i);
                }
            }

            // Правая ветка коридора (ряд right_side), если отличается
            int y2 = c.getRight_side();
            if (y2 != y1) {
                for (int i = 0; i < rooms.size(); i++) {
                    if (touchesRoomHoriz(rooms.get(i), y2, startX, finishX)) {
                        touched.add(i);
                    }
                }
            }

            addEdges(graph, touched);
        }

        // Вертикальные коридоры
        for (CorridorVertical c : ver) {
            java.util.Set<Integer> touched = new java.util.HashSet<>();

            int startY = c.getStart();
            int finishY = c.getFinish();

            // Верхняя часть (x = left_side)
            int x1 = c.getLeft_side();
            for (int i = 0; i < rooms.size(); i++) {
                if (touchesRoomVert(rooms.get(i), x1, startY, finishY)) {
                    touched.add(i);
                }
            }

            // Нижняя часть (x = right_side), если отличается
            int x2 = c.getRight_side();
            if (x2 != x1) {
                for (int i = 0; i < rooms.size(); i++) {
                    if (touchesRoomVert(rooms.get(i), x2, startY, finishY)) {
                        touched.add(i);
                    }
                }
            }

            addEdges(graph, touched);
        }

        return graph;
    }

    private static void addEdges(List<Integer>[] graph, java.util.Set<Integer> touched) {
        java.util.List<Integer> list = new java.util.ArrayList<>(touched);
        for (int i = 0; i < list.size(); i++) {
            for (int j = i + 1; j < list.size(); j++) {
                int a = list.get(i);
                int b = list.get(j);
                graph[a].add(b);
                graph[b].add(a);
            }
        }
    }

    private static boolean touchesRoomHoriz(Room r, int y, int startX, int finishX) {
        // Интерьер по Y
        int topInterior = r.getRoomY();
        int bottomInterior = r.getRoomY() + r.getRoomHeight() - 1;
        if (y < topInterior || y > bottomInterior) return false;

        int leftWall = r.getRoomX() - 1;
        int rightWall = r.getRoomX() + r.getRoomWidth();

        // Горизонтальный отрезок коридора
        int x0 = startX;
        int x1 = finishX - 1;

        // Коридор должен проходить хотя бы через одну вертикальную стену
        return (leftWall >= x0 && leftWall <= x1) ||
                (rightWall >= x0 && rightWall <= x1);
    }

    private static boolean touchesRoomVert(Room r, int x, int startY, int finishY) {
        // Интерьер по X
        int leftInterior = r.getRoomX();
        int rightInterior = r.getRoomX() + r.getRoomWidth() - 1;
        if (x < leftInterior || x > rightInterior) return false;

        int topWall = r.getRoomY() - 1;
        int bottomWall = r.getRoomY() + r.getRoomHeight();

        int y0 = startY;
        int y1 = finishY - 1;

        return (topWall >= y0 && topWall <= y1) ||
                (bottomWall >= y0 && bottomWall <= y1);
    }

    private static BfsResult bfsRooms(List<Integer>[] graph, int start) {
        int n = graph.length;
        int[] dist = new int[n];
        int[] parent = new int[n];
        Arrays.fill(dist, -1);
        Arrays.fill(parent, -1);

        ArrayDeque<Integer> q = new ArrayDeque<>();
        dist[start] = 0;
        q.add(start);

        while (!q.isEmpty()) {
            int v = q.poll();
            for (int to : graph[v]) {
                if (dist[to] == -1) {
                    dist[to] = dist[v] + 1;
                    parent[to] = v;
                    q.add(to);
                }
            }
        }

        return new BfsResult(dist, parent);
    }

    private static int chooseEndRoom(int[] dist) {
        System.out.println("dist by room:");
        int end = 0;
        for (int i = 0; i < dist.length; i++) {
            System.out.println("room " + i + " dist=" + dist[i]);
            if (dist[i] > dist[end]) end = i;
        }
        System.out.println("chosen endRoom=" + end);
        return end;
    }

    private static void placeLocksAndKeys(char[][] map,
                                          List<Room> rooms,
                                          List<CorridorHorizontal> hor,
                                          List<CorridorVertical> ver,
                                          List<Integer>[] graph,
                                          BfsResult bfs,
                                          int startRoom,
                                          int endRoom,
                                          Random rnd,
                                          List<Item> items) {
        int[] dist = bfs.dist();
        int[] parent = bfs.parent();

        List<Integer> path = new ArrayList<>();
        for (int v = endRoom; v != -1; v = parent[v]) {
            path.add(v);
            if (v == startRoom) break;
        }
        if (path.size() <= 1) return;

        for (int i = 0; i + 1 < path.size(); i++) {
            int roomB = path.get(i);
            int roomA = path.get(i + 1);

            if (roomA == startRoom) continue;

            char doorChar = switch (rnd.nextInt(3)) {
                case 0 -> 'R';
                case 1 -> 'B';
                default -> 'Y';
            };

            boolean placed = placeDoorBetweenRooms(map, rooms, hor, ver, roomA, roomB, doorChar, rnd);
            if (!placed) continue;

            List<Integer> candidateRooms = new ArrayList<>();
            for (int r = 0; r < rooms.size(); r++) {
                if (dist[r] >= 0 && dist[r] < dist[roomA]) {
                    candidateRooms.add(r);
                }
            }
            if (candidateRooms.isEmpty()) continue;

            int keyRoomIndex = candidateRooms.get(rnd.nextInt(candidateRooms.size()));
            Room keyRoom = rooms.get(keyRoomIndex);

            Item.TypeItem keyType = switch (doorChar) {
                case 'R' -> Item.TypeItem.KEY_RED;
                case 'B' -> Item.TypeItem.KEY_BLUE;
                case 'Y' -> Item.TypeItem.KEY_YELLOW;
                default -> null;
            };
            if (keyType == null) continue;

            placeKeyInRoom(map, keyRoom, keyType, items, rnd);
        }
    }

    private static boolean placeDoorBetweenRooms(char[][] map,
                                                 List<Room> rooms,
                                                 List<CorridorHorizontal> hor,
                                                 List<CorridorVertical> ver,
                                                 int roomA,
                                                 int roomB,
                                                 char doorChar,
                                                 Random rnd) {
        Room a = rooms.get(roomA);
        Room b = rooms.get(roomB);

        // Центры комнат
        int ax = a.getRoomX() + a.getRoomWidth() / 2;
        int ay = a.getRoomY() + a.getRoomHeight() / 2;
        int bx = b.getRoomX() + b.getRoomWidth() / 2;
        int by = b.getRoomY() + b.getRoomHeight() / 2;

        java.util.List<int[]> candidates = new java.util.ArrayList<>();

        // Сильнее различается по X → считаем, что комнаты соединены по горизонтали
        if (Math.abs(ax - bx) >= Math.abs(ay - by)) {
            boolean toRight = bx > ax;
            int wallX = toRight ? a.getRoomX() + a.getRoomWidth() : a.getRoomX() - 1;
            int corX  = wallX + (toRight ? 1 : -1);

            for (int y = a.getRoomY(); y < a.getRoomY() + a.getRoomHeight(); y++) {
                if (!inBounds(map, wallX, y)) continue;
                if (map[y][wallX] != '.') continue;      // в стене должно быть отверстие
                if (!inBounds(map, corX, y)) continue;
                if (map[y][corX] != '.') continue;       // а за стеной — коридор

                candidates.add(new int[]{wallX, y, corX, y});
            }
        } else {
            // Сильнее различается по Y → соединение по вертикали
            boolean toBottom = by > ay;
            int wallY = toBottom ? a.getRoomY() + a.getRoomHeight() : a.getRoomY() - 1;
            int corY  = wallY + (toBottom ? 1 : -1);

            for (int x = a.getRoomX(); x < a.getRoomX() + a.getRoomWidth(); x++) {
                if (!inBounds(map, x, wallY)) continue;
                if (map[wallY][x] != '.') continue;      // отверстие в горизонтальной стене
                if (!inBounds(map, x, corY)) continue;
                if (map[corY][x] != '.') continue;       // коридор снаружи

                candidates.add(new int[]{x, wallY, x, corY});
            }
        }

        if (candidates.isEmpty()) {
            System.out.println("doorCandidates empty (by wall scan) for rooms " + roomA + " and " + roomB);
            return false;
        }

        int[] c = candidates.get(rnd.nextInt(candidates.size()));
        int wallX = c[0], wallY = c[1];
        int corX  = c[2], corY = c[3];

        map[wallY][wallX] = doorChar;

        System.out.println("Placing door " + doorChar + " between rooms " + roomA + " and " + roomB +
                " at wall (" + wallX + "," + wallY + ") and corridor (" + corX + "," + corY + ")");
        return true;
    }

    private static void placeKeyInRoom(char[][] map,
                                       Room room,
                                       Item.TypeItem keyType,
                                       List<Item> items,
                                       Random rnd) {
        for (int attempt = 0; attempt < 50; attempt++) {
            int x = room.getRoomX() + 1 + rnd.nextInt(Math.max(1, room.getRoomWidth() - 2));
            int y = room.getRoomY() + 1 + rnd.nextInt(Math.max(1, room.getRoomHeight() - 2));

            if (!inBounds(map, x, y)) continue;
            if (map[y][x] != '.') continue;

            boolean occupied = false;
            for (Item it : items) {
                if (it.getX() == x && it.getY() == y) {
                    occupied = true;
                    break;
                }
            }
            if (occupied) continue;

            items.add(new Item(keyType, x, y, 0, room.getRoomNumber()));
            return;
        }
    }

}
