package com.fiscalsystemapi.service;

import com.fiscalsystemapi.dto.rra.CalculoRraRequest;
import com.fiscalsystemapi.dto.rra.CalculoRraResult;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class CalculoRraService {

    /**
     * Realiza o cálculo do IR para RRA com base nos dados informados.
     *
     * @param request Objeto contendo numProcesso, nomeParteAutora, nomeParteRe, quantidadeMeses, valorBruto e baseCalculoIR.
     * @return Objeto contendo os dados do processo e os valores calculados (imposto total, imposto mensal, alíquota efetiva, valor líquido e média mensal).
     */
    public CalculoRraResult calcular(CalculoRraRequest request) {
        String numProcesso = request.getNumProcesso();
        String nomeParteAutora = request.getNomeParteAutora();
        String nomeParteRe = request.getNomeParteRe();
        Integer quantidadeMeses = request.getQuantidadeMeses();
        BigDecimal valorBruto = request.getValorBruto();
        BigDecimal baseCalculoIR = request.getBaseCalculoIR();

        if (quantidadeMeses == null || quantidadeMeses < 1) {
            throw new IllegalArgumentException("A quantidade de meses deve ser pelo menos 1.");
        }
        if (valorBruto == null || baseCalculoIR == null || baseCalculoIR.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Os valores inseridos são inválidos.");
        }

        BigDecimal valorBrutoRPV = valorBruto.setScale(2, RoundingMode.HALF_UP);
        BigDecimal baseCalculo = baseCalculoIR.setScale(2, RoundingMode.HALF_UP);

        BigDecimal mediaMensal = baseCalculo.divide(BigDecimal.valueOf(quantidadeMeses), 10, RoundingMode.HALF_UP);
        BigDecimal impostoMensal = BigDecimal.ZERO;

        BigDecimal limite1 = new BigDecimal("2259.20");
        BigDecimal limite2 = new BigDecimal("2826.65");
        BigDecimal limite3 = new BigDecimal("3751.05");
        BigDecimal limite4 = new BigDecimal("4664.68");

        if (mediaMensal.compareTo(limite1) > 0) {
            if (mediaMensal.compareTo(limite2) <= 0) {
                impostoMensal = mediaMensal.multiply(new BigDecimal("0.075"))
                        .subtract(new BigDecimal("169.44"));
            } else if (mediaMensal.compareTo(limite3) <= 0) {
                impostoMensal = mediaMensal.multiply(new BigDecimal("0.15"))
                        .subtract(new BigDecimal("381.44"));
            } else if (mediaMensal.compareTo(limite4) <= 0) {
                impostoMensal = mediaMensal.multiply(new BigDecimal("0.225"))
                        .subtract(new BigDecimal("662.77"));
            } else {
                impostoMensal = mediaMensal.multiply(new BigDecimal("0.275"))
                        .subtract(new BigDecimal("896"));
            }
        }
        if (impostoMensal.compareTo(BigDecimal.ZERO) < 0) {
            impostoMensal = BigDecimal.ZERO;
        }

        BigDecimal impostoTotal = impostoMensal.multiply(BigDecimal.valueOf(quantidadeMeses))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal aliquotaEfetiva = BigDecimal.ZERO;
        if (baseCalculo.compareTo(BigDecimal.ZERO) > 0) {
            aliquotaEfetiva = impostoTotal.divide(baseCalculo, 10, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"))
                    .setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal valorLiquido = valorBrutoRPV.subtract(impostoTotal)
                .setScale(2, RoundingMode.HALF_UP);

        return CalculoRraResult.builder()
                .numProcesso(numProcesso)
                .nomeParteAutora(nomeParteAutora)
                .nomeParteRe(nomeParteRe)
                .quantidadeMeses(quantidadeMeses)
                .valorBrutoRPV(valorBrutoRPV)
                .baseCalculo(baseCalculo)
                .impostoMensal(impostoMensal.setScale(2, RoundingMode.HALF_UP))
                .impostoTotal(impostoTotal)
                .aliquotaEfetiva(aliquotaEfetiva)
                .valorLiquido(valorLiquido)
                .mediaMensal(mediaMensal.setScale(2, RoundingMode.HALF_UP))
                .build();
    }
}
