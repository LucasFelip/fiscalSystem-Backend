package com.fiscalsystemapi.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class TaxCalculationUtils {

    // Limites para as faixas progressivas
    public static final BigDecimal LIMITE1 = new BigDecimal("2259.20");
    public static final BigDecimal LIMITE2 = new BigDecimal("2826.65");
    public static final BigDecimal LIMITE3 = new BigDecimal("3751.05");
    public static final BigDecimal LIMITE4 = new BigDecimal("4664.68");

    // Alíquotas e deduções para as faixas
    public static final BigDecimal ALIQUOTA1 = new BigDecimal("0.075");
    public static final BigDecimal DEDUCAO1 = new BigDecimal("169.44");

    public static final BigDecimal ALIQUOTA2 = new BigDecimal("0.15");
    public static final BigDecimal DEDUCAO2 = new BigDecimal("381.44");

    public static final BigDecimal ALIQUOTA3 = new BigDecimal("0.225");
    public static final BigDecimal DEDUCAO3 = new BigDecimal("662.77");

    public static final BigDecimal ALIQUOTA4 = new BigDecimal("0.275");
    public static final BigDecimal DEDUCAO4 = new BigDecimal("896");

    /**
     * Calcula o imposto progressivo com base na base informada.
     * Esse método é utilizado em cálculos de honorários, RRA e FEPA.
     *
     * @param base Valor sobre o qual o imposto será calculado.
     * @return Valor do imposto com 2 casas decimais.
     */
    public static BigDecimal calcularImpostoProgressivo(BigDecimal base) {
        if (base.compareTo(LIMITE1) <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        } else if (base.compareTo(LIMITE2) <= 0) {
            return base.multiply(ALIQUOTA1).subtract(DEDUCAO1).setScale(2, RoundingMode.HALF_UP);
        } else if (base.compareTo(LIMITE3) <= 0) {
            return base.multiply(ALIQUOTA2).subtract(DEDUCAO2).setScale(2, RoundingMode.HALF_UP);
        } else if (base.compareTo(LIMITE4) <= 0) {
            return base.multiply(ALIQUOTA3).subtract(DEDUCAO3).setScale(2, RoundingMode.HALF_UP);
        } else {
            return base.multiply(ALIQUOTA4).subtract(DEDUCAO4).setScale(2, RoundingMode.HALF_UP);
        }
    }

    /**
     * Calcula a alíquota efetiva, ou seja, a porcentagem que o imposto representa sobre a base.
     *
     * @param impostoTotal Valor total do imposto.
     * @param base Base de cálculo.
     * @return Alíquota efetiva em porcentagem, com 2 casas decimais.
     */
    public static BigDecimal calcularAliquotaEfetiva(BigDecimal impostoTotal, BigDecimal base) {
        if (base.compareTo(BigDecimal.ZERO) > 0) {
            return impostoTotal.divide(base, 10, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"))
                    .setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }
}
