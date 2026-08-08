package dev.y.works.iconescatolicosonline.service.catalogo;

import dev.y.works.iconescatolicosonline.domain.catalogo.ConteudoDevocional;
import dev.y.works.iconescatolicosonline.domain.catalogo.ModeloIcone;
import dev.y.works.iconescatolicosonline.dto.catalogo.ConteudoDevocionalResponse;
import dev.y.works.iconescatolicosonline.dto.catalogo.ConteudoDevocionalRequest;
import dev.y.works.iconescatolicosonline.dto.catalogo.ModeloIconeDetalheResponse;
import dev.y.works.iconescatolicosonline.dto.catalogo.ModeloIconeRequest;
import dev.y.works.iconescatolicosonline.dto.catalogo.ModeloIconeResumoResponse;
import dev.y.works.iconescatolicosonline.exception.ConflitoException;
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

    @Transactional(readOnly = true)
    public List<ModeloIconeDetalheResponse> listarTodos() {
        return modeloIconeRepository.findAll()
                .stream()
                .map(this::paraDetalhe)
                .toList();
    }

    @Transactional(readOnly = true)
    public ModeloIconeDetalheResponse buscarPorId(Long id) {
        return paraDetalhe(buscarEntidade(id));
    }

    @Transactional
    public ModeloIconeDetalheResponse criar(ModeloIconeRequest request) {
        String nome = request.nome().trim();
        if (modeloIconeRepository.existsByNomeIgnoreCase(nome)) {
            throw new ConflitoException("Já existe um modelo de ícone com esse nome.");
        }

        ModeloIcone modelo = new ModeloIcone();
        aplicarDados(modelo, request, nome);
        return paraDetalhe(modeloIconeRepository.save(modelo));
    }

    @Transactional
    public ModeloIconeDetalheResponse atualizar(Long id, ModeloIconeRequest request) {
        ModeloIcone modelo = buscarEntidade(id);
        String nome = request.nome().trim();
        if (modeloIconeRepository.existsByNomeIgnoreCaseAndIdNot(nome, id)) {
            throw new ConflitoException("Já existe um modelo de ícone com esse nome.");
        }

        aplicarDados(modelo, request, nome);
        return paraDetalhe(modeloIconeRepository.save(modelo));
    }

    @Transactional
    public void desativar(Long id) {
        ModeloIcone modelo = buscarEntidade(id);
        modelo.setAtivo(false);
        modeloIconeRepository.save(modelo);
    }

    private ModeloIcone buscarEntidade(Long id) {
        return modeloIconeRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Modelo de ícone não encontrado."
                ));
    }

    private void aplicarDados(ModeloIcone modelo, ModeloIconeRequest request, String nome) {
        modelo.setNome(nome);
        modelo.setDescricao(request.descricao().trim());
        modelo.setImagemUrl(request.imagemUrl());
        modelo.setPrecoBase(request.precoBase());
        modelo.setAtivo(request.ativo() == null || request.ativo());
        aplicarConteudoDevocional(modelo, request.conteudoDevocional());
    }

    private void aplicarConteudoDevocional(ModeloIcone modelo, ConteudoDevocionalRequest request) {
        if (request == null) {
            modelo.setConteudoDevocional(null);
            return;
        }

        ConteudoDevocional conteudo = modelo.getConteudoDevocional();
        if (conteudo == null) {
            conteudo = new ConteudoDevocional();
            conteudo.setModeloIcone(modelo);
            modelo.setConteudoDevocional(conteudo);
        }
        conteudo.setHistoria(request.historia());
        conteudo.setSignificado(request.significado());
        conteudo.setSimbologia(request.simbologia());
        conteudo.setOracao(request.oracao());
        conteudo.setOcasiaoPresente(request.ocasiaoPresente());
        conteudo.setCuidados(request.cuidados());
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
