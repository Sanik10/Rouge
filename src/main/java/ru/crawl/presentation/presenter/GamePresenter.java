package ru.crawl.presentation.presenter;

import ru.crawl.datalayer.json.JsonLeaderboardRepository;
import ru.crawl.datalayer.json.leaderbordData.Leaderboard;
import ru.crawl.datalayer.json.saveData.SaveGame;
import ru.crawl.datalayer.json.SaveGameMapper;
import ru.crawl.domain.generation.LevelGenerator;
import ru.crawl.domain.generation.rogue.Room;
import ru.crawl.domain.model.*;
import ru.crawl.domain.usecase.DefaultGameEngine;
import ru.crawl.presentation.input.UserInput;
import ru.crawl.domain.usecase.Command;
import ru.crawl.domain.usecase.GameEngine;
import ru.crawl.domain.usecase.GameSnapshot;
import ru.crawl.presentation.input.KeyMapper;
import ru.crawl.presentation.presenter.ui.FinishMenuAction;
import ru.crawl.presentation.presenter.ui.FinishResult;
import ru.crawl.presentation.presenter.ui.StartMenuAction;
import ru.crawl.presentation.presenter.ui.UiApp;
import ru.crawl.presentation.view.LanternaView;
import ru.crawl.presentation.view.LanternaView3d;
import ru.crawl.presentation.view.View;
import ru.crawl.datalayer.json.JsonSaveRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public final class GamePresenter {
    private static final int TPS = 15;
    private static final long TICK_MS = 1000L / TPS;
    private static final long HOLD_TIMEOUT_MS = 250;

    private GameEngine engine;
    private final View view;
    private final LanternaView3d view3d;
    private final KeyMapper keyMapper;

    private Direction pendingDirection = null;
    private long lastDirectionInputMs = 0;
    private boolean moveRequested = false;
    private long lastMoveRequestMs = 0;
    private boolean running = false;
    private final UiApp uiApp;
    private AppStates state;
    private final JsonSaveRepository repo;
    private final SaveGameMapper mapper;
    private final List<Leaderboard> leaderboards;
    private final JsonLeaderboardRepository leaderboardRepository;
    private String playerName = "player";
    boolean is3d = false;

    private GameSnapshot lastSnapshot;

    private enum InventoryMenuMode {
        NONE,
        WEAPON,
        FOOD,
        ELIXIR,
        SCROLL
    }

    private InventoryMenuMode inventoryMenuMode = InventoryMenuMode.NONE;

    private final int[] inventoryMenuMapping = new int[10];

    public GamePresenter(GameEngine engine, View view, KeyMapper keyMapper) throws IOException {
        this.engine = engine;
        this.view = view;
        this.keyMapper = keyMapper;
        this.repo = new JsonSaveRepository();
        this.mapper = new SaveGameMapper();
        uiApp = new UiApp(view.getWorldW(), view.getWorldH());
        view.setScreen(uiApp.getScreen());
        this.view3d = new LanternaView3d(view.getWorldW(), view.getWorldH());
        this.view3d.setScreen(uiApp.getScreen());
        this.view3d.setBase2dView((LanternaView) view);
        state = AppStates.MENU;
        leaderboardRepository = new JsonLeaderboardRepository();
        leaderboards = leaderboardRepository.load();
        Arrays.fill(inventoryMenuMapping, -1);
    }

    public void run() throws Exception {
        FinishResult result = FinishResult.VIN;
        while (state != AppStates.EXIT) {
            if(state == AppStates.MENU) showMenu();
            else if(state == AppStates.GAME) result = gameLoop();
            else if(state == AppStates.FINISH) showFinishMenu(result);
        }
        view.close();
    }

    private FinishResult gameLoop() throws Exception {
        FinishResult result = FinishResult.VIN;
        lastSnapshot = engine.step(new Command.Wait());
        if (is3d) {
            view3d.render(lastSnapshot);
        } else {
            view.render(lastSnapshot);
        }

        long nextTick = System.currentTimeMillis();

        while (running) {

            if(lastSnapshot.player().getHp() <= 0) {
                result = FinishResult.LOSE;
                state = AppStates.FINISH;
                running = false;
                break;
            }
            else if(lastSnapshot.levelIndex() == 22) {
                state = AppStates.FINISH;
                running = false;
                break;
            }

            // 1) Ввод (non-blocking)
            UserInput in;
            boolean inventoryOpen = false;
            View activeView = is3d ? view3d : view;
            while ((in = activeView.pollInput()) != null) {
                // 1.1) Немедленные команды (quit/menu/slots/etc.)
                Optional<Command> immediate = keyMapper.mapImmediate(in);
                if (immediate.isPresent()) {
                    Command cmd = immediate.get();

                    if (inventoryMenuMode != InventoryMenuMode.NONE) {
                        if (cmd instanceof Command.Quit) {
                            running = false;
                            state = AppStates.EXIT;
                            break;
                        } else if (cmd instanceof Command.SelectSlot s) {
                            handleInventoryMenuSelection(s.index());
                        } else if (cmd instanceof Command.Cancel) {
                            inventoryMenuMode = InventoryMenuMode.NONE;
                            if (is3d) {
                                view3d.render(lastSnapshot);
                            } else {
                                view.render(lastSnapshot);
                            }
                        }
                        continue;
                    }

                    if (cmd instanceof Command.Quit) {
                        running = false;
                        state = AppStates.EXIT;
                        break;
                    } else if (cmd instanceof Command.OpenInventory) {
                        lastSnapshot = engine.step(cmd);
                        if (is3d) {
                            view3d.render(lastSnapshot);
                        } else {
                            view.render(lastSnapshot);
                        }
                        inventoryOpen = !inventoryOpen;
                        if (inventoryOpen) {
                            view.showInventory(lastSnapshot);
                        } else {
                            if (is3d) {
                                view3d.render(lastSnapshot);
                            } else {
                                view.render(lastSnapshot);
                            }
                        }
                        continue;
                    } else if (cmd instanceof Command.Save) {
                        SaveGame save = mapper.toSave(engine.getState(), engine.getSeed(), lastSnapshot, playerName);
                        repo.save(save);
                    } else if (cmd instanceof Command.Toggle3dMode) {
                        is3d = !is3d;
                        keyMapper.set3dMode(is3d);
                        if (is3d) {
                            view3d.render(lastSnapshot);
                        } else {
                            view.render(lastSnapshot);
                        }
                        continue;
                    } else if (cmd instanceof Command.RotateLeft) {
                        lastSnapshot = engine.step(cmd);
                        if (is3d) {
                            view3d.render(lastSnapshot);
                        } else {
                            view.render(lastSnapshot);
                        }
                        continue;
                    } else if (cmd instanceof Command.RotateRight) {
                        lastSnapshot = engine.step(cmd);
                        if (is3d) {
                            view3d.render(lastSnapshot);
                        } else {
                            view.render(lastSnapshot);
                        }
                        continue;
                    } else if (cmd instanceof Command.OpenWeaponMenu) {
                        openInventoryMenu(InventoryMenuMode.WEAPON);
                        continue;
                    } else if (cmd instanceof Command.OpenFoodMenu) {
                        openInventoryMenu(InventoryMenuMode.FOOD);
                        continue;
                    } else if (cmd instanceof Command.OpenElixirMenu) {
                        openInventoryMenu(InventoryMenuMode.ELIXIR);
                        continue;
                    } else if (cmd instanceof Command.OpenScrollMenu) {
                        openInventoryMenu(InventoryMenuMode.SCROLL);
                        continue;
                    }

                    // Меню/слоты/confirm/cancel выполняем сразу (не ждём тика)
                    pendingDirection = null;
                    lastSnapshot = engine.step(cmd);
                    if (is3d) {
                        view3d.render(lastSnapshot);
                    } else {
                        view.render(lastSnapshot);
                    }
                    continue;
                }

                if (inventoryMenuMode != InventoryMenuMode.NONE) {
                    continue;
                }

                // 1.2) Направление — это “удержание”
                Optional<Direction> dir = keyMapper.mapDirection(in);
                if (dir.isPresent()) {
                    pendingDirection = dir.get();
                    long t = System.currentTimeMillis();
                    lastDirectionInputMs = t;
                    moveRequested = true;
                    lastMoveRequestMs = t;
                }

            }

            if (!running) break;

            // 2) Тики
            long now = System.currentTimeMillis();
            if (now < nextTick) {
                Thread.sleep(Math.min(10, nextTick - now));
                continue;
            }
            nextTick += TICK_MS;

            // 2.1) Если давно не было направления — сброс pending
            if (pendingDirection != null && (now - lastDirectionInputMs) > HOLD_TIMEOUT_MS) {
                pendingDirection = null;
            }


            // 2.2) Один шаг за тик (turn-based)
            if (pendingDirection != null && moveRequested) {
                moveRequested = false;

                if (is3d) {
                    handleDirection3d(pendingDirection);
                } else {
                    lastSnapshot = engine.step(new Command.Move(pendingDirection));
                }

                if (is3d) {
                    view3d.render(lastSnapshot);
                } else {
                    view.render(lastSnapshot);
                }
            }


        }
        return result;
    }

    StartMenuAction showMenu() throws IOException {
        StartMenuAction action = uiApp.showStartScreen(repo.exists());
        playerName = uiApp.getName();
        switch (action) {
            case START:  running = true; state = AppStates.GAME;  break;
            case LOAD: {
                SaveGame save = repo.load();
                this.playerName = (save.playerName == null || save.playerName.isBlank()) ? "player" : save.playerName.trim();
                GameState gameState = mapper.toGameState(save);
                engine = new DefaultGameEngine(gameState, save.levelIndex, save.rngSeed);
                ((DefaultGameEngine) engine).setTurn(save.turn - 1);
                running = true;
                this.state = AppStates.GAME;
                break;
            }
            case LEADERBOARD: {
                uiApp.showLeaderboard(leaderboards);
                state = AppStates.MENU;
                break;
            }
            case EXIT: running = false; state = AppStates.EXIT; break;
        }
        return action;
    }

    FinishMenuAction showFinishMenu(FinishResult result) throws IOException {
        updateLeaderboard();
        FinishMenuAction action = uiApp.showFinishScreen(lastSnapshot.levelIndex() == 22 ? 21 : lastSnapshot.levelIndex(), lastSnapshot.player().getTreasure(), result);
        switch (action) {
            case RESTART: startNewGame(); running = true; state = AppStates.GAME; break;
            case EXIT: running = false; state = AppStates.EXIT; break;
            case TO_MENU: running = false; state = AppStates.MENU; break;
        }
        return action;
    }

    private void updateLeaderboard()  throws IOException {
        GameStats s = engine.getState().getGameStats();
        Leaderboard l = new Leaderboard();
        l.treasure = engine.getState().player().getTreasure();
        l.name = playerName;
        l.level = lastSnapshot.levelIndex() == 22 ? 21 : lastSnapshot.levelIndex();
        l.kills = s.getKills();
        l.eats = s.getEats();
        l.elixirs = s.getElixirs();
        l.scroll = s.getScroll();
        l.hit = s.getHit();
        l.miss = s.getMiss();
        l.cells = s.getCells();
        leaderboards.add(l);
        leaderboardRepository.save(leaderboards);
    }

    private void startNewGame() {
        int levelIndex = 1;
        long seed = System.nanoTime();

        LevelGenerator.Level lvl = new LevelGenerator().generate(levelIndex, seed, 0);

        int spawnRoomIndex = lvl.startRoomIndex();
        Room start = lvl.rooms().get(spawnRoomIndex);

        java.util.Random rnd = new java.util.Random(seed ^ 0xABCDEF123456789L);
        int startX = start.getRoomX() + 1 + rnd.nextInt(Math.max(1, start.getRoomWidth() - 2));
        int startY = start.getRoomY() + 1 + rnd.nextInt(Math.max(1, start.getRoomHeight() - 2));

        if (lvl.map()[startY][startX] != '.') {
            startX = start.getRoomX() + start.getRoomWidth() / 2;
            startY = start.getRoomY() + start.getRoomHeight() / 2;
        }

        GameState state = new GameState(
                LevelGenerator.WORLD_W,
                LevelGenerator.WORLD_H,
                startX,
                startY,
                spawnRoomIndex,
                lvl.map(),
                lvl.fog(),
                lvl.stairsX(),
                lvl.stairsY()
        );

        state.enemies().addAll(lvl.enemies());
        state.items().addAll(lvl.items());

        this.engine = new DefaultGameEngine(state, levelIndex, seed);
    }

    private void handleDirection3d(Direction dir) {
        Command cmd = switch (dir) {
            case UP -> new Command.MoveForward3d();
            case DOWN -> new Command.MoveBackward3d();
            default -> null;
        };
        if (cmd != null) {
            lastSnapshot = engine.step(cmd);
        }
    }

    private void openInventoryMenu(InventoryMenuMode mode) throws Exception {
        Arrays.fill(inventoryMenuMapping, -1);
        inventoryMenuMode = InventoryMenuMode.NONE;

        List<Inventory> invs = lastSnapshot.inventories();
        List<String> lines = new ArrayList<>();

        String title;
        String prompt;

        switch (mode) {
            case WEAPON -> {
                title = "Weapons";
                prompt = "Choose weapon [0-9] (0 - empty hands)";
            }
            case FOOD -> {
                title = "Food";
                prompt = "Choose food [1-9]";
            }
            case ELIXIR -> {
                title = "Elixirs";
                prompt = "Choose elixir [1-9]";
            }
            case SCROLL -> {
                title = "Scrolls";
                prompt = "Choose scroll [1-9]";
            }
            default -> {
                return;
            }
        }

        int digit = 1;
        boolean hasAny = false;

        for (int i = 0; i < invs.size() && digit <= 9; i++) {
            Inventory inv = invs.get(i);
            Item.TypeItem t = inv.getItem();

            boolean matches = switch (mode) {
                case WEAPON -> (t == Item.TypeItem.WEAPON_Sword || t == Item.TypeItem.WEAPON_Gun);
                case FOOD   -> (t == Item.TypeItem.FOOD);
                case ELIXIR -> t.name().startsWith("ELIXIR");
                case SCROLL -> t.name().startsWith("SCROLL");
                default     -> false;
            };

            if (!matches) continue;

            hasAny = true;
            inventoryMenuMapping[digit] = i + 1; //  (1..9)!!!!!!
            lines.add(digit + ". " + inv.toString());
            digit++;
        }

        if (!hasAny) {
            lines.add("No items of this type in inventory");

            if (view instanceof LanternaView lanterna) {
                lanterna.showInventoryMenu(lines, title, prompt);
            }
            return;
        }

        // для оружия пункт "0. пустые руки"
        if (mode == InventoryMenuMode.WEAPON) {
            lines.add(0, "0. Empty hands (no weapon)");
        }

        inventoryMenuMode = mode;

        // рисуем меню поверх 2D-вьюхи (как и обычный инвентарь)
        if (view instanceof LanternaView lanterna) {
            lanterna.showInventoryMenu(lines, title, prompt);
        }
    }

    private void handleInventoryMenuSelection(int digit) throws Exception {
        if (inventoryMenuMode == InventoryMenuMode.NONE) {
            return;
        }

        if (inventoryMenuMode == InventoryMenuMode.WEAPON && digit == 0) {
            lastSnapshot = engine.step(new Command.UnequipWeapon());
            if (is3d) {
                view3d.render(lastSnapshot);
            } else {
                view.render(lastSnapshot);
            }
            inventoryMenuMode = InventoryMenuMode.NONE;
            return;
        }

        if (digit < 1 || digit > 9) {
            return;
        }

        int uiSlot = inventoryMenuMapping[digit];
        if (uiSlot <= 0) {
            return;
        }

        lastSnapshot = engine.step(new Command.SelectSlot(uiSlot));
        lastSnapshot = engine.step(new Command.UseSelected());

        if (is3d) {
            view3d.render(lastSnapshot);
        } else {
            view.render(lastSnapshot);
        }

        inventoryMenuMode = InventoryMenuMode.NONE;
    }
}

enum AppStates {
    MENU, GAME, FINISH, EXIT
}
