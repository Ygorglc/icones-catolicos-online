package dev.y.works.iconescatolicosonline.domain.configuracao;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "configuracao_loja")
public class ConfiguracaoLoja {
    @Id
    private Long id;

    @Column(name = "entrega_habilitada", nullable = false)
    private boolean entregaHabilitada;

    @Column(name = "chave_pix", length = 200)
    private String chavePix;

    @Column(name = "dados_deposito", length = 1000)
    private String dadosDeposito;
}
