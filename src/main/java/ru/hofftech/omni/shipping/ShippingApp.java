package ru.hofftech.omni.shipping;

import ru.hofftech.omni.shipping.entities.Package;
import ru.hofftech.omni.shipping.entities.Truck;
import ru.hofftech.omni.shipping.interfaces.PackingAlgorithm;
import ru.hofftech.omni.shipping.services.NamedPackageTextFormatService;
import ru.hofftech.omni.shipping.services.PackageRepository;
import ru.hofftech.omni.shipping.services.PackageTextFormatService;
import ru.hofftech.omni.shipping.services.PackageValidator;
import ru.hofftech.omni.shipping.services.PackingResultJsonService;
import ru.hofftech.omni.shipping.services.TrucksJsonFileService;
import com.fasterxml.jackson.databind.ObjectMapper;
import ru.hofftech.omni.shipping.services.packing.DensePackingAlgorithm;
import ru.hofftech.omni.shipping.services.packing.EvenDistributionPackingAlgorithm;
import ru.hofftech.omni.shipping.services.packing.OptimizedPackingAlgorithm;
import ru.hofftech.omni.shipping.services.packing.SimplePackingAlgorithm;

import java.io.IOException;
import java.io.PrintStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Единое CLI-приложение с командами:
 * - pack: файл с посылками -> погрузка в кузовы (+опциональный JSON результата)
 * - split: JSON результата погрузки -> текстовый файл с посылками
 *
 * Использование:
 *   java ShippingApp pack <путь_к_файлу> <ширина_кузова> <высота_кузова> <алгоритм> <количество_машин> [json_файл_результата]
 *   java ShippingApp split <json_вход> <файл_посылок_выход>
 */
public class ShippingApp {
    private static final Map<String, Command> COMMANDS = new LinkedHashMap<>();
    private static final Object EXECUTION_LOCK = new Object();

    static {
        register(new PackCommand());
        register(new SplitCommand());
        register(new CreatePackageCommand());
        register(new FindPackageCommand());
        register(new DeletePackageCommand());
        register(new LoadCommand());
        register(new UnloadCommand());
    }

    public static void main(String[] args) {
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
        System.setErr(new PrintStream(System.err, true, StandardCharsets.UTF_8));
        System.exit(run(args));
    }

    public static int run(String[] args) {
        if (args.length == 0) {
            printUsage();
            return 1;
        }

        String commandName = args[0].toLowerCase();
        Command command = COMMANDS.get(commandName);
        if (command == null) {
            System.err.println("Неизвестная команда: " + commandName);
            printUsage();
            return 1;
        }

        String[] commandArgs = Arrays.copyOfRange(args, 1, args.length);
        if (commandArgs.length < command.minArgs()) {
            System.err.println("Недостаточно аргументов для команды: " + command.name());
            printUsage();
            return 1;
        }

        try {
            command.execute(commandArgs);
            return 0;
        } catch (Exception e) {
            System.err.println("Ошибка выполнения команды '" + command.name() + "': " + e.getMessage());
            e.printStackTrace();
            return 1;
        }
    }

    public static String executeExternalCommandLine(String commandLine) {
        List<String> args = tokenizeCommandLine(commandLine);
        if (args.isEmpty()) {
            return "Пустая команда.";
        }

        synchronized (EXECUTION_LOCK) {
            PrintStream originalOut = System.out;
            PrintStream originalErr = System.err;
            ByteArrayOutputStream outBuffer = new ByteArrayOutputStream();
            ByteArrayOutputStream errBuffer = new ByteArrayOutputStream();

            try (PrintStream tempOut = new PrintStream(outBuffer, true, StandardCharsets.UTF_8);
                 PrintStream tempErr = new PrintStream(errBuffer, true, StandardCharsets.UTF_8)) {
                System.setOut(tempOut);
                System.setErr(tempErr);
                int exitCode = run(args.toArray(new String[0]));

                String stdout = outBuffer.toString(StandardCharsets.UTF_8);
                String stderr = errBuffer.toString(StandardCharsets.UTF_8);

                StringBuilder result = new StringBuilder();
                if (!stdout.isBlank()) {
                    result.append(stdout.trim());
                }
                if (!stderr.isBlank()) {
                    if (!result.isEmpty()) {
                        result.append("\n");
                    }
                    result.append(stderr.trim());
                }

                if (result.isEmpty()) {
                    return exitCode == 0 ? "Команда выполнена." : "Команда завершилась с ошибкой.";
                }
                return result.toString();
            } finally {
                System.setOut(originalOut);
                System.setErr(originalErr);
            }
        }
    }

