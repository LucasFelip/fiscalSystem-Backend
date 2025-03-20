package com.fiscalsystemapi.service;

import com.fiscalsystemapi.dto.rra.CalculoRraRequest;
import com.fiscalsystemapi.dto.rra.CalculoRraResult;
import com.fiscalsystemapi.entity.CalculoRealizado;
import com.fiscalsystemapi.entity.User;
import com.fiscalsystemapi.entity.enums.CalculationType;
import com.fiscalsystemapi.util.TaxCalculationUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

@Service
public class CalculoRraService {

    private final CalculoRegistroService calculoRegistroService;
    private final AuthService authService;
    private final ObjectMapper objectMapper;

    public CalculoRraService(CalculoRegistroService calculoRegistroService,
                             AuthService authService,
                             ObjectMapper objectMapper) {
        this.calculoRegistroService = calculoRegistroService;
        this.authService = authService;
        this.objectMapper = objectMapper;
    }

    /**
     * Realiza o cálculo do IR para RRA com base nos dados informados e registra o cálculo realizado.
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

        // Calcula a média mensal da base de cálculo
        BigDecimal mediaMensal = baseCalculo.divide(BigDecimal.valueOf(quantidadeMeses), 10, RoundingMode.HALF_UP);
        // Utiliza o utilitário para calcular o imposto progressivo com base na média mensal
        BigDecimal impostoMensal = TaxCalculationUtils.calcularImpostoProgressivo(mediaMensal);
        BigDecimal impostoTotal = impostoMensal.multiply(BigDecimal.valueOf(quantidadeMeses))
                .setScale(2, RoundingMode.HALF_UP);
        // Calcula a alíquota efetiva usando o utilitário
        BigDecimal aliquotaEfetiva = TaxCalculationUtils.calcularAliquotaEfetiva(impostoTotal, baseCalculo);
        BigDecimal valorLiquido = valorBrutoRPV.subtract(impostoTotal).setScale(2, RoundingMode.HALF_UP);
        mediaMensal = mediaMensal.setScale(2, RoundingMode.HALF_UP);

        CalculoRraResult result = CalculoRraResult.builder()
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
                .mediaMensal(mediaMensal)
                .build();

        // Registra o cálculo no banco de dados
        try {
            String resultadoJson = objectMapper.writeValueAsString(result);
            User usuarioLogado = authService.getLoggedUser();
            CalculoRealizado registro = CalculoRealizado.builder()
                    .numProcesso(numProcesso)
                    .tipoCalculo(CalculationType.RRA.getType())
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
}
