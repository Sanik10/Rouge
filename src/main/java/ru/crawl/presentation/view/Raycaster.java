package ru.crawl.presentation.view;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;

public final class Raycaster {
    public static final int SCREEN_WIDTH = 100;
    public static final int SCREEN_HEIGHT = 40;
    public static final float MAX_DEPTH = 12.0f;
    private static final float STEP = 0.2f;

    private static final char[] STONE_SHADES = { '#', '#', '+', ':', '.' };

    public static final class RayHit {
        private final float distance;
        private final char type;

        public RayHit(float distance, char type) {
            this.distance = distance;
            this.type = type;
        }

        public float distance() { return distance; }
        public char type() { return type; }
    }

    // helper: учитываем fog
    private static char sampleCell(char[][] map, char[][] fog, int x, int y) {
        char fogCell = fog[y][x];
        // видно, если '.' или номер комнаты
        boolean visible = (fogCell == '.') || (fogCell >= '0' && fogCell <= '9');
        if (!visible) {
            return '▓'; // туман
        }
        return map[y][x];
    }

    public static RayHit castRay(char[][] map,
                                 char[][] fog,
                                 float playerX,
                                 float playerY,
                                 float rayAngle) {
        float distance = 0.0f;

        while (distance < MAX_DEPTH) {
            distance += STEP;

            float rayX = playerX + 0.5f + (float) Math.sin(rayAngle) * distance;
            float rayY = playerY + 0.5f + (float) Math.cos(rayAngle) * distance;

            int mapX = (int) rayX;
            int mapY = (int) rayY;

            if (mapY < 0 || mapY >= map.length ||
                    mapX < 0 || mapX >= map[0].length) {
                return new RayHit(distance, '#');
            }

            char cell = sampleCell(map, fog, mapX, mapY);

            if (cell == '▓') {
                // туман – считаем как особую "стену тумана"
                return new RayHit(distance, '▓');
            }
            if (cell == '#') return new RayHit(distance, '#');
            if (cell != '.' && cell != ' ') return new RayHit(distance, cell);
        }

        return new RayHit(MAX_DEPTH, '#');
    }

    public static int calculateHeight(float distance) {
        if (distance <= 0.1f) distance = 0.1f;
        int height = (int) (SCREEN_HEIGHT / distance);
        return Math.min(height, SCREEN_HEIGHT);
    }

    public static TextColor getWallColor(float distance, char type) {
        if (type == '▓') {
            return TextColor.ANSI.BLACK_BRIGHT;
        }
        if (distance < MAX_DEPTH * 0.25f) return TextColor.ANSI.WHITE_BRIGHT;
        if (distance < MAX_DEPTH * 0.5f)  return TextColor.ANSI.CYAN_BRIGHT;
        if (distance < MAX_DEPTH * 0.75f) return TextColor.ANSI.MAGENTA;
        return TextColor.ANSI.BLUE;
    }

    private static void drawFloorPixel(TextGraphics g, int x, int y) {
        float gradient = (y - (float)SCREEN_HEIGHT / 2) / ((float) SCREEN_HEIGHT / 2);

        TextColor color;
        if (gradient > 0.3f)      color = TextColor.ANSI.WHITE;
        else if (gradient > 0.1f) color = TextColor.ANSI.CYAN;
        else                      color = TextColor.ANSI.BLUE;

        char floorChar;
        if (gradient > 0.8f)      floorChar = '_';
        else if (gradient > 0.4f) floorChar = '-';
        else                      floorChar = '.';

        g.setForegroundColor(color);
        g.setCharacter(x, y, floorChar);
    }

    private static char getWallChar(float distance, char type) {
        if (type == '▓') {
            return '▓';
        }

        float t = Math.min(1f, distance / MAX_DEPTH);
        int idx = (int)(t * (STONE_SHADES.length - 1));
        return STONE_SHADES[idx];
    }

    public static void drawColumn(TextGraphics g, int x, RayHit hit) {
        float distance = hit.distance();
        char type = hit.type();

        int wallHeight = calculateHeight(distance);
        int wallTop = (SCREEN_HEIGHT - wallHeight) / 2;
        int wallBottom = wallTop + wallHeight;

        // потолок
        for (int y = 0; y < wallTop; y++) {
            float v = (float) y / wallTop; // 0 вверху, 1 у стены

            char ceilingChar;
            if (v < 0.3f)      ceilingChar = ' ';  // почти тьма
            else if (v < 0.7f) ceilingChar = '.';  // редкие точки
            else               ceilingChar = '`';  // у стыка со стеной

            TextColor color;
            if (v < 0.3f)      color = TextColor.ANSI.BLUE;         // далеко - темно-синий
            else if (v < 0.7f) color = TextColor.ANSI.BLUE_BRIGHT;  // чуть ярче
            else               color = TextColor.ANSI.CYAN;         // у стыка

            g.setForegroundColor(color);
            g.setCharacter(x, y, ceilingChar);
        }

        // стена или туман
        TextColor wallColor = getWallColor(distance, type);
        g.setForegroundColor(wallColor);
        char wallChar = getWallChar(distance, type);
        for (int y = wallTop; y < wallBottom; y++) {
            g.setCharacter(x, y, wallChar);
        }

        // пол
        for (int y = wallBottom; y < SCREEN_HEIGHT; y++) {
            drawFloorPixel(g, x, y);
        }

        // Двери и лестница: рисуем букву поверх стены
        if (type == 'R' || type == 'B' || type == 'Y' || type == '>') {
            int spriteHeight = wallHeight / 2;
            int spriteTop = (SCREEN_HEIGHT - spriteHeight) / 2;

            g.setForegroundColor(LanternaView.setColor(type));
            for (int y = spriteTop; y < spriteTop + spriteHeight && y < SCREEN_HEIGHT; y++) {
                if (y < 0) continue;
                g.setCharacter(x, y, type);
            }
        }
    }
}