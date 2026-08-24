package dev.y.works.iconescatolicosonline.dto.certificado;

import java.time.LocalDate;

public record CertificadoArtesanalResponse(
        Long id,
        Long encomendaId,
        String numeroPeca,
        LocalDate dataEmissao,
        String nomeArtesao,
        String modeloIcone,
        String tamanhoIcone,
        String acabamento,
        String materialUtilizado,
        String textoCertificado,
        String codigoPublico,
        boolean autentico
) {
}
