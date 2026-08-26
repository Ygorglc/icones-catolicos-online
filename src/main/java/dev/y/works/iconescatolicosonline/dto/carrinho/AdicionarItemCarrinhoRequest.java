package dev.y.works.iconescatolicosonline.dto.carrinho;
import dev.y.works.iconescatolicosonline.dto.encomenda.PersonalizacaoRequest; import jakarta.validation.Valid; import jakarta.validation.constraints.*;
public record AdicionarItemCarrinhoRequest(@NotNull Long modeloIconeId,@Min(1) @Max(99) int quantidade,@NotNull @Valid PersonalizacaoRequest personalizacao){}
