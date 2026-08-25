ALTER TABLE usuario
    ADD COLUMN token_recuperacao_senha_hash VARCHAR(64),
    ADD COLUMN token_recuperacao_senha_expira_em TIMESTAMPTZ;

CREATE UNIQUE INDEX uk_usuario_token_recuperacao_senha_hash
    ON usuario (token_recuperacao_senha_hash)
    WHERE token_recuperacao_senha_hash IS NOT NULL;
