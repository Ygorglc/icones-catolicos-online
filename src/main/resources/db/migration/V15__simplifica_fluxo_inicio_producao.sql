ALTER TABLE encomenda DROP CONSTRAINT IF EXISTS encomenda_status_encomenda_check;

UPDATE encomenda
SET status_encomenda = 'EM_PRODUCAO'
WHERE status_encomenda IN ('PAGAMENTO_INICIAL_CONFIRMADO', 'PRODUCAO_LIBERADA');

ALTER TABLE encomenda ADD CONSTRAINT encomenda_status_encomenda_check CHECK (
    status_encomenda IN (
        'ENCOMENDA_CRIADA', 'AGUARDANDO_PAGAMENTO_INICIAL',
        'EM_PRODUCAO', 'EM_ACABAMENTO', 'PRONTO_PARA_ENTREGA_RETIRADA',
        'AGUARDANDO_PAGAMENTO_RESTANTE', 'ENVIADO_OU_RETIRADO',
        'CONCLUIDO', 'CANCELADO'
    )
);
