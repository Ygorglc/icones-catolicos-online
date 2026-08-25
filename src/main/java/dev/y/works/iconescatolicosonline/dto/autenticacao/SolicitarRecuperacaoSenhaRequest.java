package dev.y.works.iconescatolicosonline.dto.autenticacao;
import jakarta.validation.constraints.Email; import jakarta.validation.constraints.NotBlank;
public record SolicitarRecuperacaoSenhaRequest(@NotBlank @Email String email) {}
