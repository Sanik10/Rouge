# 📊 ВИЗУАЛЬНЫЕ ДИАГРАММЫ: Архитектура и взаимодействие

## Диаграмма 1: Архитектура слоёв (ТЕКУЩЕЕ СОСТОЯНИЕ)

```
┌────────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                      │
│  (KeyMapper, ConsoleView, Main - UI и ввод)                │
└────────────────────┬───────────────────────────────────────┘
                     │
                     ↓ Command
┌────────────────────────────────────────────────────────────┐
│               USECASE LAYER (Game Logic)                   │
│  DefaultGameEngine ← здесь вся магия происходит            │
│  GameSnapshot ← результат                                  │
└────────────────────┬───────────────────────────────────────┘
                     │
                     ↓
┌────────────────────────────────────────────────────────────┐
│                DOMAIN LAYER (Model)                        │
│  ┌──────────────────────────────────────────────────────┐  │
│  │ GameState (playerX, playerY, inventory)              │  │
│  │   ❌ НЕ СОДЕРЖИТ: Player, enemies                    │  │
│  │   ← КРИТИЧЕСКАЯ ПРОБЛЕМА!                            │  │
│  └──────────────────────────────────────────────────────┘  │
│                                                            │
│  ┌──────────────────┐   ┌──────────────────┐               │
│  │ Player ✅        │   │ Enemy ✅         │                │
│  │ ├─ hp            │   │ ├─ type          │               │
│  │ ├─ strength      │   │ ├─ hp            │               │
│  │ ├─ dex           │   │ ├─ stats         │               │
│  │ └─ sleepTurns    │   │ ├─ special       │               │
│  │ (отдельно!)      │   │ └─ x, y          │               │
│  │                  │   │ (отдельно!)      │               │
│  └──────────────────┘   └──────────────────┘               │
│                                                            │
│  EnemyStats, EnemySpecial, FirstHitIgnored ✅              │
│  CompositeEnemySpecial, EnemySpecialFactory ✅             │
└────────────────────────────────────────────────────────────┘
```

---

## Диаграмма 2: Архитектура слоёв (ПРАВИЛЬНО)

```
┌────────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                      │
│  (KeyMapper, ConsoleView, Main - UI и ввод)                │
└────────────────────┬───────────────────────────────────────┘
                     │
                     ↓ Command
┌────────────────────────────────────────────────────────────┐
│               USECASE LAYER (Game Logic)                   │
│  DefaultGameEngine ← здесь вся магия происходит            │
│  GameSnapshot ← результат                                  │
└────────────────────┬───────────────────────────────────────┘
                     │
                     ↓
┌────────────────────────────────────────────────────────────┐
│                DOMAIN LAYER (Model)                        │
│  ┌──────────────────────────────────────────────────────┐  │
│  │ GameState ✅ СОДЕРЖИТ:                               │  │
│  │   ├─ Player ✅                                       │  │
│  │   │  ├─ hp, maxHp                                    │  │
│  │   │  ├─ strength, dex                                │  │
│  │   │  └─ sleepTurns                                   │  │
│  │   │                                                  │  │
│  │   ├─ List<Enemy> ✅                                  │  │
│  │   │  ├─ Enemy (type, hp, stats, special)             │  │
│  │   │  ├─ Enemy (type, hp, stats, special)             │  │
│  │   │  └─ Enemy (type, hp, stats, special)             │  │
│  │   │                                                  │  │
│  │   ├─ inventory, equippedSlot                         │  │
│  │   └─ width, height                                   │  │
│  │                                                      │  │
│  │ МЕТОДЫ:                                              │  │
│  │   ├─ tryMovePlayer()                                 │  │
│  │   ├─ playerAttackEnemy() ← НОВЫЙ                     │  │
│  │   ├─ afterPlayerTurn() ← РЕАЛИЗОВАТЬ                 │  │
│  │   ├─ enemiesTurn() ← РЕАЛИЗОВАТЬ                     │  │
│  │   └─ moveEnemyTowards(), moveEnemyRandomly()         │  │
│  └──────────────────────────────────────────────────────┘  │
│                                                            │
│  EnemyStats, EnemySpecial, FirstHitIgnored ✅              │
│  CompositeEnemySpecial, EnemySpecialFactory ✅             │
└────────────────────────────────────────────────────────────┘
```

---

