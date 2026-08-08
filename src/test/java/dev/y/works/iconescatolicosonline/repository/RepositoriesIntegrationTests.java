package dev.y.works.iconescatolicosonline.repository;

import dev.y.works.iconescatolicosonline.domain.encomenda.StatusEncomenda;
import dev.y.works.iconescatolicosonline.domain.encomenda.StatusFinanceiro;
import dev.y.works.iconescatolicosonline.domain.estoque.StatusIconePronto;
import dev.y.works.iconescatolicosonline.domain.pagamento.StatusPagamento;
import dev.y.works.iconescatolicosonline.repository.catalogo.ConteudoDevocionalRepository;
import dev.y.works.iconescatolicosonline.repository.catalogo.ModeloIconeRepository;
import dev.y.works.iconescatolicosonline.repository.encomenda.EncomendaRepository;
import dev.y.works.iconescatolicosonline.repository.encomenda.ItemEncomendaRepository;
import dev.y.works.iconescatolicosonline.repository.encomenda.PersonalizacaoRepository;
import dev.y.works.iconescatolicosonline.repository.estoque.IconeProntoRepository;
import dev.y.works.iconescatolicosonline.repository.estoque.MaterialRepository;
import dev.y.works.iconescatolicosonline.repository.financeiro.CertificadoArtesanalRepository;
import dev.y.works.iconescatolicosonline.repository.financeiro.GastoRepository;
import dev.y.works.iconescatolicosonline.repository.financeiro.VendaRepository;
import dev.y.works.iconescatolicosonline.repository.pagamento.PagamentoRepository;
import dev.y.works.iconescatolicosonline.repository.usuario.AdministradorRepository;
import dev.y.works.iconescatolicosonline.repository.usuario.ClienteRepository;
import dev.y.works.iconescatolicosonline.repository.usuario.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE)
class RepositoriesIntegrationTests {

    private static final Long ID_INEXISTENTE = Long.MAX_VALUE;

    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private AdministradorRepository administradorRepository;
    @Autowired
    private ModeloIconeRepository modeloIconeRepository;
    @Autowired
    private ConteudoDevocionalRepository conteudoDevocionalRepository;
    @Autowired
    private EncomendaRepository encomendaRepository;
    @Autowired
    private ItemEncomendaRepository itemEncomendaRepository;
    @Autowired
    private PersonalizacaoRepository personalizacaoRepository;
    @Autowired
    private PagamentoRepository pagamentoRepository;
    @Autowired
    private IconeProntoRepository iconeProntoRepository;
    @Autowired
    private MaterialRepository materialRepository;
    @Autowired
    private GastoRepository gastoRepository;
    @Autowired
    private VendaRepository vendaRepository;
    @Autowired
    private CertificadoArtesanalRepository certificadoArtesanalRepository;

    @Test
    void deveCriarOsQuatorzeRepositories() {
        assertThat(usuarioRepository).isNotNull();
        assertThat(clienteRepository).isNotNull();
        assertThat(administradorRepository).isNotNull();
        assertThat(modeloIconeRepository).isNotNull();
        assertThat(conteudoDevocionalRepository).isNotNull();
        assertThat(encomendaRepository).isNotNull();
        assertThat(itemEncomendaRepository).isNotNull();
        assertThat(personalizacaoRepository).isNotNull();
        assertThat(pagamentoRepository).isNotNull();
        assertThat(iconeProntoRepository).isNotNull();
        assertThat(materialRepository).isNotNull();
        assertThat(gastoRepository).isNotNull();
        assertThat(vendaRepository).isNotNull();
        assertThat(certificadoArtesanalRepository).isNotNull();
    }

    @Test
    void deveExecutarConsultasDeUsuariosECatalogo() {
        assertThat(usuarioRepository.findByEmailIgnoreCase("inexistente@teste.local")).isEmpty();
        assertThat(usuarioRepository.existsByEmailIgnoreCase("inexistente@teste.local")).isFalse();
        assertThat(clienteRepository.findByUsuario_Id(ID_INEXISTENTE)).isEmpty();
        assertThat(clienteRepository.findByCpf("999.999.999-99")).isEmpty();
        assertThat(administradorRepository.findByUsuario_Id(ID_INEXISTENTE)).isEmpty();
        assertThat(modeloIconeRepository.findByAtivoTrueOrderByNomeAsc()).isNotNull();
        assertThat(modeloIconeRepository.findByIdAndAtivoTrue(ID_INEXISTENTE)).isEmpty();
        assertThat(modeloIconeRepository.existsByNomeIgnoreCase("Modelo inexistente")).isFalse();
        assertThat(conteudoDevocionalRepository.findByModeloIcone_Id(ID_INEXISTENTE)).isEmpty();
    }

    @Test
    void deveExecutarConsultasDeEncomendasEPagamentos() {
        assertThat(encomendaRepository.findByCliente_IdOrderByDataCriacaoDesc(ID_INEXISTENTE)).isEmpty();
        assertThat(encomendaRepository.findByStatusEncomendaOrderByDataCriacaoAsc(
                StatusEncomenda.ENCOMENDA_CRIADA)).isNotNull();
        assertThat(encomendaRepository.findByStatusFinanceiroOrderByDataCriacaoAsc(
                StatusFinanceiro.AGUARDANDO_SINAL)).isNotNull();
        assertThat(itemEncomendaRepository.findByEncomenda_IdOrderByIdAsc(ID_INEXISTENTE)).isEmpty();
        assertThat(personalizacaoRepository.findByItemEncomenda_Id(ID_INEXISTENTE)).isEmpty();
        assertThat(pagamentoRepository.findByEncomenda_IdOrderByCriadoEmAsc(ID_INEXISTENTE)).isEmpty();
        assertThat(pagamentoRepository.findByEncomenda_IdAndStatus(
                ID_INEXISTENTE, StatusPagamento.PENDENTE)).isEmpty();
    }

    @Test
    void deveExecutarConsultasDeEstoque() {
        assertThat(iconeProntoRepository.findByStatusOrderByIdAsc(
                StatusIconePronto.DISPONIVEL)).isNotNull();
        assertThat(iconeProntoRepository
                .findFirstByModeloIcone_IdAndTamanhoIgnoreCaseAndAcabamentoIgnoreCaseAndStatus(
                        ID_INEXISTENTE,
                        "inexistente",
                        "inexistente",
                        StatusIconePronto.DISPONIVEL
                )).isEmpty();
        assertThat(materialRepository.existsByNomeIgnoreCase("Material inexistente")).isFalse();
        assertThat(materialRepository.buscarComEstoqueBaixo()).isNotNull();
    }

    @Test
    void deveExecutarConsultasFinanceiras() {
        LocalDate hoje = LocalDate.now();
        Instant agora = Instant.now();

        assertThat(gastoRepository.findByDataGastoBetweenOrderByDataGastoAsc(hoje, hoje)).isNotNull();
        assertThat(gastoRepository.findByEncomenda_IdOrderByDataGastoAsc(ID_INEXISTENTE)).isEmpty();
        assertThat(vendaRepository.findByEncomenda_Id(ID_INEXISTENTE)).isEmpty();
        assertThat(vendaRepository.findByDataVendaBetweenOrderByDataVendaAsc(
                Instant.EPOCH, agora)).isNotNull();
        assertThat(certificadoArtesanalRepository.findByEncomenda_Id(ID_INEXISTENTE)).isEmpty();
        assertThat(certificadoArtesanalRepository.findByCodigoPublico("codigo-inexistente")).isEmpty();
        assertThat(certificadoArtesanalRepository.existsByNumeroPeca("peca-inexistente")).isFalse();
    }
}
