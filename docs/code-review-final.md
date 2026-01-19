# 📋 ПОЛНЫЙ КОД-РЕВЬЮ: Crawl Game Architecture

## 🎯 СТАТУС ПРОЕКТА: 70% готов, нужно доделать боевую систему

---

## ✅ ЧТО РАБОТАЕТ ИДЕАЛЬНО

### 1. **Command System (sealed interface)** — 10/10 ⭐

```java
public sealed interface Command permits
    Command.Move,
    Command.Wait,
    Command.OpenInventory,
    // ...
{
    record Move(Direction direction) implements Command {}
    record Wait() implements Command {}
    // ...
}
```

**Почему это гений:**
- ✅ Type-safe (compile-time checking)
- ✅ Pattern matching работает идеально
- ✅ Невозможно забыть обработать тип команды
- ✅ Легко расширяется новыми командами
- ✅ Record'ы — минимум boilerplate

**Использование:**
```java
if (cmd instanceof Command.Move movement) {
    // cmd.direction() доступен
}
```

---

### 2. **GameSnapshot (immutable record)** — 10/10 ⭐

```java
public record GameSnapshot(
    int playerX,
    int playerY,
    int turnNumber,
    List<String> log
)
```

**Плюсы:**
- ✅ Immutable (безопасно)
- ✅ Auto-generated equals(), hashCode(), toString()
- ✅ Декларативный подход
- ✅ Perfect для отправки на UI

---

### 3. **Enemy & Player классы** — 9/10 ⭐

```java
// Enemy.java
public class Enemy {
    private final Type type;
    private final int level;
    private int x, y;
    private final EnemyStats stats;
    private int hp;
    private final EnemySpecial special;

    public void takeDamage(int incoming) {
        int actual = special.onBeforeTakeDamage(this, incoming);
        hp = Math.max(0, hp - actual);  // ← Правильно!
    }
}

// Player.java
public class Player {
    private int x, y;
    private int hp, maxHp;
    private int strength, dex;
    private int sleepTurns;
    
    public void takeDamage(int damage) { ... }
    public void applySleep(int turns) { ... }
}
```

**Плюсы:**
- ✅ Полная система HP
- ✅ Статистика (STR, DEX)
- ✅ Integration с EnemySpecial
- ✅ Sleep механика для MAGIC_SNAKE

**Проблема:** Они не в GameState!

---

### 4. **EnemyStats + Factory** — 10/10 ⭐

```java
public record EnemyStats(
    int hpMax,
    double strength,
    double dexterity,
    int hostilityRadius
) {}

public static EnemyStats statsFor(Enemy.Type type, int levelEnemy) {
    int lvl = Math.max(1, Math.min(21, levelEnemy));
    return switch (type) {
        case ZOMBIE -> {
            int hpMax = 30 + 3 * (lvl - 1);
            double strength = 6 + 0.3 * (lvl - 1);
            // ...
        }
        // ...
    };
}
```

**Идеально:**
- ✅ Масштабирование по уровню
- ✅ Каждый тип враги имеет уникальный профиль
- ✅ Switch expression (modern Java)
- ✅ Параметры баланса на месте

**Примеры:**
| Тип | Level 1 | Level 10 | Level 20 |
|-----|---------|----------|----------|
| **ZOMBIE** | 30 HP | 57 HP | 87 HP |
| **VAMPIRE** | 25 HP | 43 HP | 63 HP |
| **GHOST** | 8 HP | 25 HP | 36 HP |
| **OGRE** | 40 HP | 76 HP | 120 HP |

---

### 5. **EnemySpecial Hook System** — 10/10 ⭐

```java
public interface EnemySpecial {
    default int onBeforeTakeDamage(Enemy enemy, int incomingDamage) { 
        return incomingDamage; 
    }
    
    default void onAfterEnemyHit(Enemy enemy, Player player, int damageDealt) { }
    
    default void onTick(Enemy enemy, GameState state) { }
}

// Реализация
public final class FirstHitIgnoredSpecial implements EnemySpecial {
    private boolean firstHitIgnored = true;

    @Override
    public int onBeforeTakeDamage(Enemy enemy, int incomingDamage) {
        if (firstHitIgnored) {
            firstHitIgnored = false;
            return 0;  // ← БЛОКИРУЕМ УРОН!
        }
        return incomingDamage;
    }
}
```

