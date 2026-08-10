package dev.y.works.iconescatolicosonline.repository.encomenda;

import dev.y.works.iconescatolicosonline.domain.encomenda.Encomenda;
import dev.y.works.iconescatolicosonline.domain.encomenda.StatusEncomenda;
import dev.y.works.iconescatolicosonline.domain.encomenda.StatusFinanceiro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EncomendaRepository extends JpaRepository<Encomenda, Long> {

    List<Encomenda> findByCliente_IdOrderByDataCriacaoDesc(Long clienteId);

    List<Encomenda> findByStatusEncomendaOrderByDataCriacaoAsc(StatusEncomenda status);

    List<Encomenda> findByStatusFinanceiroOrderByDataCriacaoAsc(StatusFinanceiro status);

    List<Encomenda> findAllByOrderByDataCriacaoDesc();

    Optional<Encomenda> findByIdAndCliente_Usuario_EmailIgnoreCase(Long id, String email);
}
