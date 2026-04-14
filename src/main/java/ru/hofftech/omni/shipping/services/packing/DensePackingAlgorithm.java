package ru.hofftech.omni.shipping.services.packing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.hofftech.omni.shipping.entities.Package;
import ru.hofftech.omni.shipping.entities.Truck;
import ru.hofftech.omni.shipping.interfaces.PackingAlgorithm;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Максимально плотная погрузка:
 * сортируем посылки по площади и жадно размещаем в уже созданных кузовах,
 * создавая новый кузов только при необходимости.
 */
@Component
public class DensePackingAlgorithm implements PackingAlgorithm {

    private static final Logger logger = LoggerFactory.getLogger(DensePackingAlgorithm.class);

    @Override
    public String getCode() {
        return "dense";
    }

    @Override
    public String getName() {
        return "Максимально плотная погрузка";
    }

    @Override
    public List<Truck> pack(List<Package> packages, int truckWidth, int truckHeight, int maxTrucks) {
        logger.info("Начало упаковки по плотному алгоритму. Посылок: {}", packages.size());

        if (maxTrucks <= 0) {
            throw new IllegalArgumentException("Максимальное количество машин должно быть положительным");
        }

        List<Package> sorted = new ArrayList<>(packages);
        sorted.sort(Comparator.comparingInt(p -> -p.getWidth() * p.getHeight()));

        List<Truck> trucks = new ArrayList<>();

        for (Package pkg : sorted) {
            boolean placed = false;

            for (Truck truck : trucks) {
                if (truck.tryPlacePackage(pkg)) {
                    placed = true;
                    logger.debug("Посылка {} размещена в существующем кузове {} (плотный алгоритм)", pkg, truck.getId());
                    break;
                }
            }

            if (!placed) {
                if (trucks.size() >= maxTrucks) {
                    logger.error("Исчерпано количество машин: доступно {}, посылка {} не помещается", maxTrucks, pkg);
                    throw new IllegalStateException("Недостаточно машин для максимально плотной погрузки");
                }

                Truck newTruck = new Truck(truckWidth, truckHeight);
                if (!newTruck.tryPlacePackage(pkg)) {
                    logger.error("Посылка {} не помещается даже в пустой кузов", pkg);
                    throw new IllegalStateException("Посылка не помещается даже в пустой кузов");
                }
                trucks.add(newTruck);
                logger.debug("Посылка {} размещена в новом кузове {} (плотный алгоритм)", pkg, newTruck.getId());
            }
        }

        logger.info("Плотная упаковка завершена. Использовано кузовов: {}", trucks.size());
        return trucks;
    }
}

