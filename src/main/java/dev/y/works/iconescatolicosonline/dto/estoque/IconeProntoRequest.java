package dev.y.works.iconescatolicosonline.dto.estoque;

import dev.y.works.iconescatolicosonline.domain.estoque.StatusIconePronto;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record IconeProntoRequest(
        @NotNull Long modeloIconeId,
        @NotBlank @Size(max = 50) String tamanho,
        @NotBlank @Size(max = 80) String acabamento,
        @NotNull @DecimalMin("0.00") @Digits(integer = 8, fraction = 2) BigDecimal custoProducao,
        @DecimalMin("0.00") @Digits(integer = 8, fraction = 2) BigDecimal precoSugerido,
        @NotNull StatusIconePronto status,
        @Size(max = 120) String localizacao
) {
}
