package dev.y.works.iconescatolicosonline.dto.estoque;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record MaterialRequest(
        @NotBlank @Size(max = 120) String nome,
        @NotBlank @Size(max = 30) String unidadeMedida,
        @NotNull @DecimalMin("0.000") @Digits(integer = 9, fraction = 3) BigDecimal quantidade,
        @NotNull @DecimalMin("0.00") @Digits(integer = 8, fraction = 2) BigDecimal custoUnitario,
        @NotNull @DecimalMin("0.000") @Digits(integer = 9, fraction = 3) BigDecimal estoqueMinimo
) {
}
