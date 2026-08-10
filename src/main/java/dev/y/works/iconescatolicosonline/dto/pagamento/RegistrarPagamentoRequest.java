package dev.y.works.iconescatolicosonline.dto.pagamento;

import dev.y.works.iconescatolicosonline.domain.pagamento.FormaPagamento;
import dev.y.works.iconescatolicosonline.domain.pagamento.OrigemPagamento;
import dev.y.works.iconescatolicosonline.domain.pagamento.TipoPagamento;
import jakarta.validation.constraints.NotNull;

public record RegistrarPagamentoRequest(
        @NotNull TipoPagamento tipo,
        @NotNull FormaPagamento forma,
        @NotNull OrigemPagamento origem
) {
}
