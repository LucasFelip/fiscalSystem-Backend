package com.fiscalsystemapi.dto.fepa;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CalculoFepaResult {
    private String numProcesso;
    private String nomeParteAutora;
    private String nomeParteRe;
    private String periodoInicial;
    private String periodoFinal;
    private Integer meses;
    private BigDecimal valorBrutoRPV;
    private BigDecimal totalValorCorrigido;
    private BigDecimal totalFEPA;
    private BigDecimal ir;
    private BigDecimal aliquotaEfetiva;
    private BigDecimal liquido;
    private BigDecimal mediaMensal;
}
