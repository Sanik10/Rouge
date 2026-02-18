package ru.crawl.datalayer.json.saveData;

import ru.crawl.domain.model.GameStats;

import java.util.ArrayList;
import java.util.List;

public class SaveGame {

    public  int width;
    public  int height;
    //String len - width
    //Number of lines - height
    public  List<String>mapRows;
    public  List<String>fogRows;

    public  SavePlayer player;
    public String playerName;

    // враги и предметы текущего уровня
    public  List<SaveEnemy> enemies = new ArrayList<>();
    public  List<SaveItem> items = new ArrayList<>();
    public  List<SaveInventory> inventories = new ArrayList<>();
    public  List<SaveScrollEffect> scrollEffects = new ArrayList<>();

    public int equippedSlot;
    public int selectedSlot;

    public long rngSeed;

    public int turn;

    public  int stairsX;
    public  int stairsY;

    public int levelIndex;

    public SaveStats stats = new SaveStats();

}
