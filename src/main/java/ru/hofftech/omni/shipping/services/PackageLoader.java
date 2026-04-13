package ru.hofftech.omni.shipping.services;

import ru.hofftech.omni.shipping.entities.Package;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Класс для загрузки посылок из файла
 */
 public class PackageLoader {
    private static final Logger logger = LoggerFactory.getLogger(PackageLoader.class);

    /**
     * Загружает посылки из файла
     */
    public static List<Package> loadPackages(String filePath) throws IOException {
        logger.info("Начало загрузки посылок из файла: {}", filePath);
        
        List<Package> packages = new ArrayList<>();
        List<String> currentShape = new ArrayList<>();
        String currentName = null;
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            int lineNumber = 0;
            
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();
                
                if (line.isEmpty()) {
                    // Пустая строка означает конец текущей посылки
                    if (!currentShape.isEmpty()) {
                        Package pkg = createPackage(currentName, currentShape);
                        if (pkg != null) {
                            packages.add(pkg);
                            logger.debug("Загружена посылка: {}", pkg);
                        }
                        currentShape.clear();
                        currentName = null;
                    }
                } else if (line.endsWith(":") && currentShape.isEmpty()) {
                    // Новый формат: первая строка блока - имя посылки с двоеточием.
                    currentName = line.substring(0, line.length() - 1).trim();
                } else {
                    currentShape.add(line);
                }
            }
            
            // Обработка последней посылки, если файл не заканчивается пустой строкой
            if (!currentShape.isEmpty()) {
                ru.hofftech.omni.shipping.entities.Package pkg = createPackage(currentName, currentShape);
                if (pkg != null) {
                    packages.add(pkg);
                    logger.debug("Загружена посылка: {}", pkg);
                }
            }
        }
        
        logger.info("Загружено посылок: {}", packages.size());
        return packages;
    }

    /**
     * Создает посылку из списка строк
     */
    private static Package createPackage(String name, List<String> shape) {
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
        
        return new Package(name, width, height, normalizedShape);
    }
}
