package ru.crawl.domain.usecase;

import ru.crawl.domain.generation.LevelGenerator;
import ru.crawl.domain.model.GameState;
import ru.crawl.domain.model.Item;

import java.util.ArrayList;
import java.util.List;

public class DefaultGameEngine implements GameEngine {
    private GameState state;
    private long turn = 0;
    private final LevelGenerator generator = new LevelGenerator();
    private int levelIndex = 1;
    private long seed;

    public DefaultGameEngine(GameState state, int levelIndex, long seed) {
        this.state = state;
        this.levelIndex = levelIndex;
        this.seed = seed;
    }

    @Override
    public GameSnapshot step(Command cmd) {
        List<String> log = new ArrayList<>();
        log.add("=== Turn " + turn + " ===");

        boolean playerActionUsedTurn = false;

        // сон: ход пропускается
        if (state.player().isSleeping()) {
            log.add("Player is sleeping...");
            playerActionUsedTurn = true;
        } else {
            if (cmd instanceof Command.Move movement) {
                playerActionUsedTurn = state.tryMovePlayer(movement.direction(), log);
            } else if (cmd instanceof Command.MoveForward3d) {
                playerActionUsedTurn = state.movePlayerForward3d(log);
            } else if (cmd instanceof Command.MoveBackward3d) {
                playerActionUsedTurn = state.movePlayerBackward3d(log);
            } else if (cmd instanceof Command.Wait) {
                log.add("Player waits");
                playerActionUsedTurn = true;
            } else if (cmd instanceof Command.UseSelected) {
                state.tryUseItem(log);
                log.add("UseSelected");
            } else if (cmd instanceof Command.UnequipWeapon) {
                state.unequipWeapon(log);
            } else if (cmd instanceof Command.EquipSelected) {
                log.add("Selected equipped");
            } else if (cmd instanceof Command.SelectSlot s) {
                state.setSelectedSlot(s.index());
                log.add("Selected slot: " + s.index());
            } else if (cmd instanceof Command.OpenInventory) {
                log.add("Opened inventory");
            } else if (cmd instanceof Command.CloseMenu) {
                log.add("Closed menu");
            } else if (cmd instanceof Command.Confirm) {
                log.add("Confirmed");
            } else if (cmd instanceof Command.Cancel) {
                log.add("Cancelled");
            } else if (cmd instanceof Command.UseStairs) {
                log.add("Using stairs");
                playerActionUsedTurn = true;
            } else if (cmd instanceof Command.RotateLeft) {
                state.rotateLeft();
                log.add("Player turns left");
                playerActionUsedTurn = true;
            } else if (cmd instanceof Command.RotateRight) {
                state.rotateRight();
                log.add("Player turns right");
                playerActionUsedTurn = true;
            }
        }

        if (playerActionUsedTurn) {
            state.afterPlayerTurn(log);
            state.enemiesTurn(log);
            turn++;
        }

        if (playerActionUsedTurn && cmd instanceof Command.UseStairs && state.isPlayerOnStairs()) {
            if (levelIndex <= 21) {
                levelIndex++;
                seed++;

                int balance = 0;
                if (levelIndex >= 3) {
                    if (state.player().getHp() < 15) balance = 6;
                    else if (state.player().getHp() < 30) balance = 5;
                    else if (state.player().getHp() < 45) balance = 4;
                    else if (state.player().getHp() < 60) balance = 3;
                    else if (state.player().getHp() < 75) balance = 2;
                }

                LevelGenerator.Level lvl = generator.generate(levelIndex, seed, balance);

                int spawnRoomIndex = lvl.startRoomIndex();
                var start = lvl.rooms().get(spawnRoomIndex);
                int startX = start.getRoomX() + start.getRoomWidth() / 2;
                int startY = start.getRoomY() + start.getRoomHeight() / 2;

                GameState next = new GameState(
                        LevelGenerator.WORLD_W, LevelGenerator.WORLD_H,
                        startX, startY, spawnRoomIndex,
                        lvl.map(), lvl.fog(),
                        lvl.stairsX(), lvl.stairsY()
                );

                next.enemies().addAll(lvl.enemies());
                next.items().addAll(lvl.items());
                for (var inv : state.inventories()) {
                    var type = inv.getItem();
                    if (type == Item.TypeItem.KEY_RED ||
                            type == Item.TypeItem.KEY_BLUE ||
                            type == Item.TypeItem.KEY_YELLOW) {
                        // ключи не переносим
                        continue;
                    }
                    next.inventories().add(inv);
                }
                next.scrollEffects().addAll(state.scrollEffects());
                next.player().setHp(state.player().getHp() - next.player().getHp());
                next.player().setMaxHp(state.player().getMaxHp() - next.player().getMaxHp());
                next.player().setStrength(state.player().getStrength() - next.player().getStrength());
                next.player().setDex(state.player().getDex() - next.player().getDex());
                next.player().setTreasure(state.player().getTreasure());

                state = next;
                log.add("You descend to level " + levelIndex);
            } else {
                log.add("You escaped the dungeon!");
            }
        }


        String targetName = null;
        Integer targetHp = null;
        Integer targetMaxHp = null;

        int px = state.playerX();
        int py = state.playerY();

        int[][] dirs = { {0,-1}, {0,1}, {-1,0}, {1,0} };
        for (int[] d : dirs) {
            var e = state.enemyAt(px + d[0], py + d[1]);
            if (e != null && e.isAlive()) {
                targetName = e.getType().name();
                targetHp = e.getHp();
                targetMaxHp = e.getStats().hpMax();
                break;
            }
        }

        return new GameSnapshot(
                state.width(),
                state.height(),
                state.map(),
                state.fog(),
                state.enemies(),
                state.items(),
                state.inventories(),
                state.scrollEffects(),

                state.player(),
                state.getFacing(),

                (int) turn,
                levelIndex,

                targetName,
                targetHp,
                targetMaxHp,

                log
        );
    }

    @Override
    public GameState getState() {
        return state;
    }

    @Override
    public long getSeed() {
        return seed;
    }

    public void setTurn(long turn) {
        this.turn = turn;
    }
}
