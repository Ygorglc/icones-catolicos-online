package dev.y.works.iconescatolicosonline.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SegurancaTestController.class)
@Import(SecurityConfig.class)
class SegurancaHttpTests {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    JwtService jwtService;

    @MockitoBean
    UsuarioDetailsService usuarioDetailsService;

    @Test
    void devePermitirAcessoPublicoSemToken() throws Exception {
        mockMvc.perform(get("/api/publico/teste"))
                .andExpect(status().isOk())
                .andExpect(content().string("publico"));
    }

    @Test
    void deveNegarAcessoAdministrativoSemToken() throws Exception {
        mockMvc.perform(get("/api/admin/teste"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveNegarAcessoAdministrativoParaCliente() throws Exception {
        mockMvc.perform(get("/api/admin/teste")
                        .with(user("cliente@teste.local").roles("CLIENTE")))
                .andExpect(status().isForbidden());
    }

    @Test
    void devePermitirAcessoAdministrativoParaAdministrador() throws Exception {
        mockMvc.perform(get("/api/admin/teste")
                        .with(user("admin@teste.local").roles("ADMINISTRADOR")))
                .andExpect(status().isOk())
                .andExpect(content().string("administrativo"));
    }

    @Test
    void deveRejeitarTokenInvalido() throws Exception {
        when(jwtService.extrairEmail("invalido"))
                .thenThrow(new MalformedJwtException("Token inválido"));

        mockMvc.perform(get("/api/admin/teste")
                        .header("Authorization", "Bearer invalido"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveRejeitarTokenExpirado() throws Exception {
        when(jwtService.extrairEmail("expirado"))
                .thenThrow(new ExpiredJwtException(null, null, "Token expirado"));

        mockMvc.perform(get("/api/admin/teste")
                        .header("Authorization", "Bearer expirado"))
                .andExpect(status().isUnauthorized());
    }

}
