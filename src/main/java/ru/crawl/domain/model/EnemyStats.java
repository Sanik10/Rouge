package ru.crawl.domain.model;

public record EnemyStats(
    int hpMax,
    double strength,
    double dexterity,
    int hostilityRadius
) {}
