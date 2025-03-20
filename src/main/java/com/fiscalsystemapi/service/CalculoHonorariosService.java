package com.fiscalsystemapi.service;

import com.fiscalsystemapi.dto.honorarios.CalculoHonorariosRequest;
import com.fiscalsystemapi.dto.honorarios.CalculoHonorariosResult;
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
public class CalculoHonorariosService {

    private final CalculoRegistroService calculoRegistroService;
    private final AuthService authService;
    private final ObjectMapper objectMapper;

    public CalculoHonorariosService(CalculoRegistroService calculoRegistroService,
                                    AuthService authService,
                                    ObjectMapper objectMapper) {
        this.calculoRegistroService = calculoRegistroService;
        this.authService = authService;
        this.objectMapper = objectMapper;
    }

    /**
     * Realiza o cálculo do imposto e do valor líquido para honorários,
     * propagando os dados do processo e registrando o cálculo realizado no banco de dados.
     *
     * @param request Objeto que contém numProcesso, solicitante, réu e valorBruto.
     * @return Resultado do cálculo, incluindo imposto, valor líquido, alíquota efetiva e dados do processo.
     */
    public CalculoHonorariosResult calcular(CalculoHonorariosRequest request) {
        BigDecimal valorBruto = request.getValorBruto();

        BigDecimal imposto = TaxCalculationUtils.calcularImpostoProgressivo(valorBruto);

        BigDecimal liquido = valorBruto.subtract(imposto).setScale(2, RoundingMode.HALF_UP);

        BigDecimal aliquotaEfetiva = TaxCalculationUtils.calcularAliquotaEfetiva(imposto, valorBruto);

        CalculoHonorariosResult result = CalculoHonorariosResult.builder()
                .numProcesso(request.getNumProcesso())
                .solicitante(request.getSolicitante())
                .reu(request.getReu())
                .valorBruto(request.getValorBruto())
                .imposto(imposto.setScale(2, RoundingMode.HALF_UP))
                .liquido(liquido)
                .aliquotaEfetiva(aliquotaEfetiva)
                .build();

        try {
            String resultadoJson = objectMapper.writeValueAsString(result);
            User usuarioLogado = authService.getLoggedUser();
            CalculoRealizado registro = CalculoRealizado.builder()
                    .numProcesso(request.getNumProcesso())
                    .tipoCalculo(CalculationType.HONORARIOS.getType())
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
