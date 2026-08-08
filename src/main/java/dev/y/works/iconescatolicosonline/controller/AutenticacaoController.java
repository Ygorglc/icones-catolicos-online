package dev.y.works.iconescatolicosonline.controller;

import dev.y.works.iconescatolicosonline.dto.autenticacao.AutenticacaoResponse;
import dev.y.works.iconescatolicosonline.dto.autenticacao.CadastroClienteRequest;
import dev.y.works.iconescatolicosonline.dto.autenticacao.LoginRequest;
import dev.y.works.iconescatolicosonline.service.autenticacao.AutenticacaoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AutenticacaoController {

    private final AutenticacaoService autenticacaoService;

    public AutenticacaoController(AutenticacaoService autenticacaoService) {
        this.autenticacaoService = autenticacaoService;
    }

    @PostMapping("/cadastro")
    public ResponseEntity<AutenticacaoResponse> cadastrarCliente(
            @Valid @RequestBody CadastroClienteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(autenticacaoService.cadastrarCliente(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AutenticacaoResponse> login(
            @Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(autenticacaoService.login(request));
    }
}
