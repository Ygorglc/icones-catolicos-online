package dev.y.works.iconescatolicosonline.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/status")
public class SistemaStatusController {

    @GetMapping(produces = MediaType.TEXT_PLAIN_VALUE)
    public String consultarStatus() {
        return "Sistemas em atividade";
    }
}
