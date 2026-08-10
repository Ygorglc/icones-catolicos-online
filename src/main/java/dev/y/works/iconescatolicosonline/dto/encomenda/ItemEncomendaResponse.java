package dev.y.works.iconescatolicosonline.dto.encomenda;

import java.math.BigDecimal;

public record ItemEncomendaResponse(
        Long id,
        Long modeloIconeId,
        String modeloIconeNome,
        int quantidade,
        BigDecimal valorUnitario,
        BigDecimal subtotal,
        PersonalizacaoResponse personalizacao
) {
}
