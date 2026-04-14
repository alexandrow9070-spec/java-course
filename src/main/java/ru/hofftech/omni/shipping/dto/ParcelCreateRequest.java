package ru.hofftech.omni.shipping.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record ParcelCreateRequest(
        @NotBlank String name,
        List<String> shape
) {
}
