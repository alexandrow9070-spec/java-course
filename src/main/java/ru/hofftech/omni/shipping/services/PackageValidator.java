package ru.hofftech.omni.shipping.services;

import ru.hofftech.omni.shipping.entities.Package;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Класс для валидации посылок
 */
public class PackageValidator {
    private static final Logger logger = Logger.getLogger(PackageValidator.class.getName());

    /**
     * Валидирует список посылок
     */
    public static ValidationResult validate(List<Package> packages, int truckWidth, int truckHeight) {
        logger.info("Начало валидации посылок. Всего посылок: " + packages.size());
        
        ValidationResult result = new ValidationResult();
        
        for (int i = 0; i < packages.size(); i++) {
            Package pkg = packages.get(i);
            String error = validatePackage(pkg, truckWidth, truckHeight, i);
            if (error != null) {
                result.addError("Посылка #" + (i + 1) + ": " + error);
            }
        }
        
        if (result.isValid()) {
            logger.info("Валидация пройдена успешно");
        } else {
            logger.warning("Валидация не пройдена. Ошибок: " + result.getErrors().size());
        }
        
        return result;
    }

    /**
     * Валидирует одну посылку
     */
    private static String validatePackage(Package pkg, int truckWidth, int truckHeight, int index) {
        // Проверка размеров
        if (pkg.getWidth() <= 0 || pkg.getHeight() <= 0) {
            return "Неверные размеры посылки";
        }
        
        if (pkg.getWidth() > truckWidth || pkg.getHeight() > truckHeight) {
            return String.format("Посылка слишком большая (%dx%d), максимальный размер кузова: %dx%d", 
                pkg.getWidth(), pkg.getHeight(), truckWidth, truckHeight);
        }
        
        // Проверка формы
        List<String> shape = pkg.getShape();
        if (shape.size() != pkg.getHeight()) {
            return "Несоответствие высоты посылки и количества строк в форме";
        }
        
        for (int i = 0; i < shape.size(); i++) {
            String line = shape.get(i);
            if (line.length() != pkg.getWidth()) {
                return String.format("Несоответствие ширины в строке %d", i + 1);
            }
            
            // Проверка, что строка содержит хотя бы один непустой символ
            boolean hasContent = false;
            for (char ch : line.toCharArray()) {
                if (ch != ' ') {
                    hasContent = true;
                    break;
                }
            }
            if (!hasContent) {
                return String.format("Пустая строка %d в посылке", i + 1);
            }
        }
        
        return null; // Валидация пройдена
    }

    /**
     * Результат валидации
     */
    public static class ValidationResult {
        private final List<String> errors = new ArrayList<>();

        public void addError(String error) {
            errors.add(error);
        }

        public boolean isValid() {
            return errors.isEmpty();
        }

        public List<String> getErrors() {
            return new ArrayList<>(errors);
        }

        public String getErrorMessage() {
            return String.join("\n", errors);
        }
    }
}
