package dev.y.works.iconescatolicosonline.domain.estoque;

import dev.y.works.iconescatolicosonline.domain.catalogo.ModeloIcone;
import dev.y.works.iconescatolicosonline.domain.encomenda.Encomenda;
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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "icone_pronto")
public class IconePronto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "modelo_icone_id", nullable = false)
    private ModeloIcone modeloIcone;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "encomenda_id", unique = true)
    private Encomenda encomenda;

    @NotBlank
    @Size(max = 50)
    @Column(nullable = false, length = 50)
    private String tamanho;

    @NotBlank
    @Size(max = 80)
    @Column(nullable = false, length = 80)
    private String acabamento;

    @NotNull
    @DecimalMin("0.00")
    @Column(name = "custo_producao", nullable = false, precision = 10, scale = 2)
    private BigDecimal custoProducao;

    @DecimalMin("0.00")
    @Column(name = "preco_sugerido", precision = 10, scale = 2)
    private BigDecimal precoSugerido;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private StatusIconePronto status = StatusIconePronto.DISPONIVEL;

    @Size(max = 120)
    @Column(length = 120)
    private String localizacao;
}
