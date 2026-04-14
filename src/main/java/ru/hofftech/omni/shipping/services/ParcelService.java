package ru.hofftech.omni.shipping.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.hofftech.omni.shipping.dto.ParcelCreateRequest;
import ru.hofftech.omni.shipping.dto.ParcelResponse;
import ru.hofftech.omni.shipping.persistence.ParcelEntity;
import ru.hofftech.omni.shipping.persistence.ParcelJpaRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ParcelService {
    private final ParcelJpaRepository repository;

    public ParcelService(ParcelJpaRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public ParcelResponse create(ParcelCreateRequest request) {
        repository.findByName(request.name()).ifPresent(existing -> {
            throw new IllegalArgumentException("Посылка с именем '" + request.name() + "' уже существует");
        });

        ParcelEntity entity = new ParcelEntity();
        entity.setName(request.name());
        entity.setWidth(maxWidth(request.shape()));
        entity.setHeight(request.shape().size());
        entity.setShape(serializeShape(normalizeShape(request.shape())));
        ParcelEntity saved = repository.save(entity);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ParcelResponse getByName(String name) {
        ParcelEntity entity = repository.findByName(name)
                .orElseThrow(() -> new IllegalArgumentException("Посылка '" + name + "' не найдена"));
        return toResponse(entity);
    }

    @Transactional
    public void deleteByName(String name) {
        ParcelEntity entity = repository.findByName(name)
                .orElseThrow(() -> new IllegalArgumentException("Посылка '" + name + "' не найдена"));
        repository.delete(entity);
    }

    @Transactional(readOnly = true)
    public Page<ParcelResponse> getAll(Pageable pageable) {
        return repository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<ru.hofftech.omni.shipping.entities.Package> resolvePackages(List<String> names) {
        List<ru.hofftech.omni.shipping.entities.Package> result = new ArrayList<>();
        for (String name : names) {
            ParcelEntity entity = repository.findByName(name)
                    .orElseThrow(() -> new IllegalArgumentException("Посылка '" + name + "' не найдена"));
            result.add(toDomain(entity));
        }
        return result;
    }

    private ParcelResponse toResponse(ParcelEntity entity) {
        List<String> shape = deserializeShape(entity.getShape());
        return new ParcelResponse(entity.getName(), entity.getWidth(), entity.getHeight(), shape);
    }

    private ru.hofftech.omni.shipping.entities.Package toDomain(ParcelEntity entity) {
        List<String> shape = deserializeShape(entity.getShape());
        return new ru.hofftech.omni.shipping.entities.Package(entity.getName(), entity.getWidth(), entity.getHeight(), shape);
    }

    private List<String> normalizeShape(List<String> shape) {
        if (shape == null || shape.isEmpty()) {
            throw new IllegalArgumentException("Форма посылки не может быть пустой");
        }
        int width = maxWidth(shape);
        return shape.stream()
                .map(line -> line + " ".repeat(Math.max(0, width - line.length())))
                .collect(Collectors.toList());
    }

    private int maxWidth(List<String> shape) {
        return shape.stream().mapToInt(String::length).max().orElse(0);
    }

    private String serializeShape(List<String> shape) {
        return String.join("\n", shape);
    }

    private List<String> deserializeShape(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return List.of(value.split("\\R", -1));
    }
}
