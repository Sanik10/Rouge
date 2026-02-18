package ru.crawl.domain.model;

public class ScrollEffect {
    Item.TypeItem playerStat;
    int bonus;
    int turns;
    boolean inUse;

    public ScrollEffect(Item.TypeItem stat, int value, int turns) {
        this.playerStat = stat;
        this.bonus = value;
        this.turns = turns;
        this.inUse = false;
    }

    public Item.TypeItem getType() { return playerStat; }

    int getParam() { return bonus; }

    void applyTo(Player player) {
        player.addScrollEffect(playerStat, bonus);
        inUse = true;
    }

    void removeFrom(Player player) {
        player.addScrollEffect(playerStat, -bonus);
    }

    int turn() { return turns--; }

    boolean isTypeWeapon() {
        return (playerStat == Item.TypeItem.WEAPON_Gun || playerStat == Item.TypeItem.WEAPON_Sword);
    }

    public int getBonus() {  return bonus; }
    public int getTurns() { return turns; }
}