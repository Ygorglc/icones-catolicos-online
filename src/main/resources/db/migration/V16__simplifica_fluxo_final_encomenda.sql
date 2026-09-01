ALTER TABLE encomenda DROP CONSTRAINT IF EXISTS encomenda_status_encomenda_check;

UPDATE encomenda
SET status_encomenda = 'EM_PRODUCAO'
WHERE status_encomenda = 'EM_ACABAMENTO';

UPDATE encomenda
SET status_encomenda = 'AGUARDANDO_PAGAMENTO_RESTANTE'
WHERE status_encomenda = 'PRONTO_PARA_ENTREGA_RETIRADA';

UPDATE encomenda
SET status_encomenda = 'ENTREGUE_E_CONCLUIDO'
WHERE status_encomenda = 'CONCLUIDO';

ALTER TABLE encomenda ADD CONSTRAINT encomenda_status_encomenda_check CHECK (
    status_encomenda IN (
        'ENCOMENDA_CRIADA', 'AGUARDANDO_PAGAMENTO_INICIAL', 'EM_PRODUCAO',
        'AGUARDANDO_PAGAMENTO_RESTANTE', 'ENVIADO_OU_RETIRADO',
        'ENTREGUE_E_CONCLUIDO', 'CANCELADO'
    )
);
