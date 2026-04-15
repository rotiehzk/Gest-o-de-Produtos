package com.starterkit.springboot.phone;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface phoneRepository extends JpaRepository<phone, Long> {

    Optional<phone> findByCodigoUnico(String codigo);
}

