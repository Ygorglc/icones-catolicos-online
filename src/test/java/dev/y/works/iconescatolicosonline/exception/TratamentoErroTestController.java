package dev.y.works.iconescatolicosonline.exception;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/teste/erros")
class TratamentoErroTestController {

    @GetMapping("/nao-encontrado")
    void recursoNaoEncontrado() {
        throw new RecursoNaoEncontradoException("Modelo de ícone não encontrado.");
    }

    @PostMapping("/validacao")
    void validar(@Valid @RequestBody DadosTesteRequest request) {
        // O teste verifica apenas a validação anterior à execução do método.
    }

    record DadosTesteRequest(@NotBlank String nome) {
    }
}
