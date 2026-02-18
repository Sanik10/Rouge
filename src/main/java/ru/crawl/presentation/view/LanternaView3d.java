package ru.crawl.presentation.view;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import ru.crawl.domain.model.Direction;
import ru.crawl.domain.model.Enemy;
import ru.crawl.domain.model.Item;
import ru.crawl.domain.model.Player;
import ru.crawl.domain.usecase.GameSnapshot;
import ru.crawl.presentation.input.UserInput;

import java.util.List;

import static ru.crawl.presentation.view.Raycaster.SCREEN_HEIGHT;
import static ru.crawl.presentation.view.Raycaster.SCREEN_WIDTH;

public final class LanternaView3d implements View {
    private LanternaView base2dView;
    private Screen screen;
    private final int worldW;
    private final int worldH;

    private float cameraAngle = 0f;
    private final float fov = (float) (8 * Math.PI / 18); // (float) Math.PI / 3; ~60° (float) (8 * Math.PI / 18); ~80°

    public LanternaView3d(int worldW, int worldH) {
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

    @Override
    public void render(GameSnapshot snap) throws Exception {
        char[][] map = snap.map();
        char[][] fog = snap.fog();
        Player player = snap.player();
        int playerX = player.getX();
        int playerY = player.getY();

        this.cameraAngle = angleFromFacing(snap.facing());

        FogUtils.changeFog(map, fog, playerX, playerY, player);

        screen.doResizeIfNecessary();
        screen.clear();
        TextGraphics g = screen.newTextGraphics();

        TerminalSize size = screen.getTerminalSize();
        int cols = size.getColumns();
        int rows = size.getRows();

        int viewW3d = Math.min(SCREEN_WIDTH, Math.max(10, cols - 40));
        int viewH3d = Math.min(SCREEN_HEIGHT, Math.max(10, rows - 5));

        // 1) 3D
        float[] depthBuffer = new float[viewW3d];

        for (int sx = 0; sx < viewW3d; sx++) {
            float rayAngle = cameraAngle + (fov / 2) - (sx / (float) viewW3d * fov);
            Raycaster.RayHit hit = Raycaster.castRay(map, fog, playerX, playerY, rayAngle);
            depthBuffer[sx] = hit.distance();
            Raycaster.drawColumn(g, sx, hit);
        }

        // 2) Враги!
        List<Enemy> enemies = snap.enemies();

        for (Enemy e : enemies) {
            if (e == null || !e.isAlive()) continue;

            if (e.getType() == Enemy.Type.MIMIC && e.isDisguised()) continue;

            if (e.getType() == Enemy.Type.GHOST && e.getVisible() > 3) {
                continue;
            }

            float ex = e.getX() + 0.5f;
            float ey = e.getY() + 0.5f;

            float dx = ex - (playerX + 0.5f);
            float dy = ey - (playerY + 0.5f);

            float distance = (float) Math.sqrt(dx*dx + dy*dy);
            if (distance < 0.5f || distance > Raycaster.MAX_DEPTH) continue;

            float angleToEnemy = (float) Math.atan2(dx, dy);
            float angleDiff = normalizeAngle(angleToEnemy - cameraAngle);

            if (Math.abs(angleDiff) > fov / 2) continue;

            int spriteScreenX = (int)((fov / 2 - angleDiff) / fov * viewW3d);
            if (spriteScreenX < 0 || spriteScreenX >= viewW3d) continue;

            int spriteHeight = Raycaster.calculateHeight(distance);
            int spriteTop = (SCREEN_HEIGHT - spriteHeight) / 2;
            int spriteBottom = spriteTop + spriteHeight;

            if (distance >= depthBuffer[spriteScreenX]) continue;

            AsciiSprite sprite = EnemySprites.get(e.getType());
            int texW = sprite.getWidth();
            int texH = sprite.getHeight();

            int screenSpriteWidth = Math.max(1, spriteHeight * texW / texH);

            int screenLeft = spriteScreenX - screenSpriteWidth / 2;
            int screenRight = screenLeft + screenSpriteWidth;

            char baseCh = switch (e.getType()) {
                case ZOMBIE      -> 'z';
                case VAMPIRE     -> 'v';
                case GHOST       -> 'g';
                case OGRE        -> 'O';
                case MAGIC_SNAKE -> 's';
                case MIMIC       -> 'm';
            };
            g.setForegroundColor(LanternaView.setColor(baseCh));

            for (int x = screenLeft; x < screenRight; x++) {
                if (x < 0 || x >= viewW3d) continue;

                if (distance >= depthBuffer[x]) continue;

                int columnIndex = x - screenLeft;
                float u = (columnIndex + 0.5f) / (float) screenSpriteWidth;

                for (int sy = spriteTop; sy < spriteBottom; sy++) {
                    if (sy < 0 || sy >= SCREEN_HEIGHT) continue;

                    float v = (sy - spriteTop + 0.5f) / (float) (spriteBottom - spriteTop);

                    int tx = (int)(u * texW);
                    int ty = (int)(v * texH);

                    if (tx < 0) tx = 0;
                    if (tx >= texW) tx = texW - 1;
                    if (ty < 0) ty = 0;
                    if (ty >= texH) ty = texH - 1;

                    char pixel = sprite.charAt(tx, ty);

                    if (pixel == ' ') continue;

                    g.setCharacter(x, sy, pixel);
                }
            }
        }

        // 3) Предметы
        List<Item> items = snap.items();

        for (Item item : items) {
            if (!item.getItemIsFind()) continue;

            int ix = item.getX();
            int iy = item.getY();
            char chFog = fog[iy][ix];

            if (!(chFog == '.' || (chFog >= '0' && chFog <= '9'))) continue;

            float ex = ix + 0.5f;
            float ey = iy + 0.5f;

            float dx = ex - (playerX + 0.5f);
            float dy = ey - (playerY + 0.5f);

            float distance = (float) Math.sqrt(dx*dx + dy*dy);
            if (distance < 0.5f || distance > Raycaster.MAX_DEPTH) continue;

            float angleToItem = (float) Math.atan2(dx, dy);
            float angleDiff = normalizeAngle(angleToItem - cameraAngle);
            if (Math.abs(angleDiff) > fov / 2) continue;

            int spriteScreenX = (int)((fov / 2 - angleDiff) / fov * viewW3d);
            if (spriteScreenX < 0 || spriteScreenX >= viewW3d) continue;

            int spriteHeight = Raycaster.calculateHeight(distance) / 2;
            int spriteBottom = (SCREEN_HEIGHT * 3) / 4;
            int spriteTop = spriteBottom - spriteHeight;

            if (distance >= depthBuffer[spriteScreenX]) continue;

            AsciiSprite sprite = ItemSprites.get(item.getType());
            int texW = sprite.getWidth();
            int texH = sprite.getHeight();

            int screenSpriteHeight = spriteHeight;
            int screenSpriteWidth = Math.max(1, screenSpriteHeight * texW / texH);
            int screenLeft = spriteScreenX - screenSpriteWidth / 2;
            int screenRight = screenLeft + screenSpriteWidth;

            char ch = charForItemType(item.getType());

            g.setForegroundColor(LanternaView.setColor(ch));

            for (int x = screenLeft; x < screenRight; x++) {
                if (x < 0 || x >= viewW3d) continue;
                if (distance >= depthBuffer[x]) continue;

                int columnIndex = x - screenLeft;
                float u = (columnIndex + 0.5f) / (float) screenSpriteWidth;

                for (int sy = spriteTop; sy < spriteBottom; sy++) {
                    if (sy < 0) continue;

                    float v = (sy - spriteTop + 0.5f) / (float) (spriteBottom - spriteTop);

                    int tx = (int)(u * texW);
                    int ty = (int)(v * texH);

                    if (tx < 0) tx = 0;
                    if (tx >= texW) tx = texW - 1;
                    if (ty < 0) ty = 0;
                    if (ty >= texH) ty = texH - 1;

                    char pixel = sprite.charAt(tx, ty);
                    if (pixel == ' ') continue;

                    g.setCharacter(x, sy, pixel);
                }
            }
        }

        // 3b) Мимики, замаскированные под предметы
        for (Enemy e : enemies) {
            if (e == null || !e.isAlive()) continue;
            if (e.getType() != Enemy.Type.MIMIC || !e.isDisguised()) continue;

            int ix = e.getX();
            int iy = e.getY();
            char chFog = fog[iy][ix];

            if (!(chFog == '.' || (chFog >= '0' && chFog <= '9'))) continue;

            Item.TypeItem fakeType = e.getDisguiseItemType();
            if (fakeType == null) continue;

            float ex = ix + 0.5f;
            float ey = iy + 0.5f;

            float dx = ex - (playerX + 0.5f);
            float dy = ey - (playerY + 0.5f);

            float distance = (float) Math.sqrt(dx * dx + dy * dy);
            if (distance < 0.5f || distance > Raycaster.MAX_DEPTH) continue;

            float angleToItem = (float) Math.atan2(dx, dy);
            float angleDiff = normalizeAngle(angleToItem - cameraAngle);
            if (Math.abs(angleDiff) > fov / 2) continue;

            int spriteScreenX = (int)((fov / 2 - angleDiff) / fov * viewW3d);
            if (spriteScreenX < 0 || spriteScreenX >= viewW3d) continue;

            int spriteHeight = Raycaster.calculateHeight(distance) / 2;
            int spriteBottom = (SCREEN_HEIGHT * 3) / 4;
            int spriteTop = spriteBottom - spriteHeight;

            if (distance >= depthBuffer[spriteScreenX]) continue;

            AsciiSprite sprite = ItemSprites.get(fakeType);
            int texW = sprite.getWidth();
            int texH = sprite.getHeight();

            int screenSpriteHeight = spriteHeight;
            int screenSpriteWidth = Math.max(1, screenSpriteHeight * texW / texH);
            int screenLeft = spriteScreenX - screenSpriteWidth / 2;
            int screenRight = screenLeft + screenSpriteWidth;

            char ch = charForItemType(fakeType);
            g.setForegroundColor(LanternaView.setColor(ch));

            for (int x = screenLeft; x < screenRight; x++) {
                if (x < 0 || x >= viewW3d) continue;
                if (distance >= depthBuffer[x]) continue;

                int columnIndex = x - screenLeft;
                float u = (columnIndex + 0.5f) / (float) screenSpriteWidth;

                for (int sy = spriteTop; sy < spriteBottom; sy++) {
                    if (sy < 0) continue;

                    float v = (sy - spriteTop + 0.5f) / (float) (spriteBottom - spriteTop);

                    int tx = (int)(u * texW);
                    int ty = (int)(v * texH);

                    if (tx < 0) tx = 0;
                    if (tx >= texW) tx = texW - 1;
                    if (ty < 0) ty = 0;
                    if (ty >= texH) ty = texH - 1;

                    char pixel = sprite.charAt(tx, ty);
                    if (pixel == ' ') continue;

                    g.setCharacter(x, sy, pixel);
                }
            }
        }

        // 4) Миникарта: просто перерисовываем 2D-вид в маленький прямоугольник справа
        int mapX0 = viewW3d + 2;
        int mapY0 = 1;
        int miniW = Math.min(worldW, cols - mapX0 - 1);
        int miniH = Math.min(worldH, rows - mapY0 - 10);

        if (base2dView != null) {
            base2dView.renderMapAndEntities(snap, g, mapX0, mapY0, miniW, miniH);
        }

        int hudX = mapX0;
        int hudY = mapY0 + miniH + 1;
        g.setBackgroundColor(TextColor.ANSI.BLACK);
        g.setForegroundColor(TextColor.ANSI.GREEN);
        g.putString(hudX, hudY, "Crawl 3D (Lanterna)");
        g.putString(hudX, hudY + 1, "Turn: " + snap.turnNumber());
        g.putString(hudX, hudY + 2, "Pos: (" + player.getX() + "," + player.getY() + ")");
        g.putString(hudX, hudY + 3, "HP: " + player.getHp() + "/" + player.getMaxHp());
        g.putString(hudX, hudY + 4, "Dexterity: " + player.getDex());
        g.putString(hudX, hudY + 5, "Strength: " + player.getStrength());
        g.putString(hudX, hudY + 6, "Treasure: " + player.getTreasure());
        g.putString(hudX, hudY + 7, "WASD move, F toggle 3D");

        if (snap.targetName() != null) {
            g.putString(hudX, hudY + 9, "Target: " + snap.targetName());
            g.putString(hudX, hudY + 10, "EHP: " + snap.targetHp() + "/" + snap.targetMaxHp());
        }

        List<String> log = snap.log();
        int logY = hudY + 12;
        g.putString(mapX0, logY, "Log:");
        int maxLines = Math.max(0, rows - logY - 2);
        int start = Math.max(0, log.size() - maxLines);
        for (int i = 0; i < maxLines; i++) {
            int idx = start + i;
            if (idx >= log.size()) break;
            g.putString(mapX0, logY + 1 + i, "> " + log.get(idx));
        }

        screen.refresh();
    }

    @Override
    public void showInventory(GameSnapshot snapshot) throws Exception {
        // можно потом переиспользовать реализацию из LanternaView
    }

    @Override
    public void close() throws Exception {
        screen.close();
    }

    @Override
    public void setScreen(Screen screen) {
        this.screen = screen;
    }

    @Override
    public int getWorldW() {
        return worldW;
    }

    @Override
    public int getWorldH() {
        return worldH;
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

    private float normalizeAngle(float a) {
        while (a < -Math.PI) a += 2 * Math.PI;
        while (a >  Math.PI) a -= 2 * Math.PI;
        return a;
    }

    private float angleFromFacing(Direction facing) {
        if (facing == null) return cameraAngle;

        // С учётом того, что atan2(dx, dy):
        // DOWN (y+)  -> 0
        // RIGHT (x+) -> +PI/2
        // UP (y-)    -> PI
        // LEFT (x-)  -> -PI/2
        return switch (facing) {
            case UP    -> (float) Math.PI;
            case DOWN  -> 0f;
            case LEFT  -> (float) -Math.PI / 2;
            case RIGHT -> (float) Math.PI / 2;
            default    -> cameraAngle;
        };
    }

    public void setBase2dView(LanternaView view2d) {
        this.base2dView = view2d;
    }
}
