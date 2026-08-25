package dev.y.works.iconescatolicosonline.service.autenticacao;

import dev.y.works.iconescatolicosonline.domain.usuario.Usuario;
import dev.y.works.iconescatolicosonline.dto.autenticacao.RedefinirSenhaRequest;
import dev.y.works.iconescatolicosonline.dto.autenticacao.SolicitarRecuperacaoSenhaResponse;
import dev.y.works.iconescatolicosonline.dto.cliente.AlterarSenhaRequest;
import dev.y.works.iconescatolicosonline.exception.RegraNegocioException;
import dev.y.works.iconescatolicosonline.repository.usuario.UsuarioRepository;
import dev.y.works.iconescatolicosonline.service.email.EmailRecuperacaoSenhaService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets; import java.security.MessageDigest; import java.security.NoSuchAlgorithmException; import java.time.Instant; import java.time.temporal.ChronoUnit; import java.util.HexFormat; import java.util.Locale; import java.util.UUID;

@Service
public class SenhaService {
    private static final long EXPIRACAO_MINUTOS = 30;
    private final UsuarioRepository usuarioRepository; private final PasswordEncoder passwordEncoder; private final EmailRecuperacaoSenhaService emailService;
    public SenhaService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, EmailRecuperacaoSenhaService emailService) {
        this.usuarioRepository = usuarioRepository; this.passwordEncoder = passwordEncoder; this.emailService = emailService;
    }
    @Transactional
    public SolicitarRecuperacaoSenhaResponse solicitar(String email) {
        String token = UUID.randomUUID() + UUID.randomUUID().toString();
        var usuarioEncontrado = usuarioRepository.findByEmailIgnoreCase(email.trim().toLowerCase(Locale.ROOT)).filter(Usuario::isAtivo);
        usuarioEncontrado.ifPresent(usuario -> {
            usuario.setTokenRecuperacaoSenhaHash(hash(token)); usuario.setTokenRecuperacaoSenhaExpiraEm(Instant.now().plus(EXPIRACAO_MINUTOS, ChronoUnit.MINUTES)); usuarioRepository.save(usuario);
            emailService.enviarRecuperacao(usuario.getEmail(), usuario.getNome(), token, EXPIRACAO_MINUTOS);
        });
        return new SolicitarRecuperacaoSenhaResponse("Verifique no e-mail as instruções de recuperação, também verifique o spam.", EXPIRACAO_MINUTOS);
    }
    @Transactional
    public void redefinir(RedefinirSenhaRequest request) {
        Usuario usuario = usuarioRepository.findByTokenRecuperacaoSenhaHash(hash(request.token().trim())).orElseThrow(() -> new RegraNegocioException("Token de recuperação inválido ou expirado."));
        if (usuario.getTokenRecuperacaoSenhaExpiraEm() == null || usuario.getTokenRecuperacaoSenhaExpiraEm().isBefore(Instant.now())) throw new RegraNegocioException("Token de recuperação inválido ou expirado.");
        usuario.setSenha(passwordEncoder.encode(request.novaSenha())); usuario.setTokenRecuperacaoSenhaHash(null); usuario.setTokenRecuperacaoSenhaExpiraEm(null); usuarioRepository.save(usuario);
    }
    @Transactional
    public void alterar(String email, AlterarSenhaRequest request) {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email).orElseThrow();
        if (!passwordEncoder.matches(request.senhaAtual(), usuario.getSenha())) throw new RegraNegocioException("A senha atual está incorreta.");
        if (passwordEncoder.matches(request.novaSenha(), usuario.getSenha())) throw new RegraNegocioException("A nova senha deve ser diferente da senha atual.");
        usuario.setSenha(passwordEncoder.encode(request.novaSenha())); usuarioRepository.save(usuario);
    }
    private String hash(String token) { try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8))); } catch (NoSuchAlgorithmException e) { throw new IllegalStateException(e); } }
}
