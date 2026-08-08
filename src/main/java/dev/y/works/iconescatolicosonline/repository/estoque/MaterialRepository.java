package dev.y.works.iconescatolicosonline.repository.estoque;

import dev.y.works.iconescatolicosonline.domain.estoque.Material;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MaterialRepository extends JpaRepository<Material, Long> {

    boolean existsByNomeIgnoreCase(String nome);

    @Query("select m from Material m where m.quantidade <= m.estoqueMinimo order by m.nome")
    List<Material> buscarComEstoqueBaixo();
}
