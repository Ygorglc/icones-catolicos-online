package dev.y.works.iconescatolicosonline.service.certificado;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class CertificadoArtesanalService {

    private final CertificadoArtesanalRepository certificadoRepository;
    private final EncomendaRepository encomendaRepository;

    public CertificadoArtesanalService(
            CertificadoArtesanalRepository certificadoRepository,
            EncomendaRepository encomendaRepository) {
        this.certificadoRepository = certificadoRepository;
        this.encomendaRepository = encomendaRepository;
    }

    @Transactional
    public CertificadoArtesanalResponse gerar(
            Long encomendaId, GerarCertificadoRequest request) {
        if (certificadoRepository.existsByEncomenda_Id(encomendaId)) {
            throw new ConflitoException("A encomenda já possui certificado artesanal.");
        }
        Encomenda encomenda = encomendaRepository.findById(encomendaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Encomenda não encontrada."));
        validarEmissao(encomenda);
        IconePronto icone = encomenda.getIconePronto();

        CertificadoArtesanal certificado = new CertificadoArtesanal();
        certificado.setEncomenda(encomenda);
        certificado.setNumeroPeca(gerarNumeroPeca());
        certificado.setDataEmissao(LocalDate.now());
        certificado.setNomeArtesao(request.nomeArtesao().trim());
        certificado.setModeloIcone(icone.getModeloIcone().getNome());
        certificado.setTamanhoIcone(icone.getTamanho().name());
        certificado.setAcabamento(icone.getAcabamento());
        certificado.setMaterialUtilizado(request.materialUtilizado().trim());
        certificado.setTextoCertificado(criarTexto(certificado));
        certificado.setCodigoPublico(gerarCodigoPublico());
        return mapear(certificadoRepository.save(certificado));
    }

    @Transactional(readOnly = true)
    public List<CertificadoArtesanalResponse> listar() {
        return certificadoRepository.findAllByOrderByDataEmissaoDescIdDesc()
                .stream().map(this::mapear).toList();
    }

    @Transactional(readOnly = true)
    public CertificadoArtesanalResponse buscarPorEncomenda(Long encomendaId) {
        return mapear(certificadoRepository.findByEncomenda_Id(encomendaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Certificado artesanal não encontrado.")));
    }

    @Transactional(readOnly = true)
    public CertificadoArtesanalResponse buscarDoCliente(
            Long encomendaId, String emailCliente) {
        encomendaRepository.findByIdAndCliente_Usuario_EmailIgnoreCase(
                        encomendaId, emailCliente)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Encomenda não encontrada."));
        return buscarPorEncomenda(encomendaId);
    }

    @Transactional(readOnly = true)
    public CertificadoArtesanalResponse consultarPublicamente(String codigoPublico) {
        String codigo = codigoPublico == null
                ? "" : codigoPublico.trim().toLowerCase(Locale.ROOT);
        return mapear(certificadoRepository.findByCodigoPublico(codigo)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Certificado artesanal não encontrado.")));
    }

    private void validarEmissao(Encomenda encomenda) {
        if (encomenda.getStatusEncomenda() != StatusEncomenda.ENTREGUE_E_CONCLUIDO) {
            throw new RegraNegocioException(
                    "O certificado só pode ser gerado após a conclusão da encomenda.");
        }
        if (encomenda.getStatusFinanceiro() != StatusFinanceiro.PAGO_INTEGRALMENTE) {
            throw new RegraNegocioException(
                    "O certificado exige que a encomenda esteja paga integralmente.");
        }
        if (encomenda.getIconePronto() == null) {
            throw new RegraNegocioException(
                    "A encomenda precisa possuir uma peça pronta vinculada.");
        }
    }

    private String gerarNumeroPeca() {
        String numero;
        do {
            numero = "ICA-" + LocalDate.now().getYear() + "-"
                    + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
        } while (certificadoRepository.existsByNumeroPeca(numero));
        return numero;
    }

    private String gerarCodigoPublico() {
        String codigo;
        do {
            codigo = (UUID.randomUUID().toString() + UUID.randomUUID())
                    .replace("-", "").toLowerCase(Locale.ROOT);
        } while (certificadoRepository.existsByCodigoPublico(codigo));
        return codigo;
    }

    private String criarTexto(CertificadoArtesanal certificado) {
        return "Certificamos que a peça " + certificado.getNumeroPeca()
                + ", modelo " + certificado.getModeloIcone()
                + ", tamanho " + certificado.getTamanhoIcone()
                + " e acabamento " + certificado.getAcabamento()
                + ", foi produzida artesanalmente por " + certificado.getNomeArtesao() + ".";
    }

    private CertificadoArtesanalResponse mapear(CertificadoArtesanal certificado) {
        return new CertificadoArtesanalResponse(
                certificado.getId(), certificado.getEncomenda().getId(),
                certificado.getNumeroPeca(), certificado.getDataEmissao(),
                certificado.getNomeArtesao(), certificado.getModeloIcone(),
                certificado.getTamanhoIcone(), certificado.getAcabamento(),
                certificado.getMaterialUtilizado(), certificado.getTextoCertificado(),
                certificado.getCodigoPublico(), true);
    }
}
