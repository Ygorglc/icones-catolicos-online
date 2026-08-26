ALTER TABLE item_carrinho ADD COLUMN usuario_id BIGINT;

UPDATE item_carrinho item
SET usuario_id = cliente.usuario_id
FROM cliente
WHERE item.cliente_id = cliente.id;

ALTER TABLE item_carrinho ALTER COLUMN usuario_id SET NOT NULL;
ALTER TABLE item_carrinho
    ADD CONSTRAINT fk_item_carrinho_usuario
    FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE;

DROP INDEX IF EXISTS idx_item_carrinho_cliente;
ALTER TABLE item_carrinho DROP COLUMN cliente_id;
CREATE INDEX idx_item_carrinho_usuario ON item_carrinho(usuario_id);
