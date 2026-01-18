package ru.crawl.domain.usecase;

import ru.crawl.domain.model.Direction;
import ru.crawl.domain.model.GameState;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static ru.crawl.domain.usecase.GameSnapshot.EngineMode;

public final class DefaultGameEngine implements GameEngine {

    private GameState state;

    private EngineMode mode = EngineMode.GAME;
    private int selectedSlot = -1;

    private long turn = 0;

    public DefaultGameEngine(GameState initialState) {
        this.state = Objects.requireNonNull(initialState);
    }

    @Override
    public GameSnapshot step(Command command) {
        Objects.requireNonNull(command);

        List<String> log = new ArrayList<>();
        boolean consumesTurn = false;

        switch (command) {
            case Command.Move(var dir) -> consumesTurn = handleMove(dir, log);
            case Command.Wait __ -> {
                consumesTurn = true;
                log.add("Wait.");
            }

            case Command.OpenInventory __ -> {
                mode = EngineMode.INVENTORY;
                log.add("Inventory opened.");
            }
            case Command.CloseMenu __ -> {
                mode = EngineMode.GAME;
                selectedSlot = -1;
                log.add("Menu closed.");
            }

            case Command.SelectSlot(var index) -> {
                // пока просто запоминаем, позже можно валидировать по Inventory size
                selectedSlot = index;
                log.add("Selected slot: " + index);
            }

            case Command.UseSelected __ -> consumesTurn = handleUseSelected(log);
            case Command.EquipSelected __ -> consumesTurn = handleEquipSelected(log);

            case Command.Confirm __ -> log.add("Confirm.");
            case Command.Cancel __ -> {
                // базовая отмена: сброс выбора
                selectedSlot = -1;
                log.add("Cancel.");
            }
        }

        if (consumesTurn) {
            turn++;
            afterPlayerTurn(log);
            enemiesTurn(log);
        }

        return new GameSnapshot(state, mode, selectedSlot, turn, log);
    }

    private boolean handleMove(Direction dir, List<String> log) {
        if (mode != EngineMode.GAME) {
            log.add("Can't move in menu.");
            return false;
        }

        // здесь важно - usecase не обязан знать внутренности карты.
        // поэтому закладываемся на API GameState, который реализуется в model.
        boolean moved = state.tryMovePlayer(dir, log); // <-- метод нужно сделать в GameState // TODO: сделать в GameState
        if (!moved) return false;

        return true;
    }

    private boolean handleUseSelected(List<String> log) {
        if (mode != EngineMode.INVENTORY) {
            log.add("UseSelected is only in inventory.");
            return false;
        }
        if (selectedSlot < 0) {
            log.add("No slot selected.");
            return false;
        }

        boolean used = state.tryUseItem(selectedSlot, log); // <-- сделать в GameState // TODO: сделать в GameState
        return used; // used=true => тратим ход
    }

    private boolean handleEquipSelected(List<String> log) {
        if (mode != EngineMode.INVENTORY) {
            log.add("EquipSelected is only in inventory.");
            return false;
        }
        if (selectedSlot < 0) {
            log.add("No slot selected.");
            return false;
        }

        boolean equipped = state.tryEquipItem(selectedSlot, log); // <-- сделать в GameState // TODO: сделать в GameState
        return equipped; // equipped=true => тратим ход (решаить) // TODO: решить, что с ходом делать
    }

    private void afterPlayerTurn(List<String> log) {
        // например: подобрать предметы, проверить смерть, переход уровня, эффекты
        state.afterPlayerTurn(log); // <-- сделать в GameState (можно пустышкой) // TODO: сделать в GameState
    }

    private void enemiesTurn(List<String> log) {
        // turn-based: после хода игрока мир отвечает ходом врагов
        state.enemiesTurn(log); // <-- сделать в GameState (можно пустышкой) // TODO: сделать в GameState
    }
}
