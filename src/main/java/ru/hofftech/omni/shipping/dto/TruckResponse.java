package ru.hofftech.omni.shipping.dto;

import java.util.List;

public record TruckResponse(
        int id,
        int width,
        int height,
        String rendered,
        List<PackagePlacementResponse> parcels
) {
}
