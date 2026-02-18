package ru.crawl.presentation.view;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import ru.crawl.domain.model.Enemy;
import ru.crawl.domain.model.Inventory;
import ru.crawl.domain.model.Item;
import ru.crawl.domain.model.Player;
import ru.crawl.domain.usecase.GameSnapshot;
import ru.crawl.presentation.input.UserInput;

import java.util.List;

public final class LanternaView implements View {
    private Screen screen = null;
    private final int worldW;
    private final int worldH;

    // layout
    private static final int PAD_X = 2;
    private static final int PAD_Y = 1;
    private static final int HUD_W = 30;
    private static final int LOG_H = 4;

    public LanternaView(int worldW, int worldH) throws Exception {
        this.worldW = worldW;
        this.worldH = worldH;
    }

    @Override
    public UserInput pollInput() throws Exception {
        KeyStroke ks = screen.pollInput();
        if (ks == null) return null;

        if (ks.getKeyType() == KeyType.Escape) return new UserInput(UserInput.Type.ESC, null);
        if (ks.getKeyType() == KeyType.Enter)  return new UserInput(UserInput.Type.ENTER, null);

        if (ks.getKeyType() == KeyType.Character && ks.getCharacter() != null) {
            return new UserInput(UserInput.Type.CHAR, Character.toLowerCase(ks.getCharacter()));
        }
        return null;
    }

    public void renderMapAndEntities(GameSnapshot snap,
                                     TextGraphics g,
                                     int mapX0,
                                     int mapY0,
                                     int maxWidth,
                                     int maxHeight) {
        char[][] map = snap.map();
        char[][] fog = snap.fog();
        List<Enemy> enemies = snap.enemies();
        List<Item> items = snap.items();

        TerminalSize size = screen.getTerminalSize();
        int cols = size.getColumns();
        int rows = size.getRows();

        Player player = snap.player();
        int playerX = player.getX();
        int playerY = player.getY();
        int playerRoomIndex;

        int viewW = Math.min(worldW, Math.max(10, maxWidth));
        int viewH = Math.min(worldH, Math.max(5, maxHeight));

        int camX = clamp(playerX - viewW / 2, 0, worldW - viewW);
        int camY = clamp(playerY - viewH / 2, 0, worldH - viewH);

        // обновляем fog
        FogUtils.changeFog(map, fog, playerX, playerY, player);
        playerRoomIndex = player.getRoomIndex();

        // 1) карта + игрок
        g.setBackgroundColor(TextColor.ANSI.BLACK_BRIGHT);
        g.setForegroundColor(TextColor.ANSI.BLACK);
        for (int sy = 0; sy < viewH; sy++) {
            int wy = camY + sy;
            for (int sx = 0; sx < viewW; sx++) {
                int wx = camX + sx;

                char ch_map = map[wy][wx];
                char ch_fog = fog[wy][wx];
                if (wx == playerX && wy == playerY) {
                    g.setForegroundColor(TextColor.ANSI.GREEN);
                    ch_map = '☺';
                }
                if ((ch_fog >= '0' && ch_fog <= '9' && player.getVisitedRooms().contains(ch_fog - 48)) || ch_fog == '.')
                    g.setCharacter(mapX0 + sx, mapY0 + sy, ch_map);
                else
                    g.setCharacter(mapX0 + sx, mapY0 + sy, '▓');
                g.setForegroundColor(TextColor.ANSI.BLACK);
            }
        }

        // 2) Предметы
                // 2) Предметы
        items.forEach(item -> {
            int ix = item.getX();
            int iy = item.getY();

            char ch = charForItemType(item.getType());

            if (playerRoomIndex == item.getRoomIndex() || isPlayerNear(map, playerX, playerY, ix, iy)) {
                int sx = ix - camX;
                int sy = iy - camY;
                g.setForegroundColor(setColor(ch));
                char chFog = fog[iy][ix];
                if ((chFog >= '0' && chFog <= '9' || chFog == '.') && item.getItemIsFind())
                    g.setCharacter(mapX0 + sx, mapY0 + sy, ch);
            }
        });

        // 3) Враги (поверх пола/стен, но под игроком — если совпадёт, игрок рисуется выше)
        for (Enemy e : enemies) {
            if (e == null || !e.isAlive()) continue;

            int ex = e.getX();
            int ey = e.getY();

            if (ex < camX || ex >= camX + viewW || ey < camY || ey >= camY + viewH) continue;

            char ch;
            TextColor color;

            if (e.getType() == Enemy.Type.MIMIC && e.isDisguised()) {
                Item.TypeItem fake = e.getDisguiseItemType();
                if (fake == null) {
                    ch = '?';
                    color = TextColor.ANSI.YELLOW;
                } else {
                    ch = charForItemType(fake);
                    color = setColor(ch);
                }
            } else {
                ch = switch (e.getType()) {
                    case ZOMBIE      -> 'z';
                    case VAMPIRE     -> 'v';
                    case GHOST       -> 'g';
                    case OGRE        -> 'O';
                    case MAGIC_SNAKE -> 's';
                    case MIMIC       -> 'm';
                };
                color = setColor(ch);
            }

            if (playerRoomIndex == e.getRoomIndex() || isPlayerNear(map, playerX, playerY, e.getX(), e.getY())) {
                g.setForegroundColor(color);
                if (e.getType() == Enemy.Type.GHOST && e.getVisible() > 3) {
                    g.setForegroundColor(TextColor.ANSI.BLACK);
                    ch = '.';
                }

                int sx = ex - camX;
                int sy = ey - camY;

                if (ex == playerX && ey == playerY) continue;

                char chFog = fog[ey][ex];
                if (chFog >= '0' && chFog <= '9' || chFog == '.')
                    g.setCharacter(mapX0 + sx, mapY0 + sy, ch);
            }
        }
    }

