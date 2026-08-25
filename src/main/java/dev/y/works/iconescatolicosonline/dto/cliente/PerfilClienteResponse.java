package dev.y.works.iconescatolicosonline.dto.cliente;

public record PerfilClienteResponse(
        Long usuarioId,
        Long clienteId,
        String nome,
        String email,
        String telefone,
        String cpf,
        String endereco
) {
}
