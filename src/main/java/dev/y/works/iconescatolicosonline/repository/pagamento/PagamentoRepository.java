package dev.y.works.iconescatolicosonline.repository.pagamento;

import dev.y.works.iconescatolicosonline.domain.pagamento.Pagamento;
import dev.y.works.iconescatolicosonline.domain.pagamento.StatusPagamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {

    List<Pagamento> findByEncomenda_IdOrderByCriadoEmAsc(Long encomendaId);

    List<Pagamento> findByEncomenda_IdAndStatus(Long encomendaId, StatusPagamento status);

    List<Pagamento> findByStatusOrderByCriadoEmAsc(StatusPagamento status);

    boolean existsByEncomenda_IdAndStatus(Long encomendaId, StatusPagamento status);

    Optional<Pagamento> findFirstByEncomenda_IdAndStatusOrderByCriadoEmAsc(
            Long encomendaId, StatusPagamento status);
}
