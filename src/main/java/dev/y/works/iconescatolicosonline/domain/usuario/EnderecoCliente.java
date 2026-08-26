package dev.y.works.iconescatolicosonline.domain.usuario;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "endereco_cliente")
public class EnderecoCliente {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;
    @Column(nullable = false, length = 60)
    private String apelido;
    @Column(nullable = false, length = 8)
    private String cep;
    @Column(nullable = false, length = 150)
    private String logradouro;
    @Column(name = "numero_endereco", nullable = false, length = 20)
    private String numero;
    @Column(length = 100)
    private String complemento;
    @Column(nullable = false, length = 100)
    private String bairro;
    @Column(nullable = false, length = 100)
    private String cidade;
    @Column(nullable = false, length = 2)
    private String uf;
    @Column(nullable = false)
    private boolean principal;
}
