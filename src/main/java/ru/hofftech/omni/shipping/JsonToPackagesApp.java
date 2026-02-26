package ru.hofftech.omni.shipping;

import ru.hofftech.omni.shipping.entities.Package;
import ru.hofftech.omni.shipping.services.PackingResultJsonService;

import java.nio.file.Path;
import java.util.List;

/**
 * CLI-приложение для обратной операции:
 * JSON с результатом погрузки → текстовый файл с посылками.
 *
 * Использование:
 *   java JsonToPackagesApp <json_вход> <файл_посылок_выход>
 */
public class JsonToPackagesApp {

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Использование: JsonToPackagesApp <json_вход> <файл_посылок_выход>");
            System.exit(1);
        }

        String jsonInput = args[0];
        String packagesOutput = args[1];

        try {
            PackingResultJsonService service = new PackingResultJsonService();

            List<Package> packages = service.loadPackagesFromJson(Path.of(jsonInput));
            if (packages.isEmpty()) {
                System.err.println("JSON не содержит посылок");
            }

            service.writePackagesToTextFile(packages, Path.of(packagesOutput));

            System.out.printf("Создан файл посылок: %s (из %s)%n", packagesOutput, jsonInput);
        } catch (Exception e) {
            System.err.println("Ошибка при конвертации JSON → посылки: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}

