package dev.y.works.iconescatolicosonline.controller;

import dev.y.works.iconescatolicosonline.dto.certificado.CertificadoArtesanalResponse;
import dev.y.works.iconescatolicosonline.service.certificado.CertificadoArtesanalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/publico/certificados")
public class CertificadoPublicoController {

    private final CertificadoArtesanalService certificadoService;

    public CertificadoPublicoController(CertificadoArtesanalService certificadoService) {
        this.certificadoService = certificadoService;
    }

    @GetMapping("/{codigoPublico}")
    public ResponseEntity<CertificadoArtesanalResponse> consultar(
            @PathVariable String codigoPublico) {
        return ResponseEntity.ok(certificadoService.consultarPublicamente(codigoPublico));
    }
}
