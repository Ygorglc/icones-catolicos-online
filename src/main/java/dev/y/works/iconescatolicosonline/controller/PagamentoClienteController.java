package dev.y.works.iconescatolicosonline.controller;

import dev.y.works.iconescatolicosonline.dto.pagamento.HistoricoPagamentosResponse;
import dev.y.works.iconescatolicosonline.dto.pagamento.PagamentoResponse;
import dev.y.works.iconescatolicosonline.dto.pagamento.RegistrarPagamentoRequest;
import dev.y.works.iconescatolicosonline.service.pagamento.PagamentoService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/encomendas/{encomendaId}/pagamentos")
@SecurityRequirement(name = "bearerAuth")
public class PagamentoClienteController {

    private final PagamentoService pagamentoService;

    public PagamentoClienteController(PagamentoService pagamentoService) {
        this.pagamentoService = pagamentoService;
    }

    @PostMapping
    public ResponseEntity<PagamentoResponse> registrar(
            @PathVariable Long encomendaId,
            Authentication authentication,
            @Valid @RequestBody RegistrarPagamentoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                pagamentoService.registrar(encomendaId, authentication.getName(), request));
    }

    @GetMapping
    public ResponseEntity<HistoricoPagamentosResponse> listar(
            @PathVariable Long encomendaId,
            Authentication authentication) {
        return ResponseEntity.ok(
                pagamentoService.listarDoCliente(encomendaId, authentication.getName()));
    }
}
