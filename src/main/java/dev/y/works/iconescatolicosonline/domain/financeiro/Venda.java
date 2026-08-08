package dev.y.works.iconescatolicosonline.domain.financeiro;

import dev.y.works.iconescatolicosonline.domain.encomenda.Encomenda;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "venda")
public class Venda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "encomenda_id", nullable = false, unique = true)
    private Encomenda encomenda;

    @NotNull
    @DecimalMin("0.00")
    @Column(name = "valor_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorTotal;

    @NotNull
    @Column(name = "data_venda", nullable = false)
    private Instant dataVenda;

    @NotNull
    @Column(name = "lucro_bruto", nullable = false, precision = 10, scale = 2)
    private BigDecimal lucroBruto;

    @NotNull
    @Column(name = "lucro_liquido_estimado", nullable = false, precision = 10, scale = 2)
    private BigDecimal lucroLiquidoEstimado;
}
