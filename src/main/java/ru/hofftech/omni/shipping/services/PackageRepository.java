package ru.hofftech.omni.shipping.services;

import ru.hofftech.omni.shipping.entities.Package;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Репозиторий посылок с хранением в файле (test-input.txt).
 * Имена посылок уникальны.
 */
public class PackageRepository {
    private final Path dbPath;
    private final NamedPackageTextFormatService formatService;

    public PackageRepository(Path dbPath) {
        this(dbPath, new NamedPackageTextFormatService());
    }

    public PackageRepository(Path dbPath, NamedPackageTextFormatService formatService) {
        this.dbPath = dbPath;
        this.formatService = formatService;
    }

    public Map<String, Package> loadAll() throws IOException {
        if (!Files.exists(dbPath)) {
            return new LinkedHashMap<>();
        }
        return new LinkedHashMap<>(formatService.loadFromFile(dbPath));
    }

    public Optional<Package> findByName(String name) throws IOException {
        Map<String, Package> all = loadAll();
        return Optional.ofNullable(all.get(name));
    }

    public Package create(Package pkg) throws IOException {
        if (pkg.getName() == null || pkg.getName().isBlank()) {
            throw new IllegalArgumentException("Имя посылки обязательно");
        }

        Map<String, Package> all = loadAll();
        if (all.containsKey(pkg.getName())) {
            throw new IllegalArgumentException("Посылка с именем '" + pkg.getName() + "' уже существует");
        }

        all.put(pkg.getName(), pkg);
        formatService.writeToFile(all, dbPath);
        return pkg;
    }

    public boolean delete(String name) throws IOException {
        Map<String, Package> all = loadAll();
        Package removed = all.remove(name);
        if (removed == null) {
            return false;
        }
        formatService.writeToFile(all, dbPath);
        return true;
    }
}

