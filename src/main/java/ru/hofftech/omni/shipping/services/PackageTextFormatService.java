package ru.hofftech.omni.shipping.services;

import ru.hofftech.omni.shipping.entities.Package;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Сервис для работы с текстовым форматом посылок.
 * Формат: каждая посылка — блок строк, блоки разделены пустой строкой.
 */
public class PackageTextFormatService {

    public List<Package> loadFromTextFile(Path inputPath) throws IOException {
        return PackageLoader.loadPackages(inputPath.toString());
    }

    public void writeToTextFile(List<Package> packages, Path outputPath) throws IOException {
        List<String> lines = new ArrayList<>();
        for (int i = 0; i < packages.size(); i++) {
            Package pkg = packages.get(i);
            lines.addAll(pkg.getShape());
            if (i < packages.size() - 1) {
                lines.add("");
            }
        }
        Files.write(outputPath, lines);
    }
}
