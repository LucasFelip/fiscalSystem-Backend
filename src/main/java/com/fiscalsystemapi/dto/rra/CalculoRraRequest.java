package com.fiscalsystemapi.dto.rra;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CalculoRraRequest {
    private String numProcesso;
    private String nomeParteAutora;
    private String nomeParteRe;
    private Integer quantidadeMeses;
    private BigDecimal valorBruto;
    private BigDecimal baseCalculoIR;
}
