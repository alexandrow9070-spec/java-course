package main.java.ru.hofftech.omni.shipping.services;

import main.java.ru.hofftech.omni.shipping.entities.Package;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Класс для загрузки посылок из файла
 */
public class PackageLoader {
    private static final Logger logger = Logger.getLogger(PackageLoader.class.getName());

    /**
     * Загружает посылки из файла
     */
    public static List<main.java.ru.hofftech.omni.shipping.entities.Package> loadPackages(String filePath) throws IOException {
        logger.info("Начало загрузки посылок из файла: " + filePath);
        
        List<main.java.ru.hofftech.omni.shipping.entities.Package> packages = new ArrayList<>();
        List<String> currentShape = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            int lineNumber = 0;
            
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();
                
                if (line.isEmpty()) {
                    // Пустая строка означает конец текущей посылки
                    if (!currentShape.isEmpty()) {
                        main.java.ru.hofftech.omni.shipping.entities.Package pkg = createPackage(currentShape);
                        if (pkg != null) {
                            packages.add(pkg);
                            logger.fine("Загружена посылка: " + pkg);
                        }
                        currentShape.clear();
                    }
                } else {
                    currentShape.add(line);
                }
            }
            
            // Обработка последней посылки, если файл не заканчивается пустой строкой
            if (!currentShape.isEmpty()) {
                main.java.ru.hofftech.omni.shipping.entities.Package pkg = createPackage(currentShape);
                if (pkg != null) {
                    packages.add(pkg);
                    logger.fine("Загружена посылка: " + pkg);
                }
            }
        }
        
        logger.info("Загружено посылок: " + packages.size());
        return packages;
    }

    /**
     * Создает посылку из списка строк
     */
    private static main.java.ru.hofftech.omni.shipping.entities.Package createPackage(List<String> shape) {
        if (shape.isEmpty()) {
            return null;
        }
        
        int height = shape.size();
        int width = 0;
        
        // Находим максимальную ширину
        for (String line : shape) {
            width = Math.max(width, line.length());
        }
        
        // Нормализуем ширину всех строк
        List<String> normalizedShape = new ArrayList<>();
        for (String line : shape) {
            StringBuilder normalized = new StringBuilder(line);
            while (normalized.length() < width) {
                normalized.append(' ');
            }
            normalizedShape.add(normalized.toString());
        }
        
        return new Package(width, height, normalizedShape);
    }
}
