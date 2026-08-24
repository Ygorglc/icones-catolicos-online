package dev.y.works.iconescatolicosonline.repository.financeiro;

import dev.y.works.iconescatolicosonline.domain.financeiro.CertificadoArtesanal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CertificadoArtesanalRepository extends JpaRepository<CertificadoArtesanal, Long> {

    Optional<CertificadoArtesanal> findByEncomenda_Id(Long encomendaId);

    Optional<CertificadoArtesanal> findByCodigoPublico(String codigoPublico);

    boolean existsByNumeroPeca(String numeroPeca);

    boolean existsByEncomenda_Id(Long encomendaId);

    boolean existsByCodigoPublico(String codigoPublico);

    List<CertificadoArtesanal> findAllByOrderByDataEmissaoDescIdDesc();
}
