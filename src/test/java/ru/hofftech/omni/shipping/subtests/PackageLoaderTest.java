package ru.hofftech.omni.shipping.subtests;

import ru.hofftech.omni.shipping.entities.Package;
import ru.hofftech.omni.shipping.services.PackageLoader;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Тесты для класса PackageLoader
 */
public class PackageLoaderTest {
    
    public static void testLoadPackages() throws IOException {
        System.out.println("Тест: загрузка посылок из файла");
        
        // Создаем тестовый файл
        String testFile = "test-input.txt";
        try (FileWriter writer = new FileWriter(testFile)) {
            writer.write("999\n");
            writer.write("999\n");
            writer.write("999\n");
            writer.write("\n");
            writer.write("666\n");
            writer.write("666\n");
            writer.write("\n");
            writer.write("55555\n");
        }
        
        List<Package> packages = PackageLoader.loadPackages(testFile);
        assert packages.size() == 3 : "Должно быть загружено 3 посылки";
        assert packages.get(0).getHeight() == 3 : "Первая посылка должна иметь высоту 3";
        assert packages.get(1).getHeight() == 2 : "Вторая посылка должна иметь высоту 2";
        assert packages.get(2).getHeight() == 1 : "Третья посылка должна иметь высоту 1";
        
        System.out.println("✓ Тест пройден");
    }
    
    public static void runAllTests() {
        System.out.println("=== Запуск тестов PackageLoader ===\n");
        try {
            testLoadPackages();
            System.out.println("\n=== Все тесты PackageLoader пройдены ===\n");
        } catch (IOException e) {
            System.err.println("✗ Ошибка в тестах: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
