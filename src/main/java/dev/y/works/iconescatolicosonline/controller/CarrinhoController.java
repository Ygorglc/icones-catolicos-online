package dev.y.works.iconescatolicosonline.controller;
import dev.y.works.iconescatolicosonline.dto.carrinho.*; import dev.y.works.iconescatolicosonline.service.carrinho.CarrinhoService; import io.swagger.v3.oas.annotations.security.SecurityRequirement; import jakarta.validation.Valid; import org.springframework.http.*; import org.springframework.security.core.Authentication; import org.springframework.web.bind.annotation.*; import java.net.URI; import java.util.List;
@RestController @RequestMapping("/api/carrinho") @SecurityRequirement(name="bearerAuth") public class CarrinhoController {
 private final CarrinhoService service;public CarrinhoController(CarrinhoService service){this.service=service;}
 @GetMapping public List<ItemCarrinhoResponse> listar(Authentication a){return service.listar(a.getName());}
 @PostMapping("/itens") public ResponseEntity<ItemCarrinhoResponse> adicionar(Authentication a,@Valid @RequestBody AdicionarItemCarrinhoRequest r){var item=service.adicionar(a.getName(),r);return ResponseEntity.created(URI.create("/api/carrinho/itens/"+item.id())).body(item);}
 @PatchMapping("/itens/{id}") public ItemCarrinhoResponse quantidade(Authentication a,@PathVariable Long id,@Valid @RequestBody AtualizarQuantidadeCarrinhoRequest r){return service.quantidade(a.getName(),id,r.quantidade());}
 @DeleteMapping("/itens/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void remover(Authentication a,@PathVariable Long id){service.remover(a.getName(),id);}
 @DeleteMapping @ResponseStatus(HttpStatus.NO_CONTENT) public void limpar(Authentication a){service.limpar(a.getName());}
}
