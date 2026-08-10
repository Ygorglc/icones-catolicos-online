package dev.y.works.iconescatolicosonline.dto.encomenda;

import dev.y.works.iconescatolicosonline.domain.catalogo.TamanhoIcone;
import jakarta.validation.constraints.Size;

public record PersonalizacaoRequest(
        TamanhoIcone tamanho,
        @Size(max = 80) String acabamento,
        @Size(max = 255) String frase,
        @Size(max = 120) String nomeFamilia,
        @Size(max = 2_000) String observacoes
) {
}
