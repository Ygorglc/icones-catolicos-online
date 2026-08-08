package dev.y.works.iconescatolicosonline.domain.encomenda;

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
@Table(name = "personalizacao")
public class Personalizacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_encomenda_id", nullable = false, unique = true)
    private ItemEncomenda itemEncomenda;

    @Size(max = 50)
    @Column(length = 50)
    private String tamanho;

    @Size(max = 80)
    @Column(length = 80)
    private String acabamento;

    @Size(max = 255)
    @Column(length = 255)
    private String frase;

    @Size(max = 120)
    @Column(name = "nome_familia", length = 120)
    private String nomeFamilia;

    @Column(columnDefinition = "TEXT")
    private String observacoes;
}
