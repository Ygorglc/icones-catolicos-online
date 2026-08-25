package dev.y.works.iconescatolicosonline.repository.usuario;

import dev.y.works.iconescatolicosonline.domain.usuario.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    Optional<Usuario> findByTokenRecuperacaoSenhaHash(String tokenHash);
}
