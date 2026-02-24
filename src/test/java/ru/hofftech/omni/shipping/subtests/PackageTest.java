package ru.hofftech.omni.shipping.subtests;

import ru.hofftech.omni.shipping.entities.Package;
import ru.hofftech.omni.shipping.entities.Truck;

import java.util.ArrayList;
import java.util.List;

/**
 * Тесты для класса Package
 */
public class PackageTest {
    
    public static void testPackageCreation() {
        System.out.println("Тест: создание посылки");
        List<String> shape = new ArrayList<>();
        shape.add("123");
        shape.add("456");
        
        Package pkg = new Package(3, 2, shape);
        assert pkg.getWidth() == 3 : "Ширина должна быть 3";
        assert pkg.getHeight() == 2 : "Высота должна быть 2";
        System.out.println("✓ Тест пройден");
    }
    
    public static void testPackagePlacement() {
        System.out.println("Тест: размещение посылки в кузове");
        Truck truck = new Truck(6, 6);
        List<String> shape = new ArrayList<>();
        shape.add("11");
        
        Package pkg = new Package(2, 1, shape);
        boolean canPlace = pkg.canBePlacedAt(truck, 0, 5);
        assert canPlace : "Посылка должна помещаться в кузов";
        
        pkg.placeIn(truck, 0, 5);
        assert truck.getCell(0, 5) == '1' : "Ячейка должна содержать '1'";
        assert truck.getCell(1, 5) == '1' : "Ячейка должна содержать '1'";
        System.out.println("✓ Тест пройден");
    }
    
    public static void testSupportRule() {
        System.out.println("Тест: правило опоры");
        Truck truck = new Truck(6, 6);
        
        // Размещаем первую посылку
        List<String> shape1 = new ArrayList<>();
        shape1.add("11");
        Package pkg1 = new Package(2, 1, shape1);
        pkg1.placeIn(truck, 0, 5);
        
        // Пытаемся разместить вторую посылку сверху
        List<String> shape2 = new ArrayList<>();
        shape2.add("22");
        Package pkg2 = new Package(2, 1, shape2);
        
        // Должна помещаться, так как опора больше половины
        boolean canPlace = pkg2.canBePlacedAt(truck, 0, 4);
        assert canPlace : "Посылка должна помещаться с достаточной опорой";
        
        // Тест с недостаточной опорой
        boolean canPlacePartial = pkg2.canBePlacedAt(truck, 1, 4);
        assert !canPlacePartial : "Посылка не должна помещаться с недостаточной опорой";
        
        System.out.println("✓ Тест пройден");
    }
    
    public static void runAllTests() {
        System.out.println("=== Запуск тестов Package ===\n");
        testPackageCreation();
        testPackagePlacement();
        testSupportRule();
        System.out.println("\n=== Все тесты Package пройдены ===\n");
    }
}
