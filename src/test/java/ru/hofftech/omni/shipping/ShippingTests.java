package test.java.ru.hofftech.omni.shipping;

import test.java.ru.hofftech.omni.shipping.subtests.PackageLoaderTest;
import test.java.ru.hofftech.omni.shipping.subtests.PackageTest;
import test.java.ru.hofftech.omni.shipping.subtests.PackingAlgorithmTest;
import test.java.ru.hofftech.omni.shipping.subtests.TruckTest;

/**
 * Запуск всех тестов
 */
public class ShippingTests {
    
    public static void main(String[] args) {
        System.out.println("==========================================");
        System.out.println("Запуск всех тестов");
        System.out.println("==========================================\n");
        
        try {
            PackageTest.runAllTests();
            TruckTest.runAllTests();
            PackageLoaderTest.runAllTests();
            PackingAlgorithmTest.runAllTests();
            
            System.out.println("==========================================");
            System.out.println("Все тесты успешно пройдены!");
            System.out.println("==========================================");
        } catch (Exception e) {
            System.err.println("Ошибка при выполнении тестов: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
