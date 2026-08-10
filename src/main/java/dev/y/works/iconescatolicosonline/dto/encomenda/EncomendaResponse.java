package dev.y.works.iconescatolicosonline.dto.encomenda;

import dev.y.works.iconescatolicosonline.domain.encomenda.StatusEncomenda;
import dev.y.works.iconescatolicosonline.domain.encomenda.StatusFinanceiro;
import dev.y.works.iconescatolicosonline.domain.encomenda.TipoEntrega;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record EncomendaResponse(
        Long id,
        Long clienteId,
        String clienteNome,
        Instant dataCriacao,
        StatusEncomenda statusEncomenda,
        StatusFinanceiro statusFinanceiro,
        BigDecimal valorTotal,
        BigDecimal valorSinal,
        TipoEntrega tipoEntrega,
        String enderecoEntrega,
        String observacoes,
        List<ItemEncomendaResponse> itens
) {
}
