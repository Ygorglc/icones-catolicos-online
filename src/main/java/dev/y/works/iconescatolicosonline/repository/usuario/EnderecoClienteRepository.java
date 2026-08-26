package dev.y.works.iconescatolicosonline.repository.usuario;

import dev.y.works.iconescatolicosonline.domain.usuario.EnderecoCliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface EnderecoClienteRepository extends JpaRepository<EnderecoCliente, Long> {
    List<EnderecoCliente> findByCliente_Usuario_EmailIgnoreCaseOrderByPrincipalDescIdAsc(String email);
    Optional<EnderecoCliente> findByIdAndCliente_Usuario_EmailIgnoreCase(Long id, String email);
    long countByCliente_Id(Long clienteId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update EnderecoCliente e set e.principal = false where e.cliente.id = :clienteId and e.principal = true")
    void desmarcarPrincipal(Long clienteId);
}
