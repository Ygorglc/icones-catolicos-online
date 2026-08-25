package dev.y.works.iconescatolicosonline.controller;

import dev.y.works.iconescatolicosonline.dto.cliente.AtualizarPerfilClienteRequest;
import dev.y.works.iconescatolicosonline.dto.cliente.AlterarSenhaRequest;
import dev.y.works.iconescatolicosonline.dto.cliente.PerfilClienteResponse;
import dev.y.works.iconescatolicosonline.service.cliente.PerfilClienteService;
import dev.y.works.iconescatolicosonline.service.autenticacao.SenhaService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/clientes/me")
@SecurityRequirement(name = "bearerAuth")
public class PerfilClienteController {

    private final PerfilClienteService perfilClienteService;
    private final SenhaService senhaService;

    public PerfilClienteController(PerfilClienteService perfilClienteService, SenhaService senhaService) {
        this.perfilClienteService = perfilClienteService;
        this.senhaService = senhaService;
    }

    @GetMapping
    public ResponseEntity<PerfilClienteResponse> buscar(Authentication authentication) {
        return ResponseEntity.ok(perfilClienteService.buscar(authentication.getName()));
    }

    @PutMapping
    public ResponseEntity<PerfilClienteResponse> atualizar(
            Authentication authentication,
            @Valid @RequestBody AtualizarPerfilClienteRequest request) {
        return ResponseEntity.ok(
                perfilClienteService.atualizar(authentication.getName(), request));
    }

    @PutMapping("/senha")
    public ResponseEntity<Void> alterarSenha(
            Authentication authentication,
            @Valid @RequestBody AlterarSenhaRequest request) {
        senhaService.alterar(authentication.getName(), request);
        return ResponseEntity.noContent().build();
    }
}
