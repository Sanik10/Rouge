# СПИСОК ЗАДАЧ ПРОЕКТА

## DOMAIN LAYER (Game Logic)

### Model Classes 
- [x] Player класс
- [x] Enemy класс  
- [x] EnemySpecial интерфейс
- [x] FirstHitIgnoredSpecial
- [x] CompositeEnemySpecial
- [x] EnemySpecialFactory
- [x] GameState класс
- [x] Direction enum
- [x] Position класс
- [x] Inventory класс
- [x] Item класс
- [ ] Tile класс (для уровней)

### Game Engine
- [x] Command sealed class
- [x] GameEngine интерфейс
- [x] DefaultGameEngine класс
- [x] GameSnapshot класс
- [ ] Game Over conditions (смерть игрока, победа)
- [ ] Quit команда обработка

### Enemy Special Abilities (Enhancement)
- [x] FirstHitIgnored (вампир блокирует первый удар)
- [ ] VampireDrain (вампир восстанавливает HP при атаке)
- [ ] SleepOnHit (враг может усыпить игрока)
- [ ] ThornsReflection (враг наносит урон при получении удара)

### Level Generation
- [ ] LevelGenerator класс
- [ ] Генерация случайной карты 10x10
- [ ] Расставление врагов на карте
- [ ] Расставление предметов
- [ ] Проверка проходимости (connectivity)

---

## PRESENTATION LAYER (UI/Input)

### Input Handling
- [ ] KeyMapper класс (обработка клавиш)
- [ ] Integration с JCurses
- [ ] Стрелки → Move(direction)
- [ ] Enter → Confirm
- [ ] Esc → Cancel
- [ ] q → Quit
- [ ] i → OpenInventory

### Display/View
- [ ] ConsoleView класс
- [ ] GamePresenter класс
- [ ] Screen класс (JCurses wrapper)
- [ ] Отображение карты с игроком и врагами
- [ ] Отображение лога событий
- [ ] Отображение HP bars
- [ ] Отображение инвентаря
- [ ] Отображение меню выбора
- [ ] Красивое отображение боевой информации

### Main App
- [x] Bootstrap класс
- [x] Main класс (entry point)
- [ ] Game loop реализация
- [ ] Обработка команд в цикле
- [ ] Graceful shutdown

---

## DATA LAYER (Persistence)

### Save/Load System
- [ ] SaveRepository интерфейс
- [ ] JsonSaveRepository реализация
- [ ] Сохранение GameState в JSON
- [ ] Загрузка GameState из JSON
- [ ] Валидация загруженных данных
- [ ] Сохранение статистики побед/поражений

---

## ENEMY TYPES (Variety)

### Basic Enemies
- [ ] Vampire (FirstHitIgnored + Drain)
- [ ] Zombie (базовый враг)
- [ ] Ghost (дальнобойный враг)
- [ ] Orc (сильный враг)
- [ ] Goblin (быстрый слабый враг)

### Enemy AI
- [ ] Движение к игроку (если видит)
- [ ] Случайное движение (если не видит)
- [ ] Атака при соседстве
- [ ] Специальные способности на события

---

## GAME LOGIC

### Combat System
- [x] Расчёт урона (базовый + случайность)
- [x] Система Hit/Miss
- [x] Система HP и смерти
- [x] Специальные способности врагов
- [ ] Критические удары (опционально)
- [ ] Защита/броня (опционально)

### Game State
- [x] Синхронизация Player и GameState
- [x] Обновление позиции после хода
- [x] Обновление HP после урона
- [ ] Проверка смерти игрока
- [ ] Проверка победы (все враги мертвы)
- [ ] Счётчик раундов

### Items & Equipment
- [x] Инвентарь структура
- [ ] Item интеграция в боевую систему
- [ ] Экипировка оружия/брони
- [ ] Использование предметов
- [ ] Предметы на полу (pickup)
