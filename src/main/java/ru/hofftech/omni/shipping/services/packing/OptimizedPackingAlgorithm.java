package ru.hofftech.omni.shipping.services.packing;

import ru.hofftech.omni.shipping.interfaces.PackingAlgorithm;
import ru.hofftech.omni.shipping.entities.Package;
import ru.hofftech.omni.shipping.entities.Truck;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Оптимизированный алгоритм: пытается разместить несколько посылок в один кузов
 */
@Component
public class OptimizedPackingAlgorithm implements PackingAlgorithm {
    private static final Logger logger = LoggerFactory.getLogger(OptimizedPackingAlgorithm.class);

    @Override
    public String getCode() {
        return "optimized";
    }

    @Override
    public String getName() {
        return "Оптимизированный алгоритм";
    }

    @Override
    public List<Truck> pack(List<Package> packages, int truckWidth, int truckHeight, int maxTrucks) {
        logger.info("Начало упаковки по оптимизированному алгоритму. Посылок: {}", packages.size());
        
        if (maxTrucks <= 0) {
            throw new IllegalArgumentException("Максимальное количество машин должно быть положительным");
        }

        List<Truck> trucks = new ArrayList<>();
        List<Package> remainingPackages = new ArrayList<>(packages);
        
        while (!remainingPackages.isEmpty()) {
            if (trucks.size() >= maxTrucks) {
                logger.error("Исчерпано доступное количество машин: доступно {}, осталось посылок {}", maxTrucks, remainingPackages.size());
                throw new IllegalStateException("Недостаточно машин для погрузки всех посылок оптимизированным алгоритмом");
            }

            Truck truck = new Truck(truckWidth, truckHeight);
            List<Package> placedInThisTruck = new ArrayList<>();
            
            // Пытаемся разместить посылки в текущем кузове
            for (int i = 0; i < remainingPackages.size(); i++) {
                Package pkg = remainingPackages.get(i);
                
                if (truck.tryPlacePackage(pkg)) {
                    placedInThisTruck.add(pkg);
                    logger.debug("Посылка {} размещена в кузове {}", pkg, truck.getId());
                }
            }
            
            // Удаляем размещенные посылки из списка оставшихся
            remainingPackages.removeAll(placedInThisTruck);
            
            if (!placedInThisTruck.isEmpty()) {
                trucks.add(truck);
                logger.debug("Кузов {} заполнен. Посылок в кузове: {}", truck.getId(), placedInThisTruck.size());
            } else {
                // Если не удалось разместить ни одну посылку, это ошибка
                logger.error("Не удалось разместить посылку в новый кузов!");
                break;
            }
        }
        
        logger.info("Упаковка завершена. Использовано кузовов: {}", trucks.size());
        return trucks;
    }
}
