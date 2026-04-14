package ru.hofftech.omni.shipping.shell;

import org.springframework.data.domain.PageRequest;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import ru.hofftech.omni.shipping.dto.LoadRequest;
import ru.hofftech.omni.shipping.dto.ParcelCreateRequest;
import ru.hofftech.omni.shipping.dto.ParcelResponse;
import ru.hofftech.omni.shipping.dto.PackRequest;
import ru.hofftech.omni.shipping.dto.UnloadRequest;
import ru.hofftech.omni.shipping.services.ParcelService;
import ru.hofftech.omni.shipping.services.ShippingCommandService;

import java.util.Arrays;
import java.util.List;

@ShellComponent
public class ShippingShellCommands {
    private final ParcelService parcelService;
    private final ShippingCommandService commandService;

    public ShippingShellCommands(ParcelService parcelService, ShippingCommandService commandService) {
        this.parcelService = parcelService;
        this.commandService = commandService;
    }

    @ShellMethod(key = "createpackage", value = "Создать посылку")
    public String createPackage(@ShellOption String name, @ShellOption String form) {
        List<String> shape = Arrays.asList(form.split("\\\\n"));
        ParcelResponse created = parcelService.create(new ParcelCreateRequest(name, shape));
        return "Создана посылка: " + created.name();
    }

    @ShellMethod(key = "findpackage", value = "Найти посылку")
    public String findPackage(@ShellOption String name) {
        ParcelResponse parcel = parcelService.getByName(name);
        return parcel.name() + " (" + parcel.width() + "x" + parcel.height() + ")";
    }

    @ShellMethod(key = "deletepackage", value = "Удалить посылку")
    public String deletePackage(@ShellOption String name) {
        parcelService.deleteByName(name);
        return "Удалена посылка: " + name;
    }

    @ShellMethod(key = "listpackages", value = "Список посылок с пагинацией")
    public List<String> listPackages(@ShellOption(defaultValue = "0") int page,
                                     @ShellOption(defaultValue = "20") int size) {
        return parcelService.getAll(PageRequest.of(page, size))
                .stream()
                .map(parcel -> parcel.name() + " " + parcel.width() + "x" + parcel.height())
                .toList();
    }

    @ShellMethod(key = "pack", value = "Команда pack")
    public String pack(@ShellOption String parcels,
                       @ShellOption int truckWidth,
                       @ShellOption int truckHeight,
                       @ShellOption String algorithm,
                       @ShellOption int maxTrucks) {
        List<String> parcelNames = Arrays.stream(parcels.split(",")).map(String::trim).toList();
        return "Использовано кузовов: " + commandService.pack(
                new PackRequest(parcelNames, truckWidth, truckHeight, algorithm, maxTrucks)
        ).usedTrucks();
    }

    @ShellMethod(key = "load", value = "Команда load")
    public String load(@ShellOption String parcels,
                       @ShellOption String trucks,
                       @ShellOption(defaultValue = "simple") String algorithm) {
        List<String> parcelNames = Arrays.stream(parcels.split(",")).map(String::trim).toList();
        return "Использовано кузовов: " + commandService.load(
                new LoadRequest(parcelNames, trucks, algorithm)
        ).usedTrucks();
    }

    @ShellMethod(key = "unload", value = "Команда unload")
    public String unload(@ShellOption(defaultValue = "false") boolean withCount) {
        return commandService.unload(new UnloadRequest(List.of(), withCount)).toString();
    }
}
