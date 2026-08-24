package dev.y.works.iconescatolicosonline.config;

import dev.y.works.iconescatolicosonline.domain.usuario.Administrador;
import dev.y.works.iconescatolicosonline.domain.usuario.PerfilUsuario;
import dev.y.works.iconescatolicosonline.domain.usuario.Usuario;
import dev.y.works.iconescatolicosonline.repository.usuario.AdministradorRepository;
import dev.y.works.iconescatolicosonline.repository.usuario.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Locale;

@Configuration
@Profile("dev")
public class AdministradorInicialConfig {

    @Bean
    @Order(1)
    CommandLineRunner criarAdministradorInicial(
            UsuarioRepository usuarioRepository,
            AdministradorRepository administradorRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.admin-inicial.nome}") String nome,
            @Value("${app.admin-inicial.email}") String email,
            @Value("${app.admin-inicial.senha}") String senha) {
        return args -> {
            String emailNormalizado = email.trim().toLowerCase(Locale.ROOT);
            if (usuarioRepository.existsByEmailIgnoreCase(emailNormalizado)) {
                return;
            }

            Usuario usuario = new Usuario();
            usuario.setNome(nome.trim());
            usuario.setEmail(emailNormalizado);
            usuario.setSenha(passwordEncoder.encode(senha));
            usuario.setPerfil(PerfilUsuario.ADMINISTRADOR);
            usuario.setAtivo(true);
            usuario = usuarioRepository.save(usuario);

            Administrador administrador = new Administrador();
            administrador.setUsuario(usuario);
            administrador.setCargo("Administrador do sistema");
            administrador.setNivelAcesso("TOTAL");
            administradorRepository.save(administrador);
        };
    }
}
