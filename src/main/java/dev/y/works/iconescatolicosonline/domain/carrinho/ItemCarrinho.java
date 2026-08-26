package dev.y.works.iconescatolicosonline.domain.carrinho;
import dev.y.works.iconescatolicosonline.domain.catalogo.ModeloIcone; import dev.y.works.iconescatolicosonline.domain.catalogo.TamanhoIcone; import dev.y.works.iconescatolicosonline.domain.usuario.Usuario; import jakarta.persistence.*; import lombok.Getter; import lombok.NoArgsConstructor; import lombok.Setter; import java.time.Instant;
@Getter @Setter @NoArgsConstructor @Entity @Table(name="item_carrinho")
public class ItemCarrinho {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="usuario_id",nullable=false) private Usuario usuario;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="modelo_icone_id",nullable=false) private ModeloIcone modeloIcone;
 @Column(nullable=false) private int quantidade;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private TamanhoIcone tamanho;
 @Column(length=80) private String acabamento; @Column(length=255) private String frase;
 @Column(name="nome_familia",length=120) private String nomeFamilia; @Column(columnDefinition="TEXT") private String observacoes;
 @Column(name="criado_em",nullable=false,updatable=false) private Instant criadoEm=Instant.now();
}
