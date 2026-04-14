package ru.hofftech.omni.shipping.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.hofftech.omni.shipping.dto.LoadRequest;
import ru.hofftech.omni.shipping.dto.PackRequest;
import ru.hofftech.omni.shipping.dto.PackResponse;
import ru.hofftech.omni.shipping.dto.UnloadRequest;
import ru.hofftech.omni.shipping.dto.UnloadResponse;
import ru.hofftech.omni.shipping.services.ShippingCommandService;

@RestController
@RequestMapping("/api/commands")
@Tag(name = "Shipping Commands")
public class ShippingCommandController {
    private final ShippingCommandService commandService;

    public ShippingCommandController(ShippingCommandService commandService) {
        this.commandService = commandService;
    }

    @PostMapping("/pack")
    @Operation(summary = "Команда pack через REST")
    public PackResponse pack(@RequestBody PackRequest request) {
        return commandService.pack(request);
    }

    @PostMapping("/load")
    @Operation(summary = "Команда load через REST")
    public PackResponse load(@RequestBody LoadRequest request) {
        return commandService.load(request);
    }

    @PostMapping("/unload")
    @Operation(summary = "Команда unload через REST")
    public UnloadResponse unload(@RequestBody UnloadRequest request) {
        return commandService.unload(request);
    }
}
