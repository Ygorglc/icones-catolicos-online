package dev.y.works.iconescatolicosonline.service.autenticacao;

import dev.y.works.iconescatolicosonline.domain.usuario.Usuario;
import dev.y.works.iconescatolicosonline.dto.autenticacao.MensagemResponse;
import dev.y.works.iconescatolicosonline.exception.RegraNegocioException;
import dev.y.works.iconescatolicosonline.repository.usuario.UsuarioRepository;
import dev.y.works.iconescatolicosonline.service.email.EmailConfirmacaoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;

@Service
public class ConfirmacaoEmailService {
    static final long EXPIRACAO_HORAS = 24;
    private final UsuarioRepository usuarioRepository;
    private final EmailConfirmacaoService emailService;
    private final SecureRandom secureRandom = new SecureRandom();

    public ConfirmacaoEmailService(UsuarioRepository usuarioRepository, EmailConfirmacaoService emailService) {
        this.usuarioRepository = usuarioRepository;
        this.emailService = emailService;
    }

    @Transactional
    public void gerarEEnviar(Usuario usuario) {
        String token = gerarToken();
        usuario.setEmailVerificado(false);
        usuario.setTokenConfirmacaoEmailHash(hash(token));
        usuario.setTokenConfirmacaoEmailExpiraEm(Instant.now().plus(EXPIRACAO_HORAS, ChronoUnit.HOURS));
        usuarioRepository.save(usuario);
        emailService.enviar(usuario.getEmail(), usuario.getNome(), token, EXPIRACAO_HORAS);
    }

    @Transactional
    public MensagemResponse confirmar(String token) {
        Usuario usuario = usuarioRepository.findByTokenConfirmacaoEmailHash(hash(token.trim()))
                .orElseThrow(() -> new RegraNegocioException("Link de confirmação inválido ou expirado."));
        if (usuario.getTokenConfirmacaoEmailExpiraEm() == null
                || usuario.getTokenConfirmacaoEmailExpiraEm().isBefore(Instant.now())) {
            throw new RegraNegocioException("Link de confirmação inválido ou expirado.");
        }
        usuario.setEmailVerificado(true);
        usuario.setTokenConfirmacaoEmailHash(null);
        usuario.setTokenConfirmacaoEmailExpiraEm(null);
        usuarioRepository.save(usuario);
        return new MensagemResponse("E-mail confirmado. Você já pode entrar na sua conta.");
    }

    @Transactional
    public MensagemResponse reenviar(String email) {
        usuarioRepository.findByEmailIgnoreCase(email.trim().toLowerCase(Locale.ROOT))
                .filter(usuario -> usuario.isAtivo() && !usuario.isEmailVerificado())
                .ifPresent(this::gerarEEnviar);
        return new MensagemResponse("Se o cadastro estiver pendente, enviaremos uma nova confirmação. Verifique também o spam.");
    }

    private String gerarToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String token) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 indisponível.", exception);
        }
    }
}
