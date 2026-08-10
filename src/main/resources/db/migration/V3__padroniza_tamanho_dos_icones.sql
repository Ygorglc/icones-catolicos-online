UPDATE personalizacao
SET tamanho = CASE
    WHEN UPPER(TRIM(tamanho)) IN ('PEQUENO', 'MEDIO', 'GRANDE', 'PERSONALIZADO')
        THEN UPPER(TRIM(tamanho))
    ELSE 'PERSONALIZADO'
END
WHERE tamanho IS NOT NULL;

UPDATE icone_pronto
SET tamanho = CASE
    WHEN UPPER(TRIM(tamanho)) IN ('PEQUENO', 'MEDIO', 'GRANDE', 'PERSONALIZADO')
        THEN UPPER(TRIM(tamanho))
    ELSE 'PERSONALIZADO'
END;

ALTER TABLE personalizacao
    ALTER COLUMN tamanho TYPE VARCHAR(20),
    ADD CONSTRAINT ck_personalizacao_tamanho
        CHECK (tamanho IN ('PEQUENO', 'MEDIO', 'GRANDE', 'PERSONALIZADO'));

ALTER TABLE icone_pronto
    ALTER COLUMN tamanho TYPE VARCHAR(20),
    ADD CONSTRAINT ck_icone_pronto_tamanho
        CHECK (tamanho IN ('PEQUENO', 'MEDIO', 'GRANDE', 'PERSONALIZADO'));
