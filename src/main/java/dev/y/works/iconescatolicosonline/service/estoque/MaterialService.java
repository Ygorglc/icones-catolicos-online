package dev.y.works.iconescatolicosonline.service.estoque;

import dev.y.works.iconescatolicosonline.domain.estoque.Material;
import dev.y.works.iconescatolicosonline.domain.estoque.TipoMovimentacaoMaterial;
import dev.y.works.iconescatolicosonline.dto.estoque.MaterialRequest;
import dev.y.works.iconescatolicosonline.dto.estoque.MaterialResponse;
import dev.y.works.iconescatolicosonline.dto.estoque.MovimentarMaterialRequest;
import dev.y.works.iconescatolicosonline.exception.ConflitoException;
import dev.y.works.iconescatolicosonline.exception.RecursoNaoEncontradoException;
import dev.y.works.iconescatolicosonline.exception.RegraNegocioException;
import dev.y.works.iconescatolicosonline.repository.estoque.MaterialRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class MaterialService {

    private final MaterialRepository materialRepository;

    public MaterialService(MaterialRepository materialRepository) {
        this.materialRepository = materialRepository;
    }

    @Transactional(readOnly = true)
    public List<MaterialResponse> listar() {
        return materialRepository.findAllByOrderByNomeAsc().stream().map(this::mapear).toList();
    }

    @Transactional(readOnly = true)
    public List<MaterialResponse> listarComEstoqueBaixo() {
        return materialRepository.buscarComEstoqueBaixo().stream().map(this::mapear).toList();
    }

    @Transactional(readOnly = true)
    public MaterialResponse buscar(Long id) {
        return mapear(buscarEntidade(id));
    }

    @Transactional
    public MaterialResponse criar(MaterialRequest request) {
        String nome = request.nome().trim();
        if (materialRepository.existsByNomeIgnoreCase(nome)) {
            throw new ConflitoException("Já existe um material com esse nome.");
        }
        Material material = new Material();
        aplicar(material, request, nome);
        return mapear(materialRepository.save(material));
    }

    @Transactional
    public MaterialResponse atualizar(Long id, MaterialRequest request) {
        Material material = buscarEntidade(id);
        String nome = request.nome().trim();
        if (materialRepository.existsByNomeIgnoreCaseAndIdNot(nome, id)) {
            throw new ConflitoException("Já existe um material com esse nome.");
        }
        aplicar(material, request, nome);
        return mapear(materialRepository.save(material));
    }

    @Transactional
    public MaterialResponse movimentar(Long id, MovimentarMaterialRequest request) {
        Material material = materialRepository.buscarPorIdComBloqueio(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Material não encontrado."));
        BigDecimal novaQuantidade = request.tipo() == TipoMovimentacaoMaterial.ENTRADA
                ? material.getQuantidade().add(request.quantidade())
                : material.getQuantidade().subtract(request.quantidade());
        if (novaQuantidade.signum() < 0) {
            throw new RegraNegocioException("A saída excede a quantidade disponível do material.");
        }
        material.setQuantidade(novaQuantidade);
        return mapear(materialRepository.save(material));
    }

    @Transactional
    public void excluir(Long id) {
        materialRepository.delete(buscarEntidade(id));
    }

    private Material buscarEntidade(Long id) {
        return materialRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Material não encontrado."));
    }

    private void aplicar(Material material, MaterialRequest request, String nome) {
        material.setNome(nome);
        material.setUnidadeMedida(request.unidadeMedida().trim());
        material.setQuantidade(request.quantidade());
        material.setCustoUnitario(request.custoUnitario());
        material.setEstoqueMinimo(request.estoqueMinimo());
    }

    private MaterialResponse mapear(Material material) {
        return new MaterialResponse(
                material.getId(), material.getNome(), material.getUnidadeMedida(),
                material.getQuantidade(), material.getCustoUnitario(),
                material.getEstoqueMinimo(),
                material.getQuantidade().compareTo(material.getEstoqueMinimo()) <= 0);
    }
}