**Это мастерство:**
- ✅ Decorator паттерн (правильный подход)
- ✅ Hook'и для разных моментов времени
- ✅ Stateful эффекты (FirstHitIgnored имеет флаг)
- ✅ CompositeEnemySpecial позволяет комбинировать эффекты

**Как это работает:**
```
Вампир атакуется в ПЕРВЫЙ раз:
  damage = 15
  → special.onBeforeTakeDamage(vampire, 15)
  → FirstHitIgnoredSpecial возвращает 0
  → vampire.takeDamage(0)
  → HP не изменился!

Вампир атакуется во ВТОРОЙ раз:
  damage = 15
  → special.onBeforeTakeDamage(vampire, 15)
  → FirstHitIgnoredSpecial возвращает 15
  → vampire.takeDamage(15)
  → HP снизился на 15! ✓
```

---

### 6. **DefaultGameEngine основная логика** — 8/10 ⭐

```java
public class DefaultGameEngine implements GameEngine {
    private final GameState state;
    private long turn = 0;

    @Override
    public GameSnapshot step(Command cmd) {
        List<String> log = new ArrayList<>();
        log.add("=== Turn " + turn + " ===");

        boolean playerActionUsedTurn = false;

        if (cmd instanceof Command.Move movement) {
            playerActionUsedTurn = state.tryMovePlayer(movement.direction(), log);
        }
        else if (cmd instanceof Command.Wait) {
            log.add("Player waits");
            playerActionUsedTurn = true;
        }
        // ... остальные команды ...

        if (playerActionUsedTurn) {
            state.afterPlayerTurn(log);
            state.enemiesTurn(log);
            turn++;
        }

        return new GameSnapshot(state.playerX(), state.playerY(), (int) turn, log);
    }
}
```

**Плюсы:**
- ✅ Правильная логика: команда → обработка → враги → turn++
- ✅ Turn counter работает
- ✅ Log система хорошо организована
- ✅ GameSnapshot возвращается правильно

**Проблема:** Логика неполная (нет боевой системы)

---

## ⚠️ КРИТИЧЕСКИЕ ПРОБЛЕМЫ

### **ПРОБЛЕМА 1: GameState НЕ содержит Player и Enemy!**

#### ❌ СЕЙЧАС:
```java
public class GameState {
    private final int width;
    private final int height;
    private int playerX;       // ← только координаты!
    private int playerY;       // ← только координаты!
    private final String[] inventory = { "Potion", "Sword", null };
    
    // ← ГДЕ Player объект?
    // ← ГДЕ List<Enemy>?
}
```

#### ✅ ДОЛЖНО БЫТЬ:
```java
public class GameState {
    private final int width;
    private final int height;
    
    private final Player player;           // ← ДОБАВИТЬ
    private final List<Enemy> enemies;     // ← ДОБАВИТЬ
    
    private final String[] inventory = { "Potion", "Sword", null };

    public GameState(int width, int height, int startX, int startY) {
        this.width = width;
        this.height = height;
        this.player = new Player(startX, startY);      // ← CREATE
        this.enemies = new ArrayList<>();              // ← CREATE
    }

    public Player getPlayer() { return player; }
    public List<Enemy> getEnemies() { return enemies; }
    
    public void addEnemy(Enemy enemy) { enemies.add(enemy); }
    public void removeEnemy(Enemy enemy) { enemies.remove(enemy); }
    
    public Enemy getEnemyAt(int x, int y) {
        return enemies.stream()
                .filter(e -> e.getX() == x && e.getY() == y)
                .findFirst()
                .orElse(null);
    }
}
```

