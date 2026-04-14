package ru.hofftech.omni.shipping.dto;

import java.util.List;

public record PackResponse(
        String algorithm,
        int truckWidth,
        int truckHeight,
        int usedTrucks,
        List<TruckResponse> trucks
) {
}
