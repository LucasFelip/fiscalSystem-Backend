package com.fiscalsystemapi.service;

import com.fiscalsystemapi.util.FormatUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class PdfService {

    /**
     * Gera o PDF do relatório com base no tipo de cálculo, resultado e dados do processo.
     *
     * @param tipoIR   Tipo de cálculo (honorarios, fepa, rra ou pj).
     * @param resultado Objeto DTO contendo os resultados do cálculo.
     * @param dados     Objeto DTO contendo os dados do processo (número do processo, partes, etc).
     */
    public void gerarPDF(String tipoIR, Object resultado, Object dados) {
        PDDocument document = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        float margin = 20;
        float yPosition = page.getMediaBox().getHeight() - margin;
        float pageWidth = page.getMediaBox().getWidth();

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            // Título do Relatório
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
            // Centraliza aproximadamente o texto (ajuste conforme necessário)
            contentStream.newLineAtOffset((pageWidth - 200) / 2, yPosition);
            contentStream.showText("RELATÓRIO OFICIAL");
            contentStream.endText();

            yPosition -= 25;

            // Título do Cálculo
            String titulo;
            if ("honorarios".equalsIgnoreCase(tipoIR)) {
                titulo = "Cálculo de Imposto de Renda - Lei 14.848/2024";
            } else if ("fepa".equalsIgnoreCase(tipoIR)) {
                titulo = "Cálculo de RRA + FEPA - Lei 14.848/2024";
            } else if ("rra".equalsIgnoreCase(tipoIR)) {
                titulo = "Cálculo de RRA - Lei 14.848/2024";
            } else if ("pj".equalsIgnoreCase(tipoIR)) {
                titulo = "Cálculo de IR para Pessoa Jurídica - Lei 14.848/2024";
            } else {
                titulo = "Relatório";
            }

            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA, 12);
            contentStream.newLineAtOffset((pageWidth - 200) / 2, yPosition);
            contentStream.showText(titulo);
            contentStream.endText();

            yPosition -= 20;

            // Linha horizontal
            contentStream.moveTo(margin, yPosition);
            contentStream.lineTo(pageWidth - margin, yPosition);
            contentStream.stroke();
            yPosition -= 15;

            // Dados do Processo (obtidos via reflection)
            String numProcesso = getValue(dados, "getNumProcesso");
            String nomeParteAutora = getValue(dados, "getNomeParteAutora");
            String nomeParteRe = getValue(dados, "getNomeParteRe");

            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Dados do Processo:");
            contentStream.endText();
            yPosition -= 15;

            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA, 12);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Número do Processo: " + numProcesso);
            contentStream.endText();
            yPosition -= 15;

            contentStream.beginText();
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Nome da parte autora: " + nomeParteAutora);
            contentStream.endText();
            yPosition -= 15;

            contentStream.beginText();
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Nome da parte ré: " + nomeParteRe);
            contentStream.endText();
            yPosition -= 20;

            // Linha horizontal
            contentStream.moveTo(margin, yPosition);
            contentStream.lineTo(pageWidth - margin, yPosition);
            contentStream.stroke();
            yPosition -= 15;

            // Seção de Cálculo
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Cálculo:");
            contentStream.endText();
            yPosition -= 15;

            // Exemplo para o cálculo de honorários (os demais tipos seguem padrão similar)
            if ("honorarios".equalsIgnoreCase(tipoIR)) {
                // Utiliza reflection para obter os campos do resultado
                // Espera-se que o DTO possua métodos: getValorBruto(), getImposto(), getAliquotaEfetiva() e getLiquido()
                String valorBruto = FormatUtils.formatCurrency(new BigDecimal(getValue(resultado, "getValorBruto")), false);
                String imposto = FormatUtils.formatCurrency(new BigDecimal(getValue(resultado, "getImposto")), false);
                String aliquota = FormatUtils.formatCurrency(new BigDecimal(getValue(resultado, "getAliquotaEfetiva")), true);
                String liquido = FormatUtils.formatCurrency(new BigDecimal(getValue(resultado, "getLiquido")), false);

                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Valor Bruto do Pagamento: " + valorBruto);
                contentStream.endText();
                yPosition -= 15;

                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Imposto de Renda Calculado: " + imposto);
                contentStream.endText();
                yPosition -= 15;

                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Alíquota Efetiva: " + aliquota);
                contentStream.endText();
                yPosition -= 15;

                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Valor Líquido: " + liquido);
                contentStream.endText();
                yPosition -= 15;
            }
            // Para os demais tipos ("fepa", "rra", "pj"), implemente lógica similar,
            // obtendo os campos relevantes do DTO de resultado via reflection.

            yPosition -= 20;
            // Nota Explicativa
            String notaExplicativa = "";
            switch (tipoIR.toLowerCase()) {
                case "honorarios":
                    notaExplicativa = "Nota Explicativa: Dedução do IRRF, conforme a tabela progressiva contida na Lei nº 14.848/2024.";
                    break;
                case "fepa":
                    notaExplicativa = "Nota Explicativa: Base de Cálculo: Valor Bruto Corrigido (não incidindo juros); FEPA: 11% até 02/2020 e 7,5% a partir de 03/2020; IR (RRA): conforme Tabela Progressiva da Lei nº 14.848/2024.";
                    break;
                case "rra":
                    notaExplicativa = "Nota Explicativa: O cálculo do IR sobre RRA segue a tabela progressiva de tributação, levando em conta a média mensal dos valores acumulados e aplicando a dedução conforme legislação vigente (Lei nº 14.848/2024).";
                    break;
                case "pj":
                    notaExplicativa = "Nota Explicativa: O cálculo do IRPJ é baseado no regime de tributação da empresa. Empresas optantes pelo Simples Nacional estão isentas. Para demais empresas, aplicam-se as alíquotas conforme a atividade desempenhada.";
                    break;
                default:
                    notaExplicativa = "Relatório gerado.";
            }
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_OBLIQUE, 10);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText(notaExplicativa);
            contentStream.endText();
            yPosition -= 20;

            // Assinatura Digital (simulação)
            String assinatura = "Assinado digitalmente por [Nome do Usuário]";
            String crc = "CPF: [CPF do Usuário]";
            String dataHora = "Data e Hora: " + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date());

            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA, 10);
            contentStream.newLineAtOffset(margin, 40);
            contentStream.showText(assinatura);
            contentStream.endText();

            contentStream.beginText();
            contentStream.newLineAtOffset(margin, 25);
            contentStream.showText(crc);
            contentStream.endText();

            contentStream.beginText();
            contentStream.newLineAtOffset(margin, 10);
            contentStream.showText(dataHora);
            contentStream.endText();

        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            // Salva o PDF com nome composto pelo tipo e número do processo
            String fileName = "relatorio_" + tipoIR + "_" + numProcessoFromDados(dados) + ".pdf";
            document.save(fileName);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                document.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Método auxiliar para obter o valor de um método getter (por exemplo, "getNumProcesso")
     * utilizando reflection.
     */
    private String getValue(Object obj, String methodName) {
        try {
            Object value = obj.getClass().getMethod(methodName).invoke(obj);
            return value != null ? value.toString() : "";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * Extrai o número do processo do objeto dados utilizando reflection.
     */
    private String numProcessoFromDados(Object dados) {
        return getValue(dados, "getNumProcesso");
    }
}
