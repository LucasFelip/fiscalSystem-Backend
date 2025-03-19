package com.fiscalsystemapi.dto.pj;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CalculoPjResult {
    private String numProcesso;
    private String nomeParteAutora;
    private String nomeParteRe;
    private BigDecimal valorBrutoRPV;
    private BigDecimal valorCorrigidoRPV;
    private String optanteSimples;
    private String ramoAtividade;
    private BigDecimal aliquotaIR;
    private BigDecimal impostoIR;
    private BigDecimal valorLiquido;
}
