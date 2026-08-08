package dev.y.works.iconescatolicosonline.domain.catalogo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "conteudo_devocional")
public class ConteudoDevocional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "modelo_icone_id", nullable = false, unique = true)
    private ModeloIcone modeloIcone;

    @Column(columnDefinition = "TEXT")
    private String historia;

    @Column(columnDefinition = "TEXT")
    private String significado;

    @Column(columnDefinition = "TEXT")
    private String simbologia;

    @Column(columnDefinition = "TEXT")
    private String oracao;

    @Column(name = "ocasiao_presente", columnDefinition = "TEXT")
    private String ocasiaoPresente;

    @Column(columnDefinition = "TEXT")
    private String cuidados;
}
