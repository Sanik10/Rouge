package ru.crawl.domain.usecase;

import ru.crawl.domain.model.GameState;

public interface GameEngine {
    GameSnapshot step(Command command);

    GameState getState();

    long getSeed();
}
