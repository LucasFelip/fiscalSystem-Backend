package com.fiscalsystemapi.service;

import com.fiscalsystemapi.entity.User;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
public class PdfSignatureService {
    private final AuthService authService;

    public PdfSignatureService(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Valida se o documento foi gerado pela API.
     * Assume que o PDF gerado pela API possui o metadado "API_GENERATED" definido como "true".
     *
     * @param pdfBytes PDF em formato byte[]
     * @return true se o documento foi gerado pela API; false caso contrário.
     * @throws IOException Se ocorrer erro ao ler o PDF.
     */
    public boolean validateGeneratedByApi(byte[] pdfBytes) throws IOException {
        try (PDDocument document = PDDocument.load(new ByteArrayInputStream(pdfBytes))) {
            PDDocumentInformation info = document.getDocumentInformation();
            String apiGenerated = info.getCustomMetadataValue("API_GENERATED");
            return apiGenerated != null && apiGenerated.equalsIgnoreCase("true");
        }
    }

    /**
     * Valida se o documento já foi assinado pela API.
     * Verifica se o metadado "SIGNED_BY_API" está definido como "true".
     *
     * @param pdfBytes PDF em formato byte[]
     * @return true se o documento já foi assinado; false caso contrário.
     * @throws IOException Se ocorrer erro ao ler o PDF.
     */
    public boolean validateSignedByApi(byte[] pdfBytes) throws IOException {
        try (PDDocument document = PDDocument.load(new ByteArrayInputStream(pdfBytes))) {
            PDDocumentInformation info = document.getDocumentInformation();
            String signed = info.getCustomMetadataValue("SIGNED_BY_API");
            return signed != null && signed.equalsIgnoreCase("true");
        }
    }

    /**
     * Assina o documento PDF.
     * Antes de assinar, valida se o documento foi gerado pela API e se ainda não foi assinado.
     * A assinatura é inserida de forma visível em cada página (no canto inferior direito),
     * e os metadados são atualizados para indicar a assinatura.
     *
     * @param pdfBytes      PDF original em formato byte[]
     * @return PDF assinado em formato byte[]
     * @throws Exception Caso o documento não seja válido para assinatura ou ocorra algum erro.
     */
    public byte[] signPdf(byte[] pdfBytes) throws Exception {
        try (PDDocument document = PDDocument.load(new ByteArrayInputStream(pdfBytes))) {
            PDDocumentInformation info = document.getDocumentInformation();

            User signer = authService.getLoggedUser();
            String signerName = signer.getNomeCompleto();
            String signerId = signer.getCpf();
            // Adiciona a assinatura visual em cada página
            PDPageTree pages = document.getDocumentCatalog().getPages();
            for (PDPage page : pages) {
                try (PDPageContentStream contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true)) {
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10);
                    // Posiciona a assinatura no canto inferior direito
                    float margin = 20;
                    float x = page.getMediaBox().getWidth() - margin - 200; // Ajuste a largura conforme necessário
                    float y = margin;
                    contentStream.newLineAtOffset(x, y);
                    String signatureText = "Assinado por " + signerName + " (" + signerId + ")";
                    contentStream.showText(signatureText);
                    contentStream.endText();
                }
            }

            // Atualiza os metadados para indicar que o documento foi assinado
            info.setCustomMetadataValue("SIGNED_BY_API", "true");
            String signatureDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date());
            info.setCustomMetadataValue("SIGNATURE_DATE", signatureDate);
            document.setDocumentInformation(info);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new Exception("Erro ao assinar o PDF: " + e.getMessage(), e);
        }
    }
}
