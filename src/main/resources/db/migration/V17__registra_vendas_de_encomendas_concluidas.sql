INSERT INTO venda (
    encomenda_id,
    valor_total,
    data_venda,
    lucro_bruto,
    lucro_liquido_estimado
)
SELECT
    e.id,
    e.valor_total,
    COALESCE(
        (SELECT MAX(p.data_pagamento)
         FROM pagamento p
         WHERE p.encomenda_id = e.id
           AND p.status = 'CONFIRMADO'),
        e.data_criacao
    ),
    e.valor_total - COALESCE(ip.custo_producao, 0),
    e.valor_total
        - COALESCE(ip.custo_producao, 0)
        - COALESCE((SELECT SUM(g.valor) FROM gasto g WHERE g.encomenda_id = e.id), 0)
FROM encomenda e
LEFT JOIN icone_pronto ip ON ip.encomenda_id = e.id
WHERE e.status_encomenda = 'ENTREGUE_E_CONCLUIDO'
  AND e.status_financeiro = 'PAGO_INTEGRALMENTE'
  AND NOT EXISTS (SELECT 1 FROM venda v WHERE v.encomenda_id = e.id);
