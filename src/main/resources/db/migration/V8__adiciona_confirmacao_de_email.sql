ALTER TABLE usuario ADD COLUMN email_verificado BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE usuario ADD COLUMN token_confirmacao_email_hash VARCHAR(64);
ALTER TABLE usuario ADD COLUMN token_confirmacao_email_expira_em TIMESTAMPTZ;
CREATE UNIQUE INDEX idx_usuario_token_confirmacao_email
    ON usuario(token_confirmacao_email_hash)
    WHERE token_confirmacao_email_hash IS NOT NULL;
