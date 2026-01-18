package ru.crawl.domain.usecase;

import ru.crawl.domain.model.Direction;

public sealed interface Command permits
        Command.Move,
        Command.Wait,
        Command.OpenInventory,
        Command.CloseMenu,
        Command.SelectSlot,
        Command.UseSelected,
        Command.EquipSelected,
        Command.Confirm,
        Command.Cancel
{
    record Move(Direction direction) implements Command {}
    record Wait() implements Command {}

    // пока только инвентарь.
    record OpenInventory() implements Command {}
    record CloseMenu() implements Command {}

    // выбор слота 0..N-1 (UI может принимать цифры и буквы)
    record SelectSlot(int index) implements Command {}

    // действия над выбранным слотом
    record UseSelected() implements Command {}
    record EquipSelected() implements Command {}

    // вдруг будут окна по типу диалогов или ошибок
    record Confirm() implements Command {}
    record Cancel() implements Command {}
}
