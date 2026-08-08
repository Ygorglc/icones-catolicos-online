package dev.y.works.iconescatolicosonline.repository.catalogo;

import dev.y.works.iconescatolicosonline.domain.catalogo.ModeloIcone;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ModeloIconeRepository extends JpaRepository<ModeloIcone, Long> {

    List<ModeloIcone> findByAtivoTrueOrderByNomeAsc();

    Optional<ModeloIcone> findByIdAndAtivoTrue(Long id);

    boolean existsByNomeIgnoreCase(String nome);
}
