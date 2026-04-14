package ru.hofftech.omni.shipping.services;

import ru.hofftech.omni.shipping.entities.Package;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Формат базы посылок:
 *
 * name:
 * ooo
 * ooo
 *
 * other:
 * o
 */
public class NamedPackageTextFormatService {

    public Map<String, Package> loadFromFile(Path inputPath) throws IOException {
        List<String> lines = Files.readAllLines(inputPath, StandardCharsets.UTF_8);

        Map<String, Package> result = new LinkedHashMap<>();
        String currentName = null;
        List<String> currentShape = new ArrayList<>();

        for (int i = 0; i <= lines.size(); i++) {
            String raw = (i == lines.size()) ? "" : lines.get(i);
            String line = raw == null ? "" : raw;

            if (line.isBlank()) {
                if (currentName != null) {
                    Package pkg = createPackage(currentName, currentShape);
                    if (result.containsKey(pkg.getName())) {
                        throw new IllegalArgumentException("Дублируется имя посылки: " + pkg.getName());
                    }
                    result.put(pkg.getName(), pkg);
                }
                currentName = null;
                currentShape.clear();
                continue;
            }

            if (currentName == null) {
                String trimmed = line.trim();
                if (!trimmed.endsWith(":")) {
                    throw new IllegalArgumentException("Ожидалось имя посылки вида 'name:' (строка: " + (i + 1) + ")");
                }
                currentName = trimmed.substring(0, trimmed.length() - 1).trim();
                if (currentName.isBlank()) {
                    throw new IllegalArgumentException("Пустое имя посылки (строка: " + (i + 1) + ")");
                }
            } else {
                currentShape.add(line);
            }
        }

        return result;
    }

    public void writeToFile(Map<String, Package> packagesByName, Path outputPath) throws IOException {
        List<String> out = new ArrayList<>();
        int i = 0;
        for (Package pkg : packagesByName.values()) {
            if (pkg.getName() == null || pkg.getName().isBlank()) {
                throw new IllegalArgumentException("Нельзя сохранить посылку без имени");
            }
            out.add(pkg.getName() + ":");
            out.addAll(pkg.getShape());
            if (i < packagesByName.size() - 1) {
                out.add("");
            }
            i++;
        }
        Files.write(outputPath, out, StandardCharsets.UTF_8);
    }

    private static Package createPackage(String name, List<String> shape) {
        if (shape == null || shape.isEmpty()) {
            throw new IllegalArgumentException("Посылка '" + name + "' не содержит формы");
        }

        int height = shape.size();
        int width = 0;
        for (String line : shape) {
            width = Math.max(width, line.length());
        }

        List<String> normalizedShape = new ArrayList<>(height);
        for (String line : shape) {
            StringBuilder normalized = new StringBuilder(line);
            while (normalized.length() < width) {
                normalized.append(' ');
            }
            normalizedShape.add(normalized.toString());
        }

        return new Package(name, width, height, normalizedShape);
    }
}

