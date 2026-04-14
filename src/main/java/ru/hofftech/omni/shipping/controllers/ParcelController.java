package ru.hofftech.omni.shipping.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.hofftech.omni.shipping.dto.PagedResponse;
import ru.hofftech.omni.shipping.dto.ParcelCreateRequest;
import ru.hofftech.omni.shipping.dto.ParcelResponse;
import ru.hofftech.omni.shipping.services.ParcelService;

@RestController
@RequestMapping("/api/parcels")
@Tag(name = "Parcels")
public class ParcelController {
    private final ParcelService parcelService;

    public ParcelController(ParcelService parcelService) {
        this.parcelService = parcelService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Создать посылку")
    public ParcelResponse create(@Valid @RequestBody ParcelCreateRequest request) {
        return parcelService.create(request);
    }

    @GetMapping("/{name}")
    @Operation(summary = "Получить посылку по имени")
    public ParcelResponse get(@PathVariable String name) {
        return parcelService.getByName(name);
    }

    @DeleteMapping("/{name}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Удалить посылку")
    public void delete(@PathVariable String name) {
        parcelService.deleteByName(name);
    }

    @GetMapping
    @Operation(summary = "Список посылок с пагинацией")
    public PagedResponse<ParcelResponse> list(@RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "20") int size) {
        Page<ParcelResponse> data = parcelService.getAll(PageRequest.of(page, size));
        return new PagedResponse<>(data.getContent(), data.getNumber(), data.getSize(), data.getTotalElements(), data.getTotalPages());
    }
}
