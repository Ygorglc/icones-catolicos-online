package dev.y.works.iconescatolicosonline.dto.catalogo;

import jakarta.validation.constraints.Size;

public record ConteudoDevocionalRequest(
        @Size(max = 10_000) String historia,
        @Size(max = 10_000) String significado,
        @Size(max = 10_000) String simbologia,
        @Size(max = 10_000) String oracao,
        @Size(max = 10_000) String ocasiaoPresente,
        @Size(max = 10_000) String cuidados
) {
}
