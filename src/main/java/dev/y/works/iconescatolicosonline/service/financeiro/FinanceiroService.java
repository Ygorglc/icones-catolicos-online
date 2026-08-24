package dev.y.works.iconescatolicosonline.service.financeiro;

import dev.y.works.iconescatolicosonline.domain.encomenda.Encomenda;
import dev.y.works.iconescatolicosonline.domain.encomenda.StatusFinanceiro;
import dev.y.works.iconescatolicosonline.domain.financeiro.Gasto;
import dev.y.works.iconescatolicosonline.domain.financeiro.Venda;
import dev.y.works.iconescatolicosonline.dto.financeiro.GastoRequest;
import dev.y.works.iconescatolicosonline.dto.financeiro.GastoResponse;
import dev.y.works.iconescatolicosonline.dto.financeiro.RelatorioFinanceiroResponse;
import dev.y.works.iconescatolicosonline.dto.financeiro.VendaResponse;
import dev.y.works.iconescatolicosonline.exception.ConflitoException;
import dev.y.works.iconescatolicosonline.exception.RecursoNaoEncontradoException;
import dev.y.works.iconescatolicosonline.exception.RegraNegocioException;
import dev.y.works.iconescatolicosonline.repository.encomenda.EncomendaRepository;
import dev.y.works.iconescatolicosonline.repository.financeiro.GastoRepository;
import dev.y.works.iconescatolicosonline.repository.financeiro.VendaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@Service
public class FinanceiroService {

    private final GastoRepository gastoRepository;
    private final VendaRepository vendaRepository;
    private final EncomendaRepository encomendaRepository;

    public FinanceiroService(
            GastoRepository gastoRepository,
            VendaRepository vendaRepository,
            EncomendaRepository encomendaRepository) {
        this.gastoRepository = gastoRepository;
        this.vendaRepository = vendaRepository;
        this.encomendaRepository = encomendaRepository;
    }

    @Transactional(readOnly = true)
    public List<GastoResponse> listarGastos() {
        return gastoRepository.findAllByOrderByDataGastoDescIdDesc()
                .stream().map(this::mapearGasto).toList();
    }

    @Transactional(readOnly = true)
    public GastoResponse buscarGasto(Long id) {
        return mapearGasto(buscarGastoEntidade(id));
    }

    @Transactional
    public GastoResponse criarGasto(GastoRequest request) {
        Gasto gasto = new Gasto();
        aplicarGasto(gasto, request);
        Gasto salvo = gastoRepository.save(gasto);
        recalcularVendaSeExistir(salvo.getEncomenda());
        return mapearGasto(salvo);
    }

    @Transactional
    public GastoResponse atualizarGasto(Long id, GastoRequest request) {
        Gasto gasto = buscarGastoEntidade(id);
        Encomenda encomendaAnterior = gasto.getEncomenda();
        aplicarGasto(gasto, request);
        Gasto salvo = gastoRepository.save(gasto);
        if (encomendaAnterior != null
                && (salvo.getEncomenda() == null
                || !encomendaAnterior.getId().equals(salvo.getEncomenda().getId()))) {
            recalcularVendaSeExistir(encomendaAnterior);
        }
        recalcularVendaSeExistir(salvo.getEncomenda());
        return mapearGasto(salvo);
    }

    @Transactional
    public void excluirGasto(Long id) {
        Gasto gasto = buscarGastoEntidade(id);
        Encomenda encomenda = gasto.getEncomenda();
        gastoRepository.delete(gasto);
        gastoRepository.flush();
        recalcularVendaSeExistir(encomenda);
    }

    @Transactional(readOnly = true)
    public List<VendaResponse> listarVendas() {
        return vendaRepository.findAllByOrderByDataVendaDesc()
                .stream().map(this::mapearVenda).toList();
    }

