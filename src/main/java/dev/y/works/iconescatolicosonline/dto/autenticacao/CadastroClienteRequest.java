package dev.y.works.iconescatolicosonline.dto.autenticacao;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CadastroClienteRequest(
        @NotBlank @Size(max = 120) String nome,
        @NotBlank @Email @Size(max = 150) String email,
        @NotBlank @Size(min = 8, max = 72) String senha,
        @NotBlank @Pattern(regexp = "^[1-9]{2}9?\\d{8}$", message = "deve conter DDD e 10 ou 11 dígitos") String telefone,
        @NotBlank @Pattern(regexp = "^\\d{11}$", message = "deve conter 11 dígitos") String cpf
) {
}
