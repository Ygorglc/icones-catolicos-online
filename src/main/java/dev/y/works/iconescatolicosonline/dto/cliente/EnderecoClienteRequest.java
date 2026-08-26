package dev.y.works.iconescatolicosonline.dto.cliente;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record EnderecoClienteRequest(
        @NotBlank @Size(max = 60) String apelido,
        @NotBlank @Pattern(regexp = "^(?!([0-9])\\1{7}$)\\d{8}$", message = "deve conter 8 dígitos válidos") String cep,
        @NotBlank @Size(max = 150) String logradouro,
        @NotBlank @Size(max = 20) String numero,
        @Size(max = 100) String complemento,
        @NotBlank @Size(max = 100) String bairro,
        @NotBlank @Size(max = 100) String cidade,
        @NotBlank @Pattern(regexp = "(?i)^(AC|AL|AP|AM|BA|CE|DF|ES|GO|MA|MT|MS|MG|PA|PB|PR|PE|PI|RJ|RN|RS|RO|RR|SC|SP|SE|TO)$", message = "deve ser uma UF válida") String uf,
        boolean principal
) {
}
