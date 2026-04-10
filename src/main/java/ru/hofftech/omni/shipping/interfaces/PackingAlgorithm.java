package ru.hofftech.omni.shipping.interfaces;

import ru.hofftech.omni.shipping.entities.Package;
import ru.hofftech.omni.shipping.entities.Truck;

import java.util.List;

/**
 * Интерфейс для алгоритмов упаковки
 */
public interface PackingAlgorithm {
    /**
     * Выполняет упаковку посылок в кузова
     *
     * @param packages   список посылок
     * @param truckWidth ширина кузова
     * @param truckHeight высота кузова
     * @param maxTrucks максимальное количество доступных машин
     * @return список загруженных кузовов
     */
    List<Truck> pack(List<Package> packages, int truckWidth, int truckHeight, int maxTrucks);

    /**
     * Машинно‑читаемое имя алгоритма (ключ выбора алгоритма)
     */
    String getCode();

    /**
     * Человекочитаемое описание алгоритма
     */
    String getName();
}