**Почему это критично:**
- ❌ Player и Enemy существуют, но не связаны с GameState
- ❌ Боевая система невозможна без врагов в GameState
- ❌ afterPlayerTurn() не может обновить Player
- ❌ enemiesTurn() не может итерировать врагов

---

### **ПРОБЛЕМА 2: afterPlayerTurn() пусто**

#### ❌ СЕЙЧАС:
```java
public void afterPlayerTurn(List<String> log) {
    // TODO: доделать
}
```

#### ✅ ДОЛЖНО БЫТЬ:
```java
public void afterPlayerTurn(List<String> log) {
    // Обновить статусы игрока после его хода
    player.tickSleep();  // Уменьшить sleep counter
    
    // Можно добавить позже:
    // - Восстановление HP (если есть эффект)
    // - Урон от яда
    // - Очистка временных баффов
}
```

---

### **ПРОБЛЕМА 3: enemiesTurn() не реализована**

#### ❌ СЕЙЧАС:
```java
public void enemiesTurn(List<String> log) {
    log.add("[Enemies turn]");
}
```

#### ✅ ДОЛЖНО БЫТЬ:
```java
public void enemiesTurn(List<String> log) {
    List<Enemy> deadEnemies = new ArrayList<>();
    
    for (Enemy enemy : enemies) {
        // 1. Применить спец. эффекты врага
        enemy.tick(this);
        
        // 2. Враг видит игрока?
        int distToPlayer = Math.abs(enemy.getX() - player.getX()) + 
                          Math.abs(enemy.getY() - player.getY());
        
        if (distToPlayer <= enemy.getStats().hostilityRadius()) {
            // Враг преследует
            moveEnemyTowards(enemy, log);
        } else {
            // Враг ходит случайно
            moveEnemyRandomly(enemy, log);
        }
        
        // 3. Враг рядом с игроком?
        if (distToPlayer == 1) {
            enemyAttackPlayer(enemy, log);
            if (!player.isAlive()) {
                log.add("YOU DIED!");
                return;
            }
        }
        
        // 4. Враг мёртв?
        if (!enemy.isAlive()) {
            deadEnemies.add(enemy);
        }
    }
    
    // Удалить мертвых врагов
    deadEnemies.forEach(enemies::remove);
}

private void moveEnemyTowards(Enemy enemy, List<String> log) {
    int ex = enemy.getX();
    int ey = enemy.getY();
    int px = player.getX();
    int py = player.getY();
    
    int newX = ex;
    int newY = ey;
    
    // Greedy pathfinding (простое, но работает)
    if (Math.abs(px - ex) > Math.abs(py - ey)) {
        newX = px > ex ? ex + 1 : ex - 1;
    } else {
        newY = py > ey ? ey + 1 : ey - 1;
    }
    
    // Проверка границ
    if (newX >= 0 && newX < width && newY >= 0 && newY < height) {
        // Проверка столкновения с другим врагом
        if (getEnemyAt(newX, newY) == null) {
            enemy.moveTo(newX, newY);
            log.add(enemy.getType() + " moves towards you");
        }
    }
}

private void moveEnemyRandomly(Enemy enemy, List<String> log) {
    int[] dirs = { -1, 0, 1 };
    int newX = enemy.getX() + dirs[(int)(Math.random() * 3)];
    int newY = enemy.getY() + dirs[(int)(Math.random() * 3)];
    
    if (newX >= 0 && newX < width && newY >= 0 && newY < height) {
        if (getEnemyAt(newX, newY) == null) {
            enemy.moveTo(newX, newY);
        }
    }
}

private void enemyAttackPlayer(Enemy enemy, List<String> log) {
    int baseDamage = (int) enemy.getStats().strength();
    int variance = (int)(baseDamage * 0.2);
    int actualDamage = baseDamage - variance + (int)(Math.random() * variance * 2);
    
    double hitChance = (enemy.getStats().dexterity() - player.getDex()) * 2;
    hitChance = Math.max(15, Math.min(85, hitChance));  // 15%-85%
    
    if (Math.random() * 100 < hitChance) {
        player.takeDamage(actualDamage);
        log.add(enemy.getType() + " hits you for " + actualDamage + "!");
        
        // Вызвать specia эффект (вампир пьет кровь, и т.д.)
        enemy.getSpecial().onAfterEnemyHit(enemy, player, actualDamage);
    } else {
        log.add(enemy.getType() + " misses!");
    }
}
```

