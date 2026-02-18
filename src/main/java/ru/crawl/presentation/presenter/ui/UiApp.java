package ru.crawl.presentation.presenter.ui;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.DefaultWindowManager;
import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import ru.crawl.datalayer.json.leaderbordData.Leaderboard;

import java.io.IOException;
import java.util.List;

public class UiApp implements AutoCloseable{
    private final Screen screen;
    private final MultiWindowTextGUI gui;

    private final int worldW;
    private final int worldH;

    private final RougeBackground background;

    private static final int PAD_X = 2;
    private static final int PAD_Y = 1;
    private static final int HUD_W = 30;
    private static final int LOG_H = 4;

    private String playerName;

    public UiApp(int worldW, int worldH) throws IOException{
        this.worldH = worldH;
        this.worldW = worldW;
        DefaultTerminalFactory factory = new DefaultTerminalFactory();
        factory.setInitialTerminalSize(new TerminalSize(worldW + HUD_W + 5, worldH + LOG_H + 5));
        screen = factory.createScreen();
        screen.startScreen();
        screen.setCursorPosition(null);
        background = new RougeBackground();
        gui = new MultiWindowTextGUI(screen, new DefaultWindowManager(), background);
    }

    public StartMenuAction showStartScreen(boolean canLoad) {
        StartScreen start =  new StartScreen(canLoad);
        background.setBanner(BannerType.DEFAULT);
        gui.addWindowAndWait(start.getWindow());
        playerName = start.getPlayerName();
        return start.getAction()==null?StartMenuAction.EXIT:start.getAction();
    }

    public FinishMenuAction showFinishScreen(int level, int treasure, FinishResult result) {
        FinalScreen finalScreen = new FinalScreen(level, treasure, result);
        if(result == FinishResult.LOSE) background.setBanner(BannerType.LOSE);
        else background.setBanner(BannerType.VICTORY);
        gui.addWindowAndWait(finalScreen.getWindow());
        return finalScreen.getAction()==null?FinishMenuAction.EXIT:finalScreen.getAction();
    }

    public void showLeaderboard(List<Leaderboard> data) {
        LeaderboardsScreen screen = new LeaderboardsScreen(data);
        background.setBanner(BannerType.LEADERBOARD);
        gui.addWindowAndWait(screen.getWindow());
    }

    @Override
    public void close() throws Exception {
        screen.stopScreen();
    }

    public Screen getScreen() {
        return screen;
    }

    public String getName() {return playerName; }
}
