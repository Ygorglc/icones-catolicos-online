package dev.y.works.iconescatolicosonline.domain.usuario;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "cliente")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @Size(max = 20)
    @Column(length = 20)
    private String telefone;

    @Size(max = 14)
    @Column(unique = true, length = 14)
    private String cpf;

    @Size(max = 8)
    @Column(length = 8)
    private String cep;

    @Size(max = 150)
    @Column(length = 150)
    private String logradouro;

    @Size(max = 20)
    @Column(name = "numero_endereco", length = 20)
    private String numero;

    @Size(max = 100)
    @Column(length = 100)
    private String complemento;

    @Size(max = 100)
    @Column(length = 100)
    private String bairro;

    @Size(max = 100)
    @Column(length = 100)
    private String cidade;

    @Size(max = 2)
    @Column(length = 2)
    private String uf;
}
