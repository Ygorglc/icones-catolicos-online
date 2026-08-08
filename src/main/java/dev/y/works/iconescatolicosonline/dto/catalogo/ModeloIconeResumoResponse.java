package dev.y.works.iconescatolicosonline.dto.catalogo;

import java.math.BigDecimal;

public record ModeloIconeResumoResponse(
        Long id,
        String nome,
        String imagemUrl,
        BigDecimal precoBase
) {
}
