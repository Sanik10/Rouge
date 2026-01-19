# 🚀 ПОШАГОВЫЙ ГАЙД: Как реализовать боевую систему

## Часть 1: Обновление GameState (10 минут)

### Шаг 1.1: Добавить imports и поля

**Найди эту строку:**
```java
public class GameState {
    private final int width;
    private final int height;
    private int playerX;
    private int playerY;
```

**Замени на:**
```java
import java.util.ArrayList;
import java.util.List;

public class GameState {
    private final int width;
    private final int height;
    
    private final Player player;
    private final List<Enemy> enemies;
    
    private int playerX;  // ← синхронизация с player
    private int playerY;  // ← синхронизация с player
```

### Шаг 1.2: Обновить конструктор

**Найди:**
```java
public GameState(int width, int height, int startX, int startY) {
    this.width = width;
    this.height = height;
    this.playerX = startX;
    this.playerY = startY;
}
```

**Замени на:**
```java
public GameState(int width, int height, int startX, int startY) {
    this.width = width;
    this.height = height;
    this.playerX = startX;
    this.playerY = startY;
    
    // Создать Player и enemies
    this.player = new Player(startX, startY);
    this.enemies = new ArrayList<>();
}
```

### Шаг 1.3: Добавить getter'ы и вспомогательные методы

**Добавить в конец класса:**
```java
// Getters
public Player getPlayer() {
    return player;
}

public List<Enemy> getEnemies() {
    return enemies;
}

// Работа с врагами
public void addEnemy(Enemy enemy) {
    enemies.add(enemy);
}

public void removeEnemy(Enemy enemy) {
    enemies.remove(enemy);
}

public Enemy getEnemyAt(int x, int y) {
    return enemies.stream()
            .filter(e -> e.getX() == x && e.getY() == y)
            .findFirst()
            .orElse(null);
}
```

---

## Часть 2: afterPlayerTurn (2 минуты)

### Шаг 2.1: Заполнить метод

**Найди:**
```java
public void afterPlayerTurn(List<String> log) {
    // TODO: доделать
}
```

**Замени на:**
```java
public void afterPlayerTurn(List<String> log) {
    player.tickSleep();
    
    if (player.isSleeping()) {
        log.add("You are still sleeping...");
    }
}
```

---

## Часть 3: Боевая система (30 минут)

### Шаг 3.1: playerAttackEnemy

**Добавить в GameState:**
```java
private void playerAttackEnemy(Enemy enemy, List<String> log) {
    int baseDamage = player.getStrength();
    int variance = Math.max(1, baseDamage / 5);
    int actualDamage = baseDamage - variance + 
                       (int)(Math.random() * (variance * 2));
    
    double hitChance = (player.getDex() - enemy.getStats().dexterity()) * 2;
    hitChance = Math.max(10, Math.min(90, hitChance));  // Clamp 10-90%
    
    if (Math.random() * 100 < hitChance) {
        // ← КЛЮЧЕВОЙ МОМЕНТ: применить спец. эффект!
        int actualDmg = enemy.getSpecial()
                             .onBeforeTakeDamage(enemy, actualDamage);
        enemy.takeDamage(actualDmg);
        
        log.add("Hit " + enemy.getType() + " for " + actualDmg + " damage!");
        
        if (!enemy.isAlive()) {
            enemies.remove(enemy);
            log.add(enemy.getType() + " defeated!");
        }
    } else {
        log.add("Miss!");
    }
}
```

### Шаг 3.2: enemyAttackPlayer

**Добавить в GameState:**
```java
private void enemyAttackPlayer(Enemy enemy, List<String> log) {
    int baseDamage = (int) enemy.getStats().strength();
    int variance = Math.max(1, baseDamage / 5);
    int actualDamage = baseDamage - variance + 
                       (int)(Math.random() * (variance * 2));
    
    double hitChance = (enemy.getStats().dexterity() - player.getDex()) * 2;
    hitChance = Math.max(15, Math.min(85, hitChance));  // Clamp 15-85%
    
    if (Math.random() * 100 < hitChance) {
        player.takeDamage(actualDamage);
        log.add(enemy.getType() + " hits you for " + actualDamage + " damage!");
        
        // Вызвать спец. эффект врага (lifesteal, sleep, и т.д.)
        enemy.getSpecial().onAfterEnemyHit(enemy, player, actualDamage);
    } else {
        log.add(enemy.getType() + " misses!");
    }
}
```

### Шаг 3.3: moveEnemyTowards

