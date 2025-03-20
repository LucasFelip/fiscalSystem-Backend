package com.fiscalsystemapi.dto.pdf;

import com.fiscalsystemapi.dto.ProcessData;
import com.fiscalsystemapi.dto.pj.CalculoPjResult;
import lombok.Data;

@Data
public class PdfPjRequest {
    private ProcessData dados;
    private CalculoPjResult resultado;
}
