package ru.hofftech.omni.shipping.services.packing;

import ru.hofftech.omni.shipping.interfaces.PackingAlgorithm;
import ru.hofftech.omni.shipping.entities.Package;
import ru.hofftech.omni.shipping.entities.Truck;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Оптимизированный алгоритм: пытается разместить несколько посылок в один кузов
 */
public class OptimizedPackingAlgorithm implements PackingAlgorithm {
    private static final Logger logger = Logger.getLogger(OptimizedPackingAlgorithm.class.getName());

    @Override
    public String getName() {
        return "Оптимизированный алгоритм";
    }

    @Override
    public List<Truck> pack(List<Package> packages, int truckWidth, int truckHeight) {
        logger.info("Начало упаковки по оптимизированному алгоритму. Посылок: " + packages.size());
        
        List<Truck> trucks = new ArrayList<>();
        List<Package> remainingPackages = new ArrayList<>(packages);
        
        while (!remainingPackages.isEmpty()) {
            Truck truck = new Truck(truckWidth, truckHeight);
            List<Package> placedInThisTruck = new ArrayList<>();
            
            // Пытаемся разместить посылки в текущем кузове
            for (int i = 0; i < remainingPackages.size(); i++) {
                Package pkg = remainingPackages.get(i);
                
                if (truck.tryPlacePackage(pkg)) {
                    placedInThisTruck.add(pkg);
                    logger.fine("Посылка " + pkg + " размещена в кузове " + truck.getId());
                }
            }
            
            // Удаляем размещенные посылки из списка оставшихся
            remainingPackages.removeAll(placedInThisTruck);
            
            if (!placedInThisTruck.isEmpty()) {
                trucks.add(truck);
                logger.fine("Кузов " + truck.getId() + " заполнен. Посылок в кузове: " + placedInThisTruck.size());
            } else {
                // Если не удалось разместить ни одну посылку, это ошибка
                logger.severe("Не удалось разместить посылку в новый кузов!");
                break;
            }
        }
        
        logger.info("Упаковка завершена. Использовано кузовов: " + trucks.size());
        return trucks;
    }
}
