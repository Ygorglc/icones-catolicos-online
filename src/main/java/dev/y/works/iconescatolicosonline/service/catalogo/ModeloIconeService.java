package dev.y.works.iconescatolicosonline.service.catalogo;

import dev.y.works.iconescatolicosonline.domain.catalogo.ConteudoDevocional;
import dev.y.works.iconescatolicosonline.domain.catalogo.ModeloIcone;
import dev.y.works.iconescatolicosonline.dto.catalogo.ConteudoDevocionalResponse;
import dev.y.works.iconescatolicosonline.dto.catalogo.ModeloIconeDetalheResponse;
import dev.y.works.iconescatolicosonline.dto.catalogo.ModeloIconeResumoResponse;
import dev.y.works.iconescatolicosonline.exception.RecursoNaoEncontradoException;
import dev.y.works.iconescatolicosonline.repository.catalogo.ModeloIconeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ModeloIconeService {

    private final ModeloIconeRepository modeloIconeRepository;

    public ModeloIconeService(ModeloIconeRepository modeloIconeRepository) {
        this.modeloIconeRepository = modeloIconeRepository;
    }

    @Transactional(readOnly = true)
    public List<ModeloIconeResumoResponse> listarModelosAtivos() {
        return modeloIconeRepository.findByAtivoTrueOrderByNomeAsc()
                .stream()
                .map(this::paraResumo)
                .toList();
    }

    @Transactional(readOnly = true)
    public ModeloIconeDetalheResponse buscarModeloAtivo(Long id) {
        ModeloIcone modelo = modeloIconeRepository.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Modelo de ícone não encontrado."
                ));
        return paraDetalhe(modelo);
    }

    private ModeloIconeResumoResponse paraResumo(ModeloIcone modelo) {
        return new ModeloIconeResumoResponse(
                modelo.getId(),
                modelo.getNome(),
                modelo.getImagemUrl(),
                modelo.getPrecoBase()
        );
    }

    private ModeloIconeDetalheResponse paraDetalhe(ModeloIcone modelo) {
        return new ModeloIconeDetalheResponse(
                modelo.getId(),
                modelo.getNome(),
                modelo.getDescricao(),
                modelo.getImagemUrl(),
                modelo.getPrecoBase(),
                modelo.isAtivo(),
                modelo.getCriadoEm(),
                paraConteudoDevocional(modelo.getConteudoDevocional())
        );
    }

    private ConteudoDevocionalResponse paraConteudoDevocional(ConteudoDevocional conteudo) {
        if (conteudo == null) {
            return null;
        }
        return new ConteudoDevocionalResponse(
                conteudo.getId(),
                conteudo.getHistoria(),
                conteudo.getSignificado(),
                conteudo.getSimbologia(),
                conteudo.getOracao(),
                conteudo.getOcasiaoPresente(),
                conteudo.getCuidados()
        );
    }
}
