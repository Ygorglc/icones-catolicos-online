package dev.y.works.iconescatolicosonline.dto.encomenda;

import jakarta.validation.constraints.Size;

public record PersonalizacaoRequest(
        @Size(max = 50) String tamanho,
        @Size(max = 80) String acabamento,
        @Size(max = 255) String frase,
        @Size(max = 120) String nomeFamilia,
        @Size(max = 2_000) String observacoes
) {
}
