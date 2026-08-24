package dev.y.works.iconescatolicosonline.service.certificado;

import dev.y.works.iconescatolicosonline.domain.catalogo.ModeloIcone;
import dev.y.works.iconescatolicosonline.domain.catalogo.TamanhoIcone;
import dev.y.works.iconescatolicosonline.domain.encomenda.Encomenda;
import dev.y.works.iconescatolicosonline.domain.encomenda.StatusEncomenda;
import dev.y.works.iconescatolicosonline.domain.encomenda.StatusFinanceiro;
import dev.y.works.iconescatolicosonline.domain.estoque.IconePronto;
import dev.y.works.iconescatolicosonline.domain.financeiro.CertificadoArtesanal;
import dev.y.works.iconescatolicosonline.dto.certificado.CertificadoArtesanalResponse;
import dev.y.works.iconescatolicosonline.dto.certificado.GerarCertificadoRequest;
import dev.y.works.iconescatolicosonline.exception.ConflitoException;
import dev.y.works.iconescatolicosonline.exception.RecursoNaoEncontradoException;
import dev.y.works.iconescatolicosonline.exception.RegraNegocioException;
import dev.y.works.iconescatolicosonline.repository.encomenda.EncomendaRepository;
import dev.y.works.iconescatolicosonline.repository.financeiro.CertificadoArtesanalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CertificadoArtesanalServiceTests {

    @Mock CertificadoArtesanalRepository certificadoRepository;
    @Mock EncomendaRepository encomendaRepository;
    private CertificadoArtesanalService service;

    @BeforeEach
    void configurar() {
        service = new CertificadoArtesanalService(certificadoRepository, encomendaRepository);
    }

    @Test
    void deveGerarCertificadoParaEncomendaConcluidaEPaga() {
        Encomenda encomenda = criarEncomendaConcluida();
        when(encomendaRepository.findById(1L)).thenReturn(Optional.of(encomenda));
        when(certificadoRepository.save(any(CertificadoArtesanal.class)))
                .thenAnswer(invocation -> {
                    CertificadoArtesanal certificado = invocation.getArgument(0);
                    certificado.setId(30L);
                    return certificado;
                });

        CertificadoArtesanalResponse resposta = service.gerar(1L,
                new GerarCertificadoRequest("  Ygor Artesão  ", "  Madeira e verniz  "));

        assertThat(resposta.id()).isEqualTo(30L);
        assertThat(resposta.encomendaId()).isEqualTo(1L);
        assertThat(resposta.numeroPeca())
                .startsWith("ICA-" + LocalDate.now().getYear() + "-");
        assertThat(resposta.codigoPublico()).hasSize(64).matches("[0-9a-f]{64}");
        assertThat(resposta.nomeArtesao()).isEqualTo("Ygor Artesão");
        assertThat(resposta.modeloIcone()).isEqualTo("Sagrada Família");
        assertThat(resposta.tamanhoIcone()).isEqualTo("MEDIO");
        assertThat(resposta.acabamento()).isEqualTo("Envernizado");
        assertThat(resposta.materialUtilizado()).isEqualTo("Madeira e verniz");
        assertThat(resposta.autentico()).isTrue();
    }

    @Test
    void deveImpedirCertificadoAntesDaConclusao() {
        Encomenda encomenda = criarEncomendaConcluida();
        encomenda.setStatusEncomenda(StatusEncomenda.EM_PRODUCAO);
        when(encomendaRepository.findById(1L)).thenReturn(Optional.of(encomenda));

        assertThatThrownBy(() -> service.gerar(1L,
                new GerarCertificadoRequest("Ygor Artesão", "Madeira")))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("após a conclusão");

        verify(certificadoRepository, never()).save(any());
    }

    @Test
    void deveImpedirCertificadoComSaldoPendente() {
        Encomenda encomenda = criarEncomendaConcluida();
        encomenda.setStatusFinanceiro(StatusFinanceiro.AGUARDANDO_RESTANTE);
        when(encomendaRepository.findById(1L)).thenReturn(Optional.of(encomenda));

        assertThatThrownBy(() -> service.gerar(1L,
                new GerarCertificadoRequest("Ygor Artesão", "Madeira")))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("paga integralmente");
    }

    @Test
    void deveImpedirSegundoCertificadoParaMesmaEncomenda() {
        when(certificadoRepository.existsByEncomenda_Id(1L)).thenReturn(true);

        assertThatThrownBy(() -> service.gerar(1L,
                new GerarCertificadoRequest("Ygor Artesão", "Madeira")))
                .isInstanceOf(ConflitoException.class)
                .hasMessageContaining("já possui certificado");

        verify(encomendaRepository, never()).findById(any());
    }

    @Test
    void deveConsultarCertificadoPublicoPeloCodigoIgnorandoMaiusculasEEspacos() {
        CertificadoArtesanal certificado = criarCertificado();
        when(certificadoRepository.findByCodigoPublico("abc123"))
                .thenReturn(Optional.of(certificado));

        CertificadoArtesanalResponse resposta = service.consultarPublicamente("  ABC123  ");

        assertThat(resposta.codigoPublico()).isEqualTo("abc123");
        assertThat(resposta.autentico()).isTrue();
    }

    @Test
    void deveInformarQuandoCodigoPublicoNaoExistir() {
        when(certificadoRepository.findByCodigoPublico("invalido"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.consultarPublicamente("invalido"))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    private Encomenda criarEncomendaConcluida() {
        ModeloIcone modelo = new ModeloIcone();
        modelo.setId(10L);
        modelo.setNome("Sagrada Família");

        IconePronto icone = new IconePronto();
        icone.setId(20L);
        icone.setModeloIcone(modelo);
        icone.setTamanho(TamanhoIcone.MEDIO);
        icone.setAcabamento("Envernizado");

        Encomenda encomenda = new Encomenda();
        encomenda.setId(1L);
        encomenda.setStatusEncomenda(StatusEncomenda.CONCLUIDO);
        encomenda.setStatusFinanceiro(StatusFinanceiro.PAGO_INTEGRALMENTE);
        encomenda.setIconePronto(icone);
        return encomenda;
    }

    private CertificadoArtesanal criarCertificado() {
        CertificadoArtesanal certificado = new CertificadoArtesanal();
        certificado.setId(30L);
        certificado.setEncomenda(criarEncomendaConcluida());
        certificado.setNumeroPeca("ICA-2026-12345678");
        certificado.setDataEmissao(LocalDate.now());
        certificado.setNomeArtesao("Ygor Artesão");
        certificado.setModeloIcone("Sagrada Família");
        certificado.setTamanhoIcone("MEDIO");
        certificado.setAcabamento("Envernizado");
        certificado.setMaterialUtilizado("Madeira");
        certificado.setTextoCertificado("Certificado artesanal.");
        certificado.setCodigoPublico("abc123");
        return certificado;
    }
}
