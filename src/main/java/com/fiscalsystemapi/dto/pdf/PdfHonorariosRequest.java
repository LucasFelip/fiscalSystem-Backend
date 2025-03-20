package com.fiscalsystemapi.dto.pdf;

import com.fiscalsystemapi.dto.ProcessData;
import com.fiscalsystemapi.dto.honorarios.CalculoHonorariosResult;
import lombok.Data;

@Data
public class PdfHonorariosRequest {
    private ProcessData dados;
    private CalculoHonorariosResult resultado;
}
