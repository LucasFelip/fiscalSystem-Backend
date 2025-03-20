package com.fiscalsystemapi.service;

import com.fiscalsystemapi.dto.fepa.CalculoFepaRequest;
import com.fiscalsystemapi.dto.fepa.CalculoFepaResult;
import com.fiscalsystemapi.entity.CalculoRealizado;
import com.fiscalsystemapi.entity.User;
import com.fiscalsystemapi.entity.enums.CalculationType;
import com.fiscalsystemapi.util.TaxCalculationUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class CalculoFepaService {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yyyy");

    private final CalculoRegistroService calculoRegistroService;
    private final AuthService authService;
    private final ObjectMapper objectMapper;

    public CalculoFepaService(CalculoRegistroService calculoRegistroService,
                              AuthService authService,
                              ObjectMapper objectMapper) {
        this.calculoRegistroService = calculoRegistroService;
        this.authService = authService;
        this.objectMapper = objectMapper;
    }

    /**
     * Realiza o cálculo FEPA com base nos dados informados e registra o cálculo realizado.
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

        // Gera a lista de meses no período
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
            // O cálculo FEPA é específico: 11% se ano < 2020 ou (ano==2020 e mês <=2), senão 7,5%
            BigDecimal fepa = calcularFEPA(mesStr, valorCorrigidoPorMes);
            totalFEPA = totalFEPA.add(fepa);
        }
        totalValorCorrigido = totalValorCorrigido.setScale(2, RoundingMode.HALF_UP);
        totalFEPA = totalFEPA.setScale(2, RoundingMode.HALF_UP);

        // Cálculo do IR:
        // Calcula a média mensal da base corrigida e usa o utilitário para obter o imposto progressivo
        BigDecimal adjustedMeses = BigDecimal.valueOf(qtdMeses);
        BigDecimal mediaMensal = totalValorCorrigido.divide(adjustedMeses, 10, RoundingMode.HALF_UP);
        BigDecimal impostoMensal = TaxCalculationUtils.calcularImpostoProgressivo(mediaMensal);
        BigDecimal impostoTotal = impostoMensal.multiply(adjustedMeses).setScale(2, RoundingMode.HALF_UP);
        BigDecimal aliquotaEfetiva = TaxCalculationUtils.calcularAliquotaEfetiva(impostoTotal, totalValorCorrigido);

        BigDecimal liquido = valorBrutoRPV.subtract(totalFEPA).subtract(impostoTotal)
                .setScale(2, RoundingMode.HALF_UP);
        mediaMensal = mediaMensal.setScale(2, RoundingMode.HALF_UP);

        CalculoFepaResult result = CalculoFepaResult.builder()
                .numProcesso(numProcesso)
                .nomeParteAutora(nomeParteAutora)
                .nomeParteRe(nomeParteRe)
                .periodoInicial(periodoInicial)
                .periodoFinal(periodoFinal)
                .meses(qtdMeses)
                .valorBrutoRPV(valorBrutoRPV.setScale(2, RoundingMode.HALF_UP))
                .totalValorCorrigido(totalValorCorrigido)
                .totalFEPA(totalFEPA)
                .ir(impostoTotal)
                .aliquotaEfetiva(aliquotaEfetiva)
                .liquido(liquido)
                .mediaMensal(mediaMensal)
                .build();

        try {
            String resultadoJson = objectMapper.writeValueAsString(result);
            User usuarioLogado = authService.getLoggedUser();
            CalculoRealizado registro = CalculoRealizado.builder()
                    .numProcesso(numProcesso)
                    .tipoCalculo(CalculationType.FEPA.getType())
                    .resultadoJson(resultadoJson)
                    .usuario(usuarioLogado)
                    .dataGeracao(new Date())
                    .build();
            calculoRegistroService.salvarCalculo(registro);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
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
     * Aplica 11% se o ano for anterior a 2020 ou for 2020 com mês <= 2; caso contrário, 7,5%.
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
}
