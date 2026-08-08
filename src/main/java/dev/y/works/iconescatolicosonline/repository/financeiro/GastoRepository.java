package dev.y.works.iconescatolicosonline.repository.financeiro;

import dev.y.works.iconescatolicosonline.domain.financeiro.Gasto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface GastoRepository extends JpaRepository<Gasto, Long> {

    List<Gasto> findByDataGastoBetweenOrderByDataGastoAsc(LocalDate inicio, LocalDate fim);

    List<Gasto> findByEncomenda_IdOrderByDataGastoAsc(Long encomendaId);
}
