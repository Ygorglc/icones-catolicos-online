package dev.y.works.iconescatolicosonline.controller;

import dev.y.works.iconescatolicosonline.dto.cliente.EnderecoClienteRequest;
import dev.y.works.iconescatolicosonline.dto.cliente.EnderecoClienteResponse;
import dev.y.works.iconescatolicosonline.service.cliente.EnderecoClienteService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/clientes/me/enderecos")
@SecurityRequirement(name = "bearerAuth")
public class EnderecoClienteController {
    private final EnderecoClienteService service;
    public EnderecoClienteController(EnderecoClienteService service) { this.service = service; }

    @GetMapping public List<EnderecoClienteResponse> listar(Authentication auth) { return service.listar(auth.getName()); }
    @PostMapping public ResponseEntity<EnderecoClienteResponse> criar(Authentication auth, @Valid @RequestBody EnderecoClienteRequest request) { var response = service.criar(auth.getName(), request); return ResponseEntity.created(URI.create("/api/clientes/me/enderecos/" + response.id())).body(response); }
    @PutMapping("/{id}") public EnderecoClienteResponse atualizar(Authentication auth, @PathVariable Long id, @Valid @RequestBody EnderecoClienteRequest request) { return service.atualizar(auth.getName(), id, request); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> excluir(Authentication auth, @PathVariable Long id) { service.excluir(auth.getName(), id); return ResponseEntity.noContent().build(); }
}
