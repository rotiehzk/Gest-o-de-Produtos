package com.starterkit.springboot.produto;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface produtoRepository extends JpaRepository<produto, Long> {

    Optional<produto> findByCodigoUnico(String codigo);
}

