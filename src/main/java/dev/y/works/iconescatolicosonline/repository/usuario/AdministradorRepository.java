package dev.y.works.iconescatolicosonline.repository.usuario;

import dev.y.works.iconescatolicosonline.domain.usuario.Administrador;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdministradorRepository extends JpaRepository<Administrador, Long> {

    Optional<Administrador> findByUsuario_Id(Long usuarioId);

    Optional<Administrador> findByUsuario_EmailIgnoreCase(String email);
}
