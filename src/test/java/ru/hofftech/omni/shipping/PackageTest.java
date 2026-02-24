package ru.hofftech.omni.shipping;

import org.junit.jupiter.api.Test;

import ru.hofftech.omni.shipping.entities.Package;
import ru.hofftech.omni.shipping.entities.Truck;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PackageTest {

    @Test
    void createsPackageWithCorrectDimensions() {
        List<String> shape = new ArrayList<>();
        shape.add("123");
        shape.add("456");

        Package pkg = new Package(3, 2, shape);

        assertEquals(3, pkg.getWidth());
        assertEquals(2, pkg.getHeight());
        assertEquals(shape, pkg.getShape());
    }

    @Test
    void canBePlacedInTruckWithEnoughSpaceAndSupport() {
        Truck truck = new Truck(6, 6);

        List<String> shape = new ArrayList<>();
        shape.add("11");

        Package pkg = new Package(2, 1, shape);

        boolean canPlace = pkg.canBePlacedAt(truck, 0, 5);
        assertTrue(canPlace, "Посылка должна помещаться в кузов");

        pkg.placeIn(truck, 0, 5);
        assertEquals('1', truck.getCell(0, 5));
        assertEquals('1', truck.getCell(1, 5));
    }
}

