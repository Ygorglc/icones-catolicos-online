ALTER TABLE pagamento
    ADD COLUMN comprovante_arquivo VARCHAR(100),
    ADD COLUMN comprovante_nome_original VARCHAR(255),
    ADD COLUMN comprovante_tipo_conteudo VARCHAR(100);
