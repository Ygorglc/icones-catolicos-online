# Evidências dos testes integrados

O teste `FluxoCompletoPostgreSqlIntegrationTests` executa a API completa sobre um PostgreSQL
descartável iniciado pelo Testcontainers. O Flyway aplica as mesmas migrations
usadas no ambiente de desenvolvimento.

## Cenários verificados

1. Cadastro e autenticação do cliente com JWT.
2. Rejeição de acesso administrativo sem token e com perfil de cliente.
3. Criação e persistência da encomenda com personalização e cálculo do sinal.
4. Rejeição da conclusão antecipada da encomenda.
5. Pagamento simulado do sinal e liberação da produção.
6. Cadastro e reserva de uma peça pronta compatível após a liberação.
7. Avanço controlado por todos os estados da produção.
8. Pagamento do restante e quitação do saldo.
9. Conclusão da encomenda e geração do certificado artesanal.
10. Consulta pública do certificado pelo código de autenticidade.
11. Conferência direta dos registros persistidos no PostgreSQL.

## Como executar e registrar

Com o Docker Desktop em execução:

```powershell
.\gradlew.bat test --tests "*FluxoCompletoPostgreSqlIntegrationTests"
```

O relatório HTML é gerado em:

```text
build/reports/tests/test/index.html
```

Para o capítulo de resultados, registrar uma captura do relatório com o teste
aprovado e, se desejado, capturas das respostas equivalentes no Swagger.