---

### **ПРОБЛЕМА 4: tryMovePlayer() не проверяет врагов**

#### ❌ СЕЙЧАС:
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

#### ✅ ДОЛЖНО БЫТЬ:
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

    // 1. Проверка границ
    if (nx < 0 || ny < 0 || nx >= width || ny >= height) {
        log.add("Bump into wall!");
        return false;
    }

    // 2. Проверка врага на новой позиции
    Enemy enemyInTheWay = getEnemyAt(nx, ny);
    if (enemyInTheWay != null) {
        // Попытка атаки
        log.add("Attacking " + enemyInTheWay.getType() + "!");
        playerAttackEnemy(enemyInTheWay, log);
        return true;  // ← Ход потратился на атаку!
    }

    // 3. Движение
    player.moveTo(nx, ny);  // ← Использовать player объект!
    playerX = nx;           // ← Синхронизировать
    playerY = ny;           // ← Синхронизировать
    log.add("Moved to (" + playerX + "," + playerY + ")");
    return true;
}

private void playerAttackEnemy(Enemy enemy, List<String> log) {
    int baseDamage = player.getStrength();
    int variance = (int)(baseDamage * 0.2);
    int actualDamage = baseDamage - variance + (int)(Math.random() * variance * 2);
    
    double hitChance = (player.getDex() - enemy.getStats().dexterity()) * 2;
    hitChance = Math.max(10, Math.min(90, hitChance));  // 10%-90%
    
    if (Math.random() * 100 < hitChance) {
        // КЛЮЧЕВОЙ МОМЕНТ: применить special эффект ПЕРЕД урона!
        int actualDmg = enemy.getSpecial().onBeforeTakeDamage(enemy, actualDamage);
        enemy.takeDamage(actualDmg);
        
        log.add("Hit " + enemy.getType() + " for " + actualDmg + "!");
        
        if (!enemy.isAlive()) {
            enemies.remove(enemy);
            log.add(enemy.getType() + " defeated!");
        }
    } else {
        log.add("Miss!");
    }
}
```

**Почему это важно:**
```
СЦЕНАРИЙ: Игрок идёт на врага

СЕЙЧАС:
  1. Игрок ходит на врага
  2. state.playerX = enemyX, state.playerY = enemyY
  3. Два объекта на одной клетке! 💥

ДОЛЖНО:
  1. Игрок пытается ходить на врага
  2. getEnemyAt() возвращает врага
  3. playerAttackEnemy() вызывается
  4. Враг получает урон
  5. Только потом враги ходят (в enemiesTurn)
