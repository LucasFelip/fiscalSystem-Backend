package com.fiscalsystemapi.entity.enums;

public enum CalculationType {

    HONORARIOS("honorarios"),
    FEPA("fepa"),
    RRA("rra"),
    PJ("pj");

    private final String type;

    CalculationType(String type) {
        this.type = type;
    }

    /**
     * Retorna o valor em String associado a este tipo de cálculo.
     *
     * @return Tipo de cálculo em String.
     */
    public String getType() {
        return type;
    }

    /**
     * Converte uma String para o enum correspondente.
     *
     * @param text Valor em String.
     * @return CalculationType correspondente.
     * @throws IllegalArgumentException Caso não exista um enum com o valor informado.
     */
    public static CalculationType fromString(String text) {
        for (CalculationType ct : CalculationType.values()) {
            if (ct.type.equalsIgnoreCase(text)) {
                return ct;
            }
        }
        throw new IllegalArgumentException("Tipo de cálculo inválido: " + text);
    }
}
