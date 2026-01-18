package ru.crawl.domain.usecase;

import ru.crawl.domain.model.Direction;
import ru.crawl.domain.model.GameState;
import java.util.ArrayList;
import java.util.List;

public class DefaultGameEngine implements GameEngine {
    private final GameState state;
    private long turn = 0;

    public DefaultGameEngine(GameState state) {
        this.state = state;
    }

    @Override
    public GameSnapshot step(Command cmd) {
        List<String> log = new ArrayList<>();
        log.add("=== Turn " + turn + " ===");

        boolean playerActionUsedTurn = false;

        if (cmd instanceof Command.Move m) {
            playerActionUsedTurn = state.tryMovePlayer(m.direction(), log);
        }
        else if (cmd instanceof Command.Wait) {
            log.add("Player waits");
            playerActionUsedTurn = true;
        }
        else if (cmd instanceof Command.UseSelected) {
            log.add("UseSelected (slot will be determined by SelectSlot)");
        }
        else if (cmd instanceof Command.EquipSelected) {
            log.add("EquipSelected (slot will be determined by SelectSlot)");
        }
        else if (cmd instanceof Command.SelectSlot s) {
            log.add("Selected slot: " + s.index());
        }
        else if (cmd instanceof Command.OpenInventory) {
            log.add("Opened inventory");
        }
        else if (cmd instanceof Command.CloseMenu) {
            log.add("Closed menu");
        }
        else if (cmd instanceof Command.Confirm) {
            log.add("Confirmed");
        }
        else if (cmd instanceof Command.Cancel) {
            log.add("Cancelled");
        }

        if (playerActionUsedTurn) {
            state.afterPlayerTurn(log);
            state.enemiesTurn(log);
            turn++;
        }

        return new GameSnapshot(state.playerX(), state.playerY(), (int) turn, log);
    }
}
