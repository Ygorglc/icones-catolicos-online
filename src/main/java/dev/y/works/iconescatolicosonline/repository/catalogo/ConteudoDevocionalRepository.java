package dev.y.works.iconescatolicosonline.repository.catalogo;

import dev.y.works.iconescatolicosonline.domain.catalogo.ConteudoDevocional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConteudoDevocionalRepository extends JpaRepository<ConteudoDevocional, Long> {

    Optional<ConteudoDevocional> findByModeloIcone_Id(Long modeloIconeId);
}
