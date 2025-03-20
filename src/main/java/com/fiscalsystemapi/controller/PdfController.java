package com.fiscalsystemapi.controller;

import com.fiscalsystemapi.dto.pdf.PdfFepaRequest;
import com.fiscalsystemapi.dto.pdf.PdfHonorariosRequest;
import com.fiscalsystemapi.dto.pdf.PdfPjRequest;
import com.fiscalsystemapi.dto.pdf.PdfRraRequest;
import com.fiscalsystemapi.dto.pdf.PdfResponse;
import com.fiscalsystemapi.dto.pdf.PdfSignRequest;
import com.fiscalsystemapi.service.PdfFepaService;
import com.fiscalsystemapi.service.PdfHonorariosService;
import com.fiscalsystemapi.service.PdfPjService;
import com.fiscalsystemapi.service.PdfRraService;
import com.fiscalsystemapi.service.PdfSignatureService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;

@RestController
@RequestMapping("/pdf")
public class PdfController {

    private final PdfHonorariosService pdfHonorariosService;
    private final PdfFepaService pdfFepaService;
    private final PdfRraService pdfRraService;
    private final PdfPjService pdfPjService;
    private final PdfSignatureService pdfSignatureService;

    public PdfController(PdfHonorariosService pdfHonorariosService, PdfFepaService pdfFepaService,
                         PdfRraService pdfRraService, PdfPjService pdfPjService,
                         PdfSignatureService pdfSignatureService) {
        this.pdfHonorariosService = pdfHonorariosService;
        this.pdfFepaService = pdfFepaService;
        this.pdfRraService = pdfRraService;
        this.pdfPjService = pdfPjService;
        this.pdfSignatureService = pdfSignatureService;
    }

    // Endpoint para PDF de Honorários
    @PostMapping("/generate/honorarios")
    public ResponseEntity<PdfResponse> generatePdfHonorarios(@RequestBody PdfHonorariosRequest request) {
        byte[] pdfBytes = pdfHonorariosService.gerarPDFHonorarios(request.getResultado(), request.getDados());
        String base64Pdf = Base64.getEncoder().encodeToString(pdfBytes);
        PdfResponse response = PdfResponse.builder()
                .fileName("relatorio_honorarios_" + request.getDados().getNumProcesso() + ".pdf")
                .base64Pdf(base64Pdf)
                .build();
        return ResponseEntity.ok(response);
    }

    // Endpoint para PDF de FEPA
    @PostMapping("/generate/fepa")
    public ResponseEntity<PdfResponse> generatePdfFepa(@RequestBody PdfFepaRequest request) {
        byte[] pdfBytes = pdfFepaService.gerarPDFFepa(request.getResultado(), request.getDados());
        String base64Pdf = Base64.getEncoder().encodeToString(pdfBytes);
        PdfResponse response = PdfResponse.builder()
                .fileName("relatorio_fepa_" + request.getDados().getNumProcesso() + ".pdf")
                .base64Pdf(base64Pdf)
                .build();
        return ResponseEntity.ok(response);
    }

    // Endpoint para PDF de RRA
    @PostMapping("/generate/rra")
    public ResponseEntity<PdfResponse> generatePdfRra(@RequestBody PdfRraRequest request) {
        byte[] pdfBytes = pdfRraService.gerarPDFFRra(request.getResultado(), request.getDados());
        String base64Pdf = Base64.getEncoder().encodeToString(pdfBytes);
        PdfResponse response = PdfResponse.builder()
                .fileName("relatorio_rra_" + request.getDados().getNumProcesso() + ".pdf")
                .base64Pdf(base64Pdf)
                .build();
        return ResponseEntity.ok(response);
    }

    // Endpoint para PDF de PJ
    @PostMapping("/generate/pj")
    public ResponseEntity<PdfResponse> generatePdfPj(@RequestBody PdfPjRequest request) {
        byte[] pdfBytes = pdfPjService.gerarPDFPj(request.getResultado(), request.getDados());
        String base64Pdf = Base64.getEncoder().encodeToString(pdfBytes);
        PdfResponse response = PdfResponse.builder()
                .fileName("relatorio_pj_" + request.getDados().getNumProcesso() + ".pdf")
                .base64Pdf(base64Pdf)
                .build();
        return ResponseEntity.ok(response);
    }

    // Endpoint para assinatura (pode ser usado para qualquer tipo de PDF)
    @PostMapping("/sign")
    public ResponseEntity<PdfResponse> signPdf(
            @RequestBody PdfSignRequest signRequest) {
        byte[] pdfBytes = Base64.getDecoder().decode(signRequest.getBase64Pdf());
        try {
            byte[] signedPdfBytes = pdfSignatureService.signPdf(pdfBytes);
            String base64SignedPdf = Base64.getEncoder().encodeToString(signedPdfBytes);
            PdfResponse response = PdfResponse.builder()
                    .fileName("relatorio_signed.pdf")
                    .base64Pdf(base64SignedPdf)
                    .build();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
}
