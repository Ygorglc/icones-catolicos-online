package dev.y.works.iconescatolicosonline.service.autenticacao;

import dev.y.works.iconescatolicosonline.domain.usuario.Cliente;
import dev.y.works.iconescatolicosonline.domain.usuario.PerfilUsuario;
import dev.y.works.iconescatolicosonline.domain.usuario.Usuario;
import dev.y.works.iconescatolicosonline.dto.autenticacao.AutenticacaoResponse;
import dev.y.works.iconescatolicosonline.dto.autenticacao.CadastroClienteRequest;
import dev.y.works.iconescatolicosonline.dto.autenticacao.LoginRequest;
import dev.y.works.iconescatolicosonline.exception.ConflitoException;
import dev.y.works.iconescatolicosonline.repository.usuario.ClienteRepository;
import dev.y.works.iconescatolicosonline.repository.usuario.UsuarioRepository;
import dev.y.works.iconescatolicosonline.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AutenticacaoServiceTests {

    @Mock UsuarioRepository usuarioRepository;
    @Mock ClienteRepository clienteRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock AuthenticationManager authenticationManager;
    @Mock JwtService jwtService;

    @Test
    void deveCadastrarClienteComSenhaCodificada() {
        AutenticacaoService service = criarService();
        CadastroClienteRequest request = new CadastroClienteRequest(
                "Maria", " MARIA@EXEMPLO.COM ", "senha123",
                "11999999999", "12345678901", "Rua A");
        when(passwordEncoder.encode("senha123")).thenReturn("senha-bcrypt");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario usuario = invocation.getArgument(0);
            usuario.setId(1L);
            return usuario;
        });
        when(jwtService.gerarToken(any(Usuario.class))).thenReturn("jwt");
        when(jwtService.getExpiracaoEmSegundos()).thenReturn(7_200L);

        AutenticacaoResponse resposta = service.cadastrarCliente(request);

        ArgumentCaptor<Usuario> usuarioCaptor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(usuarioCaptor.capture());
        assertThat(usuarioCaptor.getValue().getSenha()).isEqualTo("senha-bcrypt");
        assertThat(usuarioCaptor.getValue().getEmail()).isEqualTo("maria@exemplo.com");
        assertThat(usuarioCaptor.getValue().getPerfil()).isEqualTo(PerfilUsuario.CLIENTE);
        verify(clienteRepository).save(any(Cliente.class));
        assertThat(resposta.token()).isEqualTo("jwt");
    }

    @Test
    void deveImpedirCadastroComEmailDuplicado() {
        AutenticacaoService service = criarService();
        CadastroClienteRequest request = new CadastroClienteRequest(
                "Maria", "maria@exemplo.com", "senha123", null, null, null);
        when(usuarioRepository.existsByEmailIgnoreCase("maria@exemplo.com")).thenReturn(true);

        assertThatThrownBy(() -> service.cadastrarCliente(request))
                .isInstanceOf(ConflitoException.class);
    }

    @Test
    void deveAutenticarEEmitirTokenNoLogin() {
        AutenticacaoService service = criarService();
        Usuario usuario = new Usuario();
        usuario.setId(3L);
        usuario.setNome("Administrador");
        usuario.setEmail("admin@icones.local");
        usuario.setPerfil(PerfilUsuario.ADMINISTRADOR);
        when(usuarioRepository.findByEmailIgnoreCase("admin@icones.local"))
                .thenReturn(Optional.of(usuario));
        when(jwtService.gerarToken(usuario)).thenReturn("jwt-admin");

        AutenticacaoResponse resposta = service.login(
                new LoginRequest("ADMIN@ICONES.LOCAL", "admin123"));

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        assertThat(resposta.token()).isEqualTo("jwt-admin");
        assertThat(resposta.perfil()).isEqualTo(PerfilUsuario.ADMINISTRADOR);
    }

    @Test
    void deveRejeitarLoginComCredenciaisInvalidas() {
        AutenticacaoService service = criarService();
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Credenciais inválidas"));

        assertThatThrownBy(() -> service.login(
                new LoginRequest("maria@exemplo.com", "incorreta")))
                .isInstanceOf(BadCredentialsException.class);
    }

    private AutenticacaoService criarService() {
        return new AutenticacaoService(
                usuarioRepository, clienteRepository, passwordEncoder,
                authenticationManager, jwtService);
    }
}
