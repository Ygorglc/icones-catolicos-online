package dev.y.works.iconescatolicosonline.repository.financeiro;

import dev.y.works.iconescatolicosonline.domain.financeiro.Venda;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface VendaRepository extends JpaRepository<Venda, Long> {

    Optional<Venda> findByEncomenda_Id(Long encomendaId);

    List<Venda> findByDataVendaBetweenOrderByDataVendaAsc(Instant inicio, Instant fim);
}
