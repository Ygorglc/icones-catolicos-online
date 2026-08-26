package dev.y.works.iconescatolicosonline.service.cliente;

import dev.y.works.iconescatolicosonline.domain.usuario.Cliente;
import dev.y.works.iconescatolicosonline.domain.usuario.EnderecoCliente;
import dev.y.works.iconescatolicosonline.dto.cliente.EnderecoClienteRequest;
import dev.y.works.iconescatolicosonline.exception.RecursoNaoEncontradoException;
import dev.y.works.iconescatolicosonline.repository.usuario.ClienteRepository;
import dev.y.works.iconescatolicosonline.repository.usuario.EnderecoClienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnderecoClienteServiceTests {
    @Mock EnderecoClienteRepository repository;
    @Mock ClienteRepository clienteRepository;
    private EnderecoClienteService service;
    private Cliente cliente;

    @BeforeEach void setup() { service = new EnderecoClienteService(repository, clienteRepository); cliente = new Cliente(); cliente.setId(1L); }

    @Test void deveDefinirPrimeiroEnderecoComoPrincipal() {
        when(clienteRepository.findByUsuario_EmailIgnoreCase("cliente@teste.com")).thenReturn(Optional.of(cliente));
        when(repository.countByCliente_Id(1L)).thenReturn(0L);
        when(repository.save(any())).thenAnswer(invocation -> { EnderecoCliente endereco = invocation.getArgument(0); endereco.setId(10L); return endereco; });
        var resposta = service.criar("cliente@teste.com", request(false));
        assertThat(resposta.principal()).isTrue();
        verify(repository).desmarcarPrincipal(1L);
    }

    @Test void deveImpedirAcessoAoEnderecoDeOutroCliente() {
        when(repository.findByIdAndCliente_Usuario_EmailIgnoreCase(9L, "cliente@teste.com")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.atualizar("cliente@teste.com", 9L, request(false)))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    private EnderecoClienteRequest request(boolean principal) {
        return new EnderecoClienteRequest("Casa", "20040002", "Rua da Assembleia", "10", null,
                "Centro", "Rio de Janeiro", "RJ", principal);
    }
}
