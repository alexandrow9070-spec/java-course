package ru.hofftech.omni.shipping.services;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * JSON формат для команды load/unload:
 *
 * [
 *   {
 *     "truck_type": "3x3",
 *     "parcels": [
 *       { "name": "test3x3", "coordinates": [[0,0],[0,1]] }
 *     ]
 *   }
 * ]
 */
public class TrucksJsonFileService {
    private final ObjectMapper objectMapper;

    public TrucksJsonFileService() {
        this(new ObjectMapper());
    }

    public TrucksJsonFileService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void write(Path outputFile, List<TruckLoadDto> trucks) throws IOException {
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(outputFile.toFile(), trucks);
    }

    public List<TruckLoadDto> read(Path inputFile) throws IOException {
        TruckLoadDto[] arr = objectMapper.readValue(inputFile.toFile(), TruckLoadDto[].class);
        List<TruckLoadDto> list = new ArrayList<>();
        if (arr != null) {
            java.util.Collections.addAll(list, arr);
        }
        return list;
    }

    public record TruckLoadDto(
            @JsonProperty("truck_type") String truckType,
            @JsonProperty("parcels") List<ParcelLoadDto> parcels
    ) {
        @JsonCreator
        public TruckLoadDto {
            parcels = parcels != null ? List.copyOf(parcels) : List.of();
        }
    }

    public record ParcelLoadDto(
            @JsonProperty("name") String name,
            @JsonProperty("coordinates") List<List<Integer>> coordinates
    ) {
        @JsonCreator
        public ParcelLoadDto {
            coordinates = coordinates != null ? List.copyOf(coordinates) : List.of();
        }
    }
}

