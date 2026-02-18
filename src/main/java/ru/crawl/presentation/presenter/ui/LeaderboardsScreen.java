package ru.crawl.presentation.presenter.ui;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.TextInputDialog;
import com.googlecode.lanterna.gui2.table.Table;
import ru.crawl.datalayer.json.leaderbordData.Leaderboard;

import java.util.Comparator;
import java.util.List;

public class LeaderboardsScreen {
    private final Window window;

    public LeaderboardsScreen(List<Leaderboard> data) {
        window = new BasicWindow("");
        Table<String> table = new Table<>(
                "Name", "Treasure", "Level", "Kills", "Eats", "Elixirs", "Scroll", "Hit", "Miss", "Cells"
        );
        table.setPreferredSize(new TerminalSize(78, 15));
        data.stream()
                .sorted(Comparator.comparingInt((Leaderboard l) -> l.treasure).reversed())
                .limit(20)
                .forEach(l -> table.getTableModel().addRow(
                        safe(l.name),
                        String.valueOf(l.treasure),
                        String.valueOf(l.level),
                        String.valueOf(l.kills),
                        String.valueOf(l.eats),
                        String.valueOf(l.elixirs),
                        String.valueOf(l.scroll),
                        String.valueOf(l.hit),
                        String.valueOf(l.miss),
                        String.valueOf(l.cells)
                ));
        Panel panel = new Panel(new LinearLayout(Direction.VERTICAL));
        panel.addComponent(table);
        panel.addComponent(new EmptySpace(new TerminalSize(0,1)));
        panel.addComponent(new Button("Back", window::close));
        Component menu = panel.withBorder(Borders.singleLine("Menu"));
        window.setComponent(panel.withBorder(Borders.singleLine("Leaderboard")));
        window.setHints(java.util.List.of(Window.Hint.CENTERED));
    }

    public Window getWindow() {
        return window;
    }

    private static String safe(String s) {
        return (s == null || s.isBlank()) ? "-" : s;
    }

}