    @Transactional(readOnly = true)
    public VendaResponse buscarVenda(Long id) {
        return mapearVenda(vendaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Venda não encontrada.")));
    }

    @Transactional
    public VendaResponse registrarVenda(Long encomendaId) {
        if (vendaRepository.existsByEncomenda_Id(encomendaId)) {
            throw new ConflitoException("A encomenda já possui uma venda registrada.");
        }
        Encomenda encomenda = buscarEncomenda(encomendaId);
        if (encomenda.getStatusFinanceiro() != StatusFinanceiro.PAGO_INTEGRALMENTE) {
            throw new RegraNegocioException(
                    "A venda só pode ser registrada após o pagamento integral da encomenda.");
        }

        Venda venda = new Venda();
        venda.setEncomenda(encomenda);
        venda.setValorTotal(encomenda.getValorTotal());
        venda.setDataVenda(Instant.now());
        calcularLucros(venda);
        return mapearVenda(vendaRepository.save(venda));
    }

    @Transactional(readOnly = true)
    public RelatorioFinanceiroResponse gerarRelatorio(LocalDate inicio, LocalDate fim) {
        validarPeriodo(inicio, fim);
        Instant instanteInicio = inicio.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant instanteFimExclusivo = fim.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);
        List<Venda> vendas = vendaRepository
                .findByDataVendaGreaterThanEqualAndDataVendaLessThanOrderByDataVendaAsc(
                        instanteInicio, instanteFimExclusivo);
        List<Gasto> gastos = gastoRepository.findByDataGastoBetweenOrderByDataGastoAsc(inicio, fim);

        BigDecimal receita = somarVendas(vendas, Venda::getValorTotal);
        BigDecimal gastosPeriodo = gastos.stream().map(Gasto::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal lucroBruto = somarVendas(vendas, Venda::getLucroBruto);
        BigDecimal lucroLiquido = somarVendas(vendas, Venda::getLucroLiquidoEstimado);

        return new RelatorioFinanceiroResponse(
                inicio, fim, vendas.size(), gastos.size(), dinheiro(receita),
                dinheiro(gastosPeriodo), dinheiro(lucroBruto), dinheiro(lucroLiquido),
                dinheiro(receita.subtract(gastosPeriodo)),
                vendas.stream().map(this::mapearVenda).toList(),
                gastos.stream().map(this::mapearGasto).toList());
    }

    private void aplicarGasto(Gasto gasto, GastoRequest request) {
        gasto.setEncomenda(request.encomendaId() == null ? null : buscarEncomenda(request.encomendaId()));
        gasto.setDescricao(request.descricao().trim());
        gasto.setValor(dinheiro(request.valor()));
        gasto.setDataGasto(request.dataGasto());
        gasto.setCategoria(request.categoria().trim());
    }

    private void calcularLucros(Venda venda) {
        Encomenda encomenda = venda.getEncomenda();
        BigDecimal custoProducao = encomenda.getIconePronto() == null
                ? BigDecimal.ZERO : encomenda.getIconePronto().getCustoProducao();
        BigDecimal gastos = totalGastos(encomenda.getId());
        venda.setValorTotal(encomenda.getValorTotal());
        venda.setLucroBruto(dinheiro(venda.getValorTotal().subtract(custoProducao)));
        venda.setLucroLiquidoEstimado(
                dinheiro(venda.getLucroBruto().subtract(gastos)));
    }

    private void recalcularVendaSeExistir(Encomenda encomenda) {
        if (encomenda == null) {
            return;
        }
        vendaRepository.findByEncomenda_Id(encomenda.getId()).ifPresent(venda -> {
            calcularLucros(venda);
            vendaRepository.save(venda);
        });
    }

    private BigDecimal totalGastos(Long encomendaId) {
        return gastoRepository.findByEncomenda_IdOrderByDataGastoAsc(encomendaId)
                .stream().map(Gasto::getValor).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Encomenda buscarEncomenda(Long id) {
        return encomendaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Encomenda não encontrada."));
    }

    private Gasto buscarGastoEntidade(Long id) {
        return gastoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Gasto não encontrado."));
    }

    private void validarPeriodo(LocalDate inicio, LocalDate fim) {
        if (inicio == null || fim == null) {
            throw new RegraNegocioException("As datas inicial e final são obrigatórias.");
        }
        if (inicio.isAfter(fim)) {
            throw new RegraNegocioException("A data inicial não pode ser posterior à data final.");
        }
    }

    private BigDecimal somarVendas(
            List<Venda> vendas, java.util.function.Function<Venda, BigDecimal> campo) {
        return vendas.stream().map(campo).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private GastoResponse mapearGasto(Gasto gasto) {
        return new GastoResponse(
                gasto.getId(), gasto.getEncomenda() == null ? null : gasto.getEncomenda().getId(),
                gasto.getDescricao(), gasto.getValor(), gasto.getDataGasto(), gasto.getCategoria());
    }

    private VendaResponse mapearVenda(Venda venda) {
        Encomenda encomenda = venda.getEncomenda();
        BigDecimal custoProducao = encomenda.getIconePronto() == null
                ? BigDecimal.ZERO : encomenda.getIconePronto().getCustoProducao();
        BigDecimal gastos = totalGastos(encomenda.getId());
        return new VendaResponse(
                venda.getId(), encomenda.getId(), encomenda.getCliente().getUsuario().getNome(),
                venda.getValorTotal(), dinheiro(custoProducao), dinheiro(gastos),
                dinheiro(custoProducao.add(gastos)), venda.getLucroBruto(),
                venda.getLucroLiquidoEstimado(), venda.getDataVenda());
    }

    private BigDecimal dinheiro(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP);
    }
}