## Диаграмма 3: Цепочка боевой системы (Один раунд)

```
┌─ РАУНД 1 ───────────────────────────────────────────────────┐
│                                                             │
│  ИГРОК (Player) в (5,5)                                     │
│  ▼                                                          │
│  ┌───────────────────────────────────────────────────────┐  │
│  │ USER INPUT: Нажимает СТРЕЛКУ ВВЕРХ                    │  │
│  └───────────────────────────────────────────────────────┘  │
│  ↓                                                          │
│  ┌───────────────────────────────────────────────────────┐  │
│  │ KeyMapper.processInput() → Command.Move(UP)           │  │
│  └───────────────────────────────────────────────────────┘  │
│  ↓                                                          │
│  ┌───────────────────────────────────────────────────────┐  │
│  │ DefaultGameEngine.step(Command.Move(UP))              │  │
│  │                                                       │  │
│  │ ┌───────────────────────────────────────────────────┐ │  │
│  │ │ 1. gameState.tryMovePlayer(UP, log)               │ │  │
│  │ │    ├─ Вычислить новую позицию: (5,4)              │ │  │
│  │ │    ├─ Проверить границы ✓                         │ │  │
│  │ │    ├─ Проверить getEnemyAt(5,4) ← ВАМПИР!         │ │  │
│  │ │    ├─ playerAttackEnemy(vampire, log)             │ │  │
│  │ │    │   ├─ Урон = 15                               │ │  │
│  │ │    │   ├─ Hit? Да (70%)                           │ │  │
│  │ │    │   ├─ actualDmg = special.onBeforeTakeDamage(15)│  │
│  │ │    │   │   → FirstHitIgnoredSpecial.return 0!     │ │  │
│  │ │    │   ├─ vampire.takeDamage(0)                   │ │  │
│  │ │    │   │   hp = Math.max(0, 25 - 0) = 25          │ │  │
│  │ │    │   └─ log.add("First hit ignored!")           │ │  │
│  │ │    └─ return true (ход потратился)                │ │  │
│  │ │                                                   │ │  │
│  │ │ 2. if (playerActionUsedTurn) {                    │ │  │
│  │ │      gameState.afterPlayerTurn(log)               │ │  │
│  │ │        └─ player.tickSleep()                      │ │  │
│  │ │                                                   │ │  │
│  │ │      gameState.enemiesTurn(log)                   │ │  │
│  │ │        ├─ for (Enemy e : enemies) {               │ │  │
│  │ │        │   ├─ e.tick(this)                        │ │  │
│  │ │        │   ├─ dist = 1 (рядом!)                   │ │  │
│  │ │        │   ├─ moveEnemyTowards(e, log)            │ │  │
│  │ │        │   │   └─ enemy.moveTo(5,5)               │ │  │
│  │ │        │   ├─ chebyDist == 1? Да!                 │ │  │
│  │ │        │   └─ enemyAttackPlayer(e, log)           │ │  │
│  │ │        │       ├─ Урон = 8                        │ │  │
│  │ │        │       ├─ Hit? Да (60%)                   │ │  │
│  │ │        │       ├─ player.takeDamage(8)            │ │  │
│  │ │        │       │   hp = Math.max(0, 100 - 8)      │ │  │
│  │ │        │       │   = 92                           │ │  │
│  │ │        │       └─ e.special.onAfterEnemyHit()     │ │  │
│  │ │        │ }                                        │ │  │
│  │ │      turn++  (0 → 1)                              │ │  │
│  │ │    }                                              │ │  │
│  │ │                                                   │ │  │
│  │ │ 3. return GameSnapshot(5, 5, 1, log)              │ │  │
│  │ │    ├─ playerX=5, playerY=5                        │ │  │
│  │ │    ├─ turnNumber=1                                │ │  │
│  │ │    └─ log=[                                       │ │  │
│  │ │        "Turn 1",                                  │ │  │
│  │ │        "Attacking VAMPIRE!",                      │ │  │
│  │ │        "First hit ignored!",                      │ │  │
│  │ │        "VAMPIRE moves towards you",               │ │  │
│  │ │        "VAMPIRE hits you for 8 damage!"           │ │  │
│  │ │    ]                                              │ │  │
│  └─────────────────────────────────────────────────────┘ │  │
│  └───────────────────────────────────────────────────────┘  │
│  ↓                                                          │
│  ┌───────────────────────────────────────────────────────┐  │
│  │ RESULT:                                               │  │
│  │   Player: (5,5) HP 92/100                             │  │
│  │   Vampire: (5,5) HP 25/25 ← БЛОКИРОВАНА!              │  │
│  │   Log: ✓ Видны все события                            │  │
│  └───────────────────────────────────────────────────────┘  │
│  ↓                                                          │
│  Ждём следующего INPUT от пользователя...                   │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

## Диаграмма 4: Состояние FirstHitIgnored

```
FirstHitIgnoredSpecial (Вампир, уровень 1)

