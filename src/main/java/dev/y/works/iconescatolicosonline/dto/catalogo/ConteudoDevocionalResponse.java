package dev.y.works.iconescatolicosonline.dto.catalogo;

public record ConteudoDevocionalResponse(
        Long id,
        String historia,
        String significado,
        String simbologia,
        String oracao,
        String ocasiaoPresente,
        String cuidados
) {
}
