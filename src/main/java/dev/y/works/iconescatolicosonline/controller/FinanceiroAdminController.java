package dev.y.works.iconescatolicosonline.controller;

import dev.y.works.iconescatolicosonline.dto.financeiro.GastoRequest;
import dev.y.works.iconescatolicosonline.dto.financeiro.GastoResponse;
import dev.y.works.iconescatolicosonline.dto.financeiro.RegistrarVendaRequest;
import dev.y.works.iconescatolicosonline.dto.financeiro.RelatorioFinanceiroResponse;
import dev.y.works.iconescatolicosonline.dto.financeiro.VendaResponse;
import dev.y.works.iconescatolicosonline.service.financeiro.FinanceiroService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin/financeiro")
@SecurityRequirement(name = "bearerAuth")
public class FinanceiroAdminController {

    private final FinanceiroService financeiroService;

    public FinanceiroAdminController(FinanceiroService financeiroService) {
        this.financeiroService = financeiroService;
    }

    @GetMapping("/gastos")
    public ResponseEntity<List<GastoResponse>> listarGastos() {
        return ResponseEntity.ok(financeiroService.listarGastos());
    }

    @GetMapping("/gastos/{id}")
    public ResponseEntity<GastoResponse> buscarGasto(@PathVariable Long id) {
        return ResponseEntity.ok(financeiroService.buscarGasto(id));
    }

    @PostMapping("/gastos")
    public ResponseEntity<GastoResponse> criarGasto(@Valid @RequestBody GastoRequest request) {
        GastoResponse criado = financeiroService.criarGasto(request);
        return ResponseEntity.created(
                URI.create("/api/admin/financeiro/gastos/" + criado.id())).body(criado);
    }

    @PutMapping("/gastos/{id}")
    public ResponseEntity<GastoResponse> atualizarGasto(
            @PathVariable Long id, @Valid @RequestBody GastoRequest request) {
        return ResponseEntity.ok(financeiroService.atualizarGasto(id, request));
    }

    @DeleteMapping("/gastos/{id}")
    public ResponseEntity<Void> excluirGasto(@PathVariable Long id) {
        financeiroService.excluirGasto(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/vendas")
    public ResponseEntity<List<VendaResponse>> listarVendas() {
        return ResponseEntity.ok(financeiroService.listarVendas());
    }

    @GetMapping("/vendas/{id}")
    public ResponseEntity<VendaResponse> buscarVenda(@PathVariable Long id) {
        return ResponseEntity.ok(financeiroService.buscarVenda(id));
    }

    @PostMapping("/vendas")
    public ResponseEntity<VendaResponse> registrarVenda(
            @Valid @RequestBody RegistrarVendaRequest request) {
        VendaResponse criada = financeiroService.registrarVenda(request.encomendaId());
        return ResponseEntity.created(
                URI.create("/api/admin/financeiro/vendas/" + criada.id())).body(criada);
    }

    @GetMapping("/relatorios")
    public ResponseEntity<RelatorioFinanceiroResponse> gerarRelatorio(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return ResponseEntity.ok(financeiroService.gerarRelatorio(inicio, fim));
    }
}
