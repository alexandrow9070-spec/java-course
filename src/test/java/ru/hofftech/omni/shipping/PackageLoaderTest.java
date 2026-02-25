package ru.hofftech.omni.shipping;

import org.junit.jupiter.api.Test;

import ru.hofftech.omni.shipping.entities.Package;
import ru.hofftech.omni.shipping.services.PackageLoader;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PackageLoaderTest {

    @Test
    void loadsPackagesFromFile() throws IOException {
        Path tempFile = Files.createTempFile("packages-", ".txt");

        try (FileWriter writer = new FileWriter(tempFile.toFile())) {
            writer.write("999\n");
            writer.write("999\n");
            writer.write("999\n");
            writer.write("\n");
            writer.write("666\n");
            writer.write("666\n");
            writer.write("\n");
            writer.write("55555\n");
        }

        List<Package> packages = PackageLoader.loadPackages(tempFile.toString());

        assertEquals(3, packages.size(), "Должно быть загружено 3 посылки");
        assertEquals(3, packages.get(0).getHeight(), "Первая посылка должна иметь высоту 3");
        assertEquals(2, packages.get(1).getHeight(), "Вторая посылка должна иметь высоту 2");
        assertEquals(1, packages.get(2).getHeight(), "Третья посылка должна иметь высоту 1");
    }
}