**Добавить в GameState:**
```java
private void moveEnemyTowards(Enemy enemy, List<String> log) {
    int ex = enemy.getX();
    int ey = enemy.getY();
    int px = player.getX();
    int py = player.getY();
    
    int newX = ex;
    int newY = ey;
    
    // Greedy pathfinding (простое, но эффективное)
    if (Math.abs(px - ex) > Math.abs(py - ey)) {
        // Горизонтальное расстояние больше
        newX = px > ex ? ex + 1 : ex - 1;
    } else {
        // Вертикальное расстояние больше или равно
        newY = py > ey ? ey + 1 : ey - 1;
    }
    
    // Проверить границы
    if (newX >= 0 && newX < width && newY >= 0 && newY < height) {
        // Проверить столкновение с другим врагом
        if (getEnemyAt(newX, newY) == null) {
            enemy.moveTo(newX, newY);
            log.add(enemy.getType() + " moves towards you");
        }
    }
}
```

### Шаг 3.4: moveEnemyRandomly

**Добавить в GameState:**
```java
private void moveEnemyRandomly(Enemy enemy, List<String> log) {
    int[] dirs = { -1, 0, 1 };
    int newX = enemy.getX() + dirs[(int)(Math.random() * 3)];
    int newY = enemy.getY() + dirs[(int)(Math.random() * 3)];
    
    // Проверить границы
    if (newX >= 0 && newX < width && newY >= 0 && newY < height) {
        // Проверить столкновение с другим врагом
        if (getEnemyAt(newX, newY) == null) {
            enemy.moveTo(newX, newY);
        }
    }
}
```

### Шаг 3.5: enemiesTurn (ГЛАВНЫЙ МЕТОД)

**Найди:**
```java
public void enemiesTurn(List<String> log) {
    log.add("[Enemies turn]");
}
```

**Замени на:**
```java
public void enemiesTurn(List<String> log) {
    List<Enemy> deadEnemies = new ArrayList<>();
    
    for (Enemy enemy : enemies) {
        if (!enemy.isAlive()) {
            deadEnemies.add(enemy);
            continue;
        }
        
        // 1. Применить спец. эффекты враага
        enemy.tick(this);
        
        // 2. Рассчитать расстояние до игрока
        int distToPlayer = Math.abs(enemy.getX() - player.getX()) + 
                          Math.abs(enemy.getY() - player.getY());
        
        // 3. Враг видит игрока?
        if (distToPlayer <= enemy.getStats().hostilityRadius()) {
            moveEnemyTowards(enemy, log);
        } else {
            moveEnemyRandomly(enemy, log);
        }
        
        // 4. Проверить, рядом ли враг (Chebyshev distance)
        int chebyDist = Math.max(
            Math.abs(enemy.getX() - player.getX()),
            Math.abs(enemy.getY() - player.getY())
        );
        
        if (chebyDist == 1) {
            // Враг рядом — атаковать!
            enemyAttackPlayer(enemy, log);
            
            if (!player.isAlive()) {
                log.add("YOU DIED!");
                return;
            }
        }
        
        if (!enemy.isAlive()) {
            deadEnemies.add(enemy);
        }
    }
    
    // Удалить мертвых врагов
    deadEnemies.forEach(enemies::remove);
}
```

---

## Часть 4: Обновить tryMovePlayer (10 минут)

### Шаг 4.1: Добавить проверку врагов и синхронизацию

**Найди:**
```java
public boolean tryMovePlayer(Direction dir, List<String> log) {
    int nx = playerX;
    int ny = playerY;

    switch (dir) {
        case UP    -> ny--;
        case DOWN  -> ny++;
        case LEFT  -> nx--;
        case RIGHT -> nx++;
    }

    if (nx < 0 || ny < 0 || nx >= width || ny >= height) {
        log.add("Bump into wall!");
        return false;
    }

    playerX = nx;
    playerY = ny;
    log.add("Moved to (" + playerX + "," + playerY + ")");
    return true;
}
```