┌───────────────────────────────────────┐
│ Создание враага:                      │
│ new Enemy(VAMPIRE, 1, 5, 4)           │
│   ├─ type = VAMPIRE                   │
│   ├─ hp = 25                          │
│   ├─ stats = {...}                    │
│   └─ special = EnemySpecialFactory.   │
│       forType(VAMPIRE, 1)             │
│       └─ return new                   │
│           CompositeEnemySpecial([     │
│               FirstHitIgnoredSpecial()│
│           ])                          │
└───────────────────────────────────────┘

┌───────────────────────────────────────┐
│ РАУНД 1: Первая атака игрока          │
│                                       │
│ damage = 15                           │
│ ↓                                     │
│ actualDmg = vampire.special           │
│   .onBeforeTakeDamage(15)             │
│   ├─ FirstHitIgnoredSpecial:          │
│   │  if (firstHitIgnored) {           │
│   │    firstHitIgnored = false ⚡      │
│   │    return 0 ← БЛОКИРУЕМ!          │
│   │  }                                │
│   └─ return 0                         │
│ ↓                                     │
│ vampire.takeDamage(0)                 │
│   hp = Math.max(0, 25 - 0) = 25       │
│ ↓                                     │
│ РЕЗУЛЬТАТ: HP не изменился!           │
│ FirstHitIgnored.firstHitIgnored ➔     │
│   false                               │
└───────────────────────────────────────┘

┌───────────────────────────────────────┐
│ РАУНД 2: Вторая атака игрока          │
│                                       │
│ damage = 14                           │
│ ↓                                     │
│ actualDmg = vampire.special           │
│   .onBeforeTakeDamage(14)             │
│   ├─ FirstHitIgnoredSpecial:          │
│   │  if (firstHitIgnored) {   false!  │
│   │    // пропускаем                  │
│   │  }                                │
│   │  return incomingDamage (14)       │
│   └─ return 14 ← ПРОПУСКАЕМ!          │
│ ↓                                     │
│ vampire.takeDamage(14)                │
│   hp = Math.max(0, 25 - 14) = 11      │
│ ↓                                     │
│ РЕЗУЛЬТАТ: HP УПАЛО НА 14!            │
│ FirstHitIgnored.firstHitIgnored ➔     │
│   false (остаётся)                    │
└───────────────────────────────────────┘
```

---

## Диаграмма 5: Как враги двигаются

```
MAP (10x10):
┌─────────────────────────────┐
│ .... ← враг видит игрока?   │
│ .V.. ← VAMPIRE (5,4)        │
│ .P.. ← PLAYER (5,5)         │
│ .Z.. ← ZOMBIE (6,5)         │
│ ....                        │
└─────────────────────────────┘

ВАМПИР (хостильность = 8):
  dist = |5-5| + |4-5| = 1
  1 <= 8 ✓ → ВРАГ ВИДИТ ИГРОКА!
  
  moveEnemyTowards(vampire, log)
    ex=5, ey=4, px=5, py=5
    |px-ex| = 0, |py-ey| = 1
    → вертикально ближе
    → newY = (py > ey) ? ey+1 : ey-1
    → newY = (5 > 4) ? 5 : 3
    → newY = 5
    
    enemy.moveTo(5, 5)  ← ВРАГ ИДЁТ НА ИГРОКА!
    
  РЕЗУЛЬТАТ: VAMPIRE теперь в (5,5) - рядом с игроком!

ЗОМБИ (хостильность = 6):
  dist = |5-6| + |5-5| = 1
  1 <= 6 ✓ → ЗОМБИ ВИДИТ ИГРОКА!
  
  Но он не может занять (5,5) - там уже вампир!
  (проверка getEnemyAt(newX, newY) != null)
  
  → Зомби остаётся в (6,5)
