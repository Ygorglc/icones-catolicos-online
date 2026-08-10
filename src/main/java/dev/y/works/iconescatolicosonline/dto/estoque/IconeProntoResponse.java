package dev.y.works.iconescatolicosonline.dto.estoque;

import dev.y.works.iconescatolicosonline.domain.catalogo.TamanhoIcone;
import dev.y.works.iconescatolicosonline.domain.estoque.StatusIconePronto;

import java.math.BigDecimal;

public record IconeProntoResponse(
        Long id,
        Long modeloIconeId,
        String modeloIconeNome,
        Long encomendaId,
        TamanhoIcone tamanho,
        String acabamento,
        BigDecimal custoProducao,
        BigDecimal precoSugerido,
        StatusIconePronto status,
        String localizacao
) {
}
