package dev.y.works.iconescatolicosonline.service.cliente;

import dev.y.works.iconescatolicosonline.domain.usuario.Cliente;
import dev.y.works.iconescatolicosonline.domain.usuario.Usuario;
import dev.y.works.iconescatolicosonline.dto.cliente.AtualizarPerfilClienteRequest;
import dev.y.works.iconescatolicosonline.exception.ConflitoException;
import dev.y.works.iconescatolicosonline.repository.usuario.ClienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PerfilClienteServiceTests {

    @Mock
    private ClienteRepository clienteRepository;
    private PerfilClienteService service;
    private Cliente cliente;

    @BeforeEach
    void configurar() {
        service = new PerfilClienteService(clienteRepository);
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("Maria");
        usuario.setEmail("maria@teste.com");
        cliente = new Cliente();
        cliente.setId(2L);
        cliente.setUsuario(usuario);
    }

    @Test
    void deveConsultarEAtualizarOProprioPerfil() {
        when(clienteRepository.findByUsuario_EmailIgnoreCase("maria@teste.com"))
                .thenReturn(Optional.of(cliente));
        when(clienteRepository.findByCpf("52998224725")).thenReturn(Optional.empty());
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var resposta = service.atualizar("maria@teste.com",
                new AtualizarPerfilClienteRequest(
                        "Maria Silva", "21999999999", "52998224725", "20040002",
                        "Rua da Assembleia", "10", null, "Centro", "Rio de Janeiro", "RJ"));

        assertThat(resposta.nome()).isEqualTo("Maria Silva");
        assertThat(resposta.email()).isEqualTo("maria@teste.com");
        assertThat(resposta.cpf()).isEqualTo("52998224725");
    }

    @Test
    void deveImpedirCpfDeOutroCliente() {
        Cliente outro = new Cliente();
        outro.setId(3L);
        when(clienteRepository.findByUsuario_EmailIgnoreCase("maria@teste.com"))
                .thenReturn(Optional.of(cliente));
        when(clienteRepository.findByCpf("52998224725")).thenReturn(Optional.of(outro));

        assertThatThrownBy(() -> service.atualizar("maria@teste.com",
                new AtualizarPerfilClienteRequest("Maria", "21999999999", "52998224725",
                        "20040002", "Rua A", "10", null, "Centro", "Rio de Janeiro", "RJ")))
                .isInstanceOf(ConflitoException.class)
                .hasMessage("CPF já cadastrado.");
    }
}
