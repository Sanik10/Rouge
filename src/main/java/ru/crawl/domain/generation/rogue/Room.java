package ru.crawl.domain.generation.rogue;

import java.util.Random;

// Класс для генерации комнат
public class Room {
    protected final int[] zone;
    // Переменные для генерации случайного числа из определенного диапазона
    static final int minWidthHeight = 2;
    static final int width = 20;
    static final int height = 10;
    static final int maxWidth = 35;
    static final int maxHeight = 15;
    protected int roomX, roomY, roomWidth, roomHeight, roomNumber;
    protected int leftExit, rightExit, topExit, bottomExit;

    /*
    Конструктор создает квадрат в определенной зоне игрового поля.
    Входящие параметры: x, y. Это координаты начала зоны в которой будет создана комната.
    Надо создать 9 комнат и соответственно наше игровое поле разделено на зоны.
    Начальная зона 0 и 0. Следующие зоны +40 по оси X и +20 по оси Y.
    Конструктор создает массив zone из 4ех элементов:
    zone[0] и zone[1] - начальные координаты комнаты,
    zone[2] - ширина комнаты,
    zone[3] - высота комнаты.
     */
    Room(int x, int y, int index) {
        Random random = new Random();
        this.roomNumber = index;
        // Генерируем ширину комнаты
        this.roomWidth = random.nextInt((width - minWidthHeight) + 1) + minWidthHeight;
        // Генерируем высоту комнаты
        this.roomHeight = random.nextInt((height - minWidthHeight) + 1) + minWidthHeight;
        // Стартовая координата комнаты X
        this.roomX = random.nextInt((maxWidth - roomWidth) + 1) + 1;
        this.roomX += x;
        // Стартовая координата комнаты Y
        this.roomY = random.nextInt((maxHeight - roomHeight) + 1) + 1;
        this.roomY += y;
        // Складываем все параметры в массив
        zone = new int[] {this.roomX, this.roomY, this.roomWidth, this.roomHeight};
        this.leftExit = random.nextInt(this.roomHeight - 1) + this.roomY;
        this.rightExit = random.nextInt(this.roomHeight - 1) + this.roomY;
        this.topExit = random.nextInt(this.roomWidth - 1) + this.roomX;
        this.bottomExit = random.nextInt(this.roomWidth - 1) + this.roomX;
    }

    public int getRoomX() {
        return roomX;
    }

    public int getRoomY() {
        return roomY;
    }

    public int getRoomWidth() {
        return roomWidth;
    }

    public int getRoomHeight() {
        return roomHeight;
    }

    public int getRoomNumber() {
        return roomNumber;
    }

    public int getRoomLeftExit() { return leftExit; }

    public int getRoomRightExit() { return rightExit; }

    public int getRoomBottomExit() { return bottomExit; }

    public int getRoomTopExit() { return topExit; }

    /*
        Данная функция для изменения начальных координат зоны, в которой будет создана комната.
        Входящий параметр: index. Просто счетчик от 0 до 8.
        Каждые 3 шага увеличиваем начальные X и Y и следом обнуляем.
        Используются зоны:
        [0, 0], [0, 40], [0, 80]
        [20, 0], [20, 40], [20, 80]
        [40, 0], [40, 40], [40, 80]
         */
    public static Room startCoordinate(int index) {
        int x = (index % 3) * 40;
        int y = (index / 3) * 20;
        return new Room(x, y, index);
    }
}
