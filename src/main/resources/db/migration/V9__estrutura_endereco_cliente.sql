ALTER TABLE cliente ADD COLUMN cep VARCHAR(8);
ALTER TABLE cliente ADD COLUMN logradouro VARCHAR(150);
ALTER TABLE cliente ADD COLUMN numero_endereco VARCHAR(20);
ALTER TABLE cliente ADD COLUMN complemento VARCHAR(100);
ALTER TABLE cliente ADD COLUMN bairro VARCHAR(100);
ALTER TABLE cliente ADD COLUMN cidade VARCHAR(100);
ALTER TABLE cliente ADD COLUMN uf VARCHAR(2);

-- O endereço antigo não pode ser dividido com segurança. Ele é preservado em
-- logradouro para que os clientes existentes possam completar os demais campos.
UPDATE cliente SET logradouro = endereco WHERE endereco IS NOT NULL;
ALTER TABLE cliente DROP COLUMN endereco;
