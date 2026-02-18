package ru.crawl.domain.model;

public class GameStats {
    private int kills = 0;
    private int eats = 0;
    private int elixirs = 0;
    private int scroll = 0;
    private int hit = 0;
    private int miss = 0;
    private int cells = 0;

    public int getCells() {
        return cells;
    }

    public void setCells(int cells) {
        this.cells = cells;
    }

    public int getHit() {
        return hit;
    }

    public void setHit(int hit) {
        this.hit = hit;
    }

    public int getMiss() {
        return miss;
    }

    public void setMiss(int miss) {
        this.miss = miss;
    }

    public int getEats() {
        return eats;
    }
    public void setEats(int eats) {
        this.eats = eats;
    }

    public int getElixirs() {
        return elixirs;
    }

    public void setElixirs(int elixirs) {
        this.elixirs = elixirs;
    }
    public int getScroll() {
        return scroll;
    }

    public void setScroll(int scroll) {
        this.scroll = scroll;
    }

    public int getKills() {
        return kills;
    }
    public void setKills(int kills) {
        this.kills = kills;
    }

    public void incKills()   { kills++; }
    public void incEats()    { eats++; }
    public void incElixirs() { elixirs++; }
    public void incScroll()  { scroll++; }
    public void incHit()     { hit++; }
    public void incMiss()    { miss++; }
    public void incCells()   { cells++; }
}
