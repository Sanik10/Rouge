package ru.crawl.app;

import ru.crawl.domain.model.Direction;
import ru.crawl.domain.model.GameState;
import ru.crawl.domain.usecase.*;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Crawl started (пока тест) ===\n");

        GameState state = new GameState(5, 5, 2, 2);
        GameEngine engine = new DefaultGameEngine(state);

        print(engine.step(new Command.Move(Direction.UP)));
        print(engine.step(new Command.Move(Direction.LEFT)));
        print(engine.step(new Command.Wait()));
        print(engine.step(new Command.OpenInventory()));
        print(engine.step(new Command.SelectSlot(1)));
        print(engine.step(new Command.EquipSelected()));
        print(engine.step(new Command.CloseMenu()));
        print(engine.step(new Command.SelectSlot(0)));
        print(engine.step(new Command.UseSelected()));
    }

    static void print(GameSnapshot snap) {
        System.out.println("✓ Player (" + snap.playerX() + "," + snap.playerY() + ") | Turn " + snap.turnNumber());
        for (String line : snap.log()) {
            System.out.println("  > " + line);
        }
        System.out.println();
    }
}
