package dev.y.works.iconescatolicosonline.controller;

import dev.y.works.iconescatolicosonline.dto.certificado.CertificadoArtesanalResponse;
import dev.y.works.iconescatolicosonline.dto.certificado.GerarCertificadoRequest;
import dev.y.works.iconescatolicosonline.service.certificado.CertificadoArtesanalService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/admin/certificados")
@SecurityRequirement(name = "bearerAuth")
public class CertificadoAdminController {

    private final CertificadoArtesanalService certificadoService;

    public CertificadoAdminController(CertificadoArtesanalService certificadoService) {
        this.certificadoService = certificadoService;
    }

    @PostMapping("/encomendas/{encomendaId}")
    public ResponseEntity<CertificadoArtesanalResponse> gerar(
            @PathVariable Long encomendaId,
            @Valid @RequestBody GerarCertificadoRequest request) {
        CertificadoArtesanalResponse criado = certificadoService.gerar(encomendaId, request);
        return ResponseEntity.created(
                URI.create("/api/publico/certificados/" + criado.codigoPublico())).body(criado);
    }

    @GetMapping
    public ResponseEntity<List<CertificadoArtesanalResponse>> listar() {
        return ResponseEntity.ok(certificadoService.listar());
    }

    @GetMapping("/encomendas/{encomendaId}")
    public ResponseEntity<CertificadoArtesanalResponse> buscarPorEncomenda(
            @PathVariable Long encomendaId) {
        return ResponseEntity.ok(certificadoService.buscarPorEncomenda(encomendaId));
    }
}