**Замени на:**
```java
public boolean tryMovePlayer(Direction dir, List<String> log) {
    int nx = playerX;
    int ny = playerY;

    switch (dir) {
        case UP    -> ny--;
        case DOWN  -> ny++;
        case LEFT  -> nx--;
        case RIGHT -> nx++;
    }

    // Проверка границ
    if (nx < 0 || ny < 0 || nx >= width || ny >= height) {
        log.add("Bump into wall!");
        return false;
    }

    // Проверка врага на новой позиции
    Enemy enemyInTheWay = getEnemyAt(nx, ny);
    if (enemyInTheWay != null) {
        log.add("Attacking " + enemyInTheWay.getType() + "!");
        playerAttackEnemy(enemyInTheWay, log);
        return true;  // ← Ход потратился на атаку!
    }

    // Движение (синхронизация между player и playerX/playerY)
    player.moveTo(nx, ny);
    playerX = nx;
    playerY = ny;
    log.add("Moved to (" + playerX + "," + playerY + ")");
    return true;
}
```

---

## Часть 5: Обновить Main для тестирования (15 минут)

### Шаг 5.1: Полный рабочий Main

**Замени весь Main.java:**
```java
package ru.crawl.app;

import ru.crawl.domain.model.*;
import ru.crawl.domain.usecase.*;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== CRAWL COMBAT SYSTEM TEST ===\n");

        // Создать игру 10х10
        GameState state = new GameState(10, 10, 5, 5);
        GameEngine engine = new DefaultGameEngine(state);

        // Добавить врагов
        Enemy vampire = new Enemy(Enemy.Type.VAMPIRE, 1, 5, 4);
        Enemy zombie = new Enemy(Enemy.Type.ZOMBIE, 1, 6, 5);
        
        state.addEnemy(vampire);
        state.addEnemy(zombie);

        System.out.println("Initial state:");
        System.out.println("  Player: (" + state.playerX() + "," + state.playerY() + ") HP=" + 
                          state.getPlayer().getHp() + "/" + 
                          state.getPlayer().getMaxHp());
        System.out.println("  Vampire: (" + vampire.getX() + "," + vampire.getY() + ") HP=" + 
                          vampire.getHp() + "/" + vampire.getStats().hpMax() + 
                          " (FirstHitIgnored)");
        System.out.println("  Zombie: (" + zombie.getX() + "," + zombie.getY() + ") HP=" + 
                          zombie.getHp() + "/" + zombie.getStats().hpMax());
        System.out.println();

        // Раунд 1: Игрок атакует вампира
        System.out.println("--- TURN 1: Attack Vampire! ---");
        print(engine.step(new Command.Move(Direction.UP)));
        
        System.out.println("After turn 1:");
        System.out.println("  Player HP: " + state.getPlayer().getHp() + "/" + 
                          state.getPlayer().getMaxHp());
        System.out.println("  Vampire HP: " + vampire.getHp() + "/" + 
                          vampire.getStats().hpMax() + 
                          " (FirstHitIgnored should have blocked!)");
        System.out.println();

        // Раунд 2: Второй удар должен пройти
        System.out.println("--- TURN 2: Attack Again! ---");
        print(engine.step(new Command.Move(Direction.UP)));
        
        System.out.println("After turn 2:");
        System.out.println("  Player HP: " + state.getPlayer().getHp() + "/" + 
                          state.getPlayer().getMaxHp());
        System.out.println("  Vampire HP: " + vampire.getHp() + "/" + 
                          vampire.getStats().hpMax() + 
                          " (This time damage should apply!)");
        System.out.println();

        // Раунд 3: Ещё раз
        System.out.println("--- TURN 3: Another Round ---");
        print(engine.step(new Command.Move(Direction.UP)));
        
        System.out.println("After turn 3:");
        System.out.println("  Vampire alive? " + vampire.isAlive());
        System.out.println("  Player HP: " + state.getPlayer().getHp() + "/" + 
                          state.getPlayer().getMaxHp());
        System.out.println();

        // Больше раундов
        for (int i = 4; i <= 10; i++) {
            System.out.println("--- TURN " + i + " ---");
            print(engine.step(new Command.Move(Direction.UP)));
            
            if (!vampire.isAlive() || !state.getPlayer().isAlive()) {
                break;
            }
        }

        System.out.println("=== FINAL STATE ===");
        System.out.println("Player alive? " + state.getPlayer().isAlive() + 
                          " HP=" + state.getPlayer().getHp());
        System.out.println("Vampire alive? " + vampire.isAlive());
        System.out.println("Zombie alive? " + zombie.isAlive());
    }

    static void print(GameSnapshot snap) {
        System.out.println("✓ Player (" + snap.playerX() + "," + snap.playerY() + 
                          ") | Turn " + snap.turnNumber());
        for (String line : snap.log()) {
            System.out.println("  > " + line);
        }
        System.out.println();
    }
}
```

---