```

---

## Диаграмма 6: Проверка расстояний

```
MANHATTAN DISTANCE (для видимости):
  dist = |x1-x2| + |y1-y2|
  
  Player (5,5) vs Vampire (5,4):
    dist = |5-5| + |5-4| = 1
    if (dist <= hostility) moveTowards() ✓

  Player (5,5) vs Zombie (6,5):
    dist = |5-6| + |5-5| = 1
    if (dist <= hostility) moveTowards() ✓

  Player (5,5) vs Ghost (10,10):
    dist = |5-10| + |5-10| = 10
    if (dist <= 3) moveTowards() ✗

┌─────────────────────────────┐
│ . . G . .  ← GHOST (10,10)  │
│ . . . . .                   │
│ . . . . .  dist=10, не видит│
│ . V . . .  ← VAMPIRE (5,4)  │
│ . P Z . .  ← PLAYER (5,5)   │
└─────────────────────────────┘

CHEBYSHEV DISTANCE (для соседства):
  dist = Math.max(|x1-x2|, |y1-y2|)
  
  Player (5,5) vs Vampire (5,5):
    dist = Math.max(0, 0) = 0 ← на одной клетке!
    if (dist == 1) attack() ✗ (не 1, а 0!)
    
  Player (5,5) vs Vampire (5,4):
    dist = Math.max(0, 1) = 1 ← соседи!
    if (dist == 1) attack() ✓
    
  Player (5,5) vs Zombie (6,5):
    dist = Math.max(1, 0) = 1 ← соседи (диагональ)!
    if (dist == 1) attack() ✓
    
  Player (5,5) vs Ghost (5,7):
    dist = Math.max(0, 2) = 2 ← не соседи!
    if (dist == 1) attack() ✗

┌─────────────────────────────┐
│ . . . . .                   │
│ . . G . .  ← dist=2         │
│ . . . . .                   │
│ . V . . .  ← dist=1         │
│ . P Z . .  ← dist=0,1       │
└─────────────────────────────┘

ПРАВИЛО:
  ✅ Manhattan для видимости (враг видит игрока издалека)
  ✅ Chebyshev для соседства (враг может атаковать рядом)
```

---

## Диаграмма 7: Очерёдность методов в step()

```
DefaultGameEngine.step(Command cmd)
    │
    ├─ 1. List<String> log = new ArrayList<>()
    │      log.add("=== Turn " + turn + " ===");
    │
    ├─ 2. boolean playerActionUsedTurn = false;
    │
    ├─ 3. if (cmd instanceof Command.Move m) {
    │        playerActionUsedTurn = state.tryMovePlayer(m.direction(), log);
    │        // → playerAttackEnemy() если враг на пути
    │        // → return true если атака/движение
    │     }
    │
    ├─ 4. else if (cmd instanceof Command.Wait) {
    │        log.add("Player waits");
    │        playerActionUsedTurn = true;
    │     }
    │
    ├─ 5. if (playerActionUsedTurn) {
    │        ├─ 5.1. state.afterPlayerTurn(log);
    │        │        └─ player.tickSleep();
    │        │           (обновить игрока)
    │        │
    │        ├─ 5.2. state.enemiesTurn(log);
    │        │        ├─ for (Enemy e : enemies) {
    │        │        │   ├─ e.tick(this);
    │        │        │   ├─ dist = Math.abs(...)
    │        │        │   ├─ if (dist <= hostility)
    │        │        │   │   moveEnemyTowards();
    │        │        │   ├─ else moveEnemyRandomly();
    │        │        │   ├─ chebyDist = Math.max(...)
    │        │        │   ├─ if (chebyDist == 1)
    │        │        │   │   enemyAttackPlayer();
    │        │        │   └─ }
    │        │        └─ removeDeadEnemies();
    │        │           (враги ходят)
    │        │
    │        └─ 5.3. turn++;
    │               (счётчик раундов)
    │     }
    │
    └─ 6. return new GameSnapshot(
             state.playerX(),
             state.playerY(),
             (int) turn,
             log
         );
         (результат для UI)

⚠️ ПОРЯДОК ВАЖЕН:
   1. tryMovePlayer() ← игрок ходит/атакует
   2. afterPlayerTurn() ← обновить состояние игрока
   3. enemiesTurn() ← враги реагируют на ЧТО-ТО
   4. turn++ ← счёт раунда

   ❌ НЕЛЬЗЯ МЕНЯТЬ ПОРЯДОК afterPlayerTurn и enemiesTurn!
