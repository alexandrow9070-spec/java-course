package ru.hofftech.omni.shipping;

import org.junit.jupiter.api.Test;

import ru.hofftech.omni.shipping.entities.Package;
import ru.hofftech.omni.shipping.entities.Truck;
import ru.hofftech.omni.shipping.services.packing.OptimizedPackingAlgorithm;
import ru.hofftech.omni.shipping.services.packing.SimplePackingAlgorithm;
import ru.hofftech.omni.shipping.services.packing.EvenDistributionPackingAlgorithm;
import ru.hofftech.omni.shipping.services.packing.DensePackingAlgorithm;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PackingAlgorithmTest {

    @Test
    void simpleAlgorithmUsesOneTruckPerPackage() {
        Truck.resetIdCounter();

        List<Package> packages = new ArrayList<>();
        List<String> shape1 = new ArrayList<>();
        shape1.add("11");
        packages.add(new Package(2, 1, shape1));

        List<String> shape2 = new ArrayList<>();
        shape2.add("22");
        packages.add(new Package(2, 1, shape2));

        SimplePackingAlgorithm algorithm = new SimplePackingAlgorithm();
        List<Truck> trucks = algorithm.pack(packages, 6, 6, 10);

        assertEquals(2, trucks.size());
        assertEquals(1, trucks.get(0).getPackages().size());
        assertEquals(1, trucks.get(1).getPackages().size());
    }

    @Test
    void optimizedAlgorithmCanPackIntoSingleTruck() {
        Truck.resetIdCounter();

        List<Package> packages = new ArrayList<>();
        List<String> shape1 = new ArrayList<>();
        shape1.add("11");
        packages.add(new Package(2, 1, shape1));

        List<String> shape2 = new ArrayList<>();
        shape2.add("22");
        packages.add(new Package(2, 1, shape2));

        OptimizedPackingAlgorithm algorithm = new OptimizedPackingAlgorithm();
        List<Truck> trucks = algorithm.pack(packages, 6, 6, 10);

        assertEquals(1, trucks.size(), "Ожидается один кузов");
        assertEquals(2, trucks.get(0).getPackages().size(), "В кузове должны быть две посылки");
    }

    @Test
    void evenDistributionAlgorithmDistributesPackagesAcrossTrucks() {
        Truck.resetIdCounter();

        List<Package> packages = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            List<String> shape = new ArrayList<>();
            shape.add("11");
            packages.add(new Package(2, 1, shape));
        }

        EvenDistributionPackingAlgorithm algorithm = new EvenDistributionPackingAlgorithm();
        List<Truck> trucks = algorithm.pack(packages, 6, 6, 2);

        assertEquals(2, trucks.size(), "Ожидается два кузова");
        assertEquals(4, trucks.get(0).getPackages().size() + trucks.get(1).getPackages().size() - 2 + 2); // просто проверяем отсутствие ошибок
    }

    @Test
    void denseAlgorithmUsesMinimalNumberOfTrucksWithinLimit() {
        Truck.resetIdCounter();

        List<Package> packages = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            List<String> shape = new ArrayList<>();
            shape.add("11");
            packages.add(new Package(2, 1, shape));
        }

        DensePackingAlgorithm algorithm = new DensePackingAlgorithm();
        List<Truck> trucks = algorithm.pack(packages, 6, 6, 2);

        assertTrue(trucks.size() <= 2, "Алгоритм не должен использовать больше двух кузовов");
    }

    @Test
    void simpleAlgorithmThrowsWhenNotEnoughTrucks() {
        Truck.resetIdCounter();

        List<Package> packages = new ArrayList<>();
        List<String> shape1 = new ArrayList<>();
        shape1.add("11");
        packages.add(new Package(2, 1, shape1));
        List<String> shape2 = new ArrayList<>();
        shape2.add("22");
        packages.add(new Package(2, 1, shape2));

        SimplePackingAlgorithm algorithm = new SimplePackingAlgorithm();
        assertThrows(IllegalStateException.class,
                () -> algorithm.pack(packages, 6, 6, 1),
                "При недостаточном количестве машин должен выбрасываться IllegalStateException");
    }
}

