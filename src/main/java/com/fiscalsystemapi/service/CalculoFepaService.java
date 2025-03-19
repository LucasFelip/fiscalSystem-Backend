package com.fiscalsystemapi.service;

import com.fiscalsystemapi.dto.fepa.CalculoFepaRequest;
import com.fiscalsystemapi.dto.fepa.CalculoFepaResult;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class CalculoFepaService {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yyyy");

    /**
     * Realiza o cálculo FEPA com base nos dados informados.
     *
     * @param request Objeto contendo os dados do processo (número, partes, período e valor bruto)
     * @return Resultado do cálculo FEPA com os campos calculados e os dados do processo.
     */
    public CalculoFepaResult calcular(CalculoFepaRequest request) {
        String numProcesso = request.getNumProcesso();
        String nomeParteAutora = request.getNomeParteAutora();
        String nomeParteRe = request.getNomeParteRe();
        String periodoInicial = request.getPeriodoInicial();
        String periodoFinal = request.getPeriodoFinal();
        BigDecimal valorBrutoRPV = request.getValorBruto();

        List<String> mesesLista = gerarListaMeses(periodoInicial, periodoFinal);
        int qtdMeses = mesesLista.size();
        if (qtdMeses == 0) {
            throw new IllegalArgumentException("Período inválido.");
        }

        BigDecimal fatorCorrecao = new BigDecimal("0.85");
        BigDecimal totalValorCorrigido = BigDecimal.ZERO;
        BigDecimal totalFEPA = BigDecimal.ZERO;

        BigDecimal valorPorMes = valorBrutoRPV.divide(BigDecimal.valueOf(qtdMeses), 10, RoundingMode.HALF_UP);

        for (String mesStr : mesesLista) {
            BigDecimal valorCorrigidoPorMes = valorPorMes.multiply(fatorCorrecao)
                    .setScale(2, RoundingMode.HALF_UP);
            totalValorCorrigido = totalValorCorrigido.add(valorCorrigidoPorMes);
            BigDecimal fepa = calcularFEPA(mesStr, valorCorrigidoPorMes);
            totalFEPA = totalFEPA.add(fepa);
        }
        totalValorCorrigido = totalValorCorrigido.setScale(2, RoundingMode.HALF_UP);
        totalFEPA = totalFEPA.setScale(2, RoundingMode.HALF_UP);

        IRResult irResult = calcularIRRRA(totalValorCorrigido, qtdMeses);
        BigDecimal ir = irResult.getImpostoTotal();
        BigDecimal aliquotaEfetiva = irResult.getAliquotaEfetiva();

        BigDecimal liquido = valorBrutoRPV.subtract(totalFEPA).subtract(ir)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal mediaMensal = totalValorCorrigido.divide(BigDecimal.valueOf(qtdMeses), 2, RoundingMode.HALF_UP);

        return CalculoFepaResult.builder()
                .numProcesso(numProcesso)
                .nomeParteAutora(nomeParteAutora)
                .nomeParteRe(nomeParteRe)
                .periodoInicial(periodoInicial)
                .periodoFinal(periodoFinal)
                .meses(qtdMeses)
                .valorBrutoRPV(valorBrutoRPV.setScale(2, RoundingMode.HALF_UP))
                .totalValorCorrigido(totalValorCorrigido)
                .totalFEPA(totalFEPA)
                .ir(ir)
                .aliquotaEfetiva(aliquotaEfetiva)
                .liquido(liquido)
                .mediaMensal(mediaMensal)
                .build();
    }

    /**
     * Gera uma lista de meses no formato MM/yyyy entre os períodos informados.
     */
    private List<String> gerarListaMeses(String periodoInicial, String periodoFinal) {
        YearMonth inicio = YearMonth.parse(periodoInicial, formatter);
        YearMonth fim = YearMonth.parse(periodoFinal, formatter);

        if (inicio.isAfter(fim)) {
            YearMonth temp = inicio;
            inicio = fim;
            fim = temp;
        }

        List<String> meses = new ArrayList<>();
        YearMonth atual = inicio;
        while (!atual.isAfter(fim)) {
            meses.add(atual.format(formatter));
            atual = atual.plusMonths(1);
        }
        return meses;
    }

    /**
     * Calcula o FEPA para um determinado mês/ano e o valor corrigido informado.
     * Aplica 11% se o ano for anterior a 2020 ou for 2020 com mês <= 2, caso contrário, 7.5%.
     *
     * @param mesAno         Mês e ano no formato "MM/yyyy".
     * @param valorCorrigido Valor corrigido para o mês.
     * @return FEPA calculado com 2 casas decimais.
     */
    private BigDecimal calcularFEPA(String mesAno, BigDecimal valorCorrigido) {
        String[] parts = mesAno.split("/");
        int mes = Integer.parseInt(parts[0]);
        int ano = Integer.parseInt(parts[1]);

        BigDecimal percentual;
        if (ano < 2020 || (ano == 2020 && mes <= 2)) {
            percentual = new BigDecimal("0.11");
        } else {
            percentual = new BigDecimal("0.075");
        }
        return valorCorrigido.multiply(percentual).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calcula o IR (imposto total) e a alíquota efetiva com base no total corrigido e quantidade de meses.
     *
     * @param totalValorCorrigido Valor total corrigido.
     * @param meses               Número de meses.
     * @return Um objeto IRResult contendo o imposto total e a alíquota efetiva.
     */
    private IRResult calcularIRRRA(BigDecimal totalValorCorrigido, int meses) {
        int adjustedMeses = Math.max(meses, 1);
        BigDecimal media = totalValorCorrigido.divide(BigDecimal.valueOf(adjustedMeses), 10, RoundingMode.HALF_UP);
        BigDecimal impostoMensal = BigDecimal.ZERO;

        BigDecimal limite1 = new BigDecimal("2259.20");
        BigDecimal limite2 = new BigDecimal("2826.65");
        BigDecimal limite3 = new BigDecimal("3751.05");
        BigDecimal limite4 = new BigDecimal("4664.68");

        if (media.compareTo(limite1) > 0) {
            if (media.compareTo(limite2) <= 0) {
                impostoMensal = media.multiply(new BigDecimal("0.075"))
                        .subtract(new BigDecimal("169.44"));
            } else if (media.compareTo(limite3) <= 0) {
                impostoMensal = media.multiply(new BigDecimal("0.15"))
                        .subtract(new BigDecimal("381.44"));
            } else if (media.compareTo(limite4) <= 0) {
                impostoMensal = media.multiply(new BigDecimal("0.225"))
                        .subtract(new BigDecimal("662.77"));
            } else {
                impostoMensal = media.multiply(new BigDecimal("0.275"))
                        .subtract(new BigDecimal("896"));
            }
        }

        if (impostoMensal.compareTo(BigDecimal.ZERO) < 0) {
            impostoMensal = BigDecimal.ZERO;
        }

        BigDecimal impostoTotal = impostoMensal.multiply(BigDecimal.valueOf(adjustedMeses))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal aliquotaEfetiva = BigDecimal.ZERO;
        if (totalValorCorrigido.compareTo(BigDecimal.ZERO) > 0) {
            aliquotaEfetiva = impostoTotal.divide(totalValorCorrigido, 10, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        return new IRResult(impostoTotal, aliquotaEfetiva);
    }

    /**
     * Classe auxiliar para encapsular o resultado do cálculo do IR.
     */
    private static class IRResult {
        private final BigDecimal impostoTotal;
        private final BigDecimal aliquotaEfetiva;

        public IRResult(BigDecimal impostoTotal, BigDecimal aliquotaEfetiva) {
            this.impostoTotal = impostoTotal;
            this.aliquotaEfetiva = aliquotaEfetiva;
        }

        public BigDecimal getImpostoTotal() {
            return impostoTotal;
        }

        public BigDecimal getAliquotaEfetiva() {
            return aliquotaEfetiva;
        }
    }
}
