package com.fiscalsystemapi.repository;

import com.fiscalsystemapi.entity.CalculoRealizado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CalculoRealizadoRepository extends JpaRepository<CalculoRealizado, Long> {
    List<CalculoRealizado> findByNumProcessoContaining(String numProcesso);
}
