package ru.crawl.datalayer.json.saveData;

public class SaveItem {
    public enum TypeItem {
        FOOD,
        ELIXIR_Strength,
        ELIXIR_Dexterity,
        ELIXIR_MaxHP,
        SCROLL_Strength,
        SCROLL_Dexterity,
        SCROLL_MaxHP,
        WEAPON_Sword,
        WEAPON_Gun,
        TREASURE,
        KEY_RED,
        KEY_BLUE,
        KEY_YELLOW
    }

    public SaveItem.TypeItem type;
    public boolean itemNotFind;
    public int x, y;
    public int param;
    public int roomIndex;
}
