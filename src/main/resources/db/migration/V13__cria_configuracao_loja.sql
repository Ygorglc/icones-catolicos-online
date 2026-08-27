CREATE TABLE configuracao_loja (
    id BIGINT PRIMARY KEY,
    entrega_habilitada BOOLEAN NOT NULL DEFAULT TRUE,
    chave_pix VARCHAR(200),
    dados_deposito VARCHAR(1000)
);

INSERT INTO configuracao_loja (id, entrega_habilitada)
VALUES (1, TRUE);
