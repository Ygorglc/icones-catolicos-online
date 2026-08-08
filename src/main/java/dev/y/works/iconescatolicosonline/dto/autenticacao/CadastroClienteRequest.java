package dev.y.works.iconescatolicosonline.dto.autenticacao;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CadastroClienteRequest(
        @NotBlank @Size(max = 120) String nome,
        @NotBlank @Email @Size(max = 150) String email,
        @NotBlank @Size(min = 8, max = 72) String senha,
        @Size(max = 20) String telefone,
        @Pattern(regexp = "^\\d{11}$", message = "deve conter 11 dígitos") String cpf,
        @Size(max = 2_000) String endereco
) {
}
