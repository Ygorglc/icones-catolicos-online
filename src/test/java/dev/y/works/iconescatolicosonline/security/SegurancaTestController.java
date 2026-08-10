package dev.y.works.iconescatolicosonline.security;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
class SegurancaTestController {

    @GetMapping("/publico/teste")
    String publico() {
        return "publico";
    }

    @GetMapping("/admin/teste")
    String administrativo() {
        return "administrativo";
    }
}
