package dev.y.works.iconescatolicosonline.dto.estoque;

import jakarta.validation.constraints.NotNull;

public record ReservarIconeProntoRequest(
        @NotNull Long encomendaId
) {
}
