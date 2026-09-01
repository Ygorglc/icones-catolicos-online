package dev.y.works.iconescatolicosonline.service.estoque;

import dev.y.works.iconescatolicosonline.domain.catalogo.ModeloIcone;
import dev.y.works.iconescatolicosonline.domain.encomenda.Encomenda;
import dev.y.works.iconescatolicosonline.domain.encomenda.ItemEncomenda;
import dev.y.works.iconescatolicosonline.domain.encomenda.Personalizacao;
import dev.y.works.iconescatolicosonline.domain.encomenda.StatusEncomenda;
import dev.y.works.iconescatolicosonline.domain.estoque.IconePronto;
import dev.y.works.iconescatolicosonline.domain.estoque.StatusIconePronto;
import dev.y.works.iconescatolicosonline.dto.estoque.IconeProntoRequest;
import dev.y.works.iconescatolicosonline.dto.estoque.IconeProntoResponse;
import dev.y.works.iconescatolicosonline.exception.RecursoNaoEncontradoException;
import dev.y.works.iconescatolicosonline.exception.RegraNegocioException;
import dev.y.works.iconescatolicosonline.repository.catalogo.ModeloIconeRepository;
import dev.y.works.iconescatolicosonline.repository.encomenda.EncomendaRepository;
import dev.y.works.iconescatolicosonline.repository.estoque.IconeProntoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
public class IconeProntoService {

    private static final Set<StatusIconePronto> STATUS_EDITAVEIS =
            Set.of(StatusIconePronto.DISPONIVEL, StatusIconePronto.EM_ACABAMENTO);

    private final IconeProntoRepository iconeProntoRepository;
    private final ModeloIconeRepository modeloIconeRepository;
    private final EncomendaRepository encomendaRepository;

    public IconeProntoService(
            IconeProntoRepository iconeProntoRepository,
            ModeloIconeRepository modeloIconeRepository,
            EncomendaRepository encomendaRepository) {
        this.iconeProntoRepository = iconeProntoRepository;
        this.modeloIconeRepository = modeloIconeRepository;
        this.encomendaRepository = encomendaRepository;
    }

    @Transactional(readOnly = true)
    public List<IconeProntoResponse> listar() {
        return iconeProntoRepository.findAllByOrderByIdDesc().stream().map(this::mapear).toList();
    }

    @Transactional(readOnly = true)
    public IconeProntoResponse buscar(Long id) {
        return mapear(buscarEntidade(id));
    }

    @Transactional
    public IconeProntoResponse criar(IconeProntoRequest request) {
        validarStatusEditavel(request.status());
        IconePronto icone = new IconePronto();
        aplicar(icone, request);
        return mapear(iconeProntoRepository.save(icone));
    }

    @Transactional
    public IconeProntoResponse atualizar(Long id, IconeProntoRequest request) {
        IconePronto icone = buscarEntidade(id);
        if (!STATUS_EDITAVEIS.contains(icone.getStatus())) {
            throw new RegraNegocioException("Uma peça reservada ou vendida não pode ser editada.");
        }
        validarStatusEditavel(request.status());
        aplicar(icone, request);
        return mapear(iconeProntoRepository.save(icone));
    }

