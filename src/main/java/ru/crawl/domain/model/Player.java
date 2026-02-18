package ru.crawl.domain.model;

import java.util.HashSet;
import java.util.Collection;

public class Player {
    private int x;
    private int y;
    private int hp;
    private int maxHp;
    private int strength;
    private int dex;
    private int sleepTurns;
    private int treasure;
    private int roomIndex;
    private HashSet<Integer> visitedRooms;

    public Player(int startX, int startY, int spawnRoomIndex) {
        x = startX;
        y = startY;
        roomIndex = spawnRoomIndex;
        visitedRooms = new HashSet<>(9);
        visitedRooms.add(spawnRoomIndex);
        hp = 100;
        maxHp = 100;
        strength = 10;
        dex = 8;
        treasure = 0;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getHp() {
        return hp;
    }

    public int getSleepTurns() { return sleepTurns; }

    public void setHp(int value) { hp += value; }

    public int getMaxHp() {
        return maxHp;
    }

    public void setMaxHp(int value) { maxHp += value; }

    public int getStrength() {
        return strength;
    }

    public void setStrength(int value) { strength += value; }

    public int getDex() {
        return dex;
    }

    public void setDex(int value) { dex += value; }

    public int getTreasure() { return treasure; }

    public void setTreasure(int value) { treasure += value; }

    public int getRoomIndex() {
        return roomIndex;
    }

    public void setRoomIndex(int roomIndex) {
        this.roomIndex = roomIndex;
    }

    public HashSet<Integer> getVisitedRooms() {
        return visitedRooms;
    }

    public void addVisitedRooms(int roomIndex) {
        this.visitedRooms.add(roomIndex);
    }

    public void restoreHp(int hp) { this.hp = Math.max(0, hp); }
    public void restoreMaxHp(int maxHp) { this.maxHp = Math.max(1, maxHp); this.hp = Math.min(this.hp, this.maxHp); }
    public void restoreStrength(int strength) { this.strength = strength; }
    public void restoreDex(int dex) { this.dex = dex; }
    public void restoreTreasure(int treasure) { this.treasure = treasure; }

    /* ========== геймплейные методы ========= */

    public void moveTo(int newX, int newY) {
        x = newX;
        y = newY;
    }

    public void takeDamage(int damage) {
        hp = Math.max(0, hp - damage); // ниже нуль - нельзя
    }

//    public void heal(int heal) {
//        hp = Math.min(maxHp, hp + heal); // больше максимума - нельзя
//    }

    public void reduceMaxHp(int amount) {
        maxHp = Math.max(1, maxHp - amount);
        hp = Math.min(hp, maxHp);
    }

//    public void increaseMaxHp(int amount) {
//        maxHp = Math.min(200, maxHp + amount);
//    }

    public boolean isAlive() {
        return hp > 0;
    }

    public boolean isSleeping() {
        return sleepTurns > 0;
    }

    public void applySleep(int turns) {
        sleepTurns = Math.max(sleepTurns, turns);
    }

    public void tickSleep() {
        if(sleepTurns > 0) {
            sleepTurns--;
        }
    }

    public void addScrollEffect(Item.TypeItem playerStat, int bonus) {
        switch (playerStat) {
            case SCROLL_Strength, WEAPON_Gun, WEAPON_Sword -> strength += bonus;
            case SCROLL_Dexterity -> dex += bonus;
            case SCROLL_MaxHP -> maxHp += bonus;
        }
    }

    public void setSleepTurns(int sleepTurns) {
        this.sleepTurns = sleepTurns;
    }

    public void restoreVisitedRooms(Collection<Integer> rooms) {
        visitedRooms.clear();
        if (rooms != null) {
            visitedRooms.addAll(rooms);
        }
        visitedRooms.add(roomIndex);
    }
}