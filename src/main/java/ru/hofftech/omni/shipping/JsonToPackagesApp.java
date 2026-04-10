package ru.hofftech.omni.shipping;

import ru.hofftech.omni.shipping.entities.Package;
import ru.hofftech.omni.shipping.entities.Truck;
import ru.hofftech.omni.shipping.interfaces.PackingAlgorithm;
import ru.hofftech.omni.shipping.services.PackageTextFormatService;
import ru.hofftech.omni.shipping.services.PackageValidator;
import ru.hofftech.omni.shipping.services.PackingResultJsonService;
import ru.hofftech.omni.shipping.services.packing.DensePackingAlgorithm;
import ru.hofftech.omni.shipping.services.packing.EvenDistributionPackingAlgorithm;
import ru.hofftech.omni.shipping.services.packing.OptimizedPackingAlgorithm;
import ru.hofftech.omni.shipping.services.packing.SimplePackingAlgorithm;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Единое CLI-приложение с командами:
 * - pack: файл с посылками -> погрузка в кузовы (+опциональный JSON результата)
 * - split: JSON результата погрузки -> текстовый файл с посылками
 *
 * Использование:
 *   java JsonToPackagesApp pack <путь_к_файлу> <ширина_кузова> <высота_кузова> <алгоритм> <количество_машин> [json_файл_результата]
 *   java JsonToPackagesApp split <json_вход> <файл_посылок_выход>
 */
public class JsonToPackagesApp {
    private static final Map<String, Command> COMMANDS = new LinkedHashMap<>();

    static {
        register(new PackCommand());
        register(new SplitCommand());
    }

    public static void main(String[] args) {
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
        System.setErr(new PrintStream(System.err, true, StandardCharsets.UTF_8));

        if (args.length == 0) {
            printUsage();
            System.exit(1);
        }

        String commandName = args[0].toLowerCase();
        Command command = COMMANDS.get(commandName);
        if (command == null) {
            System.err.println("Неизвестная команда: " + commandName);
            printUsage();
            System.exit(1);
        }

        String[] commandArgs = Arrays.copyOfRange(args, 1, args.length);
        if (commandArgs.length < command.minArgs()) {
            System.err.println("Недостаточно аргументов для команды: " + command.name());
            printUsage();
            System.exit(1);
        }

        try {
            command.execute(commandArgs);
        } catch (Exception e) {
            System.err.println("Ошибка выполнения команды '" + command.name() + "': " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void register(Command command) {
        COMMANDS.put(command.name(), command);
    }

    private static void printUsage() {
        System.err.println("Использование:");
        System.err.println("  java JsonToPackagesApp pack <путь_к_файлу> <ширина_кузова> <высота_кузова> <алгоритм> <количество_машин> [json_файл_результата]");
        System.err.println("  java JsonToPackagesApp split <json_вход> <файл_посылок_выход>");
        System.err.println("Алгоритмы для pack: simple, optimized, even, dense");
    }

    interface Command {
        String name();

        int minArgs();

        void execute(String[] args) throws Exception;
    }

    static class PackCommand implements Command {
        @Override
        public String name() {
            return "pack";
        }

        @Override
        public int minArgs() {
            return 5;
        }

        @Override
        public void execute(String[] args) throws IOException {
            String filePath = args[0];
            int truckWidth = Integer.parseInt(args[1]);
            int truckHeight = Integer.parseInt(args[2]);
            String algorithmType = args[3];
            int maxTrucks = Integer.parseInt(args[4]);
            String jsonOutputPath = args.length >= 6 ? args[5] : null;

            PackageTextFormatService textFormatService = new PackageTextFormatService();
            List<Package> packages = textFormatService.loadFromTextFile(Path.of(filePath));
            if (packages.isEmpty()) {
                throw new IllegalArgumentException("Файл не содержит посылок");
            }

            PackageValidator.ValidationResult validation = PackageValidator.validate(
                    packages, truckWidth, truckHeight
            );
            if (!validation.isValid()) {
                throw new IllegalArgumentException("Ошибки валидации:\n" + validation.getErrorMessage());
            }

            PackingAlgorithm algorithm = resolveAlgorithm(algorithmType);
            Truck.resetIdCounter();
            List<Truck> trucks = algorithm.pack(packages, truckWidth, truckHeight, maxTrucks);

            System.out.println();
            System.out.println("Результат упаковки (" + algorithm.getName() + "):");
            System.out.println("Использовано кузовов: " + trucks.size());
            System.out.println();
            for (Truck truck : trucks) {
                System.out.println("Кузов #" + truck.getId() + ":");
                System.out.println(truck.render());
                System.out.println();
            }

            if (jsonOutputPath != null) {
                PackingResultJsonService jsonService = new PackingResultJsonService();
                jsonService.saveToJson(
                        trucks,
                        truckWidth,
                        truckHeight,
                        algorithm.getCode(),
                        Path.of(jsonOutputPath)
                );
                System.out.println("Результат погрузки сохранён в JSON: " + jsonOutputPath);
            }
        }

        private PackingAlgorithm resolveAlgorithm(String algorithmType) {
            return switch (algorithmType.toLowerCase()) {
                case "optimized" -> new OptimizedPackingAlgorithm();
                case "even" -> new EvenDistributionPackingAlgorithm();
                case "dense" -> new DensePackingAlgorithm();
                case "simple" -> new SimplePackingAlgorithm();
                default -> throw new IllegalArgumentException("Неизвестный алгоритм: " + algorithmType);
            };
        }
    }

    static class SplitCommand implements Command {
        @Override
        public String name() {
            return "split";
        }

        @Override
        public int minArgs() {
            return 2;
        }

        @Override
        public void execute(String[] args) throws IOException {
            String jsonInput = args[0];
            String packagesOutput = args[1];

            PackingResultJsonService service = new PackingResultJsonService();
            List<Package> packages = service.loadPackagesFromJson(Path.of(jsonInput));
            if (packages.isEmpty()) {
                System.err.println("JSON не содержит посылок");
            }

            PackageTextFormatService textFormatService = new PackageTextFormatService();
            textFormatService.writeToTextFile(packages, Path.of(packagesOutput));
            System.out.printf("Создан файл посылок: %s (из %s)%n", packagesOutput, jsonInput);
        }
    }
}

