package ru.hofftech.omni.shipping.services;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import ru.hofftech.omni.shipping.entities.Package;
import ru.hofftech.omni.shipping.entities.Truck;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Сервис для сохранения результата погрузки (кузова и посылки) в JSON
 * и обратного извлечения посылок из JSON.
 *
 * Структура JSON:
 * {
 *   "truckWidth": 6,
 *   "truckHeight": 6,
 *   "algorithm": "dense",
 *   "trucks": [
 *     {
 *       "id": 1,
 *       "packages": [
 *         {
 *           "truckId": 1,
 *           "x": 0,
 *           "y": 5,
 *           "width": 2,
 *           "height": 1,
 *           "shape": ["11"]
 *         }
 *       ]
 *     }
 *   ]
 * }
 */
public class PackingResultJsonService {

    private final ObjectMapper objectMapper;

    public PackingResultJsonService() {
        this.objectMapper = new ObjectMapper();
    }

    public PackingResultJsonService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void saveToJson(List<Truck> trucks,
                           int truckWidth,
                           int truckHeight,
                           String algorithmCode,
                           Path outputPath) throws IOException {
        PackingResultDto dto = toDto(trucks, truckWidth, truckHeight, algorithmCode);
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(outputPath.toFile(), dto);
    }

    public List<Package> loadPackagesFromJson(Path inputPath) throws IOException {
        PackingResultDto dto = objectMapper.readValue(inputPath.toFile(), PackingResultDto.class);
        List<Package> result = new ArrayList<>();

        for (TruckDto truckDto : dto.trucks()) {
            for (PackageDto pkgDto : truckDto.packages()) {
                result.add(new Package(pkgDto.width(), pkgDto.height(), pkgDto.shape()));
            }
        }

        return result;
    }

    private PackingResultDto toDto(List<Truck> trucks,
                                   int truckWidth,
                                   int truckHeight,
                                   String algorithmCode) {
        List<TruckDto> truckDtos = new ArrayList<>();
        for (Truck truck : trucks) {
            List<PackageDto> packageDtos = new ArrayList<>();
            for (Package pkg : truck.getPackages()) {
                packageDtos.add(new PackageDto(pkg.getTruckId(), pkg.getX(), pkg.getY(),
                        pkg.getWidth(), pkg.getHeight(), pkg.getShape()));
            }
            truckDtos.add(new TruckDto(truck.getId(), packageDtos));
        }
        return new PackingResultDto(truckWidth, truckHeight, algorithmCode, truckDtos);
    }

    // DTO классы для JSON

    private record PackingResultDto(
            @JsonProperty("truckWidth") int truckWidth,
            @JsonProperty("truckHeight") int truckHeight,
            @JsonProperty("algorithm") String algorithm,
            @JsonProperty("trucks") List<TruckDto> trucks) {
        @JsonCreator
        private PackingResultDto {
            trucks = trucks != null ? trucks : List.of();
        }
    }

    private record TruckDto(
            @JsonProperty("id") int id,
            @JsonProperty("packages") List<PackageDto> packages) {
        @JsonCreator
        private TruckDto {
            packages = packages != null ? packages : List.of();
        }
    }

    private record PackageDto(
            @JsonProperty("truckId") int truckId,
            @JsonProperty("x") int x,
            @JsonProperty("y") int y,
            @JsonProperty("width") int width,
            @JsonProperty("height") int height,
            @JsonProperty("shape") List<String> shape) {
        @JsonCreator
        private PackageDto {
            shape = shape != null ? List.copyOf(shape) : List.of();
        }
    }
}

