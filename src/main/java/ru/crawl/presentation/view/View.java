package ru.crawl.presentation.view;

import com.googlecode.lanterna.screen.Screen;
import ru.crawl.domain.usecase.GameSnapshot;
import ru.crawl.presentation.input.UserInput;

public interface View extends AutoCloseable {
    UserInput pollInput() throws Exception;
    void render(GameSnapshot snapshot) throws Exception;
    void showInventory(GameSnapshot snapshot) throws Exception;
    @Override void close() throws Exception;
    void setScreen(Screen screen);

    int getWorldW();
    int getWorldH();
}
