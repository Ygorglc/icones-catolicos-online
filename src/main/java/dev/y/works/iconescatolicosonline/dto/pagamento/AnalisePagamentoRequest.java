package dev.y.works.iconescatolicosonline.dto.pagamento;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AnalisePagamentoRequest(
        @NotNull Boolean confirmado,
        @Size(max = 2_000) String observacao
) {
}
