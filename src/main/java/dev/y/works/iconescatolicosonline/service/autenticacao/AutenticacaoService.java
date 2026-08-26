package dev.y.works.iconescatolicosonline.service.autenticacao;

import dev.y.works.iconescatolicosonline.domain.usuario.Cliente;
import dev.y.works.iconescatolicosonline.domain.usuario.PerfilUsuario;
import dev.y.works.iconescatolicosonline.domain.usuario.Usuario;
import dev.y.works.iconescatolicosonline.dto.autenticacao.AutenticacaoResponse;
import dev.y.works.iconescatolicosonline.dto.autenticacao.CadastroClienteResponse;
import dev.y.works.iconescatolicosonline.dto.autenticacao.CadastroClienteRequest;
import dev.y.works.iconescatolicosonline.dto.autenticacao.LoginRequest;
import dev.y.works.iconescatolicosonline.exception.ConflitoException;
import dev.y.works.iconescatolicosonline.exception.RegraNegocioException;
import dev.y.works.iconescatolicosonline.repository.usuario.ClienteRepository;
import dev.y.works.iconescatolicosonline.repository.usuario.UsuarioRepository;
import dev.y.works.iconescatolicosonline.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AutenticacaoService {

    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final ConfirmacaoEmailService confirmacaoEmailService;

    public AutenticacaoService(
            UsuarioRepository usuarioRepository,
            ClienteRepository clienteRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            ConfirmacaoEmailService confirmacaoEmailService) {
        this.usuarioRepository = usuarioRepository;
        this.clienteRepository = clienteRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.confirmacaoEmailService = confirmacaoEmailService;
    }

    @Transactional
    public CadastroClienteResponse cadastrarCliente(CadastroClienteRequest request) {
        String email = normalizarEmail(request.email());
        String cpf = request.cpf().trim();
        String telefone = request.telefone().trim();
        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflitoException("Já existe um usuário com esse e-mail.");
        }
        if (!CpfValidator.isValid(cpf)) {
            throw new RegraNegocioException("CPF inválido.");
        }
        if (clienteRepository.existsByCpf(cpf)) {
            throw new ConflitoException("Já existe um cliente com esse CPF.");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(request.nome().trim());
        usuario.setEmail(email);
        usuario.setSenha(passwordEncoder.encode(request.senha()));
        usuario.setPerfil(PerfilUsuario.CLIENTE);
        usuario.setAtivo(true);
        usuario.setEmailVerificado(false);
        usuario = usuarioRepository.save(usuario);

        Cliente cliente = new Cliente();
        cliente.setUsuario(usuario);
        cliente.setTelefone(telefone);
        cliente.setCpf(cpf);
        cliente.setEndereco(request.endereco());
        clienteRepository.save(cliente);

        confirmacaoEmailService.gerarEEnviar(usuario);
        return new CadastroClienteResponse("Cadastro realizado. Verifique seu e-mail para confirmar a conta; confira também o spam.");
    }

    @Transactional(readOnly = true)
    public AutenticacaoResponse login(LoginRequest request) {
        String email = normalizarEmail(request.email());
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.senha()));
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email).orElseThrow();
        return criarResposta(usuario);
    }

    private AutenticacaoResponse criarResposta(Usuario usuario) {
        return new AutenticacaoResponse(
                jwtService.gerarToken(usuario),
                "Bearer",
                jwtService.getExpiracaoEmSegundos(),
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getPerfil());
    }

    private String normalizarEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
