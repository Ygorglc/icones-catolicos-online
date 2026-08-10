package dev.y.works.iconescatolicosonline.controller;

import dev.y.works.iconescatolicosonline.dto.encomenda.AtualizarStatusEncomendaRequest;
import dev.y.works.iconescatolicosonline.dto.encomenda.EncomendaResponse;
import dev.y.works.iconescatolicosonline.service.encomenda.EncomendaService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/encomendas")
@SecurityRequirement(name = "bearerAuth")
public class EncomendaAdminController {

    private final EncomendaService encomendaService;

    public EncomendaAdminController(EncomendaService encomendaService) {
        this.encomendaService = encomendaService;
    }

    @GetMapping
    public ResponseEntity<List<EncomendaResponse>> listarTodas() {
        return ResponseEntity.ok(encomendaService.listarTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EncomendaResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(encomendaService.buscarPorId(id));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<EncomendaResponse> atualizarStatus(
            @PathVariable Long id,
            @Valid @RequestBody AtualizarStatusEncomendaRequest request) {
        return ResponseEntity.ok(encomendaService.atualizarStatus(id, request.status()));
    }
}
