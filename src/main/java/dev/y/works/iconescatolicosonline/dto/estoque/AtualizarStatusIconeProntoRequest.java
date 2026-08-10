package dev.y.works.iconescatolicosonline.dto.estoque;

import dev.y.works.iconescatolicosonline.domain.estoque.StatusIconePronto;
import jakarta.validation.constraints.NotNull;

public record AtualizarStatusIconeProntoRequest(
        @NotNull StatusIconePronto status
) {
}
