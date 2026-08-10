package dev.y.works.iconescatolicosonline.repository.usuario;

import dev.y.works.iconescatolicosonline.domain.usuario.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    Optional<Cliente> findByUsuario_Id(Long usuarioId);

    Optional<Cliente> findByCpf(String cpf);

    boolean existsByCpf(String cpf);

    Optional<Cliente> findByUsuario_EmailIgnoreCase(String email);
}
