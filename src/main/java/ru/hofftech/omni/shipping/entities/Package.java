package ru.hofftech.omni.shipping.entities;

import java.util.ArrayList;
import java.util.List;

/**
 * Класс для представления посылки
 */
public class Package {
    private final int width;
    private final int height;
    private final List<String> shape;
    private int x; // Позиция в кузове
    private int y; // Позиция в кузове
    private int truckId; // ID кузова, в который помещена посылка

    public Package(int width, int height, List<String> shape) {
        this.width = width;
        this.height = height;
        this.shape = new ArrayList<>(shape);
        this.x = -1;
        this.y = -1;
        this.truckId = -1;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public List<String> getShape() {
        return new ArrayList<>(shape);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getTruckId() {
        return truckId;
    }

    public void setTruckId(int truckId) {
        this.truckId = truckId;
    }

    /**
     * Проверяет, может ли посылка быть размещена в указанной позиции кузова
     * с учетом правил опоры (больше половины основания должно иметь опору)
     */
    public boolean canBePlacedAt(Truck truck, int x, int y) {
        // Проверка границ
        if (x < 0 || y < 0 || x + width > truck.getWidth() || y + height > truck.getHeight()) {
            return false;
        }

        // Проверка, что место свободно
        for (int py = 0; py < height; py++) {
            for (int px = 0; px < width; px++) {
                if (shape.get(py).charAt(px) != ' ') {
                    if (truck.isOccupied(x + px, y + py)) {
                        return false;
                    }
                }
            }
        }

        // Проверка опоры: больше половины основания должно иметь опору
        // Проверяем только если нижний ряд посылки не на дне кузова
        int bottomRowY = y + height - 1;
        if (bottomRowY < truck.getHeight() - 1) {
            int supportCount = 0;
            int baseCells = 0;
            
            // Проверяем опору для нижнего ряда посылки
            // Опору нужно проверять в строке ниже (bottomRowY + 1)
            for (int px = 0; px < width; px++) {
                if (shape.get(height - 1).charAt(px) != ' ') {
                    baseCells++;
                    // Проверяем, есть ли опора снизу (в строке ниже нижнего ряда посылки)
                    // В системе координат: y=0 - верх, y=height-1 - низ
                    // Если bottomRowY < height-1, то bottomRowY+1 существует и это строка ниже
                    if (bottomRowY + 1 < truck.getHeight() && truck.isOccupied(x + px, bottomRowY + 1)) {
                        supportCount++;
                    }
                }
            }
            
            // Опоры должно быть больше половины
            if (baseCells > 0 && supportCount * 2 <= baseCells) {
                return false; // Опоры недостаточно
            }
        }

        return true;
    }

    /**
     * Размещает посылку в кузове
     */
    public void placeIn(Truck truck, int x, int y) {
        this.x = x;
        this.y = y;
        this.truckId = truck.getId();
        
        for (int py = 0; py < height; py++) {
            for (int px = 0; px < width; px++) {
                char ch = shape.get(py).charAt(px);
                if (ch != ' ') {
                    truck.setCell(x + px, y + py, ch);
                }
            }
        }
    }

    @Override
    public String toString() {
        return String.format("Package(%dx%d)", width, height);
    }
}
