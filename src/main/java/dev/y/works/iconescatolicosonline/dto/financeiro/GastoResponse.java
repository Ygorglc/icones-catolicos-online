package dev.y.works.iconescatolicosonline.dto.financeiro;

import java.math.BigDecimal;
import java.time.LocalDate;

public record GastoResponse(
        Long id,
        Long encomendaId,
        String descricao,
        BigDecimal valor,
        LocalDate dataGasto,
        String categoria
) {
}
