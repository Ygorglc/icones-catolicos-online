package dev.y.works.iconescatolicosonline.dto.autenticacao;

import jakarta.validation.constraints.NotBlank;

public record ConfirmarEmailRequest(@NotBlank String token) {
}
