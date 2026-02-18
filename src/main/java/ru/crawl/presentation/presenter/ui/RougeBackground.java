package ru.crawl.presentation.presenter.ui;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.gui2.AbstractComponent;
import com.googlecode.lanterna.gui2.ComponentRenderer;
import com.googlecode.lanterna.gui2.TextGUIGraphics;

public class RougeBackground extends AbstractComponent<RougeBackground> {

    private static final String[] DEFAULT_BANNER = {
            "██████╗  ██████╗  ██████╗ ██╗   ██╗███████╗",
            "██╔══██╗██╔═══██╗██╔════╝ ██║   ██║██╔════╝",
            "██████╔╝██║   ██║██║  ███╗██║   ██║█████╗",
            "██╔══██╗██║   ██║██║   ██║██║   ██║██╔══╝",
            "██║  ██║╚██████╔╝╚██████╔╝╚██████╔╝███████╗",
            "╚═╝  ╚═╝ ╚═════╝  ╚═════╝  ╚═════╝ ╚══════╝"
    };

    private static final String[] LOSE_BANNER = {
            "██╗      ██████╗  ███████╗███████╗",
            "██║     ██╔═══██╗ ██╔════╝██╔════╝",
            "██║     ██║   ██║ ███████╗█████╗",
            "██║     ██║   ██║ ╚════██║██╔══╝",
            "███████╗╚██████╔╝ ███████║███████╗",
            "╚══════╝ ╚═════╝  ╚══════╝╚══════╝"
    };


    private static final String[] VICTORY_BANNER = {
            " ██████╗ ██████╗ ███╗   ██╗ ██████╗ ██████╗  █████╗ ████████╗██╗   ██╗██╗      █████╗ ████████╗██╗ ██████╗ ███╗   ██╗███████╗",
            "██╔════╝██╔═══██╗████╗  ██║██╔════╝██╔═══██╗██╔══██╗╚══██╔══╝██║   ██║██║     ██╔══██╗╚══██╔══╝██║██╔═══██╗████╗  ██║██╔════╝",
            "██║     ██║   ██║██╔██╗ ██║██║     ██║   ██║███████║   ██║   ██║   ██║██║     ███████║   ██║   ██║██║   ██║██╔██╗ ██║███████╗",
            "██║     ██║   ██║██║╚██╗██║██║     ██║   ██║██╔══██║   ██║   ██║   ██║██║     ██╔══██║   ██║   ██║██║   ██║██║╚██╗██║╚════██║",
            "╚██████╗╚██████╔╝██║ ╚████║╚██████╗╚██████╔╝██║  ██║   ██║   ╚██████╔╝███████╗██║  ██║   ██║   ██║╚██████╔╝██║ ╚████║███████║",
            " ╚═════╝ ╚═════╝ ╚═╝  ╚═══╝ ╚═════╝ ╚═════╝ ╚═╝  ╚═╝   ╚═╝    ╚═════╝ ╚══════╝╚═╝  ╚═╝   ╚═╝   ╚═╝ ╚═════╝ ╚═╝  ╚═══╝╚══════╝"
    };

    public static final String[] LEADERBOARD_BANNER = {
            "██╗     ███████╗ █████╗ ██████╗ ███████╗██████╗ ██████╗  ██████╗  █████╗ ██████╗ ██████╗",
            "██║     ██╔════╝██╔══██╗██╔══██╗██╔════╝██╔══██╗██╔══██╗██╔═══██╗██╔══██╗██╔══██╗██╔══██╗",
            "██║     █████╗  ███████║██║  ██║█████╗  ██████╔╝██████╔╝██║   ██║███████║██████╔╝██║  ██║",
            "██║     ██╔══╝  ██╔══██║██║  ██║██╔══╝  ██╔══██╗██╔══██╗██║   ██║██╔══██║██╔══██╗██║  ██║",
            "███████╗███████╗██║  ██║██████╔╝███████╗██║  ██║██████╔╝╚██████╔╝██║  ██║██║  ██║██████╔╝",
            "╚══════╝╚══════╝╚═╝  ╚═╝╚═════╝ ╚══════╝╚═╝  ╚═╝╚═════╝  ╚═════╝ ╚═╝  ╚═╝╚═╝  ╚═╝╚═════╝"
    };


    private BannerType current = BannerType.DEFAULT;

    @Override
    protected ComponentRenderer<RougeBackground> createDefaultRenderer() {
        return new ComponentRenderer<>(){
            @Override
            public TerminalSize getPreferredSize(RougeBackground component) {
                return TerminalSize.ONE;
            }

            @Override
            public void drawComponent(TextGUIGraphics g, RougeBackground component) {
                String[] banner = switch (current) {
                    case DEFAULT -> DEFAULT_BANNER;
                    case LOSE -> LOSE_BANNER;
                    case VICTORY -> VICTORY_BANNER;
                    case LEADERBOARD ->  LEADERBOARD_BANNER;
                };
                TerminalSize size = g.getSize();
                int maxLen = 0;
                for (String line : banner) maxLen = Math.max(maxLen, line.length());
                int w = size.getColumns();
                int x = Math.max(0, (w - maxLen) / 2);
                g.setBackgroundColor(TextColor.ANSI.BLACK);
                g.setForegroundColor(TextColor.ANSI.BLACK);
                g.fill(' ');
                switch (current) {
                    case DEFAULT -> g.setForegroundColor(TextColor.ANSI.GREEN_BRIGHT);
                    case LOSE -> g.setForegroundColor(TextColor.ANSI.RED_BRIGHT);
                    case VICTORY -> g.setForegroundColor(TextColor.ANSI.YELLOW_BRIGHT);
                    case LEADERBOARD ->  g.setForegroundColor(TextColor.ANSI.CYAN_BRIGHT);
                }
                for (int i = 0; i < banner.length; i++) g.putString(x, 1+i, banner[i]);
            }
        };
    }

    public void setBanner(BannerType type) {
        this.current = type; invalidate();
    }
}
