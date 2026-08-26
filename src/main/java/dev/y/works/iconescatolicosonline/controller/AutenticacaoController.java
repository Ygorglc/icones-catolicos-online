package dev.y.works.iconescatolicosonline.controller;

import dev.y.works.iconescatolicosonline.dto.autenticacao.AutenticacaoResponse;
import dev.y.works.iconescatolicosonline.dto.autenticacao.CadastroClienteRequest;
import dev.y.works.iconescatolicosonline.dto.autenticacao.CadastroClienteResponse;
import dev.y.works.iconescatolicosonline.dto.autenticacao.ConfirmarEmailRequest;
import dev.y.works.iconescatolicosonline.dto.autenticacao.MensagemResponse;
import dev.y.works.iconescatolicosonline.dto.autenticacao.ReenviarConfirmacaoEmailRequest;
import dev.y.works.iconescatolicosonline.dto.autenticacao.LoginRequest;
import dev.y.works.iconescatolicosonline.dto.autenticacao.RedefinirSenhaRequest;
import dev.y.works.iconescatolicosonline.dto.autenticacao.SolicitarRecuperacaoSenhaRequest;
import dev.y.works.iconescatolicosonline.dto.autenticacao.SolicitarRecuperacaoSenhaResponse;
import dev.y.works.iconescatolicosonline.service.autenticacao.AutenticacaoService;
import dev.y.works.iconescatolicosonline.service.autenticacao.ConfirmacaoEmailService;
import dev.y.works.iconescatolicosonline.service.autenticacao.SenhaService;
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
    private final SenhaService senhaService;
    private final ConfirmacaoEmailService confirmacaoEmailService;

    public AutenticacaoController(AutenticacaoService autenticacaoService, SenhaService senhaService,
            ConfirmacaoEmailService confirmacaoEmailService) {
        this.autenticacaoService = autenticacaoService;
        this.senhaService = senhaService;
        this.confirmacaoEmailService = confirmacaoEmailService;
    }

    @PostMapping("/cadastro")
    public ResponseEntity<CadastroClienteResponse> cadastrarCliente(
            @Valid @RequestBody CadastroClienteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(autenticacaoService.cadastrarCliente(request));
    }

    @PostMapping("/email/confirmacao")
    public ResponseEntity<MensagemResponse> confirmarEmail(@Valid @RequestBody ConfirmarEmailRequest request) {
        return ResponseEntity.ok(confirmacaoEmailService.confirmar(request.token()));
    }

    @PostMapping("/email/confirmacao/reenviar")
    public ResponseEntity<MensagemResponse> reenviarConfirmacao(
            @Valid @RequestBody ReenviarConfirmacaoEmailRequest request) {
        return ResponseEntity.ok(confirmacaoEmailService.reenviar(request.email()));
    }

    @PostMapping("/login")
    public ResponseEntity<AutenticacaoResponse> login(
            @Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(autenticacaoService.login(request));
    }

    @PostMapping("/senha/recuperacao")
    public ResponseEntity<SolicitarRecuperacaoSenhaResponse> solicitarRecuperacao(
            @Valid @RequestBody SolicitarRecuperacaoSenhaRequest request) {
        return ResponseEntity.ok(senhaService.solicitar(request.email()));
    }

    @PostMapping("/senha/redefinicao")
    public ResponseEntity<Void> redefinirSenha(
            @Valid @RequestBody RedefinirSenhaRequest request) {
        senhaService.redefinir(request);
        return ResponseEntity.noContent().build();
    }
}
