package dev.y.works.iconescatolicosonline.dto.estoque;

import dev.y.works.iconescatolicosonline.domain.estoque.TipoMovimentacaoMaterial;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record MovimentarMaterialRequest(
        @NotNull TipoMovimentacaoMaterial tipo,
        @NotNull @DecimalMin("0.001") @Digits(integer = 9, fraction = 3) BigDecimal quantidade
) {
}
