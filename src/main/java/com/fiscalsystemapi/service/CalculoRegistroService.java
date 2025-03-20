package com.fiscalsystemapi.service;

import com.fiscalsystemapi.entity.CalculoRealizado;
import com.fiscalsystemapi.repository.CalculoRealizadoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CalculoRegistroService {

    private final CalculoRealizadoRepository repository;

    public CalculoRegistroService(CalculoRealizadoRepository repository) {
        this.repository = repository;
    }

    /**
     * Salva um registro de cálculo realizado.
     *
     * @param calculo Registro do cálculo a ser salvo.
     * @return O registro salvo.
     */
    public CalculoRealizado salvarCalculo(CalculoRealizado calculo) {
        return repository.save(calculo);
    }

    /**
     * Busca os registros de cálculo cujo número do processo contenha a string informada.
     *
     * @param numProcesso Substring a ser buscada no número do processo.
     * @return Lista de registros encontrados.
     */
    public List<CalculoRealizado> buscarPorNumProcesso(String numProcesso) {
        return repository.findByNumProcessoContaining(numProcesso);
    }
}
