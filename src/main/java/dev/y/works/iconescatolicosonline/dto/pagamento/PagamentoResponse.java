package dev.y.works.iconescatolicosonline.dto.pagamento;

import dev.y.works.iconescatolicosonline.domain.encomenda.StatusEncomenda;
import dev.y.works.iconescatolicosonline.domain.encomenda.StatusFinanceiro;
import dev.y.works.iconescatolicosonline.domain.pagamento.StatusPagamento;
import dev.y.works.iconescatolicosonline.domain.pagamento.FormaPagamento;
import dev.y.works.iconescatolicosonline.domain.pagamento.OrigemPagamento;
import dev.y.works.iconescatolicosonline.domain.pagamento.TipoPagamento;

import java.math.BigDecimal;
import java.time.Instant;

public record PagamentoResponse(
        Long id,
        Long encomendaId,
        TipoPagamento tipo,
        FormaPagamento forma,
        OrigemPagamento origem,
        BigDecimal valor,
        Instant dataPagamento,
        StatusPagamento status,
        String referenciaSimulada,
        String analisadoPor,
        Instant dataAnalise,
        String observacaoAdministrativa,
        boolean possuiComprovante,
        String comprovanteNomeOriginal,
        BigDecimal totalPago,
        BigDecimal saldoPendente,
        StatusFinanceiro statusFinanceiro,
        StatusEncomenda statusEncomenda
) {
}
