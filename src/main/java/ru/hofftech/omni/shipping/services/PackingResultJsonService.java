package ru.hofftech.omni.shipping.services;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import ru.hofftech.omni.shipping.entities.Package;
import ru.hofftech.omni.shipping.entities.Truck;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Сервис для сохранения результата погрузки (кузова и посылки) в JSON
 * и обратной конвертации JSON → файл с посылками.
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

        if (dto.trucks != null) {
            for (TruckDto truckDto : dto.trucks) {
                if (truckDto.packages == null) {
                    continue;
                }
                for (PackageDto pkgDto : truckDto.packages) {
                    result.add(new Package(pkgDto.width, pkgDto.height, pkgDto.shape));
                }
            }
        }

        return result;
    }

    /**
     * Записывает список посылок в текстовый файл в том же формате,
     * который ожидает {@link PackageLoader}: каждая посылка – блок строк,
     * блоки разделены пустой строкой.
     */
    public void writePackagesToTextFile(List<Package> packages, Path outputPath) throws IOException {
        List<String> lines = new ArrayList<>();
        for (int i = 0; i < packages.size(); i++) {
            Package pkg = packages.get(i);
            lines.addAll(pkg.getShape());
            if (i < packages.size() - 1) {
                lines.add("");
            }
        }
        Files.write(outputPath, lines);
    }

    private PackingResultDto toDto(List<Truck> trucks,
                                   int truckWidth,
                                   int truckHeight,
                                   String algorithmCode) {
        List<TruckDto> truckDtos = new ArrayList<>();
        for (Truck truck : trucks) {
            List<PackageDto> packageDtos = new ArrayList<>();
            for (Package pkg : truck.getPackages()) {
                packageDtos.add(new PackageDto(
                        pkg.getTruckId(),
                        pkg.getX(),
                        pkg.getY(),
                        pkg.getWidth(),
                        pkg.getHeight(),
                        pkg.getShape()
                ));
            }
            truckDtos.add(new TruckDto(truck.getId(), packageDtos));
        }
        return new PackingResultDto(truckWidth, truckHeight, algorithmCode, truckDtos);
    }

    // DTO классы для JSON

    public static class PackingResultDto {
        public final int truckWidth;
        public final int truckHeight;
        public final String algorithm;
        public final List<TruckDto> trucks;

        @JsonCreator
        public PackingResultDto(
                @JsonProperty("truckWidth") int truckWidth,
                @JsonProperty("truckHeight") int truckHeight,
                @JsonProperty("algorithm") String algorithm,
                @JsonProperty("trucks") List<TruckDto> trucks) {
            this.truckWidth = truckWidth;
            this.truckHeight = truckHeight;
            this.algorithm = algorithm;
            this.trucks = trucks != null ? trucks : new ArrayList<>();
        }
    }

    public static class TruckDto {
        public final int id;
        public final List<PackageDto> packages;

        @JsonCreator
        public TruckDto(
                @JsonProperty("id") int id,
                @JsonProperty("packages") List<PackageDto> packages) {
            this.id = id;
            this.packages = packages != null ? packages : new ArrayList<>();
        }
    }

    public static class PackageDto {
        public final int truckId;
        public final int x;
        public final int y;
        public final int width;
        public final int height;
        public final List<String> shape;

        @JsonCreator
        public PackageDto(
                @JsonProperty("truckId") int truckId,
                @JsonProperty("x") int x,
                @JsonProperty("y") int y,
                @JsonProperty("width") int width,
                @JsonProperty("height") int height,
                @JsonProperty("shape") List<String> shape) {
            this.truckId = truckId;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.shape = shape != null ? new ArrayList<>(shape) : new ArrayList<>();
        }
    }
}

