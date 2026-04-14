package ru.hofftech.omni.shipping.dto;

import ru.hofftech.omni.shipping.services.TrucksJsonFileService;

import java.util.List;

public record UnloadRequest(
        List<TrucksJsonFileService.TruckLoadDto> trucks,
        boolean withCount
) {
}
