# Configuração do Gmail SMTP

O sistema utiliza `oficina.sao.jose.rj.oficial@gmail.com` para enviar mensagens de recuperação de senha.

## Preparação da conta Google

1. Ative a verificação em duas etapas na conta Google.
2. Acesse as configurações de segurança da conta.
3. Abra **Senhas de app**.
4. Crie uma senha de aplicativo para o sistema.
5. Copie a senha gerada. Ela é diferente da senha normal da conta.

Se a opção **Senhas de app** não aparecer, confirme se a verificação em duas etapas está ativa e se a conta não possui uma política que bloqueia esse recurso.

## Variáveis de ambiente

Configure na execução do Spring Boot no IntelliJ:

```text
EMAIL_ENABLED=true
GMAIL_SMTP_USERNAME=oficina.sao.jose.rj.oficial@gmail.com
GMAIL_APP_PASSWORD=senha_de_aplicativo_gerada_pelo_google
EMAIL_REMETENTE=oficina.sao.jose.rj.oficial@gmail.com
FRONTEND_URL=http://localhost:4200
```

O projeto também carrega automaticamente o arquivo `.env` localizado na raiz do backend. Copie `.env.example` para `.env`, preencha a senha de aplicativo e reinicie a aplicação. Como `.env` está no `.gitignore`, suas credenciais não serão versionadas.

## Segurança

- Não utilize a senha normal da conta Google.
- Não grave a senha de aplicativo no `application.properties`.
- Não envie a credencial ao GitHub.
- Se a senha de aplicativo for exposta, revogue-a imediatamente na conta Google.
