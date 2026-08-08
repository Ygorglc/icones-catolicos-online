package dev.y.works.iconescatolicosonline.controller;

import dev.y.works.iconescatolicosonline.dto.catalogo.ModeloIconeDetalheResponse;
import dev.y.works.iconescatolicosonline.dto.catalogo.ModeloIconeRequest;
import dev.y.works.iconescatolicosonline.service.catalogo.ModeloIconeService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/admin/modelos")
@SecurityRequirement(name = "bearerAuth")
public class ModeloIconeAdminController {

    private final ModeloIconeService modeloIconeService;

    public ModeloIconeAdminController(ModeloIconeService modeloIconeService) {
        this.modeloIconeService = modeloIconeService;
    }

    @GetMapping
    public ResponseEntity<List<ModeloIconeDetalheResponse>> listarTodos() {
        return ResponseEntity.ok(modeloIconeService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ModeloIconeDetalheResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(modeloIconeService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<ModeloIconeDetalheResponse> criar(
            @Valid @RequestBody ModeloIconeRequest request) {
        ModeloIconeDetalheResponse criado = modeloIconeService.criar(request);
        return ResponseEntity.created(URI.create("/api/admin/modelos/" + criado.id())).body(criado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ModeloIconeDetalheResponse> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody ModeloIconeRequest request) {
        return ResponseEntity.ok(modeloIconeService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desativar(@PathVariable Long id) {
        modeloIconeService.desativar(id);
        return ResponseEntity.noContent().build();
    }
}
