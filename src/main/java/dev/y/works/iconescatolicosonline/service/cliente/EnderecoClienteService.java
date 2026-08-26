package dev.y.works.iconescatolicosonline.service.cliente;

import dev.y.works.iconescatolicosonline.domain.usuario.EnderecoCliente;
import dev.y.works.iconescatolicosonline.dto.cliente.EnderecoClienteRequest;
import dev.y.works.iconescatolicosonline.dto.cliente.EnderecoClienteResponse;
import dev.y.works.iconescatolicosonline.exception.RecursoNaoEncontradoException;
import dev.y.works.iconescatolicosonline.repository.usuario.ClienteRepository;
import dev.y.works.iconescatolicosonline.repository.usuario.EnderecoClienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class EnderecoClienteService {
    private final EnderecoClienteRepository repository;
    private final ClienteRepository clienteRepository;

    public EnderecoClienteService(EnderecoClienteRepository repository, ClienteRepository clienteRepository) {
        this.repository = repository;
        this.clienteRepository = clienteRepository;
    }

    @Transactional(readOnly = true)
    public List<EnderecoClienteResponse> listar(String email) {
        return repository.findByCliente_Usuario_EmailIgnoreCaseOrderByPrincipalDescIdAsc(email)
                .stream().map(this::mapear).toList();
    }

    @Transactional
    public EnderecoClienteResponse criar(String email, EnderecoClienteRequest request) {
        var cliente = clienteRepository.findByUsuario_EmailIgnoreCase(email)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente não encontrado."));
        boolean primeiro = repository.countByCliente_Id(cliente.getId()) == 0;
        if (request.principal() || primeiro) repository.desmarcarPrincipal(cliente.getId());
        EnderecoCliente endereco = new EnderecoCliente();
        endereco.setCliente(cliente);
        preencher(endereco, request);
        endereco.setPrincipal(request.principal() || primeiro);
        return mapear(repository.save(endereco));
    }

    @Transactional
    public EnderecoClienteResponse atualizar(String email, Long id, EnderecoClienteRequest request) {
        EnderecoCliente endereco = buscar(email, id);
        if (request.principal()) repository.desmarcarPrincipal(endereco.getCliente().getId());
        boolean manterPrincipal = endereco.isPrincipal() || request.principal();
        preencher(endereco, request);
        endereco.setPrincipal(manterPrincipal);
        return mapear(repository.save(endereco));
    }

    @Transactional
    public void excluir(String email, Long id) {
        EnderecoCliente endereco = buscar(email, id);
        boolean eraPrincipal = endereco.isPrincipal();
        Long clienteId = endereco.getCliente().getId();
        repository.delete(endereco);
        repository.flush();
        if (eraPrincipal) repository.findByCliente_Usuario_EmailIgnoreCaseOrderByPrincipalDescIdAsc(email)
                .stream().findFirst().ifPresent(restante -> { restante.setPrincipal(true); repository.save(restante); });
    }

    private EnderecoCliente buscar(String email, Long id) {
        return repository.findByIdAndCliente_Usuario_EmailIgnoreCase(id, email)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Endereço não encontrado."));
    }

    private void preencher(EnderecoCliente endereco, EnderecoClienteRequest request) {
        endereco.setApelido(request.apelido().trim());
        endereco.setCep(request.cep().trim());
        endereco.setLogradouro(request.logradouro().trim());
        endereco.setNumero(request.numero().trim());
        endereco.setComplemento(opcional(request.complemento()));
        endereco.setBairro(request.bairro().trim());
        endereco.setCidade(request.cidade().trim());
        endereco.setUf(request.uf().trim().toUpperCase(Locale.ROOT));
    }

    private String opcional(String valor) { return valor == null || valor.isBlank() ? null : valor.trim(); }
    private EnderecoClienteResponse mapear(EnderecoCliente e) { return new EnderecoClienteResponse(e.getId(), e.getApelido(), e.getCep(), e.getLogradouro(), e.getNumero(), e.getComplemento(), e.getBairro(), e.getCidade(), e.getUf(), e.isPrincipal()); }
}
