package dev.y.works.iconescatolicosonline.dto.erro;

import java.time.Instant;
import java.util.List;

public record ErroResposta(
        Instant instante,
        int status,
        String erro,
        String mensagem,
        String caminho,
        List<ErroCampo> campos
) {
}
