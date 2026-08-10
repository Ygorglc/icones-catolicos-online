package dev.y.works.iconescatolicosonline.security;

import dev.y.works.iconescatolicosonline.domain.usuario.PerfilUsuario;
import dev.y.works.iconescatolicosonline.domain.usuario.Usuario;
import org.junit.jupiter.api.Test;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    @Test
    void deveRejeitarTokenInvalido() {
        JwtService jwtService = new JwtService(SEGREDO, 120);

        assertThatThrownBy(() -> jwtService.extrairEmail("token-invalido"))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void deveRejeitarTokenExpirado() {
        JwtService jwtService = new JwtService(SEGREDO, -1);
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail("cliente@teste.local");
        usuario.setPerfil(PerfilUsuario.CLIENTE);
        String tokenExpirado = jwtService.gerarToken(usuario);

        assertThatThrownBy(() -> jwtService.extrairEmail(tokenExpirado))
                .isInstanceOf(ExpiredJwtException.class);
    }
}
