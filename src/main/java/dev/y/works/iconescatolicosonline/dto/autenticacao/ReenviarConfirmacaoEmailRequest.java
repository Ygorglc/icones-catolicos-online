package dev.y.works.iconescatolicosonline.dto.autenticacao;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ReenviarConfirmacaoEmailRequest(@NotBlank @Email String email) {
}
