package dev.y.works.iconescatolicosonline.dto.encomenda;

import dev.y.works.iconescatolicosonline.domain.encomenda.TipoEntrega;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CriarEncomendaRequest(
        @NotNull TipoEntrega tipoEntrega,
        @Size(max = 2_000) String enderecoEntrega,
        @Size(max = 2_000) String observacoes,
        @NotEmpty List<@Valid ItemEncomendaRequest> itens
) {
}
