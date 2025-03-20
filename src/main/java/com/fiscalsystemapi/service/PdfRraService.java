package com.fiscalsystemapi.service;

import com.fiscalsystemapi.dto.ProcessData;
import com.fiscalsystemapi.dto.rra.CalculoRraResult;
import com.fiscalsystemapi.entity.User;
import com.fiscalsystemapi.service.PdfSignatureService;
import com.fiscalsystemapi.util.FormatUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class PdfRraService {

    private final PdfSignatureService pdfSignatureService;
    private final AuthService authService;

    public PdfRraService(PdfSignatureService pdfSignatureService, AuthService authService) {
        this.pdfSignatureService = pdfSignatureService;
        this.authService = authService;
    }

    /**
     * Gera o PDF para o cálculo RRA com base nos resultados e dados do processo.
     * Após gerar o PDF, tenta assiná-lo; se a assinatura falhar, retorna o PDF sem assinatura.
     *
     * @param resultado Objeto CalculoRraResult contendo os resultados do cálculo RRA.
     * @param dados     Objeto ProcessData contendo os dados do processo.
     * @return PDF final (assinado, se possível) em formato byte[].
     */
    public byte[] gerarPDFFRra(CalculoRraResult resultado, ProcessData dados) {
        PDDocument document = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        // Define metadado que indica que o PDF foi gerado pela API
        PDDocumentInformation info = document.getDocumentInformation();
        info.setCustomMetadataValue("API_GENERATED", "true");
        document.setDocumentInformation(info);

        float margin = 20;
        float yPosition = page.getMediaBox().getHeight() - margin;
        float pageWidth = page.getMediaBox().getWidth();

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            // Cabeçalho
            yPosition = writeHeader(contentStream, pageWidth, yPosition, margin);
            // Dados do processo
            yPosition = writeProcessData(contentStream, dados, margin, yPosition);
            // Seção específica para RRA
            yPosition = writeRraSection(contentStream, resultado, margin, yPosition);
            // Nota Explicativa
            yPosition = writeExplanatoryNote(contentStream, pageWidth, margin, yPosition);
        } catch (IOException e) {
            e.printStackTrace();
        }

        byte[] unsignedPdf = saveDocument(document);

        // Tenta assinar o PDF; se ocorrer erro, retorna o PDF sem assinatura
        byte[] finalPdf = unsignedPdf;
        try {
            finalPdf = pdfSignatureService.signPdf(unsignedPdf);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return finalPdf;
    }

    // Cabeçalho: título principal e subtítulo para RRA
    private float writeHeader(PDPageContentStream contentStream, float pageWidth, float yPosition, float margin) throws IOException {
        // Título principal
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
        contentStream.newLineAtOffset((pageWidth - 200) / 2, yPosition);
        contentStream.showText("RELATÓRIO OFICIAL");
        contentStream.endText();
        yPosition -= 25;

        // Subtítulo para cálculo RRA
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA, 12);
        contentStream.newLineAtOffset((pageWidth - 200) / 2, yPosition);
        contentStream.showText("Cálculo de RRA - Lei 14.848/2024");
        contentStream.endText();
        yPosition -= 20;
        return yPosition - 15;
    }

    // Dados do processo
    private float writeProcessData(PDPageContentStream contentStream, ProcessData dados, float margin, float yPosition) throws IOException {
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
        contentStream.newLineAtOffset(margin, yPosition);
        contentStream.showText("Dados do Processo:");
        contentStream.endText();
        yPosition -= 15;

        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA, 12);
        contentStream.newLineAtOffset(margin, yPosition);
        contentStream.showText("Número do Processo: " + dados.getNumProcesso());
        contentStream.endText();
        yPosition -= 15;

        contentStream.beginText();
        contentStream.newLineAtOffset(margin, yPosition);
        contentStream.showText("Nome da parte autora: " + dados.getNomeParteAutora());
        contentStream.endText();
        yPosition -= 15;

        contentStream.beginText();
        contentStream.newLineAtOffset(margin, yPosition);
        contentStream.showText("Nome da parte ré: " + dados.getNomeParteRe());
        contentStream.endText();
        yPosition -= 20;
        return yPosition - 15;
    }

    // Seção específica para RRA
    private float writeRraSection(PDPageContentStream contentStream, CalculoRraResult resultado, float margin, float yPosition) throws IOException {
        String quantidadeMeses = resultado.getQuantidadeMeses().toString();
        String valorBrutoRPV = FormatUtils.formatCurrency(resultado.getValorBrutoRPV(), false);
        String baseCalculo = FormatUtils.formatCurrency(resultado.getBaseCalculo(), false);
        String mediaMensal = FormatUtils.formatCurrency(resultado.getMediaMensal(), false);
        String impostoMensal = FormatUtils.formatCurrency(resultado.getImpostoMensal(), false);
        String impostoTotal = FormatUtils.formatCurrency(resultado.getImpostoTotal(), false);
        String aliquota = FormatUtils.formatCurrency(resultado.getAliquotaEfetiva(), true);
        String valorLiquido = FormatUtils.formatCurrency(resultado.getValorLiquido(), false);

        yPosition = writeTextLine(contentStream, "Quantidade de meses (RRA): " + quantidadeMeses, margin, yPosition);
        yPosition = writeTextLine(contentStream, "Valor Bruto do RPV: " + valorBrutoRPV, margin, yPosition);
        yPosition = writeTextLine(contentStream, "Base de Cálculo do IR: " + baseCalculo, margin, yPosition);
        yPosition = writeTextLine(contentStream, "Média Mensal: " + mediaMensal, margin, yPosition);
        yPosition = writeTextLine(contentStream, "Imposto Mensal: " + impostoMensal, margin, yPosition);
        yPosition = writeTextLine(contentStream, "Imposto Total: " + impostoTotal, margin, yPosition);
        yPosition = writeTextLine(contentStream, "Alíquota Efetiva: " + aliquota, margin, yPosition);
        return writeTextLine(contentStream, "Valor Líquido: " + valorLiquido, margin, yPosition);
    }

    // Nota Explicativa
    private float writeExplanatoryNote(PDPageContentStream contentStream, float pageWidth, float margin, float yPosition) throws IOException {
        String notaExplicativa = "Nota Explicativa: O cálculo do IR sobre RRA segue a tabela progressiva de tributação, levando em conta a média mensal dos valores acumulados e aplicando a dedução conforme legislação vigente (Lei nº 14.848/2024).";
        String[] notaLines = notaExplicativa.split("\n");
        for (String line : notaLines) {
            yPosition = writeTextLine(contentStream, line, margin, yPosition);
        }
        return yPosition - 5;
    }

    // Método auxiliar para escrever uma linha de texto e retornar a nova posição vertical
    private float writeTextLine(PDPageContentStream contentStream, String text, float margin, float yPosition) throws IOException {
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA, 12);
        contentStream.newLineAtOffset(margin, yPosition);
        contentStream.showText(text);
        contentStream.endText();
        return yPosition - 15;
    }

    // Salva o documento e retorna os bytes
    private byte[] saveDocument(PDDocument document) {
        byte[] pdfBytes = null;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            document.save(baos);
            pdfBytes = baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                document.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return pdfBytes;
    }
}
