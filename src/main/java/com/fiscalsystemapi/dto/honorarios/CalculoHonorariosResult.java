package com.fiscalsystemapi.dto.honorarios;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CalculoHonorariosResult {
    private String numProcesso;
    private String solicitante;
    private String reu;
    private BigDecimal imposto;
    private BigDecimal liquido;
    private BigDecimal aliquotaEfetiva;
}
