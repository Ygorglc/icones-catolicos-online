ALTER TABLE certificado_artesanal
    ADD COLUMN nome_artesao VARCHAR(120),
    ADD COLUMN modelo_icone VARCHAR(120),
    ADD COLUMN tamanho_icone VARCHAR(20),
    ADD COLUMN acabamento VARCHAR(80);

UPDATE certificado_artesanal certificado
SET nome_artesao = 'Artesão responsável',
    modelo_icone = COALESCE(modelo.nome, 'Modelo não informado'),
    tamanho_icone = COALESCE(icone.tamanho, 'PERSONALIZADO'),
    acabamento = COALESCE(icone.acabamento, 'Não informado'),
    material_utilizado = COALESCE(certificado.material_utilizado, 'Não informado')
FROM encomenda encomenda
LEFT JOIN icone_pronto icone ON icone.encomenda_id = encomenda.id
LEFT JOIN modelo_icone modelo ON modelo.id = icone.modelo_icone_id
WHERE certificado.encomenda_id = encomenda.id;

ALTER TABLE certificado_artesanal
    ALTER COLUMN material_utilizado SET NOT NULL,
    ALTER COLUMN nome_artesao SET NOT NULL,
    ALTER COLUMN modelo_icone SET NOT NULL,
    ALTER COLUMN tamanho_icone SET NOT NULL,
    ALTER COLUMN acabamento SET NOT NULL;
