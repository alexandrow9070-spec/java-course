package main.java.ru.hofftech.omni.shipping.services.packing;

import main.java.ru.hofftech.omni.shipping.interfaces.PackingAlgorithm;
import main.java.ru.hofftech.omni.shipping.entities.Package;
import main.java.ru.hofftech.omni.shipping.entities.Truck;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Простой алгоритм: одна посылка - одна машина
 */
public class SimplePackingAlgorithm implements PackingAlgorithm {
    private static final Logger logger = Logger.getLogger(SimplePackingAlgorithm.class.getName());

    @Override
    public String getName() {
        return "Простой алгоритм (одна посылка - одна машина)";
    }

    @Override
    public List<Truck> pack(List<main.java.ru.hofftech.omni.shipping.entities.Package> packages, int truckWidth, int truckHeight) {
        logger.info("Начало упаковки по простому алгоритму. Посылок: " + packages.size());
        
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
                logger.fine("Посылка " + pkg + " размещена в кузове " + truck.getId());
            } else {
                logger.warning("Не удалось разместить посылку " + pkg + " в кузове");
            }
        }
        
        logger.info("Упаковка завершена. Использовано кузовов: " + trucks.size());
        return trucks;
    }
}
