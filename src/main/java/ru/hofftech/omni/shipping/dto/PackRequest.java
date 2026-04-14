package ru.hofftech.omni.shipping.dto;

import java.util.List;

public record PackRequest(
        List<String> parcelNames,
        int truckWidth,
        int truckHeight,
        String algorithm,
        int maxTrucks
) {
}