    @Override
    public void render(GameSnapshot snap) throws Exception {
        screen.doResizeIfNecessary();
        screen.clear();
        TextGraphics g = screen.newTextGraphics();

        TerminalSize size = screen.getTerminalSize();
        int cols = size.getColumns();
        int rows = size.getRows();

        Player player = snap.player();
        int playerX = player.getX();
        int playerY = player.getY();

        int viewW = Math.min(worldW, Math.max(10, cols - HUD_W - PAD_X * 2 - 1));
        int viewH = Math.min(worldH, Math.max(5, rows - LOG_H - PAD_Y * 2 - 2));

        int mapX0 = PAD_X;
        int mapY0 = PAD_Y;

        renderMapAndEntities(snap, g, mapX0, mapY0, viewW, viewH);

        // 4) HUD
        int hudX = mapX0 + viewW + 3;
        int hudY = mapY0;

        g.setBackgroundColor(TextColor.ANSI.BLACK);
        g.setForegroundColor(TextColor.ANSI.GREEN);
        g.putString(hudX, hudY, "Crawl (Lanterna)");
        g.putString(hudX, hudY + 1, "Turn: " + snap.turnNumber());
        g.putString(hudX, hudY + 2, "Pos: (" + playerX + "," + playerY + ")");
        g.putString(hudX, hudY + 3, "HP: " + player.getHp() + "/" + player.getMaxHp());
        g.putString(hudX, hudY + 4, "Dexterity: " + player.getDex());
        g.putString(hudX, hudY + 5, "Strength: " + player.getStrength());
        g.putString(hudX, hudY + 6, "Treasure: " + player.getTreasure());
        g.putString(hudX, hudY + 7, "WASD move");
        g.putString(hudX, hudY + 8, "Esc/Q quit");
        g.putString(hudX, hudY + 9, "Level: " + snap.levelIndex());
        g.putString(hudX, hudY + 10, "Pos: (" + playerX + "," + playerY + ")");

        if (snap.targetName() != null) {
            g.putString(hudX, hudY + 12, "Target: " + snap.targetName());
            g.putString(hudX, hudY + 13, "EHP: " + snap.targetHp() + "/" + snap.targetMaxHp());
        }

        // 5) Log
        List<String> log = snap.log();
        int logY = mapY0 + viewH + 1;
        g.putString(mapX0, logY, "Log:");

        int maxLines = Math.max(0, LOG_H - 2);
        int start = Math.max(0, log.size() - maxLines);

        for (int i = 0; i < maxLines; i++) {
            int idx = start + i;
            if (idx >= log.size()) break;
            g.putString(mapX0, logY + 1 + i, "> " + log.get(idx));
        }

        screen.refresh();
    }


