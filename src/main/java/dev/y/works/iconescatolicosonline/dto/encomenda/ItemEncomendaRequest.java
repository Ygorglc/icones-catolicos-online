package dev.y.works.iconescatolicosonline.dto.encomenda;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ItemEncomendaRequest(
        @NotNull Long modeloIconeId,
        @Min(1) int quantidade,
        @Valid PersonalizacaoRequest personalizacao
) {
}
