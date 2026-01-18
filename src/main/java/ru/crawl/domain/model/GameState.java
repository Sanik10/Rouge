package ru.crawl.domain.model;

import java.util.List;

// состояние игры, пока поле 5х5
public class GameState {
    private final int width;
    private final int height;
    private int playerX;
    private int playerY;

    // пока инвентарь: 3 слота
    private final String[] inventory = new String[] { "Potion", "Sword", null };
    private int equippedSlot = -1;

    public GameState(int width, int height, int startX, int startY) {
        this.width = width;
        this.height = height;
        this.playerX = startX;
        this.playerY = startY;
    }

    public int playerX() { return playerX; }
    public int playerY() { return playerY; }
    public int width() { return width; }
    public int height() { return height; }

    // попытка переместить игрока в заданном направлении
    public boolean tryMovePlayer(Direction dir, List<String> log) {
        int nx = playerX;
        int ny = playerY;

        switch (dir) {
            case UP    -> ny--;
            case DOWN  -> ny++;
            case LEFT  -> nx--;
            case RIGHT -> nx++;
        }

        if (nx < 0 || ny < 0 || nx >= width || ny >= height) {
            log.add("Bump into wall!");
            return false;
        }

        playerX = nx;
        playerY = ny;
        log.add("Moved to (" + playerX + "," + playerY + ")");
        return true;
    }

    public boolean tryUseItem(int slot, List<String> log) {
        if (slot < 0 || slot >= inventory.length || inventory[slot] == null) {
            log.add("Invalid item slot");
            return false;
        }

        String item = inventory[slot];
        if (item.equals("Potion")) {
            inventory[slot] = null;
            log.add("Used Potion!");
            return true;
        }

        log.add("Can't use " + item);
        return false;
    }

    public boolean tryEquipItem(int slot, List<String> log) {
        if (slot < 0 || slot >= inventory.length || inventory[slot] == null) {
            log.add("Invalid equipment slot");
            return false;
        }

        equippedSlot = slot;
        log.add("Equipped " + inventory[slot]);
        return true;
    }

    public void afterPlayerTurn(List<String> log) {
        // TODO: доделать
    }

    public void enemiesTurn(List<String> log) {
        log.add("[Enemies turn]");
    }
}
