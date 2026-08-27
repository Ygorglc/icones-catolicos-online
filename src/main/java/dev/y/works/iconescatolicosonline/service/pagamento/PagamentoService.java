package dev.y.works.iconescatolicosonline.service.pagamento;

import dev.y.works.iconescatolicosonline.domain.encomenda.Encomenda;
import dev.y.works.iconescatolicosonline.domain.encomenda.StatusEncomenda;
import dev.y.works.iconescatolicosonline.domain.encomenda.StatusFinanceiro;
import dev.y.works.iconescatolicosonline.domain.pagamento.FormaPagamento;
import dev.y.works.iconescatolicosonline.domain.pagamento.OrigemPagamento;
import dev.y.works.iconescatolicosonline.domain.pagamento.Pagamento;
import dev.y.works.iconescatolicosonline.domain.pagamento.StatusPagamento;
import dev.y.works.iconescatolicosonline.domain.pagamento.TipoPagamento;
import dev.y.works.iconescatolicosonline.domain.usuario.Administrador;
import dev.y.works.iconescatolicosonline.dto.pagamento.AnalisePagamentoRequest;
import dev.y.works.iconescatolicosonline.dto.pagamento.HistoricoPagamentosResponse;
import dev.y.works.iconescatolicosonline.dto.pagamento.PagamentoResponse;
import dev.y.works.iconescatolicosonline.dto.pagamento.RegistrarPagamentoRequest;
import dev.y.works.iconescatolicosonline.exception.RecursoNaoEncontradoException;
import dev.y.works.iconescatolicosonline.exception.RegraNegocioException;
import dev.y.works.iconescatolicosonline.repository.encomenda.EncomendaRepository;
import dev.y.works.iconescatolicosonline.repository.pagamento.PagamentoRepository;
import dev.y.works.iconescatolicosonline.repository.usuario.AdministradorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class PagamentoService {

    private final PagamentoRepository pagamentoRepository;
    private final EncomendaRepository encomendaRepository;
    private final AdministradorRepository administradorRepository;

    public PagamentoService(
            PagamentoRepository pagamentoRepository,
            EncomendaRepository encomendaRepository,
            AdministradorRepository administradorRepository) {
        this.pagamentoRepository = pagamentoRepository;
        this.encomendaRepository = encomendaRepository;
        this.administradorRepository = administradorRepository;
    }

    @Transactional
    public PagamentoResponse registrar(
            Long encomendaId,
            String emailCliente,
            RegistrarPagamentoRequest request) {
        Encomenda encomenda = buscarDoCliente(encomendaId, emailCliente);
        validarEncomendaRecebePagamento(encomenda);
        validarFormaEOrigem(request.forma(), request.origem());
        if (pagamentoRepository.existsByEncomenda_IdAndStatus(
                encomendaId, StatusPagamento.PENDENTE)) {
            throw new RegraNegocioException(
                    "Já existe um pagamento externo aguardando análise para esta encomenda.");
        }

        BigDecimal totalPagoAntes = calcularTotalPago(encomendaId);
        BigDecimal saldoAntes = encomenda.getValorTotal().subtract(totalPagoAntes);
        BigDecimal valor = calcularValor(request.tipo(), encomenda, totalPagoAntes, saldoAntes);

        Pagamento pagamento = new Pagamento();
        pagamento.setEncomenda(encomenda);
        pagamento.setTipo(request.tipo());
        pagamento.setFormaPagamento(request.forma());
        pagamento.setOrigem(request.origem());
        pagamento.setValor(valor);
        boolean confirmacaoAutomatica = request.origem() == OrigemPagamento.SIMULADO_SISTEMA;
        pagamento.setStatus(confirmacaoAutomatica
                ? StatusPagamento.CONFIRMADO
                : StatusPagamento.PENDENTE);
        pagamento.setDataPagamento(confirmacaoAutomatica ? Instant.now() : null);
        pagamento.setReferenciaSimulada(
                (confirmacaoAutomatica ? "SIM-" : "EXT-") + UUID.randomUUID());
        pagamento = pagamentoRepository.save(pagamento);

        BigDecimal totalPago = totalPagoAntes;
        if (confirmacaoAutomatica) {
            totalPago = totalPagoAntes.add(valor);
            atualizarEncomendaAposPagamento(encomenda, totalPago);
            encomendaRepository.save(encomenda);
        }

        return paraResponse(pagamento, totalPago);
    }

    @Transactional(readOnly = true)
    public HistoricoPagamentosResponse listarDoCliente(Long encomendaId, String emailCliente) {
        return criarHistorico(buscarDoCliente(encomendaId, emailCliente));
    }

    @Transactional(readOnly = true)
    public HistoricoPagamentosResponse listarParaAdministrador(Long encomendaId) {
        Encomenda encomenda = encomendaRepository.findById(encomendaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Encomenda não encontrada."));
        return criarHistorico(encomenda);
    }

    @Transactional(readOnly = true)
    public List<PagamentoResponse> listarPendentes() {
        return pagamentoRepository.findByStatusOrderByCriadoEmAsc(StatusPagamento.PENDENTE)
                .stream()
                .map(pagamento -> paraResponse(
                        pagamento, calcularTotalPago(pagamento.getEncomenda().getId())))
                .toList();
    }

    @Transactional
    public PagamentoResponse confirmarRecebimentoExterno(
            Long encomendaId, String emailAdministrador, RegistrarPagamentoRequest request) {
        if (request.origem() != OrigemPagamento.EXTERNO_MANUAL) {
            throw new RegraNegocioException("A confirmação administrativa aceita somente pagamento externo.");
        }
        validarFormaEOrigem(request.forma(), request.origem());
        Encomenda encomenda = encomendaRepository.findById(encomendaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Encomenda não encontrada."));
        validarEncomendaRecebePagamento(encomenda);
        var pagamentoPendente = pagamentoRepository
                .findFirstByEncomenda_IdAndStatusOrderByCriadoEmAsc(
                        encomendaId, StatusPagamento.PENDENTE);
        if (pagamentoPendente.isPresent()) {
            Pagamento existente = pagamentoPendente.get();
            if (existente.getOrigem() != OrigemPagamento.EXTERNO_MANUAL) {
                throw new RegraNegocioException("O pagamento pendente não pode ser confirmado manualmente.");
            }
            return analisar(existente.getId(), emailAdministrador,
                    new AnalisePagamentoRequest(true,
                            "Recebimento confirmado diretamente na encomenda."));
        }
        Administrador administrador = administradorRepository.findByUsuario_EmailIgnoreCase(emailAdministrador)
                .orElseThrow(() -> new RegraNegocioException("Administrador não encontrado."));
        BigDecimal totalPagoAntes = calcularTotalPago(encomendaId);
        BigDecimal valor = calcularValor(request.tipo(), encomenda, totalPagoAntes,
                encomenda.getValorTotal().subtract(totalPagoAntes));
        Pagamento pagamento = new Pagamento();
        pagamento.setEncomenda(encomenda);
        pagamento.setTipo(request.tipo());
        pagamento.setFormaPagamento(request.forma());
        pagamento.setOrigem(OrigemPagamento.EXTERNO_MANUAL);
        pagamento.setValor(valor);
        pagamento.setStatus(StatusPagamento.CONFIRMADO);
        pagamento.setDataPagamento(Instant.now());
        pagamento.setReferenciaSimulada("ADM-" + UUID.randomUUID());
        pagamento.setAnalisadoPor(administrador);
        pagamento.setDataAnalise(Instant.now());
        pagamento.setObservacaoAdministrativa("Recebimento confirmado diretamente pelo administrador.");
        pagamento = pagamentoRepository.save(pagamento);
        BigDecimal totalPago = totalPagoAntes.add(valor);
        atualizarEncomendaAposPagamento(encomenda, totalPago);
        encomendaRepository.save(encomenda);
        return paraResponse(pagamento, totalPago);
    }

    @Transactional
    public PagamentoResponse analisar(
            Long pagamentoId,
            String emailAdministrador,
            AnalisePagamentoRequest request) {
        Pagamento pagamento = pagamentoRepository.findById(pagamentoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Pagamento não encontrado."));
        if (pagamento.getOrigem() != OrigemPagamento.EXTERNO_MANUAL
                || pagamento.getStatus() != StatusPagamento.PENDENTE) {
            throw new RegraNegocioException(
                    "Somente pagamentos externos pendentes podem ser analisados.");
        }

        Administrador administrador = administradorRepository
                .findByUsuario_EmailIgnoreCase(emailAdministrador)
                .orElseThrow(() -> new RegraNegocioException(
                        "O usuário autenticado não possui cadastro de administrador."));
        pagamento.setAnalisadoPor(administrador);
        pagamento.setDataAnalise(Instant.now());
        pagamento.setObservacaoAdministrativa(request.observacao());

        Encomenda encomenda = pagamento.getEncomenda();
        BigDecimal totalPago = calcularTotalPago(encomenda.getId());
        if (Boolean.TRUE.equals(request.confirmado())) {
            validarEncomendaRecebePagamento(encomenda);
            BigDecimal novoTotal = totalPago.add(pagamento.getValor());
            if (novoTotal.compareTo(encomenda.getValorTotal()) > 0) {
                throw new RegraNegocioException(
                        "A confirmação excederia o valor total da encomenda.");
            }
            pagamento.setStatus(StatusPagamento.CONFIRMADO);
            pagamento.setDataPagamento(Instant.now());
            totalPago = novoTotal;
            atualizarEncomendaAposPagamento(encomenda, totalPago);
            encomendaRepository.save(encomenda);
        } else {
            pagamento.setStatus(StatusPagamento.CANCELADO);
        }
        pagamentoRepository.save(pagamento);
        return paraResponse(pagamento, totalPago);
    }

    private Encomenda buscarDoCliente(Long id, String email) {
        return encomendaRepository.findByIdAndCliente_Usuario_EmailIgnoreCase(id, email)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Encomenda não encontrada."));
    }

    private void validarEncomendaRecebePagamento(Encomenda encomenda) {
        if (encomenda.getStatusEncomenda() == StatusEncomenda.CANCELADO
                || encomenda.getStatusEncomenda() == StatusEncomenda.CONCLUIDO) {
            throw new RegraNegocioException(
                    "Não é possível pagar uma encomenda cancelada ou concluída.");
        }
        if (encomenda.getStatusFinanceiro() == StatusFinanceiro.PAGO_INTEGRALMENTE) {
            throw new RegraNegocioException("A encomenda já está paga integralmente.");
        }
    }

    private void validarFormaEOrigem(FormaPagamento forma, OrigemPagamento origem) {
        if (origem == OrigemPagamento.SIMULADO_SISTEMA
                && (forma == FormaPagamento.DINHEIRO || forma == FormaPagamento.DEPOSITO)) {
            throw new RegraNegocioException(
                    "Pagamento em dinheiro ou depósito deve ser registrado como externo manual.");
        }
        if (origem == OrigemPagamento.EXTERNO_MANUAL
                && forma != FormaPagamento.PIX
                && forma != FormaPagamento.DINHEIRO
                && forma != FormaPagamento.DEPOSITO) {
            throw new RegraNegocioException(
                    "Pagamento externo manual aceita somente PIX, dinheiro ou depósito.");
        }
    }

    private BigDecimal calcularValor(
            TipoPagamento tipo,
            Encomenda encomenda,
            BigDecimal totalPago,
            BigDecimal saldo) {
        if (saldo.signum() <= 0) {
            throw new RegraNegocioException("A encomenda não possui saldo pendente.");
        }
        return switch (tipo) {
            case SINAL -> {
                if (totalPago.signum() > 0) {
                    throw new RegraNegocioException(
                            "O sinal só pode ser pago antes de outros pagamentos.");
                }
                yield encomenda.getValorSinal();
            }
            case INTEGRAL -> {
                if (totalPago.signum() > 0) {
                    throw new RegraNegocioException(
                            "O pagamento integral só pode ser realizado sem pagamentos anteriores.");
                }
                yield encomenda.getValorTotal();
            }
            case RESTANTE -> {
                if (totalPago.compareTo(encomenda.getValorSinal()) < 0) {
                    throw new RegraNegocioException(
                            "O restante só pode ser pago após a confirmação do sinal.");
                }
                yield saldo;
            }
        };
    }

    private void atualizarEncomendaAposPagamento(Encomenda encomenda, BigDecimal totalPago) {
        if (totalPago.compareTo(encomenda.getValorTotal()) >= 0) {
            encomenda.setStatusFinanceiro(StatusFinanceiro.PAGO_INTEGRALMENTE);
        } else if (totalPago.compareTo(encomenda.getValorSinal()) >= 0) {
            encomenda.setStatusFinanceiro(StatusFinanceiro.SINAL_PAGO);
        } else {
            encomenda.setStatusFinanceiro(StatusFinanceiro.PAGAMENTO_PARCIAL);
        }

        if (encomenda.getStatusEncomenda() == StatusEncomenda.AGUARDANDO_PAGAMENTO_INICIAL
                && totalPago.compareTo(encomenda.getValorSinal()) >= 0) {
            encomenda.setStatusEncomenda(StatusEncomenda.PAGAMENTO_INICIAL_CONFIRMADO);
        }
    }

    private BigDecimal calcularTotalPago(Long encomendaId) {
        return pagamentoRepository
                .findByEncomenda_IdAndStatus(encomendaId, StatusPagamento.CONFIRMADO)
                .stream()
                .map(Pagamento::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private HistoricoPagamentosResponse criarHistorico(Encomenda encomenda) {
        List<Pagamento> pagamentos = pagamentoRepository
                .findByEncomenda_IdOrderByCriadoEmAsc(encomenda.getId());
        BigDecimal totalPago = pagamentos.stream()
                .filter(pagamento -> pagamento.getStatus() == StatusPagamento.CONFIRMADO)
                .map(Pagamento::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal saldo = saldo(encomenda, totalPago);
        return new HistoricoPagamentosResponse(
                encomenda.getId(),
                encomenda.getValorTotal(),
                totalPago,
                saldo,
                encomenda.getStatusFinanceiro(),
                pagamentos.stream()
                        .map(pagamento -> paraResponse(pagamento, totalPago))
                        .toList());
    }

    private PagamentoResponse paraResponse(Pagamento pagamento, BigDecimal totalPago) {
        Encomenda encomenda = pagamento.getEncomenda();
        return new PagamentoResponse(
                pagamento.getId(),
                encomenda.getId(),
                pagamento.getTipo(),
                pagamento.getFormaPagamento(),
                pagamento.getOrigem(),
                pagamento.getValor(),
                pagamento.getDataPagamento(),
                pagamento.getStatus(),
                pagamento.getReferenciaSimulada(),
                pagamento.getAnalisadoPor() == null
                        ? null
                        : pagamento.getAnalisadoPor().getUsuario().getNome(),
                pagamento.getDataAnalise(),
                pagamento.getObservacaoAdministrativa(),
                totalPago,
                saldo(encomenda, totalPago),
                encomenda.getStatusFinanceiro(),
                encomenda.getStatusEncomenda());
    }

    private BigDecimal saldo(Encomenda encomenda, BigDecimal totalPago) {
        return encomenda.getValorTotal().subtract(totalPago).max(BigDecimal.ZERO);
    }
}
