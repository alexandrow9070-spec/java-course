package ru.hofftech.omni.shipping.dto;

import java.util.List;
import java.util.Map;

public record UnloadResponse(
        List<String> parcels,
        Map<String, Integer> counts
) {
}
