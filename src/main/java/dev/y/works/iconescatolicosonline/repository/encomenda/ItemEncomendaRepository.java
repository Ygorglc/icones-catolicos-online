package dev.y.works.iconescatolicosonline.repository.encomenda;

import dev.y.works.iconescatolicosonline.domain.encomenda.ItemEncomenda;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemEncomendaRepository extends JpaRepository<ItemEncomenda, Long> {

    List<ItemEncomenda> findByEncomenda_IdOrderByIdAsc(Long encomendaId);
}
