package ru.crawl.datalayer.json;

import ru.crawl.datalayer.json.saveData.*;
import ru.crawl.domain.model.*;
import ru.crawl.domain.usecase.GameSnapshot;

import java.util.ArrayList;
import java.util.List;

public class SaveGameMapper {

    public SaveGame toSave(GameState state, long rngSeed, GameSnapshot snapshot, String playerName) {
        SaveGame save = new SaveGame();
        save.width = state.width();
        save.height = state.height();
        save.player = toSavePlayer(state.player());
        save.mapRows = toRows(state.map());
        save.fogRows = toRows(state.fog());
        save.rngSeed = rngSeed;
        save.stairsX = state.getStairsX();
        save.stairsY = state.getStairsY();
        save.turn = snapshot.turnNumber();
        save.equippedSlot = state.equippedSlot();
        save.selectedSlot = state.selectedSlot();
        save.levelIndex = snapshot.levelIndex();
        save.stats = toSaveStats(state.getGameStats());
        save.playerName = playerName;
        for(int i = 0; i < state.items().size(); i++) {
            save.items.add(itemAtSave(state.items().get(i)));
        }
        for(int i = 0; i < state.scrollEffects().size(); i++) {
            save.scrollEffects.add(effectAtSave(state.scrollEffects().get(i)));
        }
        for(int i = 0; i < state.inventories().size(); i++) {
            save.inventories.add(inventoryAtSave(state.inventories().get(i)));
        }
        for(int i = 0; i < state.enemies().size(); i++) {
            save.enemies.add(enemyAtSave(state.enemies().get(i)));
        }
        return save;
    }

    public GameState toGameState(SaveGame save) {
        char[][] map = toChars(save.mapRows, save.height, save.width);
        char[][] fog = toChars(save.fogRows, save.height, save.width);
        GameState game = new GameState(save.width, save.height, save.player.x, save.player.y, save.player.roomIndex, map, fog, save.stairsX, save.stairsY);
        game.setSelectedSlot(save.selectedSlot);
		game.setEquippedSlot(save.equippedSlot);
        restorePlayer(game, save.player);
        restoreStats(game.getGameStats(), save.stats);
        for (SaveInventory si : save.inventories) {
            game.inventories().add(restoreInventory(si));
        }
        for (SaveScrollEffect si : save.scrollEffects) {
            game.scrollEffects().add(restoreEffects(si));
        }
        for (SaveItem si : save.items) {
            game.items().add(restoreItems(si));
        }
        for (SaveEnemy si : save.enemies) {
            game.enemies().add(restoreEnemy(si));
        }

        return game;
    }

    private static List<String> toRows(char[][] grid) {
        List<String> rows = new ArrayList<>();
        for (char[] chars : grid) {
            rows.add(String.copyValueOf(chars));
        }
        return rows;
    }

    private static SavePlayer toSavePlayer(Player p) {
        SavePlayer savePlayer = new SavePlayer();
        savePlayer.dex = p.getDex();
        savePlayer.hp = p.getHp();
        savePlayer.x = p.getX();
        savePlayer.y = p.getY();
        savePlayer.maxHp = p.getMaxHp();
        savePlayer.strength = p.getStrength();
        savePlayer.treasure = p.getTreasure();
        savePlayer.sleepTurns = p.getSleepTurns();
        savePlayer.roomIndex = p.getRoomIndex();
        savePlayer.visitedRooms = new ArrayList<>(p.getVisitedRooms());
        return savePlayer;
    }

    private static char[][] toChars(List<String> rows, int height, int width) {
        char[][] grid = new char[height][width];
        for (int i = 0; i < height; i++) {
            grid[i] = rows.get(i).toCharArray();
        }
        return grid;
    }

    private static void restorePlayer(GameState game, SavePlayer save) {
        Player p =  game.player();
        p.restoreDex(save.dex);
        p.restoreHp(save.hp);
        p.restoreStrength(save.strength);
        p.restoreMaxHp(save.maxHp);
        p.restoreTreasure(save.treasure);
        p.setSleepTurns(save.sleepTurns);
        p.setRoomIndex(save.roomIndex);
        p.restoreVisitedRooms(save.visitedRooms);
    }

    private static SaveItem itemAtSave(Item item)  {
        SaveItem saveItem = new SaveItem();
        saveItem.type = SaveItem.TypeItem.valueOf(item.getType().name());
        saveItem.itemNotFind = item.getItemIsFind();
        saveItem.param =  item.getParam();
        saveItem.x = item.getX();
        saveItem.y = item.getY();
        saveItem.roomIndex = item.getRoomIndex();
        return saveItem;
    }

    private static SaveScrollEffect effectAtSave(ScrollEffect effect)  {
        SaveScrollEffect save = new SaveScrollEffect();
        save.playerStat = SaveItem.TypeItem.valueOf(effect.getType().name());
        save.bonus = effect.getBonus();
        save.turns = effect.getTurns();
        return save;
    }

    private static SaveInventory inventoryAtSave(Inventory inventory)  {
        SaveInventory save = new SaveInventory();
        save.type = SaveItem.TypeItem.valueOf(inventory.getItem().name());
        save.param = inventory.getParam();
        return save;
    }

    private static SaveEnemy enemyAtSave(Enemy enemy)  {
        SaveEnemy save = new SaveEnemy();
        save.type = SaveEnemy.Type.valueOf(enemy.getType().name());
        save.level = enemy.getLevel();
        save.x = enemy.getX();
        save.y = enemy.getY();
        save.hp = enemy.getHp();
        save.isVisible = enemy.getVisible();
        save.direction = enemy.getDirection();
        save.roomIndex = enemy.getRoomIndex();
        return save;
    }

    private static Inventory restoreInventory(SaveInventory save)  {
        Item.TypeItem type = Item.TypeItem.valueOf(save.type.name());
        Item item = new Item(type, -1, -1, save.param, -1);
        return new Inventory(item);
    }

    private  static ScrollEffect restoreEffects(SaveScrollEffect save)  {
        Item.TypeItem type = Item.TypeItem.valueOf(save.playerStat.name());
        return new ScrollEffect(type, save.bonus, save.turns);
    }

    private  static Item restoreItems(SaveItem save)  {
        Item.TypeItem type = Item.TypeItem.valueOf(save.type.name());
        Item item = new Item(type, save.x, save.y, save.param, save.roomIndex);
        item.setItemNotFind(save.itemNotFind);
        return item;
    }

    private static Enemy restoreEnemy(SaveEnemy save) {
        Enemy.Type type = Enemy.Type.valueOf(save.type.name());
        Enemy e = new Enemy(type, save.level, save.x, save.y, save.roomIndex);
        e.setDirection(save.direction);
        e.setVisible(save.isVisible);
        e.setHp(save.hp);
        return e;
    }

    private static SaveStats toSaveStats(GameStats gs) {
        SaveStats s = new SaveStats();
        s.kills = gs.getKills();
        s.eats = gs.getEats();
        s.elixirs = gs.getElixirs();
        s.scroll = gs.getScroll();
        s.hit = gs.getHit();
        s.miss = gs.getMiss();
        s.cells = gs.getCells();
        return s;
    }

    private static void restoreStats(GameStats gs, SaveStats s) {
        if (s == null) return;
        gs.setKills(s.kills);
        gs.setEats(s.eats);
        gs.setElixirs(s.elixirs);
        gs.setScroll(s.scroll);
        gs.setHit(s.hit);
        gs.setMiss(s.miss);
        gs.setCells(s.cells);
    }

}
