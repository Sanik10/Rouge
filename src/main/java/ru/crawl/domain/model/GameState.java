package ru.crawl.domain.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameState {
    private final int width;
    private final int height;

    // карта: [y][x], '#' = стена, '.' = пол
    private final char[][] map;
    private final char[][] fog;

    private final Player player;

    // враги и предметы текущего уровня
    private final List<Enemy> enemies = new ArrayList<>();
    private final List<Item> items = new ArrayList<>();
    private final List<Inventory> inventories = new ArrayList<>();
    private final List<ScrollEffect> scrollEffects = new ArrayList<>();

//    private final String[] inventory = new String[] { "Potion", "Sword", null };
    private int equippedSlot = 0;
    private int selectedSlot = -1;

    private final Random rng = new Random();

    private final int stairsX;
    private final int stairsY;

    private final GameStats gameStats = new GameStats();
    private Direction facing = Direction.UP;

    public GameState(int width, int height, int startX, int startY, int startRoomIndex, char[][] map, char[][] fog, int stairsX, int stairsY) {
        this.width = width;
        this.height = height;
        this.map = map;
        this.fog = fog;
        this.player = new Player(startX, startY, startRoomIndex);
        this.stairsX = stairsX;
        this.stairsY = stairsY;
    }

    public GameState(int width, int height, int startX, int startY) {
        this(width, height, startX, startY, -1,
                makeDefaultMap(width, height), makeDefaultMap(width, height),
                -1, -1);
    }


    private static char[][] makeDefaultMap(int w, int h) {
        char[][] m = new char[h][w];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) m[y][x] = '.';
        }
        return m;
    }

    public int playerX() { return player.getX(); }
    public int playerY() { return player.getY(); }
    public Player player() { return player; }

    public int width() { return width; }
    public int height() { return height; }
    public char[][] map() { return map; }
    public char[][] fog() { return fog; }
    public List<Enemy> enemies() { return enemies; }
    public List<Item> items() { return items; }
    public List<Inventory> inventories() { return inventories; }
    public List<ScrollEffect> scrollEffects() { return scrollEffects; }
    public int equippedSlot() { return equippedSlot; }
    public int selectedSlot() { return selectedSlot; }
    public int getStairsY() { return stairsY; }
    public int getStairsX() { return stairsX; }

    public void setSelectedSlot(int slot) { this.selectedSlot = slot; }
    public void setEquippedSlot(int slot) { this.equippedSlot = slot; }

    public GameStats getGameStats() { return gameStats; }

    public boolean inBounds(int x, int y) {
        return x >= 0 && y >= 0 && x < width && y < height;
    }

    public boolean isFloor(int x, int y) {
        if (!inBounds(x, y)) return false;
        char t = map[y][x];
        return t == '.' || t == '>';
    }

    public Enemy enemyAt(int x, int y) {
        for (Enemy e : enemies) {
            if (e.isAlive() && e.getX() == x && e.getY() == y) return e;
        }
        return null;
    }

    public Item itemAt(int x, int y) {
        for (Item i : items) {
            if (i.getX() == x && i.getY() == y && i.getItemIsFind()) {
                if (inventories.size() < 9 || i.getType() == Item.TypeItem.TREASURE)
                    i.isFind();
                return i;
            }
        }
        return null;
    }

    public boolean isWalkable(int x, int y) {
        return isFloor(x, y) && enemyAt(x, y) == null;
    }

    // атака = попытка шага в сторону врага
    public boolean tryMovePlayer(Direction dir, List<String> log) {
        int nx = player.getX();
        int ny = player.getY();

        switch (dir) {
            case UP    -> ny--;
            case DOWN  -> ny++;
            case LEFT  -> nx--;
            case RIGHT -> nx++;
        }

        Enemy enemy = enemyAt(nx, ny);
        if (enemy != null) {
            Combat.playerAttacks(player, enemy, rng, log, gameStats);
            if (!enemy.isAlive()) {
                gameStats.incKills();
                Item item = new Item(Item.TypeItem.TREASURE, nx, ny, rng.nextInt(10, 100), enemy.getRoomIndex());
                items.add(item);
            }
            return true; // ход потрачен (даже если промах)
        }

        Item item = itemAt(nx, ny);
        if (item != null) {
            if (item.getType() == Item.TypeItem.TREASURE) {
                player.setTreasure(item.getParam());
                log.add("Find treasure: " + item.getParam());
            } else if (inventories.size() < 9)
                tryEquipItem(equippedSlot, item, log);
            else log.add("Backpack is full");
            return false;
        }

        char tile = map[ny][nx];
        if (tile == 'R' || tile == 'B' || tile == 'Y') {
            Item.TypeItem neededKey = switch (tile) {
                case 'R' -> Item.TypeItem.KEY_RED;
                case 'B' -> Item.TypeItem.KEY_BLUE;
                case 'Y' -> Item.TypeItem.KEY_YELLOW;
                default  -> null;
            };

            boolean hasKey = false;
            int keyIndex = -1;
            for (int i = 0; i < inventories.size(); i++) {
                Inventory inv = inventories.get(i);
                if (inv.getItem() == neededKey) {
                    hasKey = true;
                    keyIndex = i;
                    break;
                }
            }

            if (hasKey) {
                // тратим ключ, открываем дверь и считаем тайл полом
                inventories.remove(keyIndex);
                map[ny][nx] = '.';
                log.add("Door unlocked with key " + neededKey.name());
            } else {
                log.add("The door is locked");
                return false;
            }
        }

        if (!isFloor(nx, ny)) {
            log.add("Bump into wall!");
            return false;
        }

        player.moveTo(nx, ny);
        gameStats.incCells();
        scrollEffects.removeIf(scrollEffect -> {
            if (scrollEffect.inUse) {
                int turn = scrollEffect.turn();
//                System.out.println(turn);
                if (turn == 0) {
                    scrollEffect.removeFrom(player);
                    return true;
                }
            }
            return false;
        });
        log.add("Moved to (" + player.getX() + "," + player.getY() + ")");
        return true;
    }

    public void tryUseItem(List<String> log) {
        if (inventories.isEmpty()) {
            log.add("Invalid item slot");
            return;
        }

        if (selectedSlot < 1) {
            log.add("Invalid item slot");
            return;
        }

        int index = selectedSlot - 1;
        if (index < 0 || index >= inventories.size()) {
            log.add("Invalid item slot");
            return;
        }

        Inventory invent = inventories.get(index);

        if (invent.getItem() == Item.TypeItem.FOOD) {
            gameStats.incEats();
        } else if (invent.getItem().name().startsWith("ELIXIR")) {
            gameStats.incElixirs();
        } else if (invent.getItem().name().startsWith("SCROLL")) {
            gameStats.incScroll();
        }

        if (invent.getItem() == Item.TypeItem.SCROLL_Strength ||
                invent.getItem() == Item.TypeItem.SCROLL_Dexterity) {

            ScrollEffect scrollEffect = new ScrollEffect(
                    invent.getItem(),
                    invent.getParam(),
                    rng.nextInt(100, 500)
            );
            scrollEffect.applyTo(player);
            scrollEffects.add(scrollEffect);

        } else if (invent.getItem() == Item.TypeItem.SCROLL_MaxHP) {

            ScrollEffect scrollEffect = new ScrollEffect(
                    invent.getItem(),
                    invent.getParam(),
                    rng.nextInt(100, 500)
            );
            scrollEffect.applyTo(player);
            scrollEffects.add(scrollEffect);
            player.setHp(invent.getParam());

        } else if (invent.getItem() == Item.TypeItem.WEAPON_Sword ||
                invent.getItem() == Item.TypeItem.WEAPON_Gun) {

            scrollEffects.removeIf(scrollEffect -> {
                if (scrollEffect.isTypeWeapon()) {
                    scrollEffect.removeFrom(player);
                    return true;
                }
                return false;
            });

            ScrollEffect scrollEffect = new ScrollEffect(
                    invent.getItem(),
                    invent.getParam(),
                    Integer.MAX_VALUE
            );
            scrollEffect.applyTo(player);
            scrollEffects.add(scrollEffect);

        } else {
            invent.useInventory(player);
        }

        if (!invent.getItem().name().startsWith("KEY")
                && !invent.getItem().name().startsWith("WEAPON")) {
            inventories.remove(index);
        }
    }

    public void unequipWeapon(List<String> log) {
        boolean hadWeapon = scrollEffects.removeIf(scrollEffect -> {
            if (scrollEffect.isTypeWeapon()) {
                scrollEffect.removeFrom(player);
                return true;
            }
            return false;
        });
        if (hadWeapon) {
            log.add("Weapon unequipped");
        } else {
            log.add("No weapon equipped");
        }
    }

    public void tryEquipItem(int slot, Item item, List<String> log) {
        if (slot > 9) {
            log.add("Invalid equipment slot");
        } else {
            inventories.add(new Inventory(item));
            equippedSlot++;
            log.add("Equipped " + item.getType().toString());
        }
    }

    public boolean isPlayerOnStairs() {
        return player.getX() == stairsX && player.getY() == stairsY;
    }

    public void afterPlayerTurn(List<String> log) {
        player.tickSleep();
    }

    public void enemiesTurn(List<String> log) {
        revealMimicsNearPlayer(log);

        // 1) тик (движение/особенности)
        // 2) атака, если враг рядом (чтобы заработали drain/sleep)
        // Теперь все в одном цикле
        for (Enemy e : enemies) {
            if (!e.isAlive()) continue;

            int dx = Math.abs(e.getX() - player.getX());
            int dy = Math.abs(e.getY() - player.getY());
            if (dx + dy == 1) {
                Combat.enemyAttacks(e, player, rng, log);
            } else if (dx + dy <= e.getBadBlood()) {
                if (e.isAlive()) {
                    e.setHunting(true);
                    hunting(e, e.getX() - player.getX(), e.getY() - player.getY());
                    tryMoveEnemy(e, log);
                }
            } else {
                if (e.isAlive()) tryMoveEnemy(e, log);
            }
        }

        log.add("[Enemies turn]");
    }

    public void revealMimicsNearPlayer(List<String> log) {
    int px = playerX();
    int py = playerY();

    for (Enemy e : enemies) {
        if (e.getType() == Enemy.Type.MIMIC && e.isDisguised()) {
            int dx = Math.abs(e.getX() - px);
            int dy = Math.abs(e.getY() - py);
            if (dx + dy == 1) {
                e.reveal();
                log.add("The item was a mimic!");
            }
        }
    }
}

    public void hunting(Enemy e, int playerX, int playerY) {
        int dx = Math.abs(playerX);
        int dy = Math.abs(playerY);
        if (dx > dy) {
            if (playerX > 0 && isFloor(e.getX() - 1, e.getY())) e.setDirection(Direction.LEFT);
            else if (playerX < 0 && isFloor(e.getX() + 1, e.getY())) e.setDirection(Direction.RIGHT);
            else if (playerY > 0 && isFloor(e.getX(), e.getY() - 1)) e.setDirection(Direction.UP);
            else if (playerY < 0 && isFloor(e.getX(), e.getY() + 1)) e.setDirection(Direction.DOWN);
        } else {
            if (playerY > 0 && isFloor(e.getX(), e.getY() - 1)) e.setDirection(Direction.UP);
            else if (playerY < 0 && isFloor(e.getX(), e.getY() + 1)) e.setDirection(Direction.DOWN);
            else if (playerX > 0 && isFloor(e.getX() - 1, e.getY())) e.setDirection(Direction.LEFT);
            else if (playerX < 0 && isFloor(e.getX() + 1, e.getY())) e.setDirection(Direction.RIGHT);
        }
    }

    public void tryMoveEnemy(Enemy e, List<String> log) {
        int fromX = e.getX();
        int fromY = e.getY();

        int toX = fromX;
        int toY = fromY;

        int step = (e.getType() == Enemy.Type.OGRE) ? 2 : 1;

        if ((e.getType() == Enemy.Type.VAMPIRE || e.getType() == Enemy.Type.GHOST) && !e.isHunting()) {
            if (e.getVisible() < 6) e.setVisible(e.getVisible() + 1);
            else e.setVisible(0);
            changeDirection(e);
        } else if (e.getType() == Enemy.Type.MIMIC && e.isDisguised()) {
            step = 0;
        }

        switch (e.getDirection()) {
            case UP    -> toY -= step;
            case DOWN  -> toY += step;
            case LEFT  -> toX -= step;
            case RIGHT -> toX += step;
            case UP_RIGHT -> {
                toX += step;
                toY -= step;
            }
            case UP_LEFT -> {
                toX -= step;
                toY -= step;
            }
            case DOWN_RIGHT -> {
                toX += step;
                toY += step;
            }
            case DOWN_LEFT -> {
                toX -= step;
                toY += step;
            }
        }

        if (!inBounds(toX, toY)) {
            changeDirection(e);
            return;
        }

        if (toX == player.getX() && toY == player.getY()) {
            return;
        }

        Enemy other = enemyAt(toX, toY);
        if (other != null && other != e) {
            changeDirection(e);
            return;
        }

        boolean ok = false;
        if (e.isHunting()) ok = isFloor(toX, toY);
        else if (e.getDirection() == Direction.UP || e.getDirection() == Direction.DOWN) {
            ok = isFloor(toX, toY) && (isFloor(toX + 1, toY) || isFloor(toX - 1, toY));
        } else if (e.getDirection() == Direction.LEFT || e.getDirection() == Direction.RIGHT){
            ok = isFloor(toX, toY) && (isFloor(toX, toY + 1) || isFloor(toX, toY - 1));
        } else if (e.getDirection() == Direction.UP_RIGHT) {
            ok = isFloor(toX, toY) && (isFloor(toX + 1, toY) || isFloor(toX, toY - 1));
        } else if (e.getDirection() == Direction.DOWN_RIGHT) {
            ok = isFloor(toX, toY) && (isFloor(toX + 1, toY) || isFloor(toX, toY + 1));
        } else if (e.getDirection() == Direction.DOWN_LEFT) {
            ok = isFloor(toX, toY) && (isFloor(toX - 1, toY) || isFloor(toX, toY + 1));
        } else if (e.getDirection() == Direction.UP_LEFT) {
            ok = isFloor(toX, toY) && (isFloor(toX - 1, toY) || isFloor(toX, toY - 1));
        }

        if (!ok) {
            changeDirection(e);
            return;
        }

        e.moveTo(toX, toY);
        if (fog[toY][toX] > '9' || fog[toY][toX] < '0') {
            e.setRoomIndex(-2);
        } else {
            e.setRoomIndex(fog[toY][toX] - 48);
        }
    }

    public void changeDirection (Enemy e) {
        Random random = new Random();
        int choice = random.nextInt(4);

        // Зомби и огр ходят по стене комнаты, наворачивают круги
        if (e.getType() == Enemy.Type.ZOMBIE || e.getType() == Enemy.Type.OGRE) {
            if (e.getDirection() == Direction.UP) e.setDirection(Direction.RIGHT);
            else if (e.getDirection() == Direction.RIGHT) e.setDirection(Direction.DOWN);
            else if (e.getDirection() == Direction.DOWN) e.setDirection(Direction.LEFT);
            else e.setDirection(Direction.UP);
        }
        // Вампир и призрак перемещаются рандомно, только призрак еще и исчезает
        else if (e.getType() == Enemy.Type.VAMPIRE || e.getType() == Enemy.Type.GHOST) {
            switch (choice) {
                case 0 -> e.setDirection(Direction.UP);
                case 1 -> e.setDirection(Direction.DOWN);
                case 2 -> e.setDirection(Direction.LEFT);
                default -> e.setDirection(Direction.RIGHT);
            }
        }
        // Магический змей ходит по диагонали
        else if (e.getType() == Enemy.Type.MAGIC_SNAKE) {
            if (e.getDirection() == Direction.UP_RIGHT) e.setDirection(Direction.DOWN_RIGHT);
            else if (e.getDirection() == Direction.DOWN_RIGHT) e.setDirection(Direction.DOWN_LEFT);
            else if (e.getDirection() == Direction.DOWN_LEFT) e.setDirection(Direction.UP_LEFT);
            else if (e.getDirection() == Direction.UP_LEFT) e.setDirection(Direction.UP_RIGHT);
        }
        // Мимик: пока замаскирован — направление не важно; после раскрытия ведёт себя как зомби/огр
        else if (e.getType() == Enemy.Type.MIMIC) {
            if (e.isDisguised()) {
                return;
            }
            if (e.getDirection() == Direction.UP) e.setDirection(Direction.RIGHT);
            else if (e.getDirection() == Direction.RIGHT) e.setDirection(Direction.DOWN);
            else if (e.getDirection() == Direction.DOWN) e.setDirection(Direction.LEFT);
            else e.setDirection(Direction.UP);
        }
    }

    public Direction getFacing() {
        return facing;
    }

    public void rotateLeft() {
        // поворот влево (против часовой)
        facing = switch (facing) {
            case UP    -> Direction.LEFT;
            case LEFT  -> Direction.DOWN;
            case DOWN  -> Direction.RIGHT;
            case RIGHT -> Direction.UP;
            default    -> Direction.UP; // на всякий случай
        };
    }

    public void rotateRight() {
        // поворот вправо (по часовой)
        facing = switch (facing) {
            case UP    -> Direction.RIGHT;
            case RIGHT -> Direction.DOWN;
            case DOWN  -> Direction.LEFT;
            case LEFT  -> Direction.UP;
            default    -> Direction.UP;
        };
    }

    public boolean movePlayerForward3d(java.util.List<String> log) {
        return tryMovePlayer(facing, log);
    }

    public boolean movePlayerBackward3d(java.util.List<String> log) {
        Direction back = switch (facing) {
            case UP    -> Direction.DOWN;
            case DOWN  -> Direction.UP;
            case LEFT  -> Direction.RIGHT;
            case RIGHT -> Direction.LEFT;
            default    -> Direction.DOWN;
        };
        return tryMovePlayer(back, log);
    }
}
