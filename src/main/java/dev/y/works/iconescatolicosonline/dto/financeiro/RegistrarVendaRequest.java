package dev.y.works.iconescatolicosonline.dto.financeiro;

import jakarta.validation.constraints.NotNull;

public record RegistrarVendaRequest(
        @NotNull Long encomendaId
) {
}
