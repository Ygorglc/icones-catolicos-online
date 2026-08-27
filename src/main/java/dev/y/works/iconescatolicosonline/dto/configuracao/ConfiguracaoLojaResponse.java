package dev.y.works.iconescatolicosonline.dto.configuracao;

public record ConfiguracaoLojaResponse(
        boolean entregaHabilitada,
        String chavePix,
        String dadosDeposito) {
}
