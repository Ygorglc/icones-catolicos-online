package dev.y.works.iconescatolicosonline.controller;

import dev.y.works.iconescatolicosonline.dto.estoque.MaterialRequest;
import dev.y.works.iconescatolicosonline.dto.estoque.MaterialResponse;
import dev.y.works.iconescatolicosonline.dto.estoque.MovimentarMaterialRequest;
import dev.y.works.iconescatolicosonline.service.estoque.MaterialService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/admin/estoque/materiais")
@SecurityRequirement(name = "bearerAuth")
public class MaterialAdminController {

    private final MaterialService materialService;

    public MaterialAdminController(MaterialService materialService) {
        this.materialService = materialService;
    }

    @GetMapping
    public ResponseEntity<List<MaterialResponse>> listar() {
        return ResponseEntity.ok(materialService.listar());
    }

    @GetMapping("/estoque-baixo")
    public ResponseEntity<List<MaterialResponse>> listarComEstoqueBaixo() {
        return ResponseEntity.ok(materialService.listarComEstoqueBaixo());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MaterialResponse> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(materialService.buscar(id));
    }

    @PostMapping
    public ResponseEntity<MaterialResponse> criar(@Valid @RequestBody MaterialRequest request) {
        MaterialResponse criado = materialService.criar(request);
        return ResponseEntity.created(
                URI.create("/api/admin/estoque/materiais/" + criado.id())).body(criado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MaterialResponse> atualizar(
            @PathVariable Long id, @Valid @RequestBody MaterialRequest request) {
        return ResponseEntity.ok(materialService.atualizar(id, request));
    }

    @PatchMapping("/{id}/movimentacoes")
    public ResponseEntity<MaterialResponse> movimentar(
            @PathVariable Long id, @Valid @RequestBody MovimentarMaterialRequest request) {
        return ResponseEntity.ok(materialService.movimentar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        materialService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
