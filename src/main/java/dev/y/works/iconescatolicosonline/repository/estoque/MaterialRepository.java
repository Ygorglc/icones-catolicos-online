package dev.y.works.iconescatolicosonline.repository.estoque;

import dev.y.works.iconescatolicosonline.domain.estoque.Material;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

public interface MaterialRepository extends JpaRepository<Material, Long> {

    boolean existsByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCaseAndIdNot(String nome, Long id);

    List<Material> findAllByOrderByNomeAsc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from Material m where m.id = :id")
    Optional<Material> buscarPorIdComBloqueio(@Param("id") Long id);

    @Query("select m from Material m where m.quantidade <= m.estoqueMinimo order by m.nome")
    List<Material> buscarComEstoqueBaixo();
}
