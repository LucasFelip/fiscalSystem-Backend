package com.fiscalsystemapi.dto.pdf;

import com.fiscalsystemapi.dto.ProcessData;
import com.fiscalsystemapi.dto.rra.CalculoRraResult;
import lombok.Data;

@Data
public class PdfRraRequest {
    private ProcessData dados;
    private CalculoRraResult resultado;
}
