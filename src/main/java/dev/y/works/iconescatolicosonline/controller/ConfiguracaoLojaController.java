package dev.y.works.iconescatolicosonline.controller;

import dev.y.works.iconescatolicosonline.dto.configuracao.ConfiguracaoLojaRequest;
import dev.y.works.iconescatolicosonline.dto.configuracao.ConfiguracaoLojaResponse;
import dev.y.works.iconescatolicosonline.service.configuracao.ConfiguracaoLojaService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ConfiguracaoLojaController {
    private final ConfiguracaoLojaService service;

    public ConfiguracaoLojaController(ConfiguracaoLojaService service) {
        this.service = service;
    }

    @GetMapping("/api/publico/configuracao-loja")
    public ResponseEntity<ConfiguracaoLojaResponse> buscarPublica() {
        return ResponseEntity.ok(service.buscar());
    }

    @GetMapping("/api/admin/configuracao-loja")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ConfiguracaoLojaResponse> buscarAdministrativa() {
        return ResponseEntity.ok(service.buscar());
    }

    @PutMapping("/api/admin/configuracao-loja")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ConfiguracaoLojaResponse> atualizar(
            @Valid @RequestBody ConfiguracaoLojaRequest request) {
        return ResponseEntity.ok(service.atualizar(request));
    }
}
