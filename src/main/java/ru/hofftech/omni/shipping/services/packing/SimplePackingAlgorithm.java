package ru.hofftech.omni.shipping.services.packing;

import ru.hofftech.omni.shipping.interfaces.PackingAlgorithm;
import ru.hofftech.omni.shipping.entities.Package;
import ru.hofftech.omni.shipping.entities.Truck;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Простой алгоритм: одна посылка - одна машина
 */
 public class SimplePackingAlgorithm implements PackingAlgorithm {
    private static final Logger logger = LoggerFactory.getLogger(SimplePackingAlgorithm.class);

    @Override
    public String getName() {
        return "Простой алгоритм (одна посылка - одна машина)";
    }

    @Override
    public List<Truck> pack(List<Package> packages, int truckWidth, int truckHeight) {
        logger.info("Начало упаковки по простому алгоритму. Посылок: {}", packages.size());
        
        List<Truck> trucks = new ArrayList<>();
        
        for (Package pkg : packages) {
            Truck truck = new Truck(truckWidth, truckHeight);
            
            // Пытаемся разместить посылку в нижнем левом углу
            int x = 0;
            int y = truckHeight - pkg.getHeight();
            
            if (pkg.canBePlacedAt(truck, x, y)) {
                pkg.placeIn(truck, x, y);
                truck.addPackage(pkg);
                trucks.add(truck);
                logger.debug("Посылка {} размещена в кузове {}", pkg, truck.getId());
            } else {
                logger.warn("Не удалось разместить посылку {} в кузове", pkg);
            }
        }
        
        logger.info("Упаковка завершена. Использовано кузовов: {}", trucks.size());
        return trucks;
    }
}
