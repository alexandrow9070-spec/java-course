
package ru.hofftech.omni.shipping;

import ru.hofftech.omni.shipping.interfaces.PackingAlgorithm;
import ru.hofftech.omni.shipping.entities.Package;
import ru.hofftech.omni.shipping.entities.Truck;
import ru.hofftech.omni.shipping.services.packing.OptimizedPackingAlgorithm;
import ru.hofftech.omni.shipping.services.PackageLoader;
import ru.hofftech.omni.shipping.services.PackageValidator;
import ru.hofftech.omni.shipping.services.packing.SimplePackingAlgorithm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * Главный класс программы для упаковки посылок в кузовы грузовиков
 */
public class ShippingApp {
    private static final Logger logger = LoggerFactory.getLogger(ShippingApp.class);

    public static void main(String[] args) {

        //явно указываем кодировку вывода
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
        System.setErr(new PrintStream(System.err, true, StandardCharsets.UTF_8));

        System.out.println("Предоставленные аргументы: "+Arrays.toString(args));
        if (args.length < 4) {
            System.err.println("Использование: java Main <путь_к_файлу> <ширина_кузова> <высота_кузова> [алгоритм]");
            System.err.println("Алгоритмы: simple или optimized");
            System.exit(1);
        }

        String filePath = args[0];
        int truckWidth = Integer.parseInt(args[1]);
        int truckHeight = Integer.parseInt(args[2]);
        String algorithmType = args[3];

        logger.info("Запуск программы упаковки посылок");
        logger.info("Файл: {}", filePath);
        logger.info("Алгоритм: {}", algorithmType);

        try {
            // Загрузка посылок
            List<Package> packages = PackageLoader.loadPackages(filePath);

            if (packages.isEmpty()) {
                System.err.println("Файл не содержит посылок");
                logger.warn("Файл не содержит посылок");
                System.exit(1);
            }

            // Валидация
            PackageValidator.ValidationResult validation = PackageValidator.validate(
                    packages, truckWidth, truckHeight);

            if (!validation.isValid()) {
                System.err.println("Ошибки валидации:");
                System.err.println(validation.getErrorMessage());
                logger.error("Валидация не пройдена");
                System.exit(1);
            }

            // Выбор алгоритма
            PackingAlgorithm algorithm;
            if ("optimized".equalsIgnoreCase(algorithmType)) {
                algorithm = new OptimizedPackingAlgorithm();
            } else {
                algorithm = new SimplePackingAlgorithm();
            }

            logger.info("Используется алгоритм: {}", algorithm.getName());

            // Упаковка
            Truck.resetIdCounter();
            List<Truck> trucks = algorithm.pack(packages, truckWidth, truckHeight);

            // Вывод результата
            System.out.println("\nРезультат упаковки (" + algorithm.getName() + "):");
            System.out.println("Использовано кузовов: " + trucks.size());
            System.out.println();

            for (Truck truck : trucks) {
                System.out.println("Кузов #" + truck.getId() + ":");
                System.out.println(truck.render());
                System.out.println();
            }

            logger.info("Программа завершена успешно");

        } catch (IOException e) {
            System.err.println("Ошибка при чтении файла: " + e.getMessage());
            logger.error("Ошибка при чтении файла: {}", e.getMessage(), e);
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Неожиданная ошибка: " + e.getMessage());
            logger.error("Неожиданная ошибка: {}", e.getMessage(), e);
            System.exit(1);
        }
    }
}