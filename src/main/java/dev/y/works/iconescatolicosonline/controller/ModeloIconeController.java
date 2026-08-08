package dev.y.works.iconescatolicosonline.controller;

import dev.y.works.iconescatolicosonline.dto.catalogo.ModeloIconeDetalheResponse;
import dev.y.works.iconescatolicosonline.dto.catalogo.ModeloIconeResumoResponse;
import dev.y.works.iconescatolicosonline.service.catalogo.ModeloIconeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/publico/modelos")
public class ModeloIconeController {

    private final ModeloIconeService modeloIconeService;

    public ModeloIconeController(ModeloIconeService modeloIconeService) {
        this.modeloIconeService = modeloIconeService;
    }

    @GetMapping
    public ResponseEntity<List<ModeloIconeResumoResponse>> listarModelosAtivos() {
        return ResponseEntity.ok(modeloIconeService.listarModelosAtivos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ModeloIconeDetalheResponse> buscarModeloAtivo(@PathVariable Long id) {
        return ResponseEntity.ok(modeloIconeService.buscarModeloAtivo(id));
    }
}
