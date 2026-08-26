package dev.y.works.iconescatolicosonline.dto.carrinho;
import jakarta.validation.constraints.*; public record AtualizarQuantidadeCarrinhoRequest(@Min(1) @Max(99) int quantidade){}
