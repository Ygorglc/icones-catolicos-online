package dev.y.works.iconescatolicosonline.service.financeiro;

import dev.y.works.iconescatolicosonline.domain.encomenda.Encomenda;
import dev.y.works.iconescatolicosonline.domain.encomenda.StatusFinanceiro;
import dev.y.works.iconescatolicosonline.domain.estoque.IconePronto;
import dev.y.works.iconescatolicosonline.domain.financeiro.Gasto;
import dev.y.works.iconescatolicosonline.domain.financeiro.Venda;
import dev.y.works.iconescatolicosonline.domain.usuario.Cliente;
import dev.y.works.iconescatolicosonline.domain.usuario.Usuario;
import dev.y.works.iconescatolicosonline.dto.financeiro.GastoRequest;
import dev.y.works.iconescatolicosonline.dto.financeiro.GastoResponse;
import dev.y.works.iconescatolicosonline.dto.financeiro.RelatorioFinanceiroResponse;
import dev.y.works.iconescatolicosonline.dto.financeiro.VendaResponse;
import dev.y.works.iconescatolicosonline.exception.ConflitoException;
import dev.y.works.iconescatolicosonline.exception.RegraNegocioException;
import dev.y.works.iconescatolicosonline.repository.encomenda.EncomendaRepository;
import dev.y.works.iconescatolicosonline.repository.financeiro.GastoRepository;
import dev.y.works.iconescatolicosonline.repository.financeiro.VendaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FinanceiroServiceTests {

    @Mock GastoRepository gastoRepository;
    @Mock VendaRepository vendaRepository;
    @Mock EncomendaRepository encomendaRepository;
    private FinanceiroService service;

    @BeforeEach
    void configurar() {
        service = new FinanceiroService(gastoRepository, vendaRepository, encomendaRepository);
    }

    @Test
    void deveRegistrarGastoVinculadoAEncomenda() {
        Encomenda encomenda = criarEncomenda(StatusFinanceiro.AGUARDANDO_SINAL);
        when(encomendaRepository.findById(1L)).thenReturn(Optional.of(encomenda));
        when(gastoRepository.save(any(Gasto.class))).thenAnswer(invocation -> {
            Gasto gasto = invocation.getArgument(0);
            gasto.setId(10L);
            return gasto;
        });

        GastoResponse resposta = service.criarGasto(new GastoRequest(
                1L, "Compra de verniz", new BigDecimal("25.50"),
                LocalDate.of(2026, 8, 20), "MATERIAL"));

        assertThat(resposta.id()).isEqualTo(10L);
        assertThat(resposta.encomendaId()).isEqualTo(1L);
        assertThat(resposta.valor()).isEqualByComparingTo("25.50");
    }

    @Test
    void deveCalcularLucrosAoRegistrarVenda() {
        Encomenda encomenda = criarEncomenda(StatusFinanceiro.PAGO_INTEGRALMENTE);
        IconePronto icone = new IconePronto();
        icone.setCustoProducao(new BigDecimal("120.00"));
        encomenda.setIconePronto(icone);
        when(encomendaRepository.findById(1L)).thenReturn(Optional.of(encomenda));
        when(gastoRepository.findByEncomenda_IdOrderByDataGastoAsc(1L))
                .thenReturn(List.of(criarGasto(encomenda, "30.00")));
        when(vendaRepository.save(any(Venda.class))).thenAnswer(invocation -> {
            Venda venda = invocation.getArgument(0);
            venda.setId(20L);
            return venda;
        });

        VendaResponse resposta = service.registrarVenda(1L);

        assertThat(resposta.valorTotal()).isEqualByComparingTo("300.00");
        assertThat(resposta.custoProducao()).isEqualByComparingTo("120.00");
        assertThat(resposta.gastosAdicionais()).isEqualByComparingTo("30.00");
        assertThat(resposta.lucroBruto()).isEqualByComparingTo("180.00");
        assertThat(resposta.lucroLiquidoEstimado()).isEqualByComparingTo("150.00");
    }

    @Test
    void deveImpedirVendaSemPagamentoIntegral() {
        Encomenda encomenda = criarEncomenda(StatusFinanceiro.SINAL_PAGO);
        when(encomendaRepository.findById(1L)).thenReturn(Optional.of(encomenda));

        assertThatThrownBy(() -> service.registrarVenda(1L))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("pagamento integral");
    }

    @Test
    void deveImpedirVendaDuplicada() {
        when(vendaRepository.existsByEncomenda_Id(1L)).thenReturn(true);

        assertThatThrownBy(() -> service.registrarVenda(1L))
                .isInstanceOf(ConflitoException.class)
                .hasMessage("A encomenda já possui uma venda registrada.");
    }

    @Test
    void deveGerarRelatorioPorPeriodo() {
        Encomenda encomenda = criarEncomenda(StatusFinanceiro.PAGO_INTEGRALMENTE);
        Venda venda = new Venda();
        venda.setId(20L);
        venda.setEncomenda(encomenda);
        venda.setValorTotal(new BigDecimal("300.00"));
        venda.setLucroBruto(new BigDecimal("200.00"));
        venda.setLucroLiquidoEstimado(new BigDecimal("175.00"));
        venda.setDataVenda(Instant.parse("2026-08-20T12:00:00Z"));
        Gasto gasto = criarGasto(null, "25.00");
        when(vendaRepository
                .findByDataVendaGreaterThanEqualAndDataVendaLessThanOrderByDataVendaAsc(
                        any(Instant.class), any(Instant.class))).thenReturn(List.of(venda));
        when(gastoRepository.findByDataGastoBetweenOrderByDataGastoAsc(
                LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31)))
                .thenReturn(List.of(gasto));
        when(gastoRepository.findByEncomenda_IdOrderByDataGastoAsc(1L))
                .thenReturn(List.of());

        RelatorioFinanceiroResponse resposta = service.gerarRelatorio(
                LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31));

        assertThat(resposta.quantidadeVendas()).isEqualTo(1);
        assertThat(resposta.receitaTotal()).isEqualByComparingTo("300.00");
        assertThat(resposta.gastosDoPeriodo()).isEqualByComparingTo("25.00");
        assertThat(resposta.resultadoDoPeriodo()).isEqualByComparingTo("275.00");
    }

    @Test
    void deveRejeitarPeriodoInvertido() {
        assertThatThrownBy(() -> service.gerarRelatorio(
                LocalDate.of(2026, 8, 31), LocalDate.of(2026, 8, 1)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("A data inicial não pode ser posterior à data final.");
    }

    private Encomenda criarEncomenda(StatusFinanceiro statusFinanceiro) {
        Usuario usuario = new Usuario();
        usuario.setNome("Maria");
        Cliente cliente = new Cliente();
        cliente.setId(2L);
        cliente.setUsuario(usuario);
        Encomenda encomenda = new Encomenda();
        encomenda.setId(1L);
        encomenda.setCliente(cliente);
        encomenda.setValorTotal(new BigDecimal("300.00"));
        encomenda.setStatusFinanceiro(statusFinanceiro);
        return encomenda;
    }

    private Gasto criarGasto(Encomenda encomenda, String valor) {
        Gasto gasto = new Gasto();
        gasto.setId(10L);
        gasto.setEncomenda(encomenda);
        gasto.setDescricao("Despesa");
        gasto.setValor(new BigDecimal(valor));
        gasto.setDataGasto(LocalDate.of(2026, 8, 20));
        gasto.setCategoria("MATERIAL");
        return gasto;
    }
}
