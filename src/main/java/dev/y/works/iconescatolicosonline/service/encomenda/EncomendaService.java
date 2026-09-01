package dev.y.works.iconescatolicosonline.service.encomenda;

import dev.y.works.iconescatolicosonline.domain.catalogo.ModeloIcone;
import dev.y.works.iconescatolicosonline.domain.encomenda.Encomenda;
import dev.y.works.iconescatolicosonline.domain.encomenda.ItemEncomenda;
import dev.y.works.iconescatolicosonline.domain.encomenda.Personalizacao;
import dev.y.works.iconescatolicosonline.domain.encomenda.StatusEncomenda;
import dev.y.works.iconescatolicosonline.domain.encomenda.StatusFinanceiro;
import dev.y.works.iconescatolicosonline.domain.encomenda.TipoEntrega;
import dev.y.works.iconescatolicosonline.domain.usuario.Cliente;
import dev.y.works.iconescatolicosonline.dto.encomenda.CriarEncomendaRequest;
import dev.y.works.iconescatolicosonline.dto.encomenda.EncomendaResponse;
import dev.y.works.iconescatolicosonline.dto.encomenda.ItemEncomendaRequest;
import dev.y.works.iconescatolicosonline.dto.encomenda.ItemEncomendaResponse;
import dev.y.works.iconescatolicosonline.dto.encomenda.PersonalizacaoRequest;
import dev.y.works.iconescatolicosonline.dto.encomenda.PersonalizacaoResponse;
import dev.y.works.iconescatolicosonline.exception.RecursoNaoEncontradoException;
import dev.y.works.iconescatolicosonline.exception.RegraNegocioException;
import dev.y.works.iconescatolicosonline.repository.catalogo.ModeloIconeRepository;
import dev.y.works.iconescatolicosonline.repository.configuracao.ConfiguracaoLojaRepository;
import dev.y.works.iconescatolicosonline.repository.encomenda.EncomendaRepository;
import dev.y.works.iconescatolicosonline.repository.usuario.ClienteRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class EncomendaService {

    private static final Map<StatusEncomenda, Set<StatusEncomenda>> TRANSICOES = Map.of(
            StatusEncomenda.EM_PRODUCAO, Set.of(
                    StatusEncomenda.AGUARDANDO_PAGAMENTO_RESTANTE,
                    StatusEncomenda.ENVIADO_OU_RETIRADO),
            StatusEncomenda.AGUARDANDO_PAGAMENTO_RESTANTE,
            Set.of(StatusEncomenda.ENVIADO_OU_RETIRADO),
            StatusEncomenda.ENVIADO_OU_RETIRADO, Set.of(StatusEncomenda.ENTREGUE_E_CONCLUIDO)
    );

    private final EncomendaRepository encomendaRepository;
    private final ClienteRepository clienteRepository;
    private final ModeloIconeRepository modeloIconeRepository;
    private final ConfiguracaoLojaRepository configuracaoLojaRepository;
    private final BigDecimal percentualSinal;

    public EncomendaService(
            EncomendaRepository encomendaRepository,
            ClienteRepository clienteRepository,
            ModeloIconeRepository modeloIconeRepository,
            ConfiguracaoLojaRepository configuracaoLojaRepository,
            @Value("${app.encomenda.percentual-sinal-minimo}") BigDecimal percentualSinal) {
        this.encomendaRepository = encomendaRepository;
        this.clienteRepository = clienteRepository;
        this.modeloIconeRepository = modeloIconeRepository;
        this.configuracaoLojaRepository = configuracaoLojaRepository;
        if (percentualSinal.signum() <= 0 || percentualSinal.compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("O percentual do sinal deve estar entre 0 e 100.");
        }
        this.percentualSinal = percentualSinal;
    }

    @Transactional
    public EncomendaResponse criar(String emailCliente, CriarEncomendaRequest request) {
        Cliente cliente = buscarCliente(emailCliente);
        validarEntrega(request);

        Encomenda encomenda = new Encomenda();
        encomenda.setCliente(cliente);
        encomenda.setStatusEncomenda(StatusEncomenda.AGUARDANDO_PAGAMENTO_INICIAL);
        encomenda.setStatusFinanceiro(StatusFinanceiro.AGUARDANDO_SINAL);
        encomenda.setTipoEntrega(request.tipoEntrega());
        encomenda.setEnderecoEntrega(
                request.tipoEntrega() == TipoEntrega.ENTREGA
                        ? request.enderecoEntrega().trim()
                        : null);
        encomenda.setObservacoes(request.observacoes());

        for (ItemEncomendaRequest itemRequest : request.itens()) {
            adicionarItem(encomenda, itemRequest);
        }

        BigDecimal total = encomenda.getItens().stream()
                .map(ItemEncomenda::calcularSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        encomenda.setValorTotal(total);
        encomenda.setValorSinal(total
                .multiply(percentualSinal)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP));

        return paraResponse(encomendaRepository.save(encomenda));
    }

    @Transactional(readOnly = true)
    public List<EncomendaResponse> listarDoCliente(String emailCliente) {
        Cliente cliente = buscarCliente(emailCliente);
        return encomendaRepository.findByCliente_IdOrderByDataCriacaoDesc(cliente.getId())
                .stream()
                .map(this::paraResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public EncomendaResponse buscarDoCliente(Long id, String emailCliente) {
        Encomenda encomenda = encomendaRepository
                .findByIdAndCliente_Usuario_EmailIgnoreCase(id, emailCliente)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Encomenda não encontrada."));
        return paraResponse(encomenda);
    }

    @Transactional(readOnly = true)
    public List<EncomendaResponse> listarTodas() {
        return encomendaRepository.findAllByOrderByDataCriacaoDesc()
                .stream()
                .map(this::paraResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public EncomendaResponse buscarPorId(Long id) {
        return paraResponse(buscarEntidade(id));
    }

    @Transactional
    public EncomendaResponse atualizarStatus(Long id, StatusEncomenda novoStatus) {
        Encomenda encomenda = buscarEntidade(id);
        StatusEncomenda atual = encomenda.getStatusEncomenda();

        if (novoStatus == StatusEncomenda.ENTREGUE_E_CONCLUIDO
                && encomenda.getStatusFinanceiro() != StatusFinanceiro.PAGO_INTEGRALMENTE) {
            throw new RegraNegocioException(
                    "A encomenda só pode ser concluída após o pagamento integral.");
        }

        if (novoStatus == StatusEncomenda.ENVIADO_OU_RETIRADO
                && encomenda.getStatusFinanceiro() != StatusFinanceiro.PAGO_INTEGRALMENTE) {
            throw new RegraNegocioException(
                    "A encomenda só pode ser marcada como enviada ou aguardando retirada após o pagamento integral.");
        }

        if (novoStatus == StatusEncomenda.CANCELADO) {
            if (atual == StatusEncomenda.ENTREGUE_E_CONCLUIDO || atual == StatusEncomenda.CANCELADO) {
                throw transicaoInvalida(atual, novoStatus);
            }
            encomenda.setStatusEncomenda(StatusEncomenda.CANCELADO);
            encomenda.setStatusFinanceiro(StatusFinanceiro.CANCELADO);
        } else if (!TRANSICOES.getOrDefault(atual, Set.of()).contains(novoStatus)) {
            throw transicaoInvalida(atual, novoStatus);
        } else {
            encomenda.setStatusEncomenda(novoStatus);
        }

        return paraResponse(encomendaRepository.save(encomenda));
    }

    private Cliente buscarCliente(String email) {
        return clienteRepository.findByUsuario_EmailIgnoreCase(email)
                .orElseThrow(() -> new RegraNegocioException(
                        "O usuário autenticado não possui cadastro de cliente."));
    }

    private Encomenda buscarEntidade(Long id) {
        return encomendaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Encomenda não encontrada."));
    }

    private void validarEntrega(CriarEncomendaRequest request) {
        if (request.tipoEntrega() == TipoEntrega.ENTREGA
                && configuracaoLojaRepository.findById(1L)
                .map(configuracao -> !configuracao.isEntregaHabilitada()).orElse(false)) {
            throw new RegraNegocioException("A entrega está desabilitada. Selecione retirada na oficina.");
        }
        if (request.tipoEntrega() == TipoEntrega.ENTREGA
                && (request.enderecoEntrega() == null || request.enderecoEntrega().isBlank())) {
            throw new RegraNegocioException("O endereço é obrigatório para entrega.");
        }
    }

    private void adicionarItem(Encomenda encomenda, ItemEncomendaRequest request) {
        ModeloIcone modelo = modeloIconeRepository.findByIdAndAtivoTrue(request.modeloIconeId())
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Modelo de ícone ativo não encontrado: " + request.modeloIconeId()));

        ItemEncomenda item = new ItemEncomenda();
        item.setEncomenda(encomenda);
        item.setModeloIcone(modelo);
        item.setQuantidade(request.quantidade());
        item.setValorUnitario(modelo.getPrecoBase());
        aplicarPersonalizacao(item, request.personalizacao());
        encomenda.getItens().add(item);
    }

    private void aplicarPersonalizacao(ItemEncomenda item, PersonalizacaoRequest request) {
        if (request == null) {
            return;
        }
        Personalizacao personalizacao = new Personalizacao();
        personalizacao.setItemEncomenda(item);
        personalizacao.setTamanho(request.tamanho());
        personalizacao.setAcabamento(request.acabamento());
        personalizacao.setFrase(request.frase());
        personalizacao.setNomeFamilia(request.nomeFamilia());
        personalizacao.setObservacoes(request.observacoes());
        item.setPersonalizacao(personalizacao);
    }

    private RegraNegocioException transicaoInvalida(
            StatusEncomenda atual, StatusEncomenda novoStatus) {
        return new RegraNegocioException(
                "Transição de status não permitida: " + atual + " -> " + novoStatus + ".");
    }

    private EncomendaResponse paraResponse(Encomenda encomenda) {
        return new EncomendaResponse(
                encomenda.getId(),
                encomenda.getCliente().getId(),
                encomenda.getCliente().getUsuario().getNome(),
                encomenda.getDataCriacao(),
                encomenda.getStatusEncomenda(),
                encomenda.getStatusFinanceiro(),
                encomenda.getValorTotal(),
                encomenda.getValorSinal(),
                encomenda.getTipoEntrega(),
                encomenda.getEnderecoEntrega(),
                encomenda.getObservacoes(),
                encomenda.getItens().stream().map(this::paraItemResponse).toList());
    }

    private ItemEncomendaResponse paraItemResponse(ItemEncomenda item) {
        return new ItemEncomendaResponse(
                item.getId(),
                item.getModeloIcone().getId(),
                item.getModeloIcone().getNome(),
                item.getQuantidade(),
                item.getValorUnitario(),
                item.calcularSubtotal(),
                paraPersonalizacaoResponse(item.getPersonalizacao()));
    }

    private PersonalizacaoResponse paraPersonalizacaoResponse(Personalizacao personalizacao) {
        if (personalizacao == null) {
            return null;
        }
        return new PersonalizacaoResponse(
                personalizacao.getTamanho(),
                personalizacao.getAcabamento(),
                personalizacao.getFrase(),
                personalizacao.getNomeFamilia(),
                personalizacao.getObservacoes());
    }
}
