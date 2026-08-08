package dev.y.works.iconescatolicosonline.repository.estoque;

import dev.y.works.iconescatolicosonline.domain.estoque.IconePronto;
import dev.y.works.iconescatolicosonline.domain.estoque.StatusIconePronto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IconeProntoRepository extends JpaRepository<IconePronto, Long> {

    List<IconePronto> findByStatusOrderByIdAsc(StatusIconePronto status);

    Optional<IconePronto> findFirstByModeloIcone_IdAndTamanhoIgnoreCaseAndAcabamentoIgnoreCaseAndStatus(
            Long modeloIconeId,
            String tamanho,
            String acabamento,
            StatusIconePronto status
    );
}
