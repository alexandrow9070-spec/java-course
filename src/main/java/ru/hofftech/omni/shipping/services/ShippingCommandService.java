package ru.hofftech.omni.shipping.services;

import org.springframework.stereotype.Service;
import ru.hofftech.omni.shipping.dto.LoadRequest;
import ru.hofftech.omni.shipping.dto.PackagePlacementResponse;
import ru.hofftech.omni.shipping.dto.PackRequest;
import ru.hofftech.omni.shipping.dto.PackResponse;
import ru.hofftech.omni.shipping.dto.TruckResponse;
import ru.hofftech.omni.shipping.dto.UnloadRequest;
import ru.hofftech.omni.shipping.dto.UnloadResponse;
import ru.hofftech.omni.shipping.entities.Package;
import ru.hofftech.omni.shipping.entities.Truck;
import ru.hofftech.omni.shipping.interfaces.PackingAlgorithm;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ShippingCommandService {
    private final ParcelService parcelService;
    private final PackingAlgorithmResolver algorithmResolver;

    public ShippingCommandService(ParcelService parcelService, PackingAlgorithmResolver algorithmResolver) {
        this.parcelService = parcelService;
        this.algorithmResolver = algorithmResolver;
    }

    public PackResponse pack(PackRequest request) {
        List<Package> packages = parcelService.resolvePackages(request.parcelNames());
        PackageValidator.ValidationResult validation = PackageValidator.validate(packages, request.truckWidth(), request.truckHeight());
        if (!validation.isValid()) {
            throw new IllegalArgumentException(validation.getErrorMessage());
        }

        PackingAlgorithm algorithm = algorithmResolver.resolve(request.algorithm().toLowerCase());
        Truck.resetIdCounter();
        List<Truck> trucks = algorithm.pack(packages, request.truckWidth(), request.truckHeight(), request.maxTrucks());

        List<TruckResponse> truckResponses = trucks.stream().map(this::toTruckResponse).toList();
        return new PackResponse(algorithm.getCode(), request.truckWidth(), request.truckHeight(), trucks.size(), truckResponses);
    }

    public PackResponse load(LoadRequest request) {
        List<TruckSpec> specs = parseTruckSpecs(request.trucks());
        if (specs.isEmpty()) {
            throw new IllegalArgumentException("Список кузовов пуст");
        }
        if (!allSameSize(specs)) {
            throw new IllegalArgumentException("Для REST load поддерживаются только одинаковые размеры кузовов");
        }
        TruckSpec first = specs.get(0);
        return pack(new PackRequest(request.parcelNames(), first.width(), first.height(), request.algorithm(), specs.size()));
    }

    public UnloadResponse unload(UnloadRequest request) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        List<String> parcels = new ArrayList<>();
        for (TrucksJsonFileService.TruckLoadDto truck : request.trucks()) {
            for (TrucksJsonFileService.ParcelLoadDto parcel : truck.parcels()) {
                parcels.add(parcel.name());
                counts.put(parcel.name(), counts.getOrDefault(parcel.name(), 0) + 1);
            }
        }
        return request.withCount()
                ? new UnloadResponse(List.of(), counts)
                : new UnloadResponse(parcels, Map.of());
    }

    private TruckResponse toTruckResponse(Truck truck) {
        List<PackagePlacementResponse> parcels = truck.getPackages().stream()
                .map(pkg -> new PackagePlacementResponse(pkg.getName(), pkg.getTruckId(), pkg.getX(), pkg.getY(), pkg.getWidth(), pkg.getHeight()))
                .toList();
        return new TruckResponse(truck.getId(), truck.getWidth(), truck.getHeight(), truck.render(), parcels);
    }

    private record TruckSpec(int width, int height) {}

    private List<TruckSpec> parseTruckSpecs(String trucksSpec) {
        String[] parts = trucksSpec.trim().split("[,\\s]+");
        List<TruckSpec> list = new ArrayList<>();
        for (String part : parts) {
            if (part.isBlank()) {
                continue;
            }
            String[] wh = part.toLowerCase().split("x");
            if (wh.length != 2) {
                throw new IllegalArgumentException("Неверный размер кузова: " + part);
            }
            list.add(new TruckSpec(Integer.parseInt(wh[0]), Integer.parseInt(wh[1])));
        }
        return list;
    }

    private boolean allSameSize(List<TruckSpec> specs) {
        TruckSpec first = specs.get(0);
        return specs.stream().allMatch(s -> s.width == first.width && s.height == first.height);
    }
}
