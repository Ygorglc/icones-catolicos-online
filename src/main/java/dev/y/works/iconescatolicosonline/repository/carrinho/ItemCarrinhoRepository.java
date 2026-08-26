package dev.y.works.iconescatolicosonline.repository.carrinho;
import dev.y.works.iconescatolicosonline.domain.carrinho.ItemCarrinho; import org.springframework.data.jpa.repository.JpaRepository; import java.util.List; import java.util.Optional;
public interface ItemCarrinhoRepository extends JpaRepository<ItemCarrinho,Long>{
 List<ItemCarrinho> findByUsuario_EmailIgnoreCaseOrderByCriadoEmAsc(String email);
 Optional<ItemCarrinho> findByIdAndUsuario_EmailIgnoreCase(Long id,String email);
 long deleteByUsuario_EmailIgnoreCase(String email);
}
