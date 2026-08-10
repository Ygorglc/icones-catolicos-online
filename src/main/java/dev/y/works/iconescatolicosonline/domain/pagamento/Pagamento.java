package dev.y.works.iconescatolicosonline.domain.pagamento;

import dev.y.works.iconescatolicosonline.domain.encomenda.Encomenda;
import dev.y.works.iconescatolicosonline.domain.usuario.Administrador;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "pagamento")
public class Pagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "encomenda_id", nullable = false)
    private Encomenda encomenda;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TipoPagamento tipo;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "forma_pagamento", nullable = false, length = 30)
    private FormaPagamento formaPagamento;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrigemPagamento origem;

    @NotNull
    @DecimalMin(value = "0.01")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @Column(name = "data_pagamento")
    private Instant dataPagamento;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private StatusPagamento status = StatusPagamento.PENDENTE;

    @Size(max = 100)
    @Column(name = "referencia_simulada", length = 100)
    private String referenciaSimulada;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analisado_por_administrador_id")
    private Administrador analisadoPor;

    @Column(name = "data_analise")
    private Instant dataAnalise;

    @Column(name = "observacao_administrativa", columnDefinition = "TEXT")
    private String observacaoAdministrativa;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm = Instant.now();
}
