package test.java.ru.hofftech.omni.shipping.subtests;

import main.java.ru.hofftech.omni.shipping.entities.Package;
import main.java.ru.hofftech.omni.shipping.entities.Truck;

import java.util.ArrayList;
import java.util.List;

/**
 * Тесты для класса Truck
 */
public class TruckTest {
    
    public static void testTruckCreation() {
        System.out.println("Тест: создание кузова");
        Truck truck = new Truck(6, 6);
        assert truck.getWidth() == 6 : "Ширина должна быть 6";
        assert truck.getHeight() == 6 : "Высота должна быть 6";
        assert truck.getId() == 1 : "ID должен быть 1";
        System.out.println("✓ Тест пройден");
    }
    
    public static void testTruckPlacement() {
        System.out.println("Тест: размещение посылок в кузове");
        Truck truck = new Truck(6, 6);
        
        List<String> shape = new ArrayList<>();
        shape.add("123");
        Package pkg = new Package(3, 1, shape);
        
        boolean placed = truck.tryPlacePackage(pkg);
        assert placed : "Посылка должна быть размещена";
        assert truck.getPackages().size() == 1 : "В кузове должна быть одна посылка";
        System.out.println("✓ Тест пройден");
    }
    
    public static void testMultiplePackages() {
        System.out.println("Тест: размещение нескольких посылок");
        Truck truck = new Truck(6, 6);
        
        List<String> shape1 = new ArrayList<>();
        shape1.add("11");
        Package pkg1 = new Package(2, 1, shape1);
        
        List<String> shape2 = new ArrayList<>();
        shape2.add("22");
        Package pkg2 = new Package(2, 1, shape2);
        
        truck.tryPlacePackage(pkg1);
        truck.tryPlacePackage(pkg2);
        
        assert truck.getPackages().size() == 2 : "В кузове должно быть две посылки";
        System.out.println("✓ Тест пройден");
    }
    
    public static void runAllTests() {
        System.out.println("=== Запуск тестов Truck ===\n");
        Truck.resetIdCounter();
        testTruckCreation();
        testTruckPlacement();
        testMultiplePackages();
        System.out.println("\n=== Все тесты Truck пройдены ===\n");
    }
}
