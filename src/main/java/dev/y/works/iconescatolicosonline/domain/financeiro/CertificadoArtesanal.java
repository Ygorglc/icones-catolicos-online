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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "certificado_artesanal")
public class CertificadoArtesanal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "encomenda_id", nullable = false, unique = true)
    private Encomenda encomenda;

    @NotBlank
    @Size(max = 50)
    @Column(name = "numero_peca", nullable = false, unique = true, length = 50)
    private String numeroPeca;

    @NotNull
    @Column(name = "data_emissao", nullable = false)
    private LocalDate dataEmissao;

    @Column(name = "material_utilizado", columnDefinition = "TEXT")
    private String materialUtilizado;

    @NotBlank
    @Column(name = "texto_certificado", nullable = false, columnDefinition = "TEXT")
    private String textoCertificado;

    @NotBlank
    @Size(max = 64)
    @Column(name = "codigo_publico", nullable = false, unique = true, length = 64)
    private String codigoPublico;
}
