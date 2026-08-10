ALTER TABLE pagamento
    ADD COLUMN forma_pagamento VARCHAR(30) NOT NULL DEFAULT 'PIX',
    ADD COLUMN origem VARCHAR(30) NOT NULL DEFAULT 'SIMULADO_SISTEMA',
    ADD COLUMN analisado_por_administrador_id BIGINT REFERENCES administrador(id),
    ADD COLUMN data_analise TIMESTAMPTZ,
    ADD COLUMN observacao_administrativa TEXT;

ALTER TABLE pagamento
    ADD CONSTRAINT ck_pagamento_forma
        CHECK (forma_pagamento IN ('PIX', 'DINHEIRO', 'CARTAO_DEBITO', 'CARTAO_CREDITO')),
    ADD CONSTRAINT ck_pagamento_origem
        CHECK (origem IN ('SIMULADO_SISTEMA', 'EXTERNO_MANUAL'));

CREATE INDEX idx_pagamento_status_criado_em
    ON pagamento (status, criado_em);