```

---

## Диаграмма 8: Интеграция Game Loop

```
┌─────────────────────────────────────────────────────┐
│ MAIN.JAVA - entry point                             │
└──────────────────────────────────────────────────┬──┘
                                                   │
                                                   ↓
┌─────────────────────────────────────────────────────┐
│ GameState state = new GameState(10,10,5,5)          │
│   ├─ player = new Player(5,5)                       │
│   └─ enemies = []                                   │
│                                                     │
│ state.addEnemy(new Enemy(VAMPIRE, 1, 5, 4))         │
│   └─ enemies = [Vampire(5,4)]                       │
│                                                     │
│ GameEngine engine = new DefaultGameEngine(state)    │
└──────────────────────────────────────────────────┬──┘
                                                   │
                                                   ↓
    ┌───────────────────────────────────────────────┐
    │ GAME LOOP (while (gameRunning)) {             │
    │                                               │
    │   ┌────────────────────────────────────────┐  │
    │   │ 1. User Input (KeyMapper)              │  │
    │   │    → Command cmd                       │  │
    │   └────────────────────────────────────────┘  │
    │                      ↓                        │
    │   ┌────────────────────────────────────────┐  │
    │   │ 2. Game Logic                          │  │
    │   │    GameSnapshot snap =                 │  │
    │   │      engine.step(cmd)                  │  │
    │   │    - tryMovePlayer()                   │  │
    │   │    - afterPlayerTurn()                 │  │
    │   │    - enemiesTurn()                     │  │
    │   └────────────────────────────────────────┘  │
    │                      ↓                        │
    │   ┌────────────────────────────────────────┐  │
    │   │ 3. Display                             │  │
    │   │    display.render(snap)                │  │
    │   │    - print playerX, playerY            │  │
    │   │    - print log events                  │  │
    │   └────────────────────────────────────────┘  │
    │                      ↓                        │
    │   ┌────────────────────────────────────────┐  │
    │   │ 4. Check Game State                    │  │
    │   │    if (!player.isAlive())              │  │
    │   │      gameRunning = false               │  │
    │   └────────────────────────────────────────┘  │
    │                      ↓                        │
    │   [Вернуться в начало цикла ↑]                │
    │                                               │
    └───────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────┐
│ GAME OVER                                           │
│ - Player HP = 0: YOU DIED!                          │
│ - Enemies empty: YOU WON!                           │
└─────────────────────────────────────────────────────┘
```

---

## Диаграмма 9: Классовая иерархия EnemySpecial

```
┌──────────────────────────────┐
│  EnemySpecial (interface)    │
│  ├─ onBeforeTakeDamage()     │
│  ├─ onAfterEnemyHit()        │
│  └─ onTick()                 │
└──────────────────────────────┘
    ▲        ▲        ▲
    │        │        │
    │        │        └──────────────────────────┐
    │        │                                   │
    │        │                    ┌──────────────────────┐
    │        │                    │  Composite           │
    │        │                    │ EnemySpecial         │
    │        │                    │ List<EnemySpecial>   │
    │        │                    │ specials             │
    │        │                    └──────────────────────┘
    │        │
    │        └────────────────────┐
    │                             │
    │         ┌───────────────────┴──────────────┐
    │         │                                  │
    │    ┌────────────────┐   ┌───────────────────┐
    │    │ FirstHit       │   │ VampireDrain      │
    │    │ IgnoredSpecial │   │ Special           │
    │    │                │   │ (частично)        │
    │    │ boolean first  │   │ (нужно добавить)  │
    │    │ HitIgnored     │   │                   │
    │    │ onBefore...()  │   │ onAfter...()      │
    │    └────────────────┘   │ (lifesteal)       │
    │                         └───────────────────┘
    │
    └────────────────┐
                     │
            ┌────────────────────────┐
            │  SleepOnHitSpecial     │
            │  (нужно добавить)      │
            │                        │
            │  int sleepChance       │
            │  onAfter...()          │
            │  (возможен сон 2 хода) │
            └────────────────────────┘

VAMPIRE в боевом раунде использует:
  CompositeEnemySpecial([
    FirstHitIgnoredSpecial(),
    VampireDrainSpecial()
  ])
  
  Когда враг получает урон:
    1. FirstHitIgnoredSpecial.onBeforeTakeDamage()
       → if (first) return 0; else return damage;
    2. Урон применяется
  
  Когда враг атакует:
    1. enemyAttackPlayer()
    2. VampireDrainSpecial.onAfterEnemyHit()
       → enemy.heal(damage * 0.5)
```

---

## Диаграмма 10: Полный поток данных (ONE TURN)

```
USER НАЖИМАЕТ КЛАВИШУ
    │
    ↓
┌──────────────────────────────────┐
│ KeyMapper.processInput(key)       │
│ switch(key) {                    │
│   case UP: return Move(UP)       │
│   case DOWN: return Move(DOWN)   │
│   ...                            │
│ }                                │
└──────────────────────────────────┘
    │ Command cmd
    ↓
┌──────────────────────────────────────────────────────┐
│ DefaultGameEngine.step(Command cmd)                  │
│                                                      │
│ ┌──────────────────────────────────────────────────┐ │
│ │ if (cmd instanceof Command.Move m) {             │ │
│ │   playerActionUsedTurn =                         │ │
│ │     state.tryMovePlayer(m.direction(), log)      │ │
│ │   // → может вызвать playerAttackEnemy()         │ │
│ │ }                                                │ │
│ └──────────────────────────────────────────────────┘ │
│                 │                                    │
│                 ↓ playerActionUsedTurn = true        │
│ ┌──────────────────────────────────────────────────┐ │
│ │ state.afterPlayerTurn(log)                       │ │
│ │   → player.tickSleep()                           │ │
│ │   → [обновить статусы игрока]                    │ │
│ └──────────────────────────────────────────────────┘ │
│                 │                                    │
│                 ↓                                    │
│ ┌──────────────────────────────────────────────────┐ │
│ │ state.enemiesTurn(log)                           │ │
│ │   for (Enemy e : enemies) {                      │ │
│ │     e.tick(this)  ← apply special effects        │ │
│ │     moveEnemyTowards() или moveEnemyRandomly()   │ │
│ │     if (chebyDist==1) enemyAttackPlayer()        │ │
│ │   }                                              │ │
│ └──────────────────────────────────────────────────┘ │
│                 │                                    │
│                 ↓                                    │
│ ┌──────────────────────────────────────────────────┐ │
│ │ turn++                                           │ │
│ │ return new GameSnapshot(playerX, playerY,        │ │
│ │                         turn, log)               │ │
│ └──────────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────┘
    │ GameSnapshot snap
    ↓
┌──────────────────────────────────┐
│ ConsoleView.render(snap)         │
│ print("Turn " + snap.turn)       │
│ print("Player at (" + ...)       │
│ for (log_line : snap.log()) {    │
│   print("  > " + log_line)       │
│ }                                │
└──────────────────────────────────┘
    │
    ↓
┌──────────────────────────────────┐
│ Ждём следующего INPUT            │
│ (вернуться в начало)             │
└──────────────────────────────────┘
```

---

## Диаграмма 11: State Synchronization (GameState)

```
GameState (ИСТОЧНИК ИСТИНЫ):
┌────────────────────────────────────────┐
│ private Player player                  │
│ private List<Enemy> enemies            │
│ private int playerX  ← синхронизация   │
│ private int playerY  ← синхронизация   │
└────────────────────────────────────────┘

ПРИ СОЗДАНИИ:
  state = new GameState(10, 10, 5, 5)
    └─ player = new Player(5, 5)  ✓ синхронизировано
    └─ playerX = 5, playerY = 5

ПРИ ДВИЖЕНИИ:
  state.tryMovePlayer(UP, log)
    ├─ player.moveTo(5, 4)      ← обновить Player
    ├─ playerX = 5              ← обновить GameState
    ├─ playerY = 4              ← обновить GameState
    └─ ✓ в синхронизации

ПРИ АТАКЕ:
  state.playerAttackEnemy(vampire, log)
    ├─ damage = player.getStrength() + random
    │  ← использовать объект Player ✓
    ├─ vampire.takeDamage(actualDmg)
    │  ← обновить объект Enemy ✓
    └─ ✓ обновления через объекты

ИНВАРИАНТ:
  player.getX() == playerX  ← ВСЕГДА!
  player.getY() == playerY  ← ВСЕГДА!
```

---

**Все диаграммы использованы для визуализации 🎯**