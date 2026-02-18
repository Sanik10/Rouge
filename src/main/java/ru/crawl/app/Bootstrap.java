package ru.crawl.app;

import ru.crawl.domain.generation.LevelGenerator;
import ru.crawl.domain.generation.rogue.Room;
import ru.crawl.domain.model.GameState;
import ru.crawl.domain.usecase.DefaultGameEngine;
import ru.crawl.domain.usecase.GameEngine;
import ru.crawl.presentation.input.KeyMapper;
import ru.crawl.presentation.presenter.GamePresenter;
import ru.crawl.presentation.view.LanternaView;
import ru.crawl.presentation.view.LanternaView3d;
import ru.crawl.presentation.view.View;

public final class Bootstrap {
    public void run() throws Exception {
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

        // добавили врагов
        state.enemies().addAll(lvl.enemies());
        state.items().addAll(lvl.items());

        View view = new LanternaView(LevelGenerator.WORLD_W, LevelGenerator.WORLD_H);
        View view3d = new LanternaView3d(LevelGenerator.WORLD_W, LevelGenerator.WORLD_H);
        KeyMapper keyMapper = new KeyMapper();

        GameEngine engine = new DefaultGameEngine(state, levelIndex, seed);
        GamePresenter presenter = new GamePresenter(engine, view, keyMapper);
        presenter.run();
    }
}
