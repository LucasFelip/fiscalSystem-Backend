package com.fiscalsystemapi.dto.pdf;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PdfResponse {
    private String fileName;
    private String base64Pdf;
}
