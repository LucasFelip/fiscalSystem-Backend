package com.fiscalsystemapi.dto.fepa;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CalculoFepaRequest {
    private String numProcesso;
    private String nomeParteAutora;
    private String nomeParteRe;
    private String periodoInicial;
    private String periodoFinal;
    private BigDecimal valorBruto;
}
