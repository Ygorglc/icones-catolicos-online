package dev.y.works.iconescatolicosonline.dto.cliente;

public record EnderecoClienteResponse(
        Long id, String apelido, String cep, String logradouro, String numero,
        String complemento, String bairro, String cidade, String uf, boolean principal
) {
}
