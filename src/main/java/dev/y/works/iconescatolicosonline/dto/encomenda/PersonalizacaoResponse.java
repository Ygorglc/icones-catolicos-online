package dev.y.works.iconescatolicosonline.dto.encomenda;

import dev.y.works.iconescatolicosonline.domain.catalogo.TamanhoIcone;

public record PersonalizacaoResponse(
        TamanhoIcone tamanho,
        String acabamento,
        String frase,
        String nomeFamilia,
        String observacoes
) {
}
