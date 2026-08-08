package dev.y.works.iconescatolicosonline.dto.autenticacao;

import dev.y.works.iconescatolicosonline.domain.usuario.PerfilUsuario;

public record AutenticacaoResponse(
        String token,
        String tipo,
        long expiraEmSegundos,
        Long usuarioId,
        String nome,
        String email,
        PerfilUsuario perfil
) {
}
