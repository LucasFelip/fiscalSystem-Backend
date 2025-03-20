package com.fiscalsystemapi.dto.pdf;

import lombok.Data;

@Data
public class PdfSignRequest {
    // PDF em formato Base64
    private String base64Pdf;
}
