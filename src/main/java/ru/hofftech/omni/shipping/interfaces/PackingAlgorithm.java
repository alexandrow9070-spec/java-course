package ru.hofftech.omni.shipping.interfaces;

import ru.hofftech.omni.shipping.entities.Package;
import ru.hofftech.omni.shipping.entities.Truck;

import java.util.List;

/**
 * Интерфейс для алгоритмов упаковки
 */
public interface PackingAlgorithm {
    List<Truck> pack(List<Package> packages, int truckWidth, int truckHeight);
    String getName();
}
