package dev.y.works.iconescatolicosonline.service.pagamento;

import dev.y.works.iconescatolicosonline.domain.encomenda.Encomenda;
import dev.y.works.iconescatolicosonline.domain.encomenda.StatusEncomenda;
import dev.y.works.iconescatolicosonline.domain.encomenda.StatusFinanceiro;
import dev.y.works.iconescatolicosonline.domain.encomenda.TipoEntrega;
import dev.y.works.iconescatolicosonline.domain.pagamento.Pagamento;
import dev.y.works.iconescatolicosonline.domain.pagamento.FormaPagamento;
import dev.y.works.iconescatolicosonline.domain.pagamento.OrigemPagamento;
import dev.y.works.iconescatolicosonline.domain.pagamento.StatusPagamento;
import dev.y.works.iconescatolicosonline.domain.pagamento.TipoPagamento;
import dev.y.works.iconescatolicosonline.domain.usuario.Cliente;
import dev.y.works.iconescatolicosonline.domain.usuario.Administrador;
import dev.y.works.iconescatolicosonline.domain.usuario.Usuario;
import dev.y.works.iconescatolicosonline.dto.pagamento.HistoricoPagamentosResponse;
import dev.y.works.iconescatolicosonline.dto.pagamento.AnalisePagamentoRequest;
import dev.y.works.iconescatolicosonline.dto.pagamento.PagamentoResponse;
import dev.y.works.iconescatolicosonline.dto.pagamento.RegistrarPagamentoRequest;
import dev.y.works.iconescatolicosonline.exception.RegraNegocioException;
import dev.y.works.iconescatolicosonline.repository.encomenda.EncomendaRepository;
import dev.y.works.iconescatolicosonline.repository.pagamento.PagamentoRepository;
import dev.y.works.iconescatolicosonline.repository.usuario.AdministradorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PagamentoServiceTests {

    @Mock PagamentoRepository pagamentoRepository;
    @Mock EncomendaRepository encomendaRepository;
    @Mock AdministradorRepository administradorRepository;

    private PagamentoService service;

    @BeforeEach
    void configurar() {
        service = new PagamentoService(
                pagamentoRepository, encomendaRepository, administradorRepository);
    }

    @Test
    void deveConfirmarSinalELiberarProducao() {
        Encomenda encomenda = criarEncomenda();
        prepararPagamento(encomenda, List.of());

        PagamentoResponse resposta = service.registrar(
                1L, "cliente@teste.local",
                request(TipoPagamento.SINAL));

        assertThat(resposta.valor()).isEqualByComparingTo("75.00");
        assertThat(resposta.totalPago()).isEqualByComparingTo("75.00");
        assertThat(resposta.saldoPendente()).isEqualByComparingTo("175.00");
        assertThat(resposta.status()).isEqualTo(StatusPagamento.CONFIRMADO);
        assertThat(encomenda.getStatusFinanceiro()).isEqualTo(StatusFinanceiro.SINAL_PAGO);
        assertThat(encomenda.getStatusEncomenda()).isEqualTo(StatusEncomenda.PRODUCAO_LIBERADA);
    }

    @Test
    void deveConfirmarPagamentoIntegral() {
        Encomenda encomenda = criarEncomenda();
        prepararPagamento(encomenda, List.of());

        PagamentoResponse resposta = service.registrar(
                1L, "cliente@teste.local",
                request(TipoPagamento.INTEGRAL));

        assertThat(resposta.valor()).isEqualByComparingTo("250.00");
        assertThat(resposta.saldoPendente()).isZero();
        assertThat(encomenda.getStatusFinanceiro())
                .isEqualTo(StatusFinanceiro.PAGO_INTEGRALMENTE);
        assertThat(encomenda.getStatusEncomenda()).isEqualTo(StatusEncomenda.PRODUCAO_LIBERADA);
    }

    @Test
    void deveQuitarRestanteAposSinal() {
        Encomenda encomenda = criarEncomenda();
        encomenda.setStatusEncomenda(StatusEncomenda.EM_PRODUCAO);
        encomenda.setStatusFinanceiro(StatusFinanceiro.SINAL_PAGO);
        Pagamento sinal = criarPagamento(encomenda, TipoPagamento.SINAL, "75.00");
        prepararPagamento(encomenda, List.of(sinal));

        PagamentoResponse resposta = service.registrar(
                1L, "cliente@teste.local",
                request(TipoPagamento.RESTANTE));

        assertThat(resposta.valor()).isEqualByComparingTo("175.00");
        assertThat(resposta.totalPago()).isEqualByComparingTo("250.00");
        assertThat(resposta.saldoPendente()).isZero();
        assertThat(encomenda.getStatusFinanceiro())
                .isEqualTo(StatusFinanceiro.PAGO_INTEGRALMENTE);
    }

    @Test
    void deveImpedirPagamentoRestanteAntesDoSinal() {
        Encomenda encomenda = criarEncomenda();
        when(encomendaRepository.findByIdAndCliente_Usuario_EmailIgnoreCase(
                1L, "cliente@teste.local")).thenReturn(Optional.of(encomenda));
        when(pagamentoRepository.findByEncomenda_IdAndStatus(
                1L, StatusPagamento.CONFIRMADO)).thenReturn(List.of());

        assertThatThrownBy(() -> service.registrar(
                1L, "cliente@teste.local",
                request(TipoPagamento.RESTANTE)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("O restante só pode ser pago após a confirmação do sinal.");
    }

    @Test
    void deveRetornarHistoricoComTotais() {
        Encomenda encomenda = criarEncomenda();
        encomenda.setStatusFinanceiro(StatusFinanceiro.SINAL_PAGO);
        Pagamento sinal = criarPagamento(encomenda, TipoPagamento.SINAL, "75.00");
        when(encomendaRepository.findByIdAndCliente_Usuario_EmailIgnoreCase(
                1L, "cliente@teste.local")).thenReturn(Optional.of(encomenda));
        when(pagamentoRepository.findByEncomenda_IdOrderByCriadoEmAsc(1L))
                .thenReturn(List.of(sinal));

        HistoricoPagamentosResponse historico = service.listarDoCliente(
                1L, "cliente@teste.local");

        assertThat(historico.pagamentos()).hasSize(1);
        assertThat(historico.totalPago()).isEqualByComparingTo("75.00");
        assertThat(historico.saldoPendente()).isEqualByComparingTo("175.00");
    }

    @Test
    void deveManterPagamentoExternoPendenteSemLiberarProducao() {
        Encomenda encomenda = criarEncomenda();
        when(encomendaRepository.findByIdAndCliente_Usuario_EmailIgnoreCase(
                1L, "cliente@teste.local")).thenReturn(Optional.of(encomenda));
        when(pagamentoRepository.findByEncomenda_IdAndStatus(
                1L, StatusPagamento.CONFIRMADO)).thenReturn(List.of());
        when(pagamentoRepository.save(any(Pagamento.class))).thenAnswer(invocation -> {
            Pagamento pagamento = invocation.getArgument(0);
            pagamento.setId(20L);
            return pagamento;
        });

        PagamentoResponse resposta = service.registrar(
                1L, "cliente@teste.local",
                new RegistrarPagamentoRequest(
                        TipoPagamento.SINAL,
                        FormaPagamento.PIX,
                        OrigemPagamento.EXTERNO_MANUAL));

        assertThat(resposta.status()).isEqualTo(StatusPagamento.PENDENTE);
        assertThat(resposta.totalPago()).isZero();
        assertThat(encomenda.getStatusFinanceiro()).isEqualTo(StatusFinanceiro.AGUARDANDO_SINAL);
        assertThat(encomenda.getStatusEncomenda())
                .isEqualTo(StatusEncomenda.AGUARDANDO_PAGAMENTO_SINAL);
    }

    @Test
    void deveConfirmarPagamentoExternoPeloAdministrador() {
        Encomenda encomenda = criarEncomenda();
        Pagamento pagamento = criarPagamento(encomenda, TipoPagamento.SINAL, "75.00");
        pagamento.setStatus(StatusPagamento.PENDENTE);
        pagamento.setOrigem(OrigemPagamento.EXTERNO_MANUAL);
        Administrador administrador = criarAdministrador();
        when(pagamentoRepository.findById(10L)).thenReturn(Optional.of(pagamento));
        when(administradorRepository.findByUsuario_EmailIgnoreCase("admin@teste.local"))
                .thenReturn(Optional.of(administrador));
        when(pagamentoRepository.findByEncomenda_IdAndStatus(
                1L, StatusPagamento.CONFIRMADO)).thenReturn(List.of());
        when(pagamentoRepository.save(pagamento)).thenReturn(pagamento);
        when(encomendaRepository.save(encomenda)).thenReturn(encomenda);

        PagamentoResponse resposta = service.analisar(
                10L, "admin@teste.local",
                new AnalisePagamentoRequest(true, "Valor identificado"));

        assertThat(resposta.status()).isEqualTo(StatusPagamento.CONFIRMADO);
        assertThat(resposta.analisadoPor()).isEqualTo("Administrador");
        assertThat(encomenda.getStatusFinanceiro()).isEqualTo(StatusFinanceiro.SINAL_PAGO);
        assertThat(encomenda.getStatusEncomenda()).isEqualTo(StatusEncomenda.PRODUCAO_LIBERADA);
    }

    @Test
    void deveRejeitarPagamentoExternoPeloAdministrador() {
        Encomenda encomenda = criarEncomenda();
        Pagamento pagamento = criarPagamento(encomenda, TipoPagamento.SINAL, "75.00");
        pagamento.setStatus(StatusPagamento.PENDENTE);
        pagamento.setOrigem(OrigemPagamento.EXTERNO_MANUAL);
        Administrador administrador = criarAdministrador();
        when(pagamentoRepository.findById(10L)).thenReturn(Optional.of(pagamento));
        when(administradorRepository.findByUsuario_EmailIgnoreCase("admin@teste.local"))
                .thenReturn(Optional.of(administrador));
        when(pagamentoRepository.findByEncomenda_IdAndStatus(
                1L, StatusPagamento.CONFIRMADO)).thenReturn(List.of());
        when(pagamentoRepository.save(pagamento)).thenReturn(pagamento);

        PagamentoResponse resposta = service.analisar(
                10L, "admin@teste.local",
                new AnalisePagamentoRequest(false, "Valor não identificado"));

        assertThat(resposta.status()).isEqualTo(StatusPagamento.CANCELADO);
        assertThat(resposta.observacaoAdministrativa()).isEqualTo("Valor não identificado");
        assertThat(encomenda.getStatusFinanceiro()).isEqualTo(StatusFinanceiro.AGUARDANDO_SINAL);
    }

    private void prepararPagamento(Encomenda encomenda, List<Pagamento> confirmados) {
        when(encomendaRepository.findByIdAndCliente_Usuario_EmailIgnoreCase(
                1L, "cliente@teste.local")).thenReturn(Optional.of(encomenda));
        when(pagamentoRepository.findByEncomenda_IdAndStatus(
                1L, StatusPagamento.CONFIRMADO)).thenReturn(confirmados);
        when(pagamentoRepository.save(any(Pagamento.class))).thenAnswer(invocation -> {
            Pagamento pagamento = invocation.getArgument(0);
            pagamento.setId(20L);
            return pagamento;
        });
        when(encomendaRepository.save(encomenda)).thenReturn(encomenda);
    }

    private Encomenda criarEncomenda() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("Maria");
        usuario.setEmail("cliente@teste.local");
        Cliente cliente = new Cliente();
        cliente.setId(2L);
        cliente.setUsuario(usuario);

        Encomenda encomenda = new Encomenda();
        encomenda.setId(1L);
        encomenda.setCliente(cliente);
        encomenda.setValorTotal(new BigDecimal("250.00"));
        encomenda.setValorSinal(new BigDecimal("75.00"));
        encomenda.setStatusEncomenda(StatusEncomenda.AGUARDANDO_PAGAMENTO_SINAL);
        encomenda.setStatusFinanceiro(StatusFinanceiro.AGUARDANDO_SINAL);
        encomenda.setTipoEntrega(TipoEntrega.RETIRADA);
        return encomenda;
    }

    private Pagamento criarPagamento(
            Encomenda encomenda, TipoPagamento tipo, String valor) {
        Pagamento pagamento = new Pagamento();
        pagamento.setId(10L);
        pagamento.setEncomenda(encomenda);
        pagamento.setTipo(tipo);
        pagamento.setFormaPagamento(FormaPagamento.PIX);
        pagamento.setOrigem(OrigemPagamento.SIMULADO_SISTEMA);
        pagamento.setValor(new BigDecimal(valor));
        pagamento.setStatus(StatusPagamento.CONFIRMADO);
        pagamento.setDataPagamento(Instant.parse("2026-08-10T12:00:00Z"));
        pagamento.setReferenciaSimulada("SIM-TESTE");
        return pagamento;
    }

    private RegistrarPagamentoRequest request(TipoPagamento tipo) {
        return new RegistrarPagamentoRequest(
                tipo, FormaPagamento.PIX, OrigemPagamento.SIMULADO_SISTEMA);
    }

    private Administrador criarAdministrador() {
        Usuario usuario = new Usuario();
        usuario.setId(3L);
        usuario.setNome("Administrador");
        usuario.setEmail("admin@teste.local");
        Administrador administrador = new Administrador();
        administrador.setId(4L);
        administrador.setUsuario(usuario);
        return administrador;
    }
}
