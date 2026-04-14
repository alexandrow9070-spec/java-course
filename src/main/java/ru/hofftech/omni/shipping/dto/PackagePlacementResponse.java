package ru.hofftech.omni.shipping.dto;

public record PackagePlacementResponse(
        String name,
        int truckId,
        int x,
        int y,
        int width,
        int height
) {
}
