package ru.crawl.presentation.presenter.ui;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.TextInputDialog;
import com.googlecode.lanterna.screen.Screen;

public class StartScreen {
   private final Window window;
   private StartMenuAction action;
   private String playerName;
   private static final String DEFAULT_NAME = "Player";

    public StartScreen(boolean canLoad) {
        window = new BasicWindow("");
        Panel panel = new Panel(new LinearLayout(Direction.VERTICAL));
        panel.addComponent(new Button("Start", () -> {
            String name = TextInputDialog.showDialog(
                    window.getTextGUI(),
                    "Player name",
                    "Enter your name:",
                    DEFAULT_NAME
            );
            if (name == null || name.isBlank()) {
                name = DEFAULT_NAME;
            }else {
                name = name.trim();
                if (name.length() > 12) {
                    name = name.substring(0, 12);
                }
            }
                this.playerName = name;
                action = StartMenuAction.START;
                window.close();
        }));
        panel.addComponent(new EmptySpace(new TerminalSize(0,1)));
        Button loadButton = new Button("Load", () -> {action = StartMenuAction.LOAD; window.close();});
        loadButton.setEnabled(canLoad);
        panel.addComponent(loadButton);
        panel.addComponent(new EmptySpace(new TerminalSize(0,1)));
        panel.addComponent(new Button("Leaderboard", () -> {action = StartMenuAction.LEADERBOARD; window.close();}));
        panel.addComponent(new EmptySpace(new TerminalSize(0,1)));
        panel.addComponent(new Button("Exit", () -> {action = StartMenuAction.EXIT; window.close();}));
        panel.addComponent(new EmptySpace(new TerminalSize(0,5)));
        panel.addComponent(new Label("WASD - Move"));
        panel.addComponent(new Label("Enter - Confirm"));
        panel.addComponent(new Label("> - Enter in the room"));
        panel.addComponent(new Label("I - Inventory"));
        panel.addComponent(new Label("1-9 - Select item"));
        panel.addComponent(new Label("E - Use item"));
        panel.addComponent(new Label("P - Save game"));
        panel.addComponent(new Label("Q/Esc - Quit"));
        Component menu = panel.withBorder(Borders.singleLine("Menu"));

        window.setComponent(menu);
        window.setHints(java.util.List.of(Window.Hint.CENTERED));
    }

    public Window getWindow() {
        return window;
    }

    public StartMenuAction getAction() {
        return action;
    }

    public String getPlayerName() {
        return playerName;
    }

}
