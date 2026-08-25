package dev.y.works.iconescatolicosonline.service.cliente;

import dev.y.works.iconescatolicosonline.domain.usuario.Cliente;
import dev.y.works.iconescatolicosonline.dto.cliente.AtualizarPerfilClienteRequest;
import dev.y.works.iconescatolicosonline.dto.cliente.PerfilClienteResponse;
import dev.y.works.iconescatolicosonline.exception.ConflitoException;
import dev.y.works.iconescatolicosonline.exception.RecursoNaoEncontradoException;
import dev.y.works.iconescatolicosonline.repository.usuario.ClienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PerfilClienteService {

    private final ClienteRepository clienteRepository;

    public PerfilClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Transactional(readOnly = true)
    public PerfilClienteResponse buscar(String email) {
        return mapear(buscarCliente(email));
    }

    @Transactional
    public PerfilClienteResponse atualizar(
            String email, AtualizarPerfilClienteRequest request) {
        Cliente cliente = buscarCliente(email);
        String cpf = normalizar(request.cpf());
        if (cpf != null) {
            clienteRepository.findByCpf(cpf)
                    .filter(outro -> !outro.getId().equals(cliente.getId()))
                    .ifPresent(outro -> {
                        throw new ConflitoException("CPF já cadastrado.");
                    });
        }
        cliente.getUsuario().setNome(request.nome().trim());
        cliente.setTelefone(normalizar(request.telefone()));
        cliente.setCpf(cpf);
        cliente.setEndereco(normalizar(request.endereco()));
        return mapear(clienteRepository.save(cliente));
    }

    private Cliente buscarCliente(String email) {
        return clienteRepository.findByUsuario_EmailIgnoreCase(email)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Cliente não encontrado."));
    }

    private String normalizar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    private PerfilClienteResponse mapear(Cliente cliente) {
        return new PerfilClienteResponse(
                cliente.getUsuario().getId(), cliente.getId(),
                cliente.getUsuario().getNome(), cliente.getUsuario().getEmail(),
                cliente.getTelefone(), cliente.getCpf(), cliente.getEndereco());
    }
}
