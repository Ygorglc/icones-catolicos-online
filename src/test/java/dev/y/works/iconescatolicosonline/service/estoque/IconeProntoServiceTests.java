package dev.y.works.iconescatolicosonline.service.estoque;

import dev.y.works.iconescatolicosonline.domain.catalogo.ModeloIcone;
import dev.y.works.iconescatolicosonline.domain.catalogo.TamanhoIcone;
import dev.y.works.iconescatolicosonline.domain.encomenda.Encomenda;
import dev.y.works.iconescatolicosonline.domain.encomenda.ItemEncomenda;
import dev.y.works.iconescatolicosonline.domain.encomenda.Personalizacao;
import dev.y.works.iconescatolicosonline.domain.encomenda.StatusEncomenda;
import dev.y.works.iconescatolicosonline.domain.estoque.IconePronto;
import dev.y.works.iconescatolicosonline.domain.estoque.StatusIconePronto;
import dev.y.works.iconescatolicosonline.dto.estoque.IconeProntoRequest;
import dev.y.works.iconescatolicosonline.dto.estoque.IconeProntoResponse;
import dev.y.works.iconescatolicosonline.exception.RegraNegocioException;
import dev.y.works.iconescatolicosonline.repository.catalogo.ModeloIconeRepository;
import dev.y.works.iconescatolicosonline.repository.encomenda.EncomendaRepository;
import dev.y.works.iconescatolicosonline.repository.estoque.IconeProntoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IconeProntoServiceTests {

    @Mock IconeProntoRepository iconeProntoRepository;
    @Mock ModeloIconeRepository modeloIconeRepository;
    @Mock EncomendaRepository encomendaRepository;
    private IconeProntoService service;

    @BeforeEach
    void configurar() {
        service = new IconeProntoService(
                iconeProntoRepository, modeloIconeRepository, encomendaRepository);
    }

    @Test
    void deveCriarPecaDisponivel() {
        ModeloIcone modelo = criarModelo();
        when(modeloIconeRepository.findById(10L)).thenReturn(Optional.of(modelo));
        when(iconeProntoRepository.save(any(IconePronto.class))).thenAnswer(invocation -> {
            IconePronto icone = invocation.getArgument(0);
            icone.setId(20L);
            return icone;
        });

        IconeProntoResponse resposta = service.criar(new IconeProntoRequest(
                10L, TamanhoIcone.MEDIO, "Envernizado", new BigDecimal("120.00"),
                new BigDecimal("250.00"), StatusIconePronto.DISPONIVEL, "Prateleira A"));

        assertThat(resposta.id()).isEqualTo(20L);
        assertThat(resposta.status()).isEqualTo(StatusIconePronto.DISPONIVEL);
    }

    @Test
    void deveImpedirCriacaoDiretamenteComoReservada() {
        assertThatThrownBy(() -> service.criar(new IconeProntoRequest(
                10L, TamanhoIcone.MEDIO, "Envernizado", new BigDecimal("120.00"),
                null, StatusIconePronto.RESERVADO, null)))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void deveReservarPecaCompativel() {
        ModeloIcone modelo = criarModelo();
        Encomenda encomenda = criarEncomenda(modelo);
        IconePronto icone = criarIcone(modelo);
        when(encomendaRepository.findById(1L)).thenReturn(Optional.of(encomenda));
        when(iconeProntoRepository.findFirstByModeloIcone_IdAndTamanhoAndAcabamentoIgnoreCaseAndStatus(
                10L, TamanhoIcone.MEDIO, "Envernizado", StatusIconePronto.DISPONIVEL))
                .thenReturn(Optional.of(icone));
        when(iconeProntoRepository.save(icone)).thenReturn(icone);

        IconeProntoResponse resposta = service.reservarCompativel(1L);

        assertThat(resposta.status()).isEqualTo(StatusIconePronto.RESERVADO);
        assertThat(resposta.encomendaId()).isEqualTo(1L);
    }

    @Test
    void deveLiberarReservaVoltandoPecaParaDisponivel() {
        ModeloIcone modelo = criarModelo();
        IconePronto icone = criarIcone(modelo);
        icone.setEncomenda(criarEncomenda(modelo));
        icone.setStatus(StatusIconePronto.RESERVADO);
        when(iconeProntoRepository.findById(20L)).thenReturn(Optional.of(icone));
        when(iconeProntoRepository.save(icone)).thenReturn(icone);

        IconeProntoResponse resposta = service.atualizarStatus(
                20L, StatusIconePronto.DISPONIVEL);

        assertThat(resposta.status()).isEqualTo(StatusIconePronto.DISPONIVEL);
        assertThat(resposta.encomendaId()).isNull();
    }

    @Test
    void deveImpedirSegundaPecaNaMesmaEncomenda() {
        Encomenda encomenda = criarEncomenda(criarModelo());
        when(encomendaRepository.findById(1L)).thenReturn(Optional.of(encomenda));
        when(iconeProntoRepository.existsByEncomenda_Id(1L)).thenReturn(true);

        assertThatThrownBy(() -> service.reservarCompativel(1L))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("A encomenda já possui uma peça pronta vinculada.");
    }

    @Test
    void deveFalharQuandoNaoExistirPecaCompativel() {
        ModeloIcone modelo = criarModelo();
        Encomenda encomenda = criarEncomenda(modelo);
        when(encomendaRepository.findById(1L)).thenReturn(Optional.of(encomenda));
        when(iconeProntoRepository.findFirstByModeloIcone_IdAndTamanhoAndAcabamentoIgnoreCaseAndStatus(
                10L, TamanhoIcone.MEDIO, "Envernizado", StatusIconePronto.DISPONIVEL))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.reservarCompativel(1L))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("Não existe peça pronta compatível");
    }

    private ModeloIcone criarModelo() {
        ModeloIcone modelo = new ModeloIcone();
        modelo.setId(10L);
        modelo.setNome("Sagrada Família");
        return modelo;
    }

    private Encomenda criarEncomenda(ModeloIcone modelo) {
        Encomenda encomenda = new Encomenda();
        encomenda.setId(1L);
        encomenda.setStatusEncomenda(StatusEncomenda.EM_PRODUCAO);
        ItemEncomenda item = new ItemEncomenda();
        item.setModeloIcone(modelo);
        item.setEncomenda(encomenda);
        Personalizacao personalizacao = new Personalizacao();
        personalizacao.setItemEncomenda(item);
        personalizacao.setTamanho(TamanhoIcone.MEDIO);
        personalizacao.setAcabamento("Envernizado");
        item.setPersonalizacao(personalizacao);
        encomenda.getItens().add(item);
        return encomenda;
    }

    private IconePronto criarIcone(ModeloIcone modelo) {
        IconePronto icone = new IconePronto();
        icone.setId(20L);
        icone.setModeloIcone(modelo);
        icone.setTamanho(TamanhoIcone.MEDIO);
        icone.setAcabamento("Envernizado");
        icone.setCustoProducao(new BigDecimal("120.00"));
        icone.setStatus(StatusIconePronto.DISPONIVEL);
        return icone;
    }
}
