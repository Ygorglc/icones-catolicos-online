package dev.y.works.iconescatolicosonline.domain.estoque;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "material")
public class Material {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 120)
    @Column(nullable = false, unique = true, length = 120)
    private String nome;

    @NotBlank
    @Size(max = 30)
    @Column(name = "unidade_medida", nullable = false, length = 30)
    private String unidadeMedida;

    @NotNull
    @DecimalMin("0.000")
    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal quantidade = BigDecimal.ZERO;

    @NotNull
    @DecimalMin("0.00")
    @Column(name = "custo_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal custoUnitario = BigDecimal.ZERO;

    @NotNull
    @DecimalMin("0.000")
    @Column(name = "estoque_minimo", nullable = false, precision = 12, scale = 3)
    private BigDecimal estoqueMinimo = BigDecimal.ZERO;
}
