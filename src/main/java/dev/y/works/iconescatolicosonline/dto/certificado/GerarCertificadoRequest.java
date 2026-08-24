package dev.y.works.iconescatolicosonline.dto.certificado;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GerarCertificadoRequest(
        @NotBlank @Size(max = 120) String nomeArtesao,
        @NotBlank @Size(max = 2_000) String materialUtilizado
) {
}
