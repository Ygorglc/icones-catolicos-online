package dev.y.works.iconescatolicosonline.dto.cliente;

public record PerfilClienteResponse(
        Long usuarioId,
        Long clienteId,
        String nome,
        String email,
        String telefone,
        String cpf,
        String cep,
        String logradouro,
        String numero,
        String complemento,
        String bairro,
        String cidade,
        String uf
) {
}
