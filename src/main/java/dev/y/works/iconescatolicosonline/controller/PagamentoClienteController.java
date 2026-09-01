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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import java.nio.charset.StandardCharsets;

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

    @PostMapping(value = "/{pagamentoId}/comprovante", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PagamentoResponse> anexarComprovante(
            @PathVariable Long encomendaId,
            @PathVariable Long pagamentoId,
            Authentication authentication,
            @RequestPart("arquivo") MultipartFile arquivo) {
        return ResponseEntity.ok(pagamentoService.anexarComprovante(
                encomendaId, pagamentoId, authentication.getName(), arquivo));
    }

    @GetMapping("/{pagamentoId}/comprovante")
    public ResponseEntity<Resource> baixarComprovante(
            @PathVariable Long encomendaId,
            @PathVariable Long pagamentoId,
            Authentication authentication) {
        var comprovante = pagamentoService.buscarComprovanteDoCliente(
                encomendaId, pagamentoId, authentication.getName());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(comprovante.tipoConteudo()))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(comprovante.nomeOriginal(), StandardCharsets.UTF_8).build().toString())
                .body(comprovante.recurso());
    }
}
