package dev.y.works.iconescatolicosonline.service.encomenda;

import dev.y.works.iconescatolicosonline.domain.catalogo.ModeloIcone;
import dev.y.works.iconescatolicosonline.domain.catalogo.TamanhoIcone;
import dev.y.works.iconescatolicosonline.domain.encomenda.Encomenda;
import dev.y.works.iconescatolicosonline.domain.encomenda.StatusEncomenda;
import dev.y.works.iconescatolicosonline.domain.encomenda.StatusFinanceiro;
import dev.y.works.iconescatolicosonline.domain.encomenda.TipoEntrega;
import dev.y.works.iconescatolicosonline.domain.usuario.Cliente;
import dev.y.works.iconescatolicosonline.domain.usuario.Usuario;
import dev.y.works.iconescatolicosonline.dto.encomenda.CriarEncomendaRequest;
import dev.y.works.iconescatolicosonline.dto.encomenda.EncomendaResponse;
import dev.y.works.iconescatolicosonline.dto.encomenda.ItemEncomendaRequest;
import dev.y.works.iconescatolicosonline.dto.encomenda.PersonalizacaoRequest;
import dev.y.works.iconescatolicosonline.exception.RegraNegocioException;
import dev.y.works.iconescatolicosonline.repository.catalogo.ModeloIconeRepository;
import dev.y.works.iconescatolicosonline.repository.configuracao.ConfiguracaoLojaRepository;
import dev.y.works.iconescatolicosonline.repository.encomenda.EncomendaRepository;
import dev.y.works.iconescatolicosonline.repository.usuario.ClienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EncomendaServiceTests {

    @Mock EncomendaRepository encomendaRepository;
    @Mock ClienteRepository clienteRepository;
    @Mock ModeloIconeRepository modeloIconeRepository;
    @Mock ConfiguracaoLojaRepository configuracaoLojaRepository;

    private EncomendaService service;

    @BeforeEach
    void configurar() {
        service = new EncomendaService(
                encomendaRepository, clienteRepository, modeloIconeRepository, configuracaoLojaRepository,
                new BigDecimal("30"));
    }

    @Test
    void deveCriarEncomendaCalculandoTotalESinal() {
        Cliente cliente = criarCliente();
        ModeloIcone modelo = criarModelo();
        when(clienteRepository.findByUsuario_EmailIgnoreCase("cliente@teste.local"))
                .thenReturn(Optional.of(cliente));
        when(modeloIconeRepository.findByIdAndAtivoTrue(10L)).thenReturn(Optional.of(modelo));
        when(encomendaRepository.save(any(Encomenda.class))).thenAnswer(invocation -> {
            Encomenda encomenda = invocation.getArgument(0);
            encomenda.setId(100L);
            encomenda.getItens().getFirst().setId(101L);
            return encomenda;
        });
        CriarEncomendaRequest request = new CriarEncomendaRequest(
                TipoEntrega.ENTREGA,
                " Rua das Flores, 10 ",
                "Presente de casamento",
                List.of(new ItemEncomendaRequest(
                        10L,
                        2,
                        new PersonalizacaoRequest(
                                TamanhoIcone.MEDIO, "Envernizado", "Abençoe este lar",
                                "Família Silva", null))));

        EncomendaResponse resposta = service.criar("cliente@teste.local", request);

        assertThat(resposta.id()).isEqualTo(100L);
        assertThat(resposta.valorTotal()).isEqualByComparingTo("500.00");
        assertThat(resposta.valorSinal()).isEqualByComparingTo("150.00");
        assertThat(resposta.statusEncomenda())
                .isEqualTo(StatusEncomenda.AGUARDANDO_PAGAMENTO_INICIAL);
        assertThat(resposta.statusFinanceiro()).isEqualTo(StatusFinanceiro.AGUARDANDO_SINAL);
        assertThat(resposta.enderecoEntrega()).isEqualTo("Rua das Flores, 10");
        assertThat(resposta.itens().getFirst().subtotal()).isEqualByComparingTo("500.00");
        assertThat(resposta.itens().getFirst().personalizacao().nomeFamilia())
                .isEqualTo("Família Silva");
    }

    @Test
    void deveExigirEnderecoParaEntrega() {
        when(clienteRepository.findByUsuario_EmailIgnoreCase("cliente@teste.local"))
                .thenReturn(Optional.of(criarCliente()));
        CriarEncomendaRequest request = new CriarEncomendaRequest(
                TipoEntrega.ENTREGA, " ", null,
                List.of(new ItemEncomendaRequest(10L, 1, null)));

        assertThatThrownBy(() -> service.criar("cliente@teste.local", request))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("O endereço é obrigatório para entrega.");
    }

    @Test
    void deveAtualizarStatusQuandoTransicaoForPermitida() {
        Encomenda encomenda = criarEncomenda(StatusEncomenda.PAGAMENTO_INICIAL_CONFIRMADO);
        when(encomendaRepository.findById(1L)).thenReturn(Optional.of(encomenda));
        when(encomendaRepository.save(encomenda)).thenReturn(encomenda);

        EncomendaResponse resposta = service.atualizarStatus(
                1L, StatusEncomenda.PRODUCAO_LIBERADA);

        assertThat(resposta.statusEncomenda()).isEqualTo(StatusEncomenda.PRODUCAO_LIBERADA);
    }

    @Test
    void deveRejeitarSaltoDeStatus() {
        Encomenda encomenda = criarEncomenda(StatusEncomenda.AGUARDANDO_PAGAMENTO_INICIAL);
        when(encomendaRepository.findById(1L)).thenReturn(Optional.of(encomenda));

        assertThatThrownBy(() -> service.atualizarStatus(1L, StatusEncomenda.EM_PRODUCAO))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("Transição de status não permitida");
    }

    @Test
    void deveImpedirConclusaoComSaldoPendente() {
        Encomenda encomenda = criarEncomenda(StatusEncomenda.ENVIADO_OU_RETIRADO);
        when(encomendaRepository.findById(1L)).thenReturn(Optional.of(encomenda));

        assertThatThrownBy(() -> service.atualizarStatus(1L, StatusEncomenda.CONCLUIDO))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("A encomenda só pode ser concluída após o pagamento integral.");
    }

    private Cliente criarCliente() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("Maria");
        usuario.setEmail("cliente@teste.local");
        Cliente cliente = new Cliente();
        cliente.setId(2L);
        cliente.setUsuario(usuario);
        return cliente;
    }

    private ModeloIcone criarModelo() {
        ModeloIcone modelo = new ModeloIcone();
        modelo.setId(10L);
        modelo.setNome("Sagrada Família");
        modelo.setPrecoBase(new BigDecimal("250.00"));
        modelo.setAtivo(true);
        return modelo;
    }

    private Encomenda criarEncomenda(StatusEncomenda status) {
        Encomenda encomenda = new Encomenda();
        encomenda.setId(1L);
        encomenda.setCliente(criarCliente());
        encomenda.setStatusEncomenda(status);
        encomenda.setStatusFinanceiro(StatusFinanceiro.AGUARDANDO_SINAL);
        encomenda.setTipoEntrega(TipoEntrega.RETIRADA);
        encomenda.setValorTotal(new BigDecimal("250.00"));
        encomenda.setValorSinal(new BigDecimal("75.00"));
        return encomenda;
    }
}
