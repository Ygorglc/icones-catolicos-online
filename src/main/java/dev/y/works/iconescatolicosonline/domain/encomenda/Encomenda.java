package dev.y.works.iconescatolicosonline.domain.encomenda;

import dev.y.works.iconescatolicosonline.domain.estoque.IconePronto;
import dev.y.works.iconescatolicosonline.domain.financeiro.CertificadoArtesanal;
import dev.y.works.iconescatolicosonline.domain.financeiro.Gasto;
import dev.y.works.iconescatolicosonline.domain.financeiro.Venda;
import dev.y.works.iconescatolicosonline.domain.pagamento.Pagamento;
import dev.y.works.iconescatolicosonline.domain.usuario.Cliente;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "encomenda")
public class Encomenda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Column(name = "data_criacao", nullable = false, updatable = false)
    private Instant dataCriacao = Instant.now();

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status_encomenda", nullable = false, length = 50)
    private StatusEncomenda statusEncomenda = StatusEncomenda.ENCOMENDA_CRIADA;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status_financeiro", nullable = false, length = 50)
    private StatusFinanceiro statusFinanceiro = StatusFinanceiro.AGUARDANDO_SINAL;

    @NotNull
    @DecimalMin("0.00")
    @Column(name = "valor_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorTotal = BigDecimal.ZERO;

    @NotNull
    @DecimalMin("0.00")
    @Column(name = "valor_sinal", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorSinal = BigDecimal.ZERO;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_entrega", nullable = false, length = 20)
    private TipoEntrega tipoEntrega;

    @Column(name = "endereco_entrega", columnDefinition = "TEXT")
    private String enderecoEntrega;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    @OneToMany(mappedBy = "encomenda", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemEncomenda> itens = new ArrayList<>();

    @OneToMany(mappedBy = "encomenda", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Pagamento> pagamentos = new ArrayList<>();

    @OneToMany(mappedBy = "encomenda")
    private List<Gasto> gastos = new ArrayList<>();

    @OneToOne(mappedBy = "encomenda", fetch = FetchType.LAZY)
    private IconePronto iconePronto;

    @OneToOne(mappedBy = "encomenda", fetch = FetchType.LAZY)
    private Venda venda;

    @OneToOne(mappedBy = "encomenda", fetch = FetchType.LAZY)
    private CertificadoArtesanal certificadoArtesanal;
}
