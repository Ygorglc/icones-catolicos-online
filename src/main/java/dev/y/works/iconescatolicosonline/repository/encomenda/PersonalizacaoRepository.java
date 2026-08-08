package dev.y.works.iconescatolicosonline.repository.encomenda;

import dev.y.works.iconescatolicosonline.domain.encomenda.Personalizacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PersonalizacaoRepository extends JpaRepository<Personalizacao, Long> {

    Optional<Personalizacao> findByItemEncomenda_Id(Long itemEncomendaId);
}
