package dev.y.works.iconescatolicosonline.dto.financeiro;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record GastoRequest(
        Long encomendaId,
        @NotBlank @Size(max = 2_000) String descricao,
        @NotNull @DecimalMin("0.01") @Digits(integer = 8, fraction = 2) BigDecimal valor,
        @NotNull LocalDate dataGasto,
        @NotBlank @Size(max = 80) String categoria
) {
}
