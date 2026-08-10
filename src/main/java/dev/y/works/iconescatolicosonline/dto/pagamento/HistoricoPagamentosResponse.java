package dev.y.works.iconescatolicosonline.dto.pagamento;

import dev.y.works.iconescatolicosonline.domain.encomenda.StatusFinanceiro;

import java.math.BigDecimal;
import java.util.List;

public record HistoricoPagamentosResponse(
        Long encomendaId,
        BigDecimal valorTotal,
        BigDecimal totalPago,
        BigDecimal saldoPendente,
        StatusFinanceiro statusFinanceiro,
        List<PagamentoResponse> pagamentos
) {
}
