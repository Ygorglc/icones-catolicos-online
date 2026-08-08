package dev.y.works.iconescatolicosonline.dto.catalogo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ModeloIconeRequest(
        @NotBlank @Size(max = 120) String nome,
        @NotBlank @Size(max = 10_000) String descricao,
        @Size(max = 2_000) String imagemUrl,
        @NotNull @DecimalMin("0.00") @Digits(integer = 8, fraction = 2) BigDecimal precoBase,
        Boolean ativo,
        @Valid ConteudoDevocionalRequest conteudoDevocional
) {
}
