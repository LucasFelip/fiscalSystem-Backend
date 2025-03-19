package com.fiscalsystemapi.dto.rra;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CalculoRraResult {
    private String numProcesso;
    private String nomeParteAutora;
    private String nomeParteRe;
    private Integer quantidadeMeses;
    private BigDecimal valorBrutoRPV;
    private BigDecimal baseCalculo;
    private BigDecimal impostoTotal;
    private BigDecimal impostoMensal;
    private BigDecimal aliquotaEfetiva;
    private BigDecimal valorLiquido;
    private BigDecimal mediaMensal;
}
