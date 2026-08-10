package dev.y.works.iconescatolicosonline.controller;

import dev.y.works.iconescatolicosonline.dto.estoque.AtualizarStatusIconeProntoRequest;
import dev.y.works.iconescatolicosonline.dto.estoque.IconeProntoRequest;
import dev.y.works.iconescatolicosonline.dto.estoque.IconeProntoResponse;
import dev.y.works.iconescatolicosonline.dto.estoque.ReservarIconeProntoRequest;
import dev.y.works.iconescatolicosonline.service.estoque.IconeProntoService;
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
@RequestMapping("/api/admin/estoque/icones-prontos")
@SecurityRequirement(name = "bearerAuth")
public class IconeProntoAdminController {

    private final IconeProntoService iconeProntoService;

    public IconeProntoAdminController(IconeProntoService iconeProntoService) {
        this.iconeProntoService = iconeProntoService;
    }

    @GetMapping
    public ResponseEntity<List<IconeProntoResponse>> listar() {
        return ResponseEntity.ok(iconeProntoService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<IconeProntoResponse> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(iconeProntoService.buscar(id));
    }

    @PostMapping
    public ResponseEntity<IconeProntoResponse> criar(
            @Valid @RequestBody IconeProntoRequest request) {
        IconeProntoResponse criado = iconeProntoService.criar(request);
        return ResponseEntity.created(
                URI.create("/api/admin/estoque/icones-prontos/" + criado.id())).body(criado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<IconeProntoResponse> atualizar(
            @PathVariable Long id, @Valid @RequestBody IconeProntoRequest request) {
        return ResponseEntity.ok(iconeProntoService.atualizar(id, request));
    }

    @PostMapping("/reservas")
    public ResponseEntity<IconeProntoResponse> reservar(
            @Valid @RequestBody ReservarIconeProntoRequest request) {
        return ResponseEntity.ok(iconeProntoService.reservarCompativel(request.encomendaId()));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<IconeProntoResponse> atualizarStatus(
            @PathVariable Long id,
            @Valid @RequestBody AtualizarStatusIconeProntoRequest request) {
        return ResponseEntity.ok(iconeProntoService.atualizarStatus(id, request.status()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        iconeProntoService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
