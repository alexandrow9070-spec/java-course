package ru.hofftech.omni.shipping;

import org.junit.jupiter.api.Test;

import ru.hofftech.omni.shipping.entities.Package;
import ru.hofftech.omni.shipping.entities.Truck;
import ru.hofftech.omni.shipping.services.packing.OptimizedPackingAlgorithm;
import ru.hofftech.omni.shipping.services.packing.SimplePackingAlgorithm;

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
        List<Truck> trucks = algorithm.pack(packages, 6, 6);

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
        List<Truck> trucks = algorithm.pack(packages, 6, 6);

        assertEquals(1, trucks.size(), "Ожидается один кузов");
        assertEquals(2, trucks.get(0).getPackages().size(), "В кузове должны быть две посылки");
    }
}