    @Transactional
    public IconeProntoResponse reservarCompativel(Long encomendaId) {
        Encomenda encomenda = encomendaRepository.findById(encomendaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Encomenda não encontrada."));
        validarEncomendaParaReserva(encomenda);
        if (iconeProntoRepository.existsByEncomenda_Id(encomendaId)) {
            throw new RegraNegocioException("A encomenda já possui uma peça pronta vinculada.");
        }

        for (ItemEncomenda item : encomenda.getItens()) {
            Personalizacao personalizacao = item.getPersonalizacao();
            if (personalizacao == null
                    || personalizacao.getTamanho() == null
                    || personalizacao.getAcabamento() == null) {
                continue;
            }
            var encontrada = iconeProntoRepository
                    .findFirstByModeloIcone_IdAndTamanhoAndAcabamentoIgnoreCaseAndStatus(
                            item.getModeloIcone().getId(),
                            personalizacao.getTamanho(),
                            personalizacao.getAcabamento().trim(),
                            StatusIconePronto.DISPONIVEL);
            if (encontrada.isPresent()) {
                IconePronto icone = encontrada.get();
                icone.setEncomenda(encomenda);
                icone.setStatus(StatusIconePronto.RESERVADO);
                return mapear(iconeProntoRepository.save(icone));
            }
        }
        throw new RegraNegocioException(
                "Não existe peça pronta compatível com os itens personalizados da encomenda.");
    }

    @Transactional
    public IconeProntoResponse atualizarStatus(Long id, StatusIconePronto novoStatus) {
        IconePronto icone = buscarEntidade(id);
        StatusIconePronto atual = icone.getStatus();
        if (atual == novoStatus) {
            return mapear(icone);
        }
        boolean permitida = (atual == StatusIconePronto.DISPONIVEL
                && novoStatus == StatusIconePronto.EM_ACABAMENTO)
                || (atual == StatusIconePronto.EM_ACABAMENTO
                && novoStatus == StatusIconePronto.DISPONIVEL)
                || (atual == StatusIconePronto.RESERVADO
                && (novoStatus == StatusIconePronto.VENDIDO
                || novoStatus == StatusIconePronto.DISPONIVEL));
        if (!permitida) {
            throw new RegraNegocioException(
                    "Transição de estado da peça não permitida: " + atual + " -> " + novoStatus + ".");
        }
        if (atual == StatusIconePronto.RESERVADO && novoStatus == StatusIconePronto.DISPONIVEL) {
            icone.setEncomenda(null);
        }
        icone.setStatus(novoStatus);
        return mapear(iconeProntoRepository.save(icone));
    }

    @Transactional
    public void excluir(Long id) {
        IconePronto icone = buscarEntidade(id);
        if (!STATUS_EDITAVEIS.contains(icone.getStatus())) {
            throw new RegraNegocioException("Uma peça reservada ou vendida não pode ser excluída.");
        }
        iconeProntoRepository.delete(icone);
    }

    private void aplicar(IconePronto icone, IconeProntoRequest request) {
        ModeloIcone modelo = modeloIconeRepository.findById(request.modeloIconeId())
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Modelo de ícone não encontrado."));
        icone.setModeloIcone(modelo);
        icone.setTamanho(request.tamanho());
        icone.setAcabamento(request.acabamento().trim());
        icone.setCustoProducao(request.custoProducao());
        icone.setPrecoSugerido(request.precoSugerido());
        icone.setStatus(request.status());
        icone.setLocalizacao(request.localizacao());
    }

    private void validarStatusEditavel(StatusIconePronto status) {
        if (!STATUS_EDITAVEIS.contains(status)) {
            throw new RegraNegocioException(
                    "Use as operações de reserva e atualização de estado para reservar ou vender uma peça.");
        }
    }

    private void validarEncomendaParaReserva(Encomenda encomenda) {
        if (encomenda.getStatusEncomenda() == StatusEncomenda.CANCELADO
                || encomenda.getStatusEncomenda() == StatusEncomenda.ENTREGUE_E_CONCLUIDO
                || encomenda.getStatusEncomenda() == StatusEncomenda.AGUARDANDO_PAGAMENTO_INICIAL) {
            throw new RegraNegocioException(
                    "A encomenda não está em uma etapa que permita reservar uma peça pronta.");
        }
    }

    private IconePronto buscarEntidade(Long id) {
        return iconeProntoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Peça pronta não encontrada."));
    }

    private IconeProntoResponse mapear(IconePronto icone) {
        return new IconeProntoResponse(
                icone.getId(), icone.getModeloIcone().getId(), icone.getModeloIcone().getNome(),
                icone.getEncomenda() == null ? null : icone.getEncomenda().getId(),
                icone.getTamanho(), icone.getAcabamento(), icone.getCustoProducao(),
                icone.getPrecoSugerido(), icone.getStatus(), icone.getLocalizacao());
    }
}
