package dev.y.works.iconescatolicosonline.service.catalogo;

import dev.y.works.iconescatolicosonline.domain.catalogo.ConteudoDevocional;
import dev.y.works.iconescatolicosonline.domain.catalogo.ModeloIcone;
import dev.y.works.iconescatolicosonline.dto.catalogo.ModeloIconeDetalheResponse;
import dev.y.works.iconescatolicosonline.dto.catalogo.ModeloIconeRequest;
import dev.y.works.iconescatolicosonline.dto.catalogo.ModeloIconeResumoResponse;
import dev.y.works.iconescatolicosonline.exception.ConflitoException;
import dev.y.works.iconescatolicosonline.exception.RecursoNaoEncontradoException;
import dev.y.works.iconescatolicosonline.repository.catalogo.ModeloIconeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class ModeloIconeServiceTests {

    @Mock
    private ModeloIconeRepository modeloIconeRepository;

    @InjectMocks
    private ModeloIconeService modeloIconeService;

    @Test
    void deveListarSomenteOsModelosAtivosRetornadosPeloRepository() {
        ModeloIcone modelo = criarModelo();
        when(modeloIconeRepository.findByAtivoTrueOrderByNomeAsc())
                .thenReturn(List.of(modelo));

        List<ModeloIconeResumoResponse> resultado = modeloIconeService.listarModelosAtivos();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.getFirst().id()).isEqualTo(1L);
        assertThat(resultado.getFirst().nome()).isEqualTo("Sagrada Família");
        assertThat(resultado.getFirst().precoBase()).isEqualByComparingTo("320.00");
        verify(modeloIconeRepository).findByAtivoTrueOrderByNomeAsc();
    }

    @Test
    void deveRetornarDetalhesComConteudoDevocional() {
        ModeloIcone modelo = criarModelo();
        ConteudoDevocional conteudo = new ConteudoDevocional();
        conteudo.setId(10L);
        conteudo.setModeloIcone(modelo);
        conteudo.setSignificado("União e cuidado da família.");
        modelo.setConteudoDevocional(conteudo);
        when(modeloIconeRepository.findByIdAndAtivoTrue(1L)).thenReturn(Optional.of(modelo));

        ModeloIconeDetalheResponse resultado = modeloIconeService.buscarModeloAtivo(1L);

        assertThat(resultado.id()).isEqualTo(1L);
        assertThat(resultado.descricao()).isEqualTo("Ícone artesanal em madeira.");
        assertThat(resultado.conteudoDevocional()).isNotNull();
        assertThat(resultado.conteudoDevocional().significado())
                .isEqualTo("União e cuidado da família.");
    }

    @Test
    void deveFalharQuandoModeloAtivoNaoExistir() {
        when(modeloIconeRepository.findByIdAndAtivoTrue(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> modeloIconeService.buscarModeloAtivo(99L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Modelo de ícone não encontrado.");
    }

    @Test
    void deveCriarModeloAtivoPorPadrao() {
        ModeloIconeRequest request = new ModeloIconeRequest(
                "  São Bento  ", "Ícone em madeira", null,
                new BigDecimal("250.00"), null, null);
        when(modeloIconeRepository.existsByNomeIgnoreCase("São Bento")).thenReturn(false);
        when(modeloIconeRepository.save(any(ModeloIcone.class))).thenAnswer(invocation -> {
            ModeloIcone salvo = invocation.getArgument(0);
            salvo.setId(2L);
            return salvo;
        });

        ModeloIconeDetalheResponse resultado = modeloIconeService.criar(request);

        assertThat(resultado.id()).isEqualTo(2L);
        assertThat(resultado.nome()).isEqualTo("São Bento");
        assertThat(resultado.ativo()).isTrue();
    }

    @Test
    void deveImpedirNomeDuplicadoAoCriar() {
        ModeloIconeRequest request = new ModeloIconeRequest(
                "São Bento", "Ícone em madeira", null,
                new BigDecimal("250.00"), true, null);
        when(modeloIconeRepository.existsByNomeIgnoreCase("São Bento")).thenReturn(true);

        assertThatThrownBy(() -> modeloIconeService.criar(request))
                .isInstanceOf(ConflitoException.class)
                .hasMessage("Já existe um modelo de ícone com esse nome.");
    }

    @Test
    void deveDesativarModeloSemExcluiLo() {
        ModeloIcone modelo = criarModelo();
        when(modeloIconeRepository.findById(1L)).thenReturn(Optional.of(modelo));

        modeloIconeService.desativar(1L);

        assertThat(modelo.isAtivo()).isFalse();
        verify(modeloIconeRepository).save(modelo);
    }

    private ModeloIcone criarModelo() {
        ModeloIcone modelo = new ModeloIcone();
        modelo.setId(1L);
        modelo.setNome("Sagrada Família");
        modelo.setDescricao("Ícone artesanal em madeira.");
        modelo.setImagemUrl("https://exemplo.local/sagrada-familia.jpg");
        modelo.setPrecoBase(new BigDecimal("320.00"));
        modelo.setAtivo(true);
        modelo.setCriadoEm(Instant.parse("2026-08-08T12:00:00Z"));
        return modelo;
    }
}
