package ru.hofftech.omni.shipping.dto;

import java.util.List;

public record LoadRequest(
        List<String> parcelNames,
        String trucks,
        String algorithm
) {
}
