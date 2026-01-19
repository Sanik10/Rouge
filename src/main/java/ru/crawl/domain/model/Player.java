package ru.crawl.domain.model;

public class Player {
    private int x;
    private int y;
    private int hp;
    private int maxHp;
    private int strength;
    private int dex;
    private int sleepTurns;

    public Player(int startX, int startY) {
        x = startX;
        y = startY;
        hp = 100;
        maxHp = 100;
        strength = 10;
        dex = 8;
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

    public int getMaxHp() {
        return maxHp;
    }

    public int getStrength() {
        return strength;
    }

    public int getDex() {
        return dex;
    }

    /* ========== геймплейные методы ========= */

    public void moveTo(int newX, int newY) {
        x = newX;
        y = newY;
    }

    public void takeDamage(int damage) {
        hp = Math.max(0, hp - damage); // ниже нуль - нельзя
    }

    public void heal(int heal) {
        hp = Math.min(maxHp, hp + heal); // больше максимума - нельзя
    }

    public void reduceMaxHp(int amount) {
        maxHp = Math.max(1, maxHp - amount);
        hp = Math.min(hp, maxHp);
    }

    public void increaseMaxHp(int amount) {
        maxHp = Math.min(200, maxHp + amount);
    }

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
}