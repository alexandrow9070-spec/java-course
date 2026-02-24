package ru.hofftech.omni.shipping.subtests;

import ru.hofftech.omni.shipping.services.packing.OptimizedPackingAlgorithm;
import ru.hofftech.omni.shipping.entities.Package;
import ru.hofftech.omni.shipping.services.packing.SimplePackingAlgorithm;
import ru.hofftech.omni.shipping.entities.Truck;

import java.util.ArrayList;
import java.util.List;

/**
 * Тесты для алгоритмов упаковки
 */
public class PackingAlgorithmTest {
    
    public static void testSimpleAlgorithm() {
        System.out.println("Тест: простой алгоритм упаковки");
        Truck.resetIdCounter();
        
        List<Package> packages = new ArrayList<>();
        List<String> shape1 = new ArrayList<>();
        shape1.add("11");
        packages.add(new Package(2, 1, shape1));
        
        List<String> shape2 = new ArrayList<>();
        shape2.add("22");
        packages.add(new Package(2, 1, shape2));
        
        SimplePackingAlgorithm algorithm = new SimplePackingAlgorithm();
        List<Truck> trucks = algorithm.pack(packages, 6, 6);
        
        assert trucks.size() == 2 : "Должно быть использовано 2 кузова";
        assert trucks.get(0).getPackages().size() == 1 : "В первом кузове должна быть одна посылка";
        assert trucks.get(1).getPackages().size() == 1 : "Во втором кузове должна быть одна посылка";
        
        System.out.println("✓ Тест пройден");
    }
    
    public static void testOptimizedAlgorithm() {
        System.out.println("Тест: оптимизированный алгоритм упаковки");
        Truck.resetIdCounter();
        
        List<Package> packages = new ArrayList<>();
        List<String> shape1 = new ArrayList<>();
        shape1.add("11");
        packages.add(new Package(2, 1, shape1));
        
        List<String> shape2 = new ArrayList<>();
        shape2.add("22");
        packages.add(new Package(2, 1, shape2));
        
        OptimizedPackingAlgorithm algorithm = new OptimizedPackingAlgorithm();
        List<Truck> trucks = algorithm.pack(packages, 6, 6);
        
        assert trucks.size() == 1 : "Должен быть использован 1 кузов";
        assert trucks.get(0).getPackages().size() == 2 : "В кузове должно быть две посылки";
        
        System.out.println("✓ Тест пройден");
    }
    
    public static void testExampleFromDescription() {
        System.out.println("Тест: пример из описания задачи");
        Truck.resetIdCounter();
        
        List<Package> packages = new ArrayList<>();
        
        // 999
        // 999
        // 999
        List<String> shape1 = new ArrayList<>();
        shape1.add("999");
        shape1.add("999");
        shape1.add("999");
        packages.add(new Package(3, 3, shape1));
        
        // 666
        // 666
        List<String> shape2 = new ArrayList<>();
        shape2.add("666");
        shape2.add("666");
        packages.add(new Package(3, 2, shape2));
        
        // 55555
        List<String> shape3 = new ArrayList<>();
        shape3.add("55555");
        packages.add(new Package(5, 1, shape3));
        
        // 1
        List<String> shape4 = new ArrayList<>();
        shape4.add("1");
        packages.add(new Package(1, 1, shape4));
        
        // 1
        List<String> shape5 = new ArrayList<>();
        shape5.add("1");
        packages.add(new Package(1, 1, shape5));
        
        // 333
        List<String> shape6 = new ArrayList<>();
        shape6.add("333");
        packages.add(new Package(3, 1, shape6));
        
        OptimizedPackingAlgorithm algorithm = new OptimizedPackingAlgorithm();
        List<Truck> trucks = algorithm.pack(packages, 6, 6);
        
        assert trucks.size() > 0 : "Должен быть использован хотя бы один кузов";
        
        System.out.println("✓ Тест пройден");
        System.out.println("  Использовано кузовов: " + trucks.size());
    }
    
    public static void runAllTests() {
        System.out.println("=== Запуск тестов алгоритмов упаковки ===\n");
        testSimpleAlgorithm();
        testOptimizedAlgorithm();
        testExampleFromDescription();
        System.out.println("\n=== Все тесты алгоритмов пройдены ===\n");
    }
}
