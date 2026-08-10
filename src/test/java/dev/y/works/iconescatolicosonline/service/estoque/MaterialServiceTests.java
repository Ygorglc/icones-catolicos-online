package dev.y.works.iconescatolicosonline.service.estoque;

import dev.y.works.iconescatolicosonline.domain.estoque.Material;
import dev.y.works.iconescatolicosonline.domain.estoque.TipoMovimentacaoMaterial;
import dev.y.works.iconescatolicosonline.dto.estoque.MaterialRequest;
import dev.y.works.iconescatolicosonline.dto.estoque.MaterialResponse;
import dev.y.works.iconescatolicosonline.dto.estoque.MovimentarMaterialRequest;
import dev.y.works.iconescatolicosonline.exception.RegraNegocioException;
import dev.y.works.iconescatolicosonline.repository.estoque.MaterialRepository;
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
class MaterialServiceTests {

    @Mock MaterialRepository materialRepository;
    private MaterialService service;

    @BeforeEach
    void configurar() {
        service = new MaterialService(materialRepository);
    }

    @Test
    void deveCriarMaterialIndicandoEstoqueBaixo() {
        when(materialRepository.save(any(Material.class))).thenAnswer(invocation -> {
            Material material = invocation.getArgument(0);
            material.setId(1L);
            return material;
        });
        MaterialRequest request = new MaterialRequest(
                "Madeira cedro", "UNIDADE", new BigDecimal("2.000"),
                new BigDecimal("40.00"), new BigDecimal("3.000"));

        MaterialResponse resposta = service.criar(request);

        assertThat(resposta.id()).isEqualTo(1L);
        assertThat(resposta.estoqueBaixo()).isTrue();
    }

    @Test
    void deveRegistrarEntradaESaida() {
        Material material = criarMaterial("5.000");
        when(materialRepository.buscarPorIdComBloqueio(1L)).thenReturn(Optional.of(material));
        when(materialRepository.save(material)).thenReturn(material);

        MaterialResponse entrada = service.movimentar(
                1L, new MovimentarMaterialRequest(
                        TipoMovimentacaoMaterial.ENTRADA, new BigDecimal("2.000")));
        MaterialResponse saida = service.movimentar(
                1L, new MovimentarMaterialRequest(
                        TipoMovimentacaoMaterial.SAIDA, new BigDecimal("1.500")));

        assertThat(entrada.quantidade()).isEqualByComparingTo("7.000");
        assertThat(saida.quantidade()).isEqualByComparingTo("5.500");
    }

    @Test
    void deveImpedirSaidaMaiorQueEstoque() {
        Material material = criarMaterial("2.000");
        when(materialRepository.buscarPorIdComBloqueio(1L)).thenReturn(Optional.of(material));

        assertThatThrownBy(() -> service.movimentar(
                1L, new MovimentarMaterialRequest(
                        TipoMovimentacaoMaterial.SAIDA, new BigDecimal("3.000"))))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("A saída excede a quantidade disponível do material.");
    }

    private Material criarMaterial(String quantidade) {
        Material material = new Material();
        material.setId(1L);
        material.setNome("Madeira cedro");
        material.setUnidadeMedida("UNIDADE");
        material.setQuantidade(new BigDecimal(quantidade));
        material.setCustoUnitario(new BigDecimal("40.00"));
        material.setEstoqueMinimo(new BigDecimal("1.000"));
        return material;
    }
}
