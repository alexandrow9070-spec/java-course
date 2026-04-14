package ru.hofftech.omni.shipping.services.packing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.hofftech.omni.shipping.entities.Package;
import ru.hofftech.omni.shipping.entities.Truck;
import ru.hofftech.omni.shipping.interfaces.PackingAlgorithm;

import java.util.ArrayList;
import java.util.List;

/**
 * Равномерная погрузка по машинам (round-robin по доступным кузовам)
 */
@Component
public class EvenDistributionPackingAlgorithm implements PackingAlgorithm {

    private static final Logger logger = LoggerFactory.getLogger(EvenDistributionPackingAlgorithm.class);

    @Override
    public String getCode() {
        return "even";
    }

    @Override
    public String getName() {
        return "Равномерная погрузка по машинам";
    }

    @Override
    public List<Truck> pack(List<Package> packages, int truckWidth, int truckHeight, int maxTrucks) {
        logger.info("Начало упаковки по равномерному алгоритму. Посылок: {}", packages.size());

        if (maxTrucks <= 0) {
            throw new IllegalArgumentException("Максимальное количество машин должно быть положительным");
        }

        List<Truck> allTrucks = new ArrayList<>();
        for (int i = 0; i < maxTrucks; i++) {
            allTrucks.add(new Truck(truckWidth, truckHeight));
        }

        int startIndex = 0;

        for (Package pkg : packages) {
            boolean placed = false;

            for (int attempt = 0; attempt < maxTrucks; attempt++) {
                int truckIndex = (startIndex + attempt) % maxTrucks;
                Truck truck = allTrucks.get(truckIndex);

                if (truck.tryPlacePackage(pkg)) {
                    placed = true;
                    logger.debug("Посылка {} размещена в кузове {} (равномерный алгоритм)", pkg, truck.getId());
                    startIndex = (truckIndex + 1) % maxTrucks;
                    break;
                }
            }

            if (!placed) {
                logger.error("Не удалось равномерно разместить посылку {} ни в одном из {} кузовов", pkg, maxTrucks);
                throw new IllegalStateException("Недостаточно места для равномерной погрузки всех посылок");
            }
        }

        List<Truck> result = new ArrayList<>();
        for (Truck truck : allTrucks) {
            if (!truck.getPackages().isEmpty()) {
                result.add(truck);
            }
        }

        logger.info("Равномерная упаковка завершена. Использовано кузовов: {}", result.size());
        return result;
    }
}

