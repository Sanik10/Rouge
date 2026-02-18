package ru.crawl.domain.model;

public class Inventory {
    private final Item item;

    public Inventory(Item item) {
        this.item = item;
    }

    public Item.TypeItem getItem() {
        return item.getType();
    }

    public Item item() { return item; }

    public int getParam() {
        return item.getParam();
    }

    @Override
    public String toString() {
        String str = "";
        if (item.getType() == Item.TypeItem.FOOD) {
            str = "FOOD: add to health " + item.getParam() + " Hp";
        } else if (item.getType() == Item.TypeItem.ELIXIR_Strength) {
            str = "ELIXIR Strength: add to Strength " + item.getParam();
        } else if (item.getType() == Item.TypeItem.ELIXIR_Dexterity) {
            str = "ELIXIR Dexterity: add to Dexterity " + item.getParam();
        } else if (item.getType() == Item.TypeItem.ELIXIR_MaxHP) {
            str = "ELIXIR MaxHP: add to MaxHP " + item.getParam();
        } else if (item.getType() == Item.TypeItem.SCROLL_Strength) {
            str = "SCROLL Strength: add to Strength " + item.getParam() + ". Duration: < 500 step";
        } else if (item.getType() == Item.TypeItem.SCROLL_Dexterity) {
            str = "SCROLL Dexterity: add to Dexterity " + item.getParam() + ". Duration: < 500 step";
        } else if (item.getType() == Item.TypeItem.SCROLL_MaxHP) {
            str = "SCROLL MaxHP: add to MaxHP " + item.getParam() + " Hp" + ". Duration: < 500 step";
        } else if (item.getType() == Item.TypeItem.WEAPON_Sword) {
            str = "WEAPON Sword: add to Strength " + item.getParam();
        } else if (item.getType() == Item.TypeItem.WEAPON_Gun) {
            str = "WEAPON Gun: add to Strength " + item.getParam();
        } else if (item.getType() == Item.TypeItem.KEY_RED) {
            str = "KEY Red";
        } else if (item.getType() == Item.TypeItem.KEY_BLUE) {
            str = "KEY Blue";
        } else if (item.getType() == Item.TypeItem.KEY_YELLOW) {
            str = "KEY Yellow";
        }
        return str;
    }

    public void useInventory(Player player) {
        if (item.getType() == Item.TypeItem.FOOD) {
            if (player.getHp() + item.getParam() >= player.getMaxHp())
                player.setHp(player.getMaxHp() - player.getHp());
            else player.setHp(item.getParam());
        } else if (item.getType() == Item.TypeItem.ELIXIR_Strength) {
            player.setStrength(item.getParam());
        } else if (item.getType() == Item.TypeItem.ELIXIR_Dexterity) {
            player.setDex(item.getParam());
        } else if (item.getType() == Item.TypeItem.ELIXIR_MaxHP) {
            player.setMaxHp(item.getParam());
            if (player.getHp() + item.getParam() >= player.getMaxHp())
                player.setHp(player.getMaxHp() - player.getHp());
            else player.setHp(item.getParam());
        }
    }
}