```

---

## 📊 ТАБЛИЦА: ЧТО ЕСТЬ VS ЧТО НУЖНО

| Компонент | Статус | Проблема | Время фиксации |
|-----------|--------|---------|-----------------|
| **Command system** | ✅ Perfect | - | 0 мин |
| **GameSnapshot** | ✅ Perfect | - | 0 мин |
| **Enemy class** | ✅ Perfect | Не в GameState | 5 мин |
| **Player class** | ✅ Perfect | Не в GameState | 5 мин |
| **EnemyStats** | ✅ Perfect | - | 0 мин |
| **EnemySpecial** | ✅ Perfect | - | 0 мин |
| **FirstHitIgnored** | ✅ Working | - | 0 мин |
| **GameState.player** | ❌ Missing | CRITICAL | 2 мин |
| **GameState.enemies** | ❌ Missing | CRITICAL | 2 мин |
| **afterPlayerTurn()** | ❌ Empty | Просто заполнить | 2 мин |
| **enemiesTurn()** | ❌ Empty | Сложная логика | 20 мин |
| **tryMovePlayer()** | ⚠️ Incomplete | Нет проверки врагов | 10 мин |
| **playerAttackEnemy()** | ❌ Missing | CRITICAL | 10 мин |
| **enemyAttackPlayer()** | ❌ Missing | CRITICAL | 10 мин |
| **Main test** | ⚠️ Basic | Не проверяет боевую систему | 10 мин |

---

## 🎯 ПЛАН ФИКСАЦИИ (81 минута)

### Фаза 1: GameState структура (10 минут)
```
[ ] Добавить private final Player player;
[ ] Добавить private final List<Enemy> enemies = new ArrayList<>();
[ ] Реализовать getPlayer()
[ ] Реализовать getEnemies()
[ ] Реализовать addEnemy()
[ ] Реализовать removeEnemy()
[ ] Реализовать getEnemyAt(x, y)
```

### Фаза 2: afterPlayerTurn (2 минуты)
```
[ ] Заполнить player.tickSleep()
```

### Фаза 3: Боевая система в GameState (30 минут)
```
[ ] Реализовать playerAttackEnemy()
[ ] Реализовать enemyAttackPlayer()
[ ] Реализовать moveEnemyTowards()
[ ] Реализовать moveEnemyRandomly()
[ ] Заполнить enemiesTurn()
```

### Фаза 4: Обновить tryMovePlayer (10 минут)
```
[ ] Добавить проверку getEnemyAt()
[ ] Вызвать playerAttackEnemy() если враг есть
[ ] Использовать player.moveTo() вместо playerX = nx
```

### Фаза 5: Main тест (20 минут)
```
[ ] Добавить создание врагов
[ ] Добавить addEnemy() в GameState
[ ] Запустить и увидеть боевую систему
[ ] Проверить FirstHitIgnored
```

### Фаза 6: Дополнительные Specials (9 минут)
```
[ ] Реализовать VampireDrainSpecial
[ ] Реализовать SleepOnHitSpecial
[ ] Обновить EnemySpecialFactory
```

---

## 🔄 ЦЕПОЧКА БОЕВОЙ СИСТЕМЫ

```
USER НАЖИМАЕТ СТРЕЛКУ ВВЕРХ
    ↓
KeyMapper → Command.Move(UP)
    ↓
