package com.fiscalsystemapi.service;

import com.fiscalsystemapi.dto.honorarios.CalculoHonorariosRequest;
import com.fiscalsystemapi.dto.honorarios.CalculoHonorariosResult;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class CalculoHonorariosService {

    /**
     * Realiza o cálculo do imposto e do valor líquido para honorários,
     * utilizando as faixas definidas e retornando os dados do processo.
     *
     * @param request Objeto que contém numProcesso, solicitante, réu e valorBruto.
     * @return Resultado do cálculo, incluindo imposto, valor líquido, alíquota efetiva e dados do processo.
     */
    public CalculoHonorariosResult calcular(CalculoHonorariosRequest request) {
        BigDecimal valorBruto = request.getValorBruto();
        BigDecimal imposto;

        BigDecimal limite1 = new BigDecimal("2259.20");
        BigDecimal limite2 = new BigDecimal("2826.65");
        BigDecimal limite3 = new BigDecimal("3751.05");
        BigDecimal limite4 = new BigDecimal("4664.68");

        if (valorBruto.compareTo(limite1) <= 0) {
            imposto = BigDecimal.ZERO;
        } else if (valorBruto.compareTo(limite2) <= 0) {
            imposto = valorBruto.multiply(new BigDecimal("0.075"))
                    .subtract(new BigDecimal("169.44"));
        } else if (valorBruto.compareTo(limite3) <= 0) {
            imposto = valorBruto.multiply(new BigDecimal("0.15"))
                    .subtract(new BigDecimal("381.44"));
        } else if (valorBruto.compareTo(limite4) <= 0) {
            imposto = valorBruto.multiply(new BigDecimal("0.225"))
                    .subtract(new BigDecimal("662.77"));
        } else {
            imposto = valorBruto.multiply(new BigDecimal("0.275"))
                    .subtract(new BigDecimal("896"));
        }

        if (imposto.compareTo(BigDecimal.ZERO) < 0) {
            imposto = BigDecimal.ZERO;
        }

        BigDecimal liquido = valorBruto.subtract(imposto);
        BigDecimal aliquotaEfetiva = BigDecimal.ZERO;
        if (valorBruto.compareTo(BigDecimal.ZERO) != 0) {
            aliquotaEfetiva = imposto.divide(valorBruto, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
        }

        return CalculoHonorariosResult.builder()
                .numProcesso(request.getNumProcesso())
                .solicitante(request.getSolicitante())
                .reu(request.getReu())
                .imposto(imposto.setScale(2, RoundingMode.HALF_UP))
                .liquido(liquido.setScale(2, RoundingMode.HALF_UP))
                .aliquotaEfetiva(aliquotaEfetiva.setScale(2, RoundingMode.HALF_UP))
                .build();
    }
}
