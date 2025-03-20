package com.fiscalsystemapi.dto.pdf;

import com.fiscalsystemapi.dto.ProcessData;
import com.fiscalsystemapi.dto.fepa.CalculoFepaResult;
import lombok.Data;

@Data
public class PdfFepaRequest {
    private ProcessData dados;
    private CalculoFepaResult resultado;
}
