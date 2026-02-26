package ru.hofftech.omni.shipping;

import org.junit.jupiter.api.Test;
import ru.hofftech.omni.shipping.entities.Package;
import ru.hofftech.omni.shipping.entities.Truck;
import ru.hofftech.omni.shipping.services.PackingResultJsonService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PackingResultJsonServiceTest {

    @Test
    void savesAndLoadsPackagesViaJson() throws IOException {
        Truck.resetIdCounter();

        Truck truck = new Truck(6, 6);
        List<String> shape = new ArrayList<>();
        shape.add("11");
        Package pkg = new Package(2, 1, shape);
        assertTrue(truck.tryPlacePackage(pkg));

        List<Truck> trucks = List.of(truck);

        PackingResultJsonService service = new PackingResultJsonService();
        Path tempJson = Files.createTempFile("trucks-", ".json");

        service.saveToJson(trucks, 6, 6, "dense", tempJson);

        List<Package> loadedPackages = service.loadPackagesFromJson(tempJson);
        assertEquals(1, loadedPackages.size(), "Должна быть загружена одна посылка");
        assertEquals(pkg.getWidth(), loadedPackages.get(0).getWidth());
        assertEquals(pkg.getHeight(), loadedPackages.get(0).getHeight());
        assertEquals(pkg.getShape(), loadedPackages.get(0).getShape());
    }

    @Test
    void writesPackagesToTextFileInLoaderFormat() throws IOException {
        List<String> shape1 = new ArrayList<>();
        shape1.add("111");
        List<String> shape2 = new ArrayList<>();
        shape2.add("22");

        Package pkg1 = new Package(3, 1, shape1);
        Package pkg2 = new Package(2, 1, shape2);

        List<Package> packages = List.of(pkg1, pkg2);

        PackingResultJsonService service = new PackingResultJsonService();
        Path tempFile = Files.createTempFile("packages-from-json-", ".txt");

        service.writePackagesToTextFile(packages, tempFile);

        List<String> lines = Files.readAllLines(tempFile);
        assertTrue(lines.contains("111"));
        assertTrue(lines.contains("22"));
    }
}

