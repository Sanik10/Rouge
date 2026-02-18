package ru.crawl.presentation.input;

import ru.crawl.domain.model.Direction;
import ru.crawl.domain.usecase.Command;

import java.util.Optional;

public final class KeyMapper {

    private boolean is3dMode = false;

    public Optional<Command> mapImmediate(UserInput in) {
        if (in == null) return Optional.empty();

        if (in.type() == UserInput.Type.ESC) return Optional.of(new Command.Quit());
        if (in.type() == UserInput.Type.ENTER) return Optional.of(new Command.Confirm());

        if (in.type() == UserInput.Type.CHAR && in.ch() != null) {
            char ch = Character.toLowerCase(in.ch());

            if (ch == 'q' || ch == 'й') return Optional.of(new Command.Quit());
            if (ch == 'i' || ch == 'ш') return Optional.of(new Command.OpenInventory());
//            if (ch == 'e' || ch == 'у') return Optional.of(new Command.EquipSelected());
            if (ch == 'u' || ch == 'г') return Optional.of(new Command.UseSelected());
            if (ch == 'x' || ch == 'ч') return Optional.of(new Command.Cancel());
            if (ch == '.' || ch == 'ю') return Optional.of(new Command.UseStairs());
            if (ch == 'p' || ch == 'з') return Optional.of(new Command.Save());
            if (ch == 'f' || ch == 'а') return Optional.of(new Command.Toggle3dMode());
            if (ch == 'h' || ch == 'р') return Optional.of(new Command.OpenWeaponMenu());
            if (ch == 'j' || ch == 'о') return Optional.of(new Command.OpenFoodMenu());
            if (ch == 'k' || ch == 'л') return Optional.of(new Command.OpenElixirMenu());
            if (ch == 'e' || ch == 'у') return Optional.of(new Command.OpenScrollMenu());


            if (is3dMode) {
                if (ch == 'a' || ch == 'ф') return Optional.of(new Command.RotateLeft());
                if (ch == 'd' || ch == 'в') return Optional.of(new Command.RotateRight());
            }

            if (ch >= '0' && ch <= '9') return Optional.of(new Command.SelectSlot(ch - '0'));
        }

        return Optional.empty();
    }

    public Optional<Direction> mapDirection(UserInput in) {
        if (in == null || in.type() != UserInput.Type.CHAR || in.ch() == null) return Optional.empty();
        char ch = Character.toLowerCase(in.ch());

        if (is3dMode) {
            return switch (ch) {
                case 'w', 'ц' -> Optional.of(Direction.UP);   // вперёд
                case 's', 'ы' -> Optional.of(Direction.DOWN); // назад
                default -> Optional.empty();
            };
        } else {
            return switch (ch) {
                case 'w', 'ц' -> Optional.of(Direction.UP);
                case 'a', 'ф' -> Optional.of(Direction.LEFT);
                case 's', 'ы' -> Optional.of(Direction.DOWN);
                case 'd', 'в' -> Optional.of(Direction.RIGHT);
                default -> Optional.empty();
            };
        }
    }

    public void set3dMode(boolean is3dMode) {
        this.is3dMode = is3dMode;
    }
}
