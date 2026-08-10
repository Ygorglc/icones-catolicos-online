package dev.y.works.iconescatolicosonline.dto.encomenda;

import dev.y.works.iconescatolicosonline.domain.encomenda.StatusEncomenda;
import jakarta.validation.constraints.NotNull;

public record AtualizarStatusEncomendaRequest(
        @NotNull StatusEncomenda status
) {
}
