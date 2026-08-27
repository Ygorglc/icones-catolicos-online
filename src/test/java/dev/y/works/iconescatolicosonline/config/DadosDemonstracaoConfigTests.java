package dev.y.works.iconescatolicosonline.config;

import dev.y.works.iconescatolicosonline.domain.catalogo.ModeloIcone;
import dev.y.works.iconescatolicosonline.domain.encomenda.Encomenda;
import dev.y.works.iconescatolicosonline.domain.encomenda.StatusEncomenda;
import dev.y.works.iconescatolicosonline.domain.usuario.Administrador;
import dev.y.works.iconescatolicosonline.domain.usuario.Cliente;
import dev.y.works.iconescatolicosonline.domain.usuario.Usuario;
import dev.y.works.iconescatolicosonline.repository.catalogo.ConteudoDevocionalRepository;
import dev.y.works.iconescatolicosonline.repository.catalogo.ModeloIconeRepository;
import dev.y.works.iconescatolicosonline.repository.encomenda.EncomendaRepository;
import dev.y.works.iconescatolicosonline.repository.estoque.IconeProntoRepository;
import dev.y.works.iconescatolicosonline.repository.estoque.MaterialRepository;
import dev.y.works.iconescatolicosonline.repository.financeiro.GastoRepository;
import dev.y.works.iconescatolicosonline.repository.financeiro.VendaRepository;
import dev.y.works.iconescatolicosonline.repository.pagamento.PagamentoRepository;
import dev.y.works.iconescatolicosonline.repository.usuario.AdministradorRepository;
import dev.y.works.iconescatolicosonline.repository.usuario.ClienteRepository;
import dev.y.works.iconescatolicosonline.repository.usuario.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DadosDemonstracaoConfigTests {

    @Mock UsuarioRepository usuarioRepository;
    @Mock ClienteRepository clienteRepository;
    @Mock AdministradorRepository administradorRepository;
    @Mock ModeloIconeRepository modeloRepository;
    @Mock ConteudoDevocionalRepository conteudoRepository;
    @Mock EncomendaRepository encomendaRepository;
    @Mock MaterialRepository materialRepository;
    @Mock IconeProntoRepository iconeProntoRepository;
    @Mock PagamentoRepository pagamentoRepository;
    @Mock GastoRepository gastoRepository;
    @Mock VendaRepository vendaRepository;
    @Mock PasswordEncoder passwordEncoder;
    private DadosDemonstracaoConfig carga;

    @BeforeEach
    void configurar() {
        carga = new DadosDemonstracaoConfig(
                usuarioRepository, clienteRepository, administradorRepository,
                modeloRepository, conteudoRepository, encomendaRepository,
                materialRepository, iconeProntoRepository, pagamentoRepository,
                gastoRepository, vendaRepository, passwordEncoder,
                "CLIENTE@ICONES.LOCAL", "cliente123");
    }

    @Test
    void deveIgnorarCargaQuandoClienteDemoJaExistir() {
        when(usuarioRepository.existsByEmailIgnoreCase("cliente@icones.local"))
                .thenReturn(true);

        carga.run(null);

        verify(usuarioRepository, never()).save(any());
        verify(modeloRepository, never()).save(any());
        verify(encomendaRepository, never()).save(any());
    }

    @Test
    void deveCriarConjuntoCompletoDeDadosDemonstracao() {
        Administrador administrador = new Administrador();
        administrador.setId(1L);
        when(administradorRepository.findAll()).thenReturn(List.of(administrador));
        when(passwordEncoder.encode("cliente123")).thenReturn("senha-bcrypt");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario usuario = invocation.getArgument(0);
            usuario.setId(2L);
            return usuario;
        });
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(invocation -> {
            Cliente cliente = invocation.getArgument(0);
            cliente.setId(2L);
            return cliente;
        });
        AtomicLong modeloId = new AtomicLong(10);
        when(modeloRepository.save(any(ModeloIcone.class))).thenAnswer(invocation -> {
            ModeloIcone modelo = invocation.getArgument(0);
            modelo.setId(modeloId.getAndIncrement());
            return modelo;
        });
        AtomicLong encomendaId = new AtomicLong(20);
        when(encomendaRepository.save(any(Encomenda.class))).thenAnswer(invocation -> {
            Encomenda encomenda = invocation.getArgument(0);
            encomenda.setId(encomendaId.getAndIncrement());
            return encomenda;
        });

        carga.run(null);

        verify(usuarioRepository).save(any(Usuario.class));
        verify(clienteRepository).save(any(Cliente.class));
        verify(modeloRepository, times(3)).save(any(ModeloIcone.class));
        verify(conteudoRepository, times(3)).save(any());
        verify(materialRepository, times(4)).save(any());
        verify(iconeProntoRepository, times(3)).save(any());
        verify(pagamentoRepository, times(4)).save(any());
        verify(gastoRepository, times(4)).save(any());
        verify(vendaRepository).save(any());

        ArgumentCaptor<Encomenda> captor = ArgumentCaptor.forClass(Encomenda.class);
        verify(encomendaRepository, times(4)).save(captor.capture());
        assertThat(captor.getAllValues())
                .extracting(Encomenda::getStatusEncomenda)
                .containsExactly(
                        StatusEncomenda.AGUARDANDO_PAGAMENTO_INICIAL,
                        StatusEncomenda.EM_PRODUCAO,
                        StatusEncomenda.EM_ACABAMENTO,
                        StatusEncomenda.CONCLUIDO);
    }
}
