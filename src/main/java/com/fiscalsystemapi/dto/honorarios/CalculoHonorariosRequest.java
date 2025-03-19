package com.fiscalsystemapi.dto.honorarios;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CalculoHonorariosRequest {
    private String numProcesso;
    private String solicitante;
    private String reu;
    private BigDecimal valorBruto;
}
