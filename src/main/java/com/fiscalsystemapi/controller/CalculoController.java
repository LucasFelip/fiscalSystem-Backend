package com.fiscalsystemapi.controller;

import com.fiscalsystemapi.dto.honorarios.CalculoHonorariosRequest;
import com.fiscalsystemapi.dto.honorarios.CalculoHonorariosResult;
import com.fiscalsystemapi.dto.fepa.CalculoFepaRequest;
import com.fiscalsystemapi.dto.fepa.CalculoFepaResult;
import com.fiscalsystemapi.dto.rra.CalculoRraRequest;
import com.fiscalsystemapi.dto.rra.CalculoRraResult;
import com.fiscalsystemapi.dto.pj.CalculoPjRequest;
import com.fiscalsystemapi.dto.pj.CalculoPjResult;
import com.fiscalsystemapi.service.CalculoHonorariosService;
import com.fiscalsystemapi.service.CalculoFepaService;
import com.fiscalsystemapi.service.CalculoRraService;
import com.fiscalsystemapi.service.CalculoPjService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/calculos")
public class CalculoController {

    private final CalculoHonorariosService calculoHonorariosService;
    private final CalculoFepaService calculoFepaService;
    private final CalculoRraService calculoRraService;
    private final CalculoPjService calculoPjService;

    public CalculoController(CalculoHonorariosService calculoHonorariosService,
                             CalculoFepaService calculoFepaService,
                             CalculoRraService calculoRraService,
                             CalculoPjService calculoPjService) {
        this.calculoHonorariosService = calculoHonorariosService;
        this.calculoFepaService = calculoFepaService;
        this.calculoRraService = calculoRraService;
        this.calculoPjService = calculoPjService;
    }

    @PostMapping("/honorarios")
    public ResponseEntity<CalculoHonorariosResult> calcularHonorarios(@RequestBody CalculoHonorariosRequest request) {
        CalculoHonorariosResult result = calculoHonorariosService.calcular(request);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/fepa")
    public ResponseEntity<CalculoFepaResult> calcularFepa(@RequestBody CalculoFepaRequest request) {
        CalculoFepaResult result = calculoFepaService.calcular(request);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/rra")
    public ResponseEntity<CalculoRraResult> calcularRra(@RequestBody CalculoRraRequest request) {
        CalculoRraResult result = calculoRraService.calcular(request);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/pj")
    public ResponseEntity<CalculoPjResult> calcularPj(@RequestBody CalculoPjRequest request) {
        CalculoPjResult result = calculoPjService.calcular(request);
        return ResponseEntity.ok(result);
    }
}
