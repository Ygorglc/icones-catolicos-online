package dev.y.works.iconescatolicosonline.dto.estoque;

import java.math.BigDecimal;

public record MaterialResponse(
        Long id,
        String nome,
        String unidadeMedida,
        BigDecimal quantidade,
        BigDecimal custoUnitario,
        BigDecimal estoqueMinimo,
        boolean estoqueBaixo
) {
}
