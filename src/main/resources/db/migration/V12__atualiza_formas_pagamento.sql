ALTER TABLE pagamento DROP CONSTRAINT IF EXISTS pagamento_forma_pagamento_check;
ALTER TABLE pagamento DROP CONSTRAINT IF EXISTS ck_pagamento_forma;

UPDATE pagamento
SET forma_pagamento = 'DEPOSITO'
WHERE forma_pagamento IN ('CARTAO_DEBITO', 'CARTAO_CREDITO');

ALTER TABLE pagamento ADD CONSTRAINT pagamento_forma_pagamento_check
    CHECK (forma_pagamento IN ('PIX', 'DINHEIRO', 'DEPOSITO'));
