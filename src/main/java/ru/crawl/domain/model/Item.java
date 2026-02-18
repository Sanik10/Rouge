package ru.crawl.domain.model;

public class Item {
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

    private final TypeItem type;
    private boolean itemNotFind;
    final int x, y;
    final int param;
    private int roomIndex;

    public Item(TypeItem type, int x, int y, int param, int roomIndex) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.param = param;
        this.itemNotFind = true;
        this.roomIndex = roomIndex;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public TypeItem getType() { return type; }
    public boolean getItemIsFind() { return itemNotFind; }
    public int getParam() { return param; }

    public int getRoomIndex() { return roomIndex; }
    public void setRoomIndex(int roomIndex) { this.roomIndex = roomIndex; }

    public void isFind() {
        itemNotFind = false;
    }

    public void setItemNotFind(boolean itemNotFind) {
        this.itemNotFind = itemNotFind;
    }
}