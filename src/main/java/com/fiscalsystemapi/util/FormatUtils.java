package com.fiscalsystemapi.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class FormatUtils {

    /**
     * Formata um valor numérico para o padrão de moeda brasileiro.
     * Se for porcentagem, formata o número com duas casas decimais e adiciona o símbolo "%".
     *
     * @param value     O valor a ser formatado.
     * @param isPercent Se verdadeiro, formata como percentual; caso contrário, como moeda.
     * @return String formatada.
     */
    public static String formatCurrency(BigDecimal value, boolean isPercent) {
        if (value == null) {
            return "";
        }
        if (isPercent) {
            NumberFormat numberFormat = NumberFormat.getNumberInstance(new Locale("pt", "BR"));
            numberFormat.setMinimumFractionDigits(2);
            numberFormat.setMaximumFractionDigits(2);
            return numberFormat.format(value) + "%";
        } else {
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
            return currencyFormat.format(value);
        }
    }

    /**
     * Formata o número do processo para o padrão esperado.
     * Se o número contiver exatamente 20 dígitos, formata-o no padrão:
     * "XXXXXXX-XX.XXXX.X.XX.XXXX".
     *
     * @param num Número do processo.
     * @return Número formatado ou o valor original se não atender a regra.
     */
    public static String formatProcessNumber(String num) {
        if (num == null) {
            return "";
        }
        String digits = num.replaceAll("\\D", "");
        if (digits.length() == 20) {
            String part1 = digits.substring(0, 7);
            String part2 = digits.substring(7, 9);
            String part3 = digits.substring(9, 13);
            String part4 = digits.substring(13, 14);
            String part5 = digits.substring(14, 16);
            String part6 = digits.substring(16, 20);
            return String.format("%s-%s.%s.%s.%s.%s", part1, part2, part3, part4, part5, part6);
        }
        return num;
    }

    /**
     * Formata o CPF para o padrão brasileiro: "XXX.XXX.XXX-XX".
     *
     * @param cpf O CPF a ser formatado.
     * @return CPF formatado ou "CPF N/A" se o CPF estiver vazio ou nulo.
     */
    public static String formatCPF(String cpf) {
        if (cpf == null || cpf.trim().isEmpty()) {
            return "CPF N/A";
        }
        String digits = cpf.replaceAll("\\D", "");
        if (digits.length() == 11) {
            return String.format("%s.%s.%s-%s",
                    digits.substring(0, 3),
                    digits.substring(3, 6),
                    digits.substring(6, 9),
                    digits.substring(9, 11));
        }
        return cpf;
    }
}
