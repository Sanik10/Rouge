package ru.crawl.domain.usecase;

import ru.crawl.domain.model.Direction;

public sealed interface Command permits
        Command.Move,
        Command.UseStairs,
        Command.Wait,
        Command.OpenInventory,
        Command.CloseMenu,
        Command.SelectSlot,
        Command.UseSelected,
        Command.EquipSelected,
        Command.Confirm,
        Command.Cancel,
        Command.Quit,
        Command.Save,
        Command.Toggle3dMode,
        Command.RotateLeft,
        Command.RotateRight,
        Command.MoveForward3d,
        Command.MoveBackward3d,
        Command.OpenWeaponMenu,
        Command.OpenFoodMenu,
        Command.OpenElixirMenu,
        Command.OpenScrollMenu,
        Command.UnequipWeapon

{
    record Move(Direction direction) implements Command {}
    record UseStairs() implements Command {}
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

    record Quit() implements Command {}

    record Save() implements Command {}
    record Toggle3dMode() implements Command {}
    record RotateLeft() implements Command {}
    record RotateRight() implements Command {}

    record MoveForward3d() implements Command {}
    record MoveBackward3d() implements Command {}
    record OpenWeaponMenu() implements Command {}
    record OpenFoodMenu() implements Command {}
    record OpenElixirMenu() implements Command {}
    record OpenScrollMenu() implements Command {}

    record UnequipWeapon() implements Command {}
}
