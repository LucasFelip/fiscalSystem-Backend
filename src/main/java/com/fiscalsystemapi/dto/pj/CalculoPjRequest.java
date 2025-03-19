package com.fiscalsystemapi.dto.pj;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CalculoPjRequest {
    private String numProcesso;
    private String nomeParteAutora;
    private String nomeParteRe;
    private BigDecimal valorBruto;
    private BigDecimal valorCorrigido;
    private String optanteSimples;
    private String ramoAtividade;
}