    public static List<String> tokenizeCommandLine(String input) {
        List<String> tokens = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        char quoteChar = 0;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (inQuotes) {
                if (c == '\\' && i + 1 < input.length()) {
                    char next = input.charAt(i + 1);
                    if (next == quoteChar || next == '\\' || next == 'n' || next == 't' || next == 'r') {
                        if (next == 'n') current.append("\\n");
                        else if (next == 't') current.append("\\t");
                        else if (next == 'r') current.append("\\r");
                        else current.append(next);
                        i++;
                        continue;
                    }
                }
                if (c == quoteChar) {
                    inQuotes = false;
                } else {
                    current.append(c);
                }
                continue;
            }

            if (c == '"' || c == '\'') {
                inQuotes = true;
                quoteChar = c;
                continue;
            }

            if (Character.isWhitespace(c)) {
                if (!current.isEmpty()) {
                    tokens.add(current.toString());
                    current.setLength(0);
                }
            } else {
                current.append(c);
            }
        }

        if (!current.isEmpty()) {
            tokens.add(current.toString());
        }
        return tokens;
    }

    private static void register(Command command) {
        COMMANDS.put(command.name(), command);
    }

    private static void printUsage() {
        System.err.println("Использование:");
        System.err.println("  java ShippingApp pack <путь_к_файлу> <ширина_кузова> <высота_кузова> <алгоритм> <количество_машин> [json_файл_результата]");
        System.err.println("  java ShippingApp split <json_вход> <файл_посылок_выход>");
        System.err.println("  java ShippingApp createpackage -name \"test4x4\" -form \"oooo\\no  o\\no  o\\noooo\\n\"");
        System.err.println("  java ShippingApp findpackage \"test4x4\"");
        System.err.println("  java ShippingApp deletepackage \"test4x4\"");
        System.err.println("  java ShippingApp load -parcels-text \"test3x3,test3x2\" -trucks \"3x3 4x4\" -type \"simple\" -out text");
        System.err.println("  java ShippingApp load -parcels-file \"parcels.csv\" -trucks \"3x3 4x4\" -type \"simple\" -out json-file -out-filename \"trucks.json\"");
        System.err.println("  java ShippingApp unload -infile \"trucks.json\" -outfile \"parcels.csv\" [--withcount]");
        System.err.println("Алгоритмы для pack: simple, optimized, even, dense");
    }

    private static PackingAlgorithm resolveAlgorithm(String algorithmType) {
        return switch (algorithmType.toLowerCase()) {
            case "optimized" -> new OptimizedPackingAlgorithm();
            case "even" -> new EvenDistributionPackingAlgorithm();
            case "dense" -> new DensePackingAlgorithm();
            case "simple" -> new SimplePackingAlgorithm();
            default -> throw new IllegalArgumentException("Неизвестный алгоритм: " + algorithmType);
        };
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

            List<Package> namedPackages = ensureNames(packages);
            Map<String, Package> byName = new LinkedHashMap<>();
            for (Package pkg : namedPackages) {
                byName.put(pkg.getName(), pkg);
            }
            NamedPackageTextFormatService namedFormatService = new NamedPackageTextFormatService();
            namedFormatService.writeToFile(byName, Path.of(packagesOutput));
            System.out.printf("Создан файл посылок: %s (из %s)%n", packagesOutput, jsonInput);
        }
    }

    static class CreatePackageCommand implements Command {
        @Override
        public String name() {
            return "createpackage";
        }

        @Override
        public int minArgs() {
            return 4;
        }

        @Override
        public void execute(String[] args) throws Exception {
            Map<String, String> flags = parseFlags(args);
            String name = require(flags, "-name");
            String form = require(flags, "-form");

            PackageRepository repo =
                    new PackageRepository(
                            Path.of("test-input.txt"),
                            new NamedPackageTextFormatService()
                    );

            Package pkg = parsePackageFromForm(name, form);
            repo.create(pkg);
            printPackage(pkg);
        }
    }

    static class FindPackageCommand implements Command {
        @Override
        public String name() {
            return "findpackage";
        }

        @Override
        public int minArgs() {
            return 1;
        }

        @Override
        public void execute(String[] args) throws Exception {
            String name = stripQuotes(args[0].trim());
            PackageRepository repo =
                    new PackageRepository(
                            Path.of("test-input.txt"),
                            new NamedPackageTextFormatService()
                    );

            Package pkg = repo.findByName(name)
                    .orElseThrow(() -> new IllegalArgumentException("Посылка '" + name + "' не найдена"));
            printPackage(pkg);
        }
    }

    static class DeletePackageCommand implements Command {
        @Override
        public String name() {
            return "deletepackage";
        }

        @Override
        public int minArgs() {
            return 1;
        }

        @Override
        public void execute(String[] args) throws Exception {
            String name = stripQuotes(args[0].trim());
            PackageRepository repo =
                    new PackageRepository(
                            Path.of("test-input.txt"),
                            new NamedPackageTextFormatService()
                    );

            boolean deleted = repo.delete(name);
            if (!deleted) {
                throw new IllegalArgumentException("Посылка '" + name + "' не найдена");
            }
            System.out.println("Посылка \"" + name + "\" удалена.");
        }
    }

    private static Map<String, String> parseFlags(String[] args) {
        Map<String, String> flags = new LinkedHashMap<>();
        for (int i = 0; i < args.length; i++) {
            String key = args[i];
            if (!key.startsWith("-")) {
                continue;
            }
            // boolean flags: --withcount
            if (key.startsWith("--")) {
                flags.put(key.toLowerCase(), "true");
                continue;
            }
            if (i + 1 >= args.length) {
                throw new IllegalArgumentException("Флаг без значения: " + key);
            }
            String k = key.toLowerCase();
            StringBuilder value = new StringBuilder();
            i++;
            while (i < args.length) {
                String token = args[i];
                if (token.startsWith("-")) {
                    i--; // откатываемся, следующий цикл обработает новый ключ
                    break;
                }
                if (!value.isEmpty()) value.append(" ");
                value.append(stripQuotes(token));
                i++;
            }
            flags.put(k, value.toString());
        }
        return flags;
    }

    private static String require(Map<String, String> flags, String key) {
        String v = flags.get(key.toLowerCase());
        if (v == null) {
            throw new IllegalArgumentException("Не задан обязательный флаг: " + key);
        }
        return v;
    }

    private static String stripQuotes(String s) {
        if (s == null) return null;
        if (s.length() >= 2 && ((s.startsWith("\"") && s.endsWith("\"")) || (s.startsWith("'") && s.endsWith("'")))) {
            return s.substring(1, s.length() - 1);
        }
        return s;
    }

    private static String unescape(String s) {
        // минимум необходимый для -form "...\\n..."
        return s.replace("\\\\", "\\")
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t");
    }

    private static Package parsePackageFromForm(String name, String rawForm) {
        String form = unescape(rawForm);
        if (form.endsWith("\n")) {
            form = form.substring(0, form.length() - 1);
        }
        String[] lines = form.split("\\R", -1);
        List<String> shape = new ArrayList<>();
        for (String line : lines) {
            if (line.isEmpty()) continue;
            shape.add(line);
        }
        if (shape.isEmpty()) {
            throw new IllegalArgumentException("Пустая форма посылки");
        }

        int height = shape.size();
        int width = 0;
        for (String line : shape) {
            width = Math.max(width, line.length());
        }

        List<String> normalized = new ArrayList<>(height);
        for (String line : shape) {
            StringBuilder sb = new StringBuilder(line);
            while (sb.length() < width) sb.append(' ');
            normalized.add(sb.toString());
        }

        return new Package(name, width, height, normalized);
    }

    private static void printPackage(Package pkg) {
        System.out.println("id(name): \"" + pkg.getName() + "\"");
        System.out.println("form:");
        for (String line : pkg.getShape()) {
            System.out.println(line);
        }
    }

    static class LoadCommand implements Command {
        @Override
        public String name() {
            return "load";
        }

        @Override
        public int minArgs() {
            return 8;
        }

        @Override
        public void execute(String[] args) throws Exception {
            Map<String, String> flags = parseFlags(args);

            String parcelsText = flags.get("-parcels-text");
            String parcelsFile = flags.get("-parcels-file");
            if ((parcelsText == null && parcelsFile == null) || (parcelsText != null && parcelsFile != null)) {
                throw new IllegalArgumentException("Нужно указать ровно один из флагов: -parcels-text или -parcels-file");
            }

            String trucksSpec = require(flags, "-trucks");
            String type = require(flags, "-type").toLowerCase();
            String out = require(flags, "-out").toLowerCase();
            String outFilename = flags.get("-out-filename");

            List<String> parcelNames = (parcelsText != null)
                    ? parseParcelsText(parcelsText)
                    : loadParcelNamesFromFile(Path.of(parcelsFile));

            List<TruckSpec> truckSpecs = parseTruckSpecs(trucksSpec);
            if (truckSpecs.isEmpty()) {
                throw new IllegalArgumentException("Список кузовов пуст");
            }

            List<Package> parcels = loadParcelsFromDb(parcelNames);

            List<Truck> trucks;
            if (allSameSize(truckSpecs)) {
                TruckSpec first = truckSpecs.get(0);
                PackingAlgorithm algorithm = resolveAlgorithm(type);
                Truck.resetIdCounter();
                trucks = algorithm.pack(parcels, first.width, first.height, truckSpecs.size());
            } else {
                if (!type.equals("simple")) {
                    throw new IllegalArgumentException("Для разных размеров кузовов поддерживается только type=\"simple\"");
                }
                Truck.resetIdCounter();
                trucks = new ArrayList<>();
                for (TruckSpec ts : truckSpecs) {
                    trucks.add(new Truck(ts.width, ts.height));
                }
                packSimpleMulti(parcels, trucks);
            }

            if (out.equals("text")) {
                printLoadText(truckSpecs, trucks);
                return;
            }
            if (out.equals("json-file")) {
                if (outFilename == null || outFilename.isBlank()) {
                    throw new IllegalArgumentException("Для -out json-file нужно указать -out-filename");
                }
                Path outPath = Path.of(outFilename);
                TrucksJsonFileService service = new TrucksJsonFileService(new ObjectMapper());
                service.write(outPath, toLoadDtos(truckSpecs, trucks));
                System.out.println(outFilename);
                return;
            }

            throw new IllegalArgumentException("Неизвестный формат вывода -out: " + out);
        }

    }

    static class UnloadCommand implements Command {
        @Override
        public String name() {
            return "unload";
        }

        @Override
        public int minArgs() {
            return 4;
        }

        @Override
        public void execute(String[] args) throws Exception {
            Map<String, String> flags = parseFlags(args);
            boolean withCount = hasFlag(args, "--withcount");

            Path inFile = Path.of(require(flags, "-infile"));
            Path outFile = Path.of(require(flags, "-outfile"));

            TrucksJsonFileService service = new TrucksJsonFileService(new ObjectMapper());
            List<TrucksJsonFileService.TruckLoadDto> trucks = service.read(inFile);

            if (withCount) {
                LinkedHashMap<String, Integer> counts = new LinkedHashMap<>();
                for (TrucksJsonFileService.TruckLoadDto t : trucks) {
                    for (TrucksJsonFileService.ParcelLoadDto p : t.parcels()) {
                        counts.put(p.name(), counts.getOrDefault(p.name(), 0) + 1);
                    }
                }
                List<String> lines = new ArrayList<>();
                for (Map.Entry<String, Integer> e : counts.entrySet()) {
                    lines.add("\"" + e.getKey() + "\";" + e.getValue());
                }
                Files.write(outFile, lines, StandardCharsets.UTF_8);
            } else {
                List<String> lines = new ArrayList<>();
                for (TrucksJsonFileService.TruckLoadDto t : trucks) {
                    for (TrucksJsonFileService.ParcelLoadDto p : t.parcels()) {
                        lines.add("\"" + p.name() + "\"");
                    }
                }
                Files.write(outFile, lines, StandardCharsets.UTF_8);
            }

            System.out.println(outFile.getFileName());
        }
    }

    private static boolean hasFlag(String[] args, String flag) {
        for (String a : args) {
            if (a.equalsIgnoreCase(flag)) return true;
        }
        return false;
    }

    private record TruckSpec(int width, int height) {
        String asType() {
            return width + "x" + height;
        }
    }

    private static List<String> parseParcelsText(String parcelsText) {
        String s = parcelsText.trim();
        if (s.isBlank()) return List.of();
        String[] parts = s.split(",");
        List<String> names = new ArrayList<>();
        for (String p : parts) {
            String name = p.trim();
            if (!name.isEmpty()) names.add(name);
        }
        return names;
    }

    private static List<String> loadParcelNamesFromFile(Path parcelsFile) throws IOException {
        List<String> rawLines = Files.readAllLines(parcelsFile, StandardCharsets.UTF_8);
        List<String> names = new ArrayList<>();
        for (String raw : rawLines) {
            if (raw == null) continue;
            String line = raw.trim();
            if (line.isEmpty()) continue;
            // поддержка CSV: "name" или "name";count
            String[] parts = line.split(";", -1);
            String name = stripQuotes(parts[0].trim());
            if (!name.isBlank()) names.add(name);
        }
        return names;
    }

    private static List<TruckSpec> parseTruckSpecs(String trucksSpec) {
        String spec = trucksSpec.trim();
        if (spec.isBlank()) return List.of();
        String[] parts = spec.split("[,\\s]+");
        List<TruckSpec> list = new ArrayList<>();
        for (String p : parts) {
            String[] wh = p.toLowerCase().split("x");
            if (wh.length != 2) {
                throw new IllegalArgumentException("Неверный размер кузова: " + p + " (ожидается WxH)");
            }
            int w = Integer.parseInt(wh[0]);
            int h = Integer.parseInt(wh[1]);
            list.add(new TruckSpec(w, h));
        }
        return list;
    }

    private static boolean allSameSize(List<TruckSpec> specs) {
        if (specs.isEmpty()) return true;
        TruckSpec first = specs.get(0);
        for (TruckSpec s : specs) {
            if (s.width != first.width || s.height != first.height) return false;
        }
        return true;
    }

    private static List<Package> loadParcelsFromDb(List<String> names) throws IOException {
        PackageRepository repo =
                new PackageRepository(
                        Path.of("test-input.txt"),
                        new NamedPackageTextFormatService()
                );

        List<Package> parcels = new ArrayList<>();
        Set<String> missing = new LinkedHashSet<>();

        for (String name : names) {
            Package fromDb = repo.findByName(name).orElse(null);
            if (fromDb == null) {
                missing.add(name);
                continue;
            }
            parcels.add(clonePackage(fromDb));
        }

        if (!missing.isEmpty()) {
            throw new IllegalArgumentException("Посылки не найдены в базе: " + String.join(", ", missing));
        }

        return parcels;
    }

    private static Package clonePackage(Package p) {
        return new Package(p.getName(), p.getWidth(), p.getHeight(), p.getShape());
    }

    private static List<Package> ensureNames(List<Package> packages) throws IOException {
        boolean hasMissing = false;
        for (Package pkg : packages) {
            if (pkg.getName() == null || pkg.getName().isBlank()) {
                hasMissing = true;
                break;
            }
        }
        if (!hasMissing) {
            return packages;
        }

        PackageRepository repo =
                new PackageRepository(
                        Path.of("test-input.txt"),
                        new NamedPackageTextFormatService()
                );
        Map<String, Package> dbByName = repo.loadAll();
        Map<String, List<String>> shapeToNames = new LinkedHashMap<>();
        for (Package dbPkg : dbByName.values()) {
            String key = shapeKey(dbPkg.getShape());
            shapeToNames.computeIfAbsent(key, unused -> new ArrayList<>()).add(dbPkg.getName());
        }

        List<Package> resolved = new ArrayList<>(packages.size());
        for (Package pkg : packages) {
            if (pkg.getName() != null && !pkg.getName().isBlank()) {
                resolved.add(pkg);
                continue;
            }

            String key = shapeKey(pkg.getShape());
            List<String> names = shapeToNames.get(key);
            if (names == null || names.isEmpty()) {
                throw new IllegalArgumentException(
                        "Невозможно восстановить имя посылки по форме из базы test-input.txt");
            }
            if (names.size() > 1) {
                throw new IllegalArgumentException(
                        "Найдено несколько имен для одной формы в базе test-input.txt: " + names);
            }
            resolved.add(new Package(names.get(0), pkg.getWidth(), pkg.getHeight(), pkg.getShape()));
        }
        return resolved;
    }

    private static String shapeKey(List<String> shape) {
        return String.join("\n", shape);
    }

    private static void packSimpleMulti(List<Package> parcels, List<Truck> trucks) {
        int parcelIdx = 0;
        for (Truck truck : trucks) {
            if (parcelIdx >= parcels.size()) break;
            Package pkg = parcels.get(parcelIdx);
            boolean ok = truck.tryPlacePackage(pkg);
            if (!ok) {
                throw new IllegalStateException("Не удалось разместить посылку '" + pkg.getName() + "' в кузове " + truck.getWidth() + "x" + truck.getHeight());
            }
            parcelIdx++;
        }
        if (parcelIdx < parcels.size()) {
            throw new IllegalStateException("Недостаточно кузовов для погрузки всех посылок");
        }
    }

    private static void printLoadText(List<TruckSpec> specs, List<Truck> trucks) {
        // trucks могут быть меньше specs (алгоритмы pack создают по потребности) — печатаем то, что есть
        for (int i = 0; i < trucks.size(); i++) {
            Truck truck = trucks.get(i);
            TruckSpec spec = (i < specs.size()) ? specs.get(i) : new TruckSpec(truck.getWidth(), truck.getHeight());

            System.out.println("Кузов: " + spec.asType());
            System.out.println(renderForLoad(truck));

            for (Package pkg : truck.getPackages()) {
                System.out.println("Посылка: " + pkg.getName());
                for (String line : pkg.getShape()) {
                    System.out.println(line);
                }
                System.out.println("Координаты посылки " + pkg.getName() + ":");
                System.out.println(formatCoordinates(pkg));
            }
        }
    }

    private static String renderForLoad(Truck truck) {
        StringBuilder sb = new StringBuilder();
        for (int y = 0; y < truck.getHeight(); y++) {
            sb.append("+");
            for (int x = 0; x < truck.getWidth(); x++) {
                sb.append(truck.getCell(x, y));
            }
            sb.append("+\n");
        }
        sb.append("+".repeat(truck.getWidth() + 2));
        return sb.toString();
    }

    private static String formatCoordinates(Package pkg) {
        List<String> coords = new ArrayList<>();
        List<String> shape = pkg.getShape();
        for (int py = 0; py < pkg.getHeight(); py++) {
            String row = shape.get(py);
            for (int px = 0; px < pkg.getWidth(); px++) {
                if (row.charAt(px) != ' ') {
                    int y = pkg.getY() + py;
                    int x = pkg.getX() + px;
                    coords.add("[" + y + ", " + x + "]");
                }
            }
        }
        return String.join(",", coords);
    }

    private static List<TrucksJsonFileService.TruckLoadDto> toLoadDtos(List<TruckSpec> specs, List<Truck> trucks) {
        List<TrucksJsonFileService.TruckLoadDto> dtos = new ArrayList<>();

        int count = Math.max(specs.size(), trucks.size());
        for (int i = 0; i < count; i++) {
            TruckSpec spec = (i < specs.size()) ? specs.get(i) : null;
            Truck truck = (i < trucks.size()) ? trucks.get(i) : null;

            String truckType = (spec != null) ? spec.asType() : (truck != null ? (truck.getWidth() + "x" + truck.getHeight()) : "");
            List<TrucksJsonFileService.ParcelLoadDto> parcels = new ArrayList<>();

            if (truck != null) {
                for (Package pkg : truck.getPackages()) {
                    parcels.add(new TrucksJsonFileService.ParcelLoadDto(pkg.getName(), toCoordinatesList(pkg)));
                }
            }

            dtos.add(new TrucksJsonFileService.TruckLoadDto(truckType, parcels));
        }

        return dtos;
    }

    private static List<List<Integer>> toCoordinatesList(Package pkg) {
        List<List<Integer>> coords = new ArrayList<>();
        List<String> shape = pkg.getShape();
        for (int py = 0; py < pkg.getHeight(); py++) {
            String row = shape.get(py);
            for (int px = 0; px < pkg.getWidth(); px++) {
                if (row.charAt(px) != ' ') {
                    coords.add(List.of(pkg.getY() + py, pkg.getX() + px));
                }
            }
        }
        return coords;
    }
}

