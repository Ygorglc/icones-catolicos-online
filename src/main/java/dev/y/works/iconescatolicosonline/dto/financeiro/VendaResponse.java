package dev.y.works.iconescatolicosonline.dto.financeiro;

import java.math.BigDecimal;
import java.time.Instant;

public record VendaResponse(
        Long id,
        Long encomendaId,
        String clienteNome,
        BigDecimal valorTotal,
        BigDecimal custoProducao,
        BigDecimal gastosAdicionais,
        BigDecimal custoTotal,
        BigDecimal lucroBruto,
        BigDecimal lucroLiquidoEstimado,
        Instant dataVenda
) {
}
