package dev.y.works.iconescatolicosonline.dto.cliente;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AtualizarPerfilClienteRequest(
        @NotBlank @Size(max = 120) String nome,
        @Size(max = 20) String telefone,
        @Pattern(regexp = "^\\d{11}$", message = "deve conter 11 dígitos") String cpf,
        @Size(max = 2_000) String endereco
) {
}
