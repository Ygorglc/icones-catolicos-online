package dev.y.works.iconescatolicosonline.dto.configuracao;

import jakarta.validation.constraints.Size;

public record ConfiguracaoLojaRequest(
        boolean entregaHabilitada,
        @Size(max = 200) String chavePix,
        @Size(max = 1000) String dadosDeposito) {
}
