package dev.y.works.iconescatolicosonline.repository.pagamento;

import dev.y.works.iconescatolicosonline.domain.pagamento.Pagamento;
import dev.y.works.iconescatolicosonline.domain.pagamento.StatusPagamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {

    List<Pagamento> findByEncomenda_IdOrderByCriadoEmAsc(Long encomendaId);

    List<Pagamento> findByEncomenda_IdAndStatus(Long encomendaId, StatusPagamento status);
}
