package dev.y.works.iconescatolicosonline.security;

import dev.y.works.iconescatolicosonline.domain.usuario.PerfilUsuario;
import dev.y.works.iconescatolicosonline.domain.usuario.Usuario;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTests {

    private static final String SEGREDO =
            "aWNvbmVzLWNhdG9saWNvcy1zZWdyZWRvLWp3dC0yMDI2LXNlZ3Vybw==";

    @Test
    void deveGerarEValidarToken() {
        JwtService jwtService = new JwtService(SEGREDO, 120);
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail("cliente@teste.local");
        usuario.setPerfil(PerfilUsuario.CLIENTE);

        String token = jwtService.gerarToken(usuario);

        assertThat(token).isNotBlank();
        assertThat(jwtService.extrairEmail(token)).isEqualTo("cliente@teste.local");
        assertThat(jwtService.getExpiracaoEmSegundos()).isEqualTo(7_200);
    }
}
