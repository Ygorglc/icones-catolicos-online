package dev.y.works.iconescatolicosonline.controller;

import dev.y.works.iconescatolicosonline.dto.pagamento.AnalisePagamentoRequest;
import dev.y.works.iconescatolicosonline.dto.pagamento.HistoricoPagamentosResponse;
import dev.y.works.iconescatolicosonline.dto.pagamento.PagamentoResponse;
import dev.y.works.iconescatolicosonline.service.pagamento.PagamentoService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import dev.y.works.iconescatolicosonline.dto.pagamento.RegistrarPagamentoRequest;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@SecurityRequirement(name = "bearerAuth")
public class PagamentoAdminController {

    private final PagamentoService pagamentoService;

    public PagamentoAdminController(PagamentoService pagamentoService) {
        this.pagamentoService = pagamentoService;
    }

    @GetMapping("/encomendas/{encomendaId}/pagamentos")
    public ResponseEntity<HistoricoPagamentosResponse> listar(
            @PathVariable Long encomendaId) {
        return ResponseEntity.ok(
                pagamentoService.listarParaAdministrador(encomendaId));
    }

    @GetMapping("/pagamentos/pendentes")
    public ResponseEntity<List<PagamentoResponse>> listarPendentes() {
        return ResponseEntity.ok(pagamentoService.listarPendentes());
    }

    @PatchMapping("/pagamentos/{pagamentoId}/analise")
    public ResponseEntity<PagamentoResponse> analisar(
            @PathVariable Long pagamentoId,
            Authentication authentication,
            @Valid @RequestBody AnalisePagamentoRequest request) {
        return ResponseEntity.ok(pagamentoService.analisar(
                pagamentoId, authentication.getName(), request));
    }

    @PostMapping("/encomendas/{encomendaId}/pagamentos/confirmacao-externa")
    public ResponseEntity<PagamentoResponse> confirmarRecebimentoExterno(
            @PathVariable Long encomendaId,
            Authentication authentication,
            @Valid @RequestBody RegistrarPagamentoRequest request) {
        return ResponseEntity.ok(pagamentoService.confirmarRecebimentoExterno(
                encomendaId, authentication.getName(), request));
    }
}