    public void showInventory(GameSnapshot snapshot) throws Exception {
        int startI = 30, startJ = 5, maxJ = 9, maxI = 100, pos = 1;
        TextGraphics g = screen.newTextGraphics();
        g.setForegroundColor(TextColor.ANSI.GREEN);
        List<Inventory> inventories = snapshot.inventories();
        int len = inventories.size();
        maxJ += len;
        for (int i = startI; i < maxI; i++) {
            for (int j = startJ; j < maxJ; j++) {
                g.setCharacter(i, j, ' ');
                g.setCharacter(i, startJ, '═');
                g.setCharacter(i, maxJ - 1, '═');
                g.setCharacter(startI, j, '║');
                g.setCharacter(maxI - 1, j, '║');
            }
        }
        g.setCharacter(startI, startJ, '╔');
        g.setCharacter(maxI - 1, startJ, '╗');
        g.setCharacter(startI, maxJ - 1, '╚');
        g.setCharacter(maxI - 1, maxJ - 1, '╝');
        g.putString(startI + 15, startJ + 1, "Inventory:");
        g.putString(startI + 5, startJ + 2, "Use: h-weapon, j-food, k-elixir, e-scroll");
        startJ += 3;
        for (Inventory inventory : inventories) {
            g.putString(startI + 5, startJ, pos + ". " + inventory.toString());
            startJ++;
            pos++;
        }
        screen.refresh();
    }

    public void showInventoryMenu(java.util.List<String> lines, String title, String help) throws Exception {
        int startI = 30;
        int startJ = 5;
        int maxI = 100;
        int maxJ = startJ + 4 + lines.size();

        TextGraphics g = screen.newTextGraphics();
        g.setForegroundColor(TextColor.ANSI.GREEN);

        for (int i = startI; i < maxI; i++) {
            for (int j = startJ; j < maxJ; j++) {
                g.setCharacter(i, j, ' ');
                g.setCharacter(i, startJ, '═');
                g.setCharacter(i, maxJ - 1, '═');
                g.setCharacter(startI, j, '║');
                g.setCharacter(maxI - 1, j, '║');
            }
        }

        g.setCharacter(startI, startJ, '╔');
        g.setCharacter(maxI - 1, startJ, '╗');
        g.setCharacter(startI, maxJ - 1, '╚');
        g.setCharacter(maxI - 1, maxJ - 1, '╝');

        g.putString(startI + 15, startJ + 1, title);
        g.putString(startI + 5, startJ + 2, help);

        int y = startJ + 3;
        for (String line : lines) {
            g.putString(startI + 5, y++, line);
        }

        screen.refresh();
    }

    static int clamp(int v, int lo, int hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    static TextColor setColor(char ch) {
        return switch (ch) {
            case 'z' -> TextColor.ANSI.GREEN_BRIGHT;
            case '*', 'v' -> TextColor.ANSI.RED;
            case 'm', 'g' -> TextColor.ANSI.WHITE;
            case '§', 'O' -> TextColor.ANSI.YELLOW;
            case 's' -> TextColor.ANSI.WHITE_BRIGHT;
            case '♂' -> TextColor.ANSI.CYAN;
            case '+' -> TextColor.ANSI.MAGENTA;
            case '☼' -> TextColor.ANSI.YELLOW_BRIGHT;
            case 'r' -> TextColor.ANSI.RED;
            case 'b' -> TextColor.ANSI.BLUE;
            case 'y' -> TextColor.ANSI.YELLOW;
            default -> TextColor.ANSI.GREEN;

        };
    }

    private static char charForItemType(Item.TypeItem type) {
        return switch (type) {
            case ELIXIR_Strength,
                ELIXIR_Dexterity,
                ELIXIR_MaxHP -> '♂';
            case FOOD -> '+';
            case SCROLL_Strength,
                SCROLL_Dexterity,
                SCROLL_MaxHP -> '§';
            case WEAPON_Sword,
                WEAPON_Gun -> '*';
            case TREASURE -> '☼';
            case KEY_RED -> 'r';
            case KEY_BLUE -> 'b';
            case KEY_YELLOW -> 'y';
        };
    }

    private boolean isPlayerNear(char[][] map, int playerX, int playerY, int x, int y) {
        boolean result = false;
        for (int i = playerY - 1; i < playerY + 2; i++) {
            if (i >= map.length || i < 0) continue;
            for (int j = playerX - 1; j < playerX + 2; j++) {
                if (j >= map[0].length || j < 0) continue;
                if (i == y && j == x) {
                    result = true;
                    break;
                }
            }
            if (result) break;
        }
        return result;
    }

    @Override
    public void setScreen(Screen screen) {
        this.screen = screen;
    }

    @Override
    public void close() throws Exception {
        screen.close();
    }

    @Override
    public int getWorldW() {
        return worldW;
    }

    @Override
    public int getWorldH() {
        return worldH;
    }
}
