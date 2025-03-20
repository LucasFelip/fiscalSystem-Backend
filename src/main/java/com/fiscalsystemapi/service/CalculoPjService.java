package com.fiscalsystemapi.service;

import com.fiscalsystemapi.dto.pj.CalculoPjRequest;
import com.fiscalsystemapi.dto.pj.CalculoPjResult;
import com.fiscalsystemapi.entity.CalculoRealizado;
import com.fiscalsystemapi.entity.User;
import com.fiscalsystemapi.entity.enums.CalculationType;
import com.fiscalsystemapi.exception.ApiException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

@Service
public class CalculoPjService {

    private final CalculoRegistroService calculoRegistroService;
    private final AuthService authService;
    private final ObjectMapper objectMapper;

    public CalculoPjService(CalculoRegistroService calculoRegistroService,
                            AuthService authService,
                            ObjectMapper objectMapper) {
        this.calculoRegistroService = calculoRegistroService;
        this.authService = authService;
        this.objectMapper = objectMapper;
    }

    /**
     * Realiza o cálculo do imposto para pessoa jurídica (PJ) com base nos dados informados,
     * e registra o cálculo realizado no banco de dados.
     *
     * @param request Objeto contendo numProcesso, nomeParteAutora, nomeParteRe, valorBruto, valorCorrigido,
     *                optanteSimples e ramoAtividade.
     * @return CalculoPjResult com os dados do processo e os valores calculados.
     * @throws ApiException Caso os valores informados sejam inválidos ou o ramo de atividade seja inválido.
     */
    public CalculoPjResult calcular(CalculoPjRequest request) {
        String numProcesso = request.getNumProcesso();
        String nomeParteAutora = request.getNomeParteAutora();
        String nomeParteRe = request.getNomeParteRe();
        BigDecimal valorBruto = request.getValorBruto();
        BigDecimal valorCorrigido = request.getValorCorrigido();
        String optanteSimples = request.getOptanteSimples();
        String ramoAtividade = request.getRamoAtividade();

        if (valorBruto == null || valorCorrigido == null || valorCorrigido.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ApiException("Os valores inseridos são inválidos.");
        }

        BigDecimal valorBrutoRPV = valorBruto.setScale(2, RoundingMode.HALF_UP);
        BigDecimal valorCorrigidoRPV = valorCorrigido.setScale(2, RoundingMode.HALF_UP);

        BigDecimal aliquotaIR = BigDecimal.ZERO;
        String descricaoRamo = "Optante Simples (Sem IR)";
        BigDecimal impostoIR = BigDecimal.ZERO;

        if ("nao".equalsIgnoreCase(optanteSimples)) {
            switch (ramoAtividade) {
                case "1":
                    aliquotaIR = new BigDecimal("0.048");
                    descricaoRamo = "Modalidade Geral (4.8%)";
                    break;
                case "2":
                    aliquotaIR = new BigDecimal("0.015");
                    descricaoRamo = "Profissional Liberal (1.5%)";
                    break;
                case "3":
                    aliquotaIR = new BigDecimal("0.01");
                    descricaoRamo = "Cessão de Mão de Obra (1.0%)";
                    break;
                default:
                    throw new ApiException("Ramo de atividade inválido.");
            }
            impostoIR = valorCorrigidoRPV.multiply(aliquotaIR).setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal valorLiquido = valorBrutoRPV.subtract(impostoIR).setScale(2, RoundingMode.HALF_UP);
        BigDecimal aliquotaPercent = aliquotaIR.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP);

        CalculoPjResult result = CalculoPjResult.builder()
                .numProcesso(numProcesso)
                .nomeParteAutora(nomeParteAutora)
                .nomeParteRe(nomeParteRe)
                .valorBrutoRPV(valorBrutoRPV)
                .valorCorrigidoRPV(valorCorrigidoRPV)
                .optanteSimples(optanteSimples)
                .ramoAtividade(descricaoRamo)
                .aliquotaIR(aliquotaPercent)
                .impostoIR(impostoIR)
                .valorLiquido(valorLiquido)
                .build();

        // Registro do cálculo no banco de dados
        try {
            String resultadoJson = objectMapper.writeValueAsString(result);
            User usuarioLogado = authService.getLoggedUser();
            CalculoRealizado registro = CalculoRealizado.builder()
                    .numProcesso(numProcesso)
                    .tipoCalculo(CalculationType.PJ.getType())
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
