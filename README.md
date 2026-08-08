# Ícones Católicos Online — Backend

Backend do sistema de gestão de encomendas de ícones católicos artesanais,
desenvolvido com Java 21, Spring Boot, Gradle e PostgreSQL.

## PostgreSQL com Docker

Pré-requisitos:

- Docker Desktop em execução;
- WSL 2 configurado;
- porta `5432` disponível.

Para iniciar o banco:

```powershell
docker compose up -d
```

Para verificar o estado:

```powershell
docker compose ps
docker compose logs postgres
```

Configuração local padrão:

- host: `localhost`;
- porta: `5432`;
- banco: `icones_catolicos`;
- usuário: `icones_app`;
- senha: `icones_app`.

Esses valores são apenas para desenvolvimento local e podem ser substituídos
por variáveis de ambiente. Copie `.env.example` para `.env` caso queira
personalizá-los.

Para parar o contêiner sem apagar os dados:

```powershell
docker compose stop
```

Para parar e remover o contêiner, preservando o volume:

```powershell
docker compose down
```

## Executar o backend

```powershell
.\gradlew.bat bootRun
```

O suporte do Spring Boot ao Docker Compose inicia o serviço automaticamente
durante o desenvolvimento quando o Docker Desktop está disponível.

## Perfis de configuração

O perfil `dev` é usado por padrão. Ele conecta ao PostgreSQL local e permite ao
Spring Boot controlar o `compose.yaml`.

O perfil `prod` não inicia Docker e exige as variáveis `DB_URL`, `DB_USERNAME`
e `DB_PASSWORD`. Para ativá-lo:

```powershell
$env:SPRING_PROFILES_ACTIVE="prod"
$env:DB_URL="jdbc:postgresql://servidor:5432/icones_catolicos"
$env:DB_USERNAME="usuario"
$env:DB_PASSWORD="senha"
.\gradlew.bat bootRun
```

Não versione senhas reais ou arquivos `.env`.
