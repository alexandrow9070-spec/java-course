package ru.hofftech.omni.shipping.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ParcelJpaRepository extends JpaRepository<ParcelEntity, Long> {
    Optional<ParcelEntity> findByName(String name);

    void deleteByName(String name);
}
