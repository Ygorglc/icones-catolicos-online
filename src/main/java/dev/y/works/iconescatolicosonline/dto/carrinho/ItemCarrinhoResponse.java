package dev.y.works.iconescatolicosonline.dto.carrinho;
import dev.y.works.iconescatolicosonline.dto.encomenda.PersonalizacaoResponse; import java.math.BigDecimal;
public record ItemCarrinhoResponse(Long id,Long modeloIconeId,String nome,String imagemUrl,BigDecimal precoUnitario,int quantidade,PersonalizacaoResponse personalizacao){}
