package ru.crawl.presentation.presenter.ui;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;

public class FinalScreen {
    private final Window window;
    private FinishMenuAction action;

    public FinalScreen(int level, int treasure, FinishResult result) {
        window = new BasicWindow("");
        Panel panel = new Panel(new LinearLayout(Direction.VERTICAL));
        if(result == FinishResult.LOSE) panel.addComponent(new Label("Game Over! Don’t worry — you can try again… if you dare."));
        else panel.addComponent(new Label("Victory! You survived this time… but will you dare to return?"));
        panel.addComponent(new EmptySpace(new TerminalSize(0,2)));
        panel.addComponent(new Label("Treasure:" + treasure));
        panel.addComponent(new Label("Level: " + level));
        panel.addComponent(new EmptySpace(new TerminalSize(0,2)));
        panel.addComponent(new Button("Restart", () -> {
            action = FinishMenuAction.RESTART;
            window.close();
        }));
        panel.addComponent(new EmptySpace(new TerminalSize(0,1)));
        panel.addComponent(new Button("Main menu", () -> {
            action = FinishMenuAction.TO_MENU;
            window.close();
        }));
        panel.addComponent(new EmptySpace(new TerminalSize(0,1)));
        panel.addComponent(new Button("Exit", () -> {
            action = FinishMenuAction.EXIT;
            window.close();
        }));
        Component menu = panel.withBorder(Borders.doubleLineBevel());
        window.setComponent(menu);
        window.setHints(java.util.List.of(Window.Hint.CENTERED, Window.Hint.NO_DECORATIONS));

    }

    public Window getWindow() {
        return window;
    }

    public FinishMenuAction getAction() {
        return action;
    }
}
