package ru.crawl.presentation.input;

public record UserInput(Type type, Character ch) {
  public enum Type { CHAR, ESC, ENTER }
}
