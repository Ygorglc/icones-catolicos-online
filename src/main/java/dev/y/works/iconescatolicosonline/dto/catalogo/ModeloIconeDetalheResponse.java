package dev.y.works.iconescatolicosonline.dto.catalogo;

import java.math.BigDecimal;
import java.time.Instant;

public record ModeloIconeDetalheResponse(
        Long id,
        String nome,
        String descricao,
        String imagemUrl,
        BigDecimal precoBase,
        boolean ativo,
        Instant criadoEm,
        ConteudoDevocionalResponse conteudoDevocional
) {
}
