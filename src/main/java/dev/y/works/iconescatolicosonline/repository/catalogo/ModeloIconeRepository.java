package dev.y.works.iconescatolicosonline.repository.catalogo;

import dev.y.works.iconescatolicosonline.domain.catalogo.ModeloIcone;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ModeloIconeRepository extends JpaRepository<ModeloIcone, Long> {

    List<ModeloIcone> findByAtivoTrueOrderByNomeAsc();

    boolean existsByNomeIgnoreCase(String nome);
}
