package ru.crawl.presentation.view;

public final class AsciiSprite {
    private final String[] lines;
    private final int width;
    private final int height;

    public AsciiSprite(String... lines) {
        if (lines == null || lines.length == 0) {
            throw new IllegalArgumentException("Sprite must have at least one line");
        }
        int w = lines[0].length();
        for (String s : lines) {
            if (s.length() != w) {
                throw new IllegalArgumentException("All sprite lines must have same length");
            }
        }
        this.lines = lines;
        this.height = lines.length;
        this.width = w;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public char charAt(int x, int y) {
        return lines[y].charAt(x);
    }
}