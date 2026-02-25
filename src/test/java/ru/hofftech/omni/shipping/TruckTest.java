package ru.hofftech.omni.shipping;

import org.junit.jupiter.api.Test;

import ru.hofftech.omni.shipping.entities.Package;
import ru.hofftech.omni.shipping.entities.Truck;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TruckTest {

    @Test
    void createsTruckWithCorrectDimensionsAndId() {
        Truck.resetIdCounter();
        Truck truck = new Truck(6, 6);

        assertEquals(6, truck.getWidth());
        assertEquals(6, truck.getHeight());
        assertEquals(1, truck.getId());
    }

    @Test
    void tryPlacePackagePlacesSinglePackage() {
        Truck.resetIdCounter();
        Truck truck = new Truck(6, 6);

        List<String> shape = new ArrayList<>();
        shape.add("123");

        Package pkg = new Package(3, 1, shape);

        boolean placed = truck.tryPlacePackage(pkg);
        assertTrue(placed, "Посылка должна быть размещена");
        assertEquals(1, truck.getPackages().size());
    }

    @Test
    void tryPlacePackageCanPlaceMultiplePackages() {
        Truck.resetIdCounter();
        Truck truck = new Truck(6, 6);

        List<String> shape1 = new ArrayList<>();
        shape1.add("11");
        Package pkg1 = new Package(2, 1, shape1);

        List<String> shape2 = new ArrayList<>();
        shape2.add("22");
        Package pkg2 = new Package(2, 1, shape2);

        assertTrue(truck.tryPlacePackage(pkg1));
        assertTrue(truck.tryPlacePackage(pkg2));

        assertEquals(2, truck.getPackages().size());
    }
}