## Часть 6: Опционально — Спец. Способности Врагов

### Шаг 6.1: VampireDrainSpecial

**Создать новый файл: VampireDrainSpecial.java**
```java
package ru.crawl.domain.model;

public final class VampireDrainSpecial implements EnemySpecial {
    @Override
    public void onAfterEnemyHit(Enemy enemy, Player player, int damageDealt) {
        // Вампир восстанавливает 50% нанесённого урона
        int healed = Math.max(1, (damageDealt * 50) / 100);
        enemy.takeDamage(-healed);  // ← Отрицательный урон = исцеление
        
        // Или если добавишь метод heal():
        // enemy.heal(healed);
    }
}
```

### Шаг 6.2: SleepOnHitSpecial

**Создать новый файл: SleepOnHitSpecial.java**
```java
package ru.crawl.domain.model;

public final class SleepOnHitSpecial implements EnemySpecial {
    private final int sleepChance;  // 0-100%

    public SleepOnHitSpecial(int sleepChance) {
        this.sleepChance = sleepChance;
    }

    @Override
    public void onAfterEnemyHit(Enemy enemy, Player player, int damageDealt) {
        if (Math.random() * 100 < sleepChance) {
            player.applySleep(2);  // 2 хода сна
        }
    }
}
```

### Шаг 6.3: Обновить EnemySpecialFactory

**Найди:**
```java
public static EnemySpecial forType(Enemy.Type type, int level) {
    List<EnemySpecial> list = new ArrayList<>();

    switch (type) {
        case VAMPIRE -> {
            list.add(new FirstHitIgnoredSpecial());
            // TODO: добавить VampireDrainMaxHpSpecial
        }
        case MAGIC_SNAKE -> {
            // TODO: добавить SleepOnHitSpecial (25–35%)
        }
        default -> { }
    }

    if (list.isEmpty()) return new NoSpecial();
    if (list.size() == 1) return list.get(0);
    return new CompositeEnemySpecial(list);
}
```

**Замени на:**
```java
public static EnemySpecial forType(Enemy.Type type, int level) {
    List<EnemySpecial> list = new ArrayList<>();

    switch (type) {
        case VAMPIRE -> {
            list.add(new FirstHitIgnoredSpecial());
            list.add(new VampireDrainSpecial());
        }
        case MAGIC_SNAKE -> {
            list.add(new SleepOnHitSpecial(30));  // 30% шанс усыпить
        }
        default -> { }
    }

    if (list.isEmpty()) return new NoSpecial();
    if (list.size() == 1) return list.get(0);
    return new CompositeEnemySpecial(list);
}
```

---

## ✅ ФИНАЛЬНЫЙ CHECKLIST

Перед тем как запустить:

- [ ] GameState содержит `private final Player player`
- [ ] GameState содержит `private final List<Enemy> enemies`
- [ ] Конструктор инициализирует player и enemies
- [ ] afterPlayerTurn вызывает `player.tickSleep()`
- [ ] tryMovePlayer проверяет врагов через getEnemyAt()
- [ ] playerAttackEnemy реализована с проверкой специальностей
- [ ] enemyAttackPlayer реализована с проверкой специальностей
- [ ] enemiesTurn полностью реализована
- [ ] moveEnemyTowards реализована
- [ ] moveEnemyRandomly реализована
- [ ] Main тест добавляет врагов
- [ ] Main тест создаёт вампира с FirstHitIgnored
- [ ] Main тест показывает боевую систему в работе

**ГОТОВО К ЗАПУСКУ! 🚀**

## 🎯 ЧТО ДОЛЖНО ПРОИЗОЙТИ ПРИ ЗАПУСКЕ:

```
=== CRAWL COMBAT SYSTEM TEST ===

Initial state:
  Player: (5,5) HP=100/100
  Vampire: (5,4) HP=25/25 (FirstHitIgnored)
  Zombie: (6,5) HP=30/30

--- TURN 1: Attack Vampire! ---
✓ Player (5,5) | Turn 1
  > === Turn 0 ===
  > Attacking VAMPIRE!
  > Hit VAMPIRE for 13 damage!   ← урон рассчитан
  > VAMPIRE moves towards you
  > VAMPIRE hits you for 8 damage!
  > ZOMBIE waits...

After turn 1:
  Player HP: 92/100
  Vampire HP: 25/25  ← БЛОКИРОВАНА! FirstHitIgnored работает!
```

Если видишь это, значит **боевая система работает идеально!** ✓