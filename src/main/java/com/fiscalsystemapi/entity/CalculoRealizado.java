package com.fiscalsystemapi.entity;



import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "calculos_realizados")
public class CalculoRealizado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "num_processo", nullable = false)
    private String numProcesso;

    @Column(name = "tipo_calculo", nullable = false)
    private String tipoCalculo;

    @Lob
    @Column(name = "resultado_json", columnDefinition = "TEXT")
    private String resultadoJson;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private User usuario;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "data_geracao", nullable = false)
    private Date dataGeracao;
}
