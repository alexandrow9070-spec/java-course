package ru.hofftech.omni.shipping.services;

import org.springframework.stereotype.Service;
import ru.hofftech.omni.shipping.interfaces.PackingAlgorithm;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PackingAlgorithmResolver {
    private final Map<String, PackingAlgorithm> algorithms;

    public PackingAlgorithmResolver(List<PackingAlgorithm> algorithms) {
        this.algorithms = algorithms.stream()
                .collect(Collectors.toMap(PackingAlgorithm::getCode, Function.identity()));
    }

    public PackingAlgorithm resolve(String code) {
        PackingAlgorithm algorithm = algorithms.get(code);
        if (algorithm == null) {
            throw new IllegalArgumentException("Неизвестный алгоритм: " + code);
        }
        return algorithm;
    }
}
