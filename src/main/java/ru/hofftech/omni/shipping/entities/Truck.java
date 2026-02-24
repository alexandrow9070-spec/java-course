package ru.hofftech.omni.shipping.entities;

import java.util.ArrayList;
import java.util.List;

/**
 * Класс для представления кузова грузовика
 */
public class Truck {
    private static int nextId = 1;
    
    private final int id;
    private final int width;
    private final int height;
    private final char[][] cargo;
    private final List<Package> packages;

    public Truck(int width, int height) {
        this.id = nextId++;
        this.width = width;
        this.height = height;
        this.cargo = new char[height][width];
        this.packages = new ArrayList<>();
        
        // Инициализация пустым пространством
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                cargo[y][x] = ' ';
            }
        }
    }

    public int getId() {
        return id;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean isOccupied(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return true; // За границами считается занятым
        }
        return cargo[y][x] != ' ';
    }

    public void setCell(int x, int y, char ch) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            cargo[y][x] = ch;
        }
    }

    public char getCell(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return ' ';
        }
        return cargo[y][x];
    }

    public void addPackage(Package pkg) {
        packages.add(pkg);
    }

    public List<Package> getPackages() {
        return new ArrayList<>(packages);
    }

    /**
     * Проверяет, есть ли свободное место для размещения посылки
     */
    public boolean hasSpaceFor(Package pkg) {
        for (int y = 0; y <= height - pkg.getHeight(); y++) {
            for (int x = 0; x <= width - pkg.getWidth(); x++) {
                if (pkg.canBePlacedAt(this, x, y)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Пытается найти место для посылки и разместить её
     */
    public boolean tryPlacePackage(Package pkg) {
        // Сначала пробуем разместить на дне (y = height - pkg.getHeight())
        for (int y = height - pkg.getHeight(); y >= 0; y--) {
            for (int x = 0; x <= width - pkg.getWidth(); x++) {
                if (pkg.canBePlacedAt(this, x, y)) {
                    pkg.placeIn(this, x, y);
                    addPackage(pkg);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Возвращает строковое представление кузова
     */
    public String render() {
        StringBuilder sb = new StringBuilder();
        
        // Верхняя граница
        sb.append("+".repeat(width + 2)).append("\n");
        
        // Содержимое
        for (int y = 0; y < height; y++) {
            sb.append("+");
            for (int x = 0; x < width; x++) {
                sb.append(cargo[y][x]);
            }
            sb.append("+\n");
        }
        
        // Нижняя граница
        sb.append("+".repeat(width + 2));
        
        return sb.toString();
    }

    public static void resetIdCounter() {
        nextId = 1;
    }
}
