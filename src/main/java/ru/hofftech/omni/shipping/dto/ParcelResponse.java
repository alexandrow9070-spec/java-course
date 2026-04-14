package ru.hofftech.omni.shipping.dto;

import java.util.List;

public record ParcelResponse(
        String name,
        int width,
        int height,
        List<String> shape
) {
}
