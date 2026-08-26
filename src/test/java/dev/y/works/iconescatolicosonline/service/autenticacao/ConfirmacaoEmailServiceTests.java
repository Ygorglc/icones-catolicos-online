package dev.y.works.iconescatolicosonline.service.autenticacao;

import dev.y.works.iconescatolicosonline.domain.usuario.Usuario;
import dev.y.works.iconescatolicosonline.exception.RegraNegocioException;
import dev.y.works.iconescatolicosonline.repository.usuario.UsuarioRepository;
import dev.y.works.iconescatolicosonline.service.email.EmailConfirmacaoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfirmacaoEmailServiceTests {
    @Mock UsuarioRepository usuarioRepository;
    @Mock EmailConfirmacaoService emailService;

    @Test
    void deveGerarTokenEmHashEEnviarConfirmacao() {
        Usuario usuario = usuario();
        ConfirmacaoEmailService service = new ConfirmacaoEmailService(usuarioRepository, emailService);

        service.gerarEEnviar(usuario);

        assertThat(usuario.isEmailVerificado()).isFalse();
        assertThat(usuario.getTokenConfirmacaoEmailHash()).hasSize(64);
        assertThat(usuario.getTokenConfirmacaoEmailExpiraEm()).isAfter(Instant.now());
        ArgumentCaptor<String> token = ArgumentCaptor.forClass(String.class);
        verify(emailService).enviar(org.mockito.ArgumentMatchers.eq(usuario.getEmail()),
                org.mockito.ArgumentMatchers.eq(usuario.getNome()), token.capture(),
                org.mockito.ArgumentMatchers.eq(24L));
        assertThat(token.getValue()).doesNotContain(usuario.getTokenConfirmacaoEmailHash());
    }

    @Test
    void deveConfirmarTokenValido() {
        Usuario usuario = usuario();
        usuario.setTokenConfirmacaoEmailExpiraEm(Instant.now().plusSeconds(60));
        ConfirmacaoEmailService service = new ConfirmacaoEmailService(usuarioRepository, emailService);
        service.gerarEEnviar(usuario);
        String hash = usuario.getTokenConfirmacaoEmailHash();
        ArgumentCaptor<String> token = ArgumentCaptor.forClass(String.class);
        verify(emailService).enviar(any(), any(), token.capture(), org.mockito.ArgumentMatchers.eq(24L));
        when(usuarioRepository.findByTokenConfirmacaoEmailHash(hash)).thenReturn(Optional.of(usuario));

        var resposta = service.confirmar(token.getValue());

        assertThat(usuario.isEmailVerificado()).isTrue();
        assertThat(usuario.getTokenConfirmacaoEmailHash()).isNull();
        assertThat(resposta.mensagem()).contains("confirmado");
    }

    @Test
    void deveRejeitarTokenInvalido() {
        ConfirmacaoEmailService service = new ConfirmacaoEmailService(usuarioRepository, emailService);
        when(usuarioRepository.findByTokenConfirmacaoEmailHash(any())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.confirmar("invalido"))
                .isInstanceOf(RegraNegocioException.class);
    }

    private Usuario usuario() {
        Usuario usuario = new Usuario();
        usuario.setNome("Maria");
        usuario.setEmail("maria@teste.com");
        return usuario;
    }
}
