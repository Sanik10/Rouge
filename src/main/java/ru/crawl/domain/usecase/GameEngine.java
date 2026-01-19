package ru.crawl.domain.usecase;

public interface GameEngine {
    GameSnapshot step(Command command);
}
