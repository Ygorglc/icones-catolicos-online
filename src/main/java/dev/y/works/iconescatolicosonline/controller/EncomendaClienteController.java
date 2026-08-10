package dev.y.works.iconescatolicosonline.controller;

import dev.y.works.iconescatolicosonline.dto.encomenda.CriarEncomendaRequest;
import dev.y.works.iconescatolicosonline.dto.encomenda.EncomendaResponse;
import dev.y.works.iconescatolicosonline.service.encomenda.EncomendaService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/encomendas")
@SecurityRequirement(name = "bearerAuth")
public class EncomendaClienteController {

    private final EncomendaService encomendaService;

    public EncomendaClienteController(EncomendaService encomendaService) {
        this.encomendaService = encomendaService;
    }

    @PostMapping
    public ResponseEntity<EncomendaResponse> criar(
            Authentication authentication,
            @Valid @RequestBody CriarEncomendaRequest request) {
        EncomendaResponse criada = encomendaService.criar(authentication.getName(), request);
        return ResponseEntity.created(URI.create("/api/encomendas/" + criada.id())).body(criada);
    }

    @GetMapping
    public ResponseEntity<List<EncomendaResponse>> listar(Authentication authentication) {
        return ResponseEntity.ok(encomendaService.listarDoCliente(authentication.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EncomendaResponse> buscar(
            @PathVariable Long id,
            Authentication authentication) {
        return ResponseEntity.ok(
                encomendaService.buscarDoCliente(id, authentication.getName()));
    }
}