DefaultGameEngine.step(Command.Move(UP))
    ├─ gameState.tryMovePlayer(UP, log)
    │   ├─ Вычислить новую позицию (2, 1)
    │   ├─ Проверить границы ✓
    │   ├─ Проверить getEnemyAt(2, 1) ← ВРАГ ТАМ!
    │   ├─ playerAttackEnemy(enemy, log)
    │   │   ├─ Рассчитать damage (10 ± 20%)
    │   │   ├─ Рассчитать hitChance (DEX-based)
    │   │   ├─ Hit? → enemy.takeDamage()
    │   │   │   └─ enemy.special.onBeforeTakeDamage() ← FirstHitIgnored!
    │   │   └─ Log "Hit for 15!"
    │   └─ return true (ход потратился)
    │
    ├─ if (playerActionUsedTurn) {
    │   ├─ state.afterPlayerTurn(log)
    │   │   └─ player.tickSleep()
    │   │
    │   ├─ state.enemiesTurn(log)
    │   │   ├─ for (Enemy e : enemies) {
    │   │   │   ├─ e.tick(this)
    │   │   │   ├─ int dist = chebyshev(e, player)
    │   │   │   ├─ if (dist <= hostility) moveEnemyTowards(e, log)
    │   │   │   ├─ else moveEnemyRandomly(e, log)
    │   │   │   ├─ if (dist == 1) enemyAttackPlayer(e, log)
    │   │   │   │   ├─ int dmg = e.strength() ± 20%
    │   │   │   │   ├─ Hit? → player.takeDamage(dmg)
    │   │   │   │   └─ e.special.onAfterEnemyHit() ← Vampire drinks!
    │   │   │   └─ }
    │   │   └─ Удалить мертвых врагов
    │   │
    │   └─ turn++
    │
    └─ return GameSnapshot(2, 2, 1, ["Hit vampire!", "Vampire moves...", "Vampire hits you!"])
        ↓
    ConsoleView отображает результат
        ↓
    Ждём следующей команды
```

---

## 💡 КРИТИЧЕСКИЕ МОМЕНТЫ

### 1. **FirstHitIgnored работает только если вызвать onBeforeTakeDamage**

```java
// ✅ ПРАВИЛЬНО
int dmg = enemy.getSpecial().onBeforeTakeDamage(enemy, damage);
enemy.takeDamage(dmg);

// ❌ НЕПРАВИЛЬНО (обойдёшь спец. эффект!)
enemy.takeDamage(damage);  // ← FirstHitIgnored не сработает!
```

### 2. **Player и Enemy должны быть в GameState для интеграции**

```
❌ Сейчас:
  DefaultGameEngine → GameState (playerX, playerY)
  DefaultGameEngine → Player (отдельно)
  DefaultGameEngine → List<Enemy> (отдельно)
  
✅ Должно:
  DefaultGameEngine → GameState
                      ├─ Player
                      └─ List<Enemy>
```

### 3. **afterPlayerTurn() → enemiesTurn() порядок важен**

```
if (playerActionUsedTurn) {
    state.afterPlayerTurn(log);  // ← ПЕРВЫЙ (игрок обновляется)
    state.enemiesTurn(log);      // ← ВТОРОЙ (враги ходят)
    turn++;
}

❌ НЕЛЬЗЯ наоборот, иначе враги реагируют на старое состояние игрока!
```

### 4. **Расстояние Чебышева для проверки соседства**

```java
// ПРАВИЛЬНО (8-направленная сетка)
int dist = Math.max(Math.abs(x1 - x2), Math.abs(y1 - y2));
if (dist == 1) { /* враг рядом */ }

// НЕПРАВИЛЬНО (только 4-направления)
int dist = Math.abs(x1 - x2) + Math.abs(y1 - y2);  // Manhattan
if (dist == 1) { /* пропустит диагональ */ }
```

---

## 🎮 ПРИМЕР РАБОТАЮЩЕГО ТЕСТА

После реализации вы должны увидеть:

```
=== Crawl Combat Test ===

=== Initial State ===
Player: (5,5) HP=100
Vampire: (5,4) HP=25 (FirstHitIgnored)
Zombie: (6,5) HP=30

--- TURN 1: Attack! ---
✓ Player (5,5) | Turn 1
  > === Turn 0 ===
  > Attacking VAMPIRE!
  > Hit VAMPIRE for 15!  ← но FirstHitIgnored блокирует!
  > VAMPIRE moves towards you  ← враг преследует
  > VAMPIRE hits you for 8!
  > ZOMBIE waits...

After turn 1:
  Player: HP 92/100
  Vampire: HP 25/25  ← НЕ ПОЛУЧИЛ УРОН!
  
--- TURN 2: Second Hit! ---
✓ Player (5,4) | Turn 2
  > === Turn 1 ===
  > Attacking VAMPIRE!
  > Hit VAMPIRE for 14!  ← ТЕПЕРЬ УРОН ПРОХОДИТ!
  > VAMPIRE moves towards you
  > VAMPIRE hits you for 9!

After turn 2:
  Player: HP 83/100
  Vampire: HP 11/25  ← УРОН ПРИМЕНИЛСЯ!
```

---

## 📝 ИТОГОВЫЙ CHECKLIST

- [ ] Прочитал полный разбор
- [ ] Понимаю, почему FirstHitIgnored должен быть в Game State
- [ ] Понимаю цепочку боевой системы
- [ ] Знаю, какие файлы нужно изменить
- [ ] Готов к реализации (81 минута)

**АРХИТЕКТУРА ОТЛИЧНАЯ. ПРОСТО НУЖНО ДОДЕЛАТЬ ДЕТАЛИ! 🚀**