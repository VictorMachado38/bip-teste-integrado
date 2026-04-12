# Como rodar o projeto localmente

BIP — Benefícios Integrados de Pagamento.  
Sistema desenvolvido como desafio técnico para o **Sicoob**.

---

## Visão geral da arquitetura

```
┌────────────────────────────────────────────────────┐
│                  Docker Compose                    │
│                                                    │
│  ┌──────────┐   ┌──────────┐   ┌────────────────┐  │
│  │ Postgres │   │ Keycloak │   │    Backend     │  │
│  │  :5432   │   │  :8180   │   │  Spring Boot   │  │
│  └──────────┘   └──────────┘   │    :8080       │  │
│        ▲               ▲       └────────────────┘  │
│        │               │               ▲           │
│        └───────────────┴───────────────┤           │
│                                        │           │
│                               ┌────────────────┐   │
│                               │    Frontend    │   │
│                               │  Angular+Nginx │   │
│                               │    :4200       │   │
│                               └────────────────┘   │
└────────────────────────────────────────────────────┘
```

| Serviço | Tecnologia | Porta | Descrição |
|---------|-----------|-------|-----------|
| **postgres** | PostgreSQL 16 | 5432 | Banco de dados relacional |
| **keycloak** | Keycloak 24 | 8180 | Identity Provider (autenticação OAuth2/OIDC) |
| **backend** | Spring Boot 3.2 + Java 17 | 8080 | API REST |
| **frontend** | Angular 21 + Nginx | 4200 | Interface web |

---

## Pré-requisitos

| Ferramenta | Versão mínima | Verificar |
|-----------|--------------|-----------|
| Docker Desktop | 4.x | `docker --version` |
| Docker Compose | v2 | `docker compose version` |
| Git | qualquer | `git --version` |

> Não é necessário ter Java, Node ou Maven instalados — tudo roda dentro dos containers.

---

## Subindo o ambiente

### 1. Clone o repositório

```bash
git clone <url-do-repositório>
cd bip-teste-integrado
```

### 2. Suba todos os serviços

```bash
cd docker/local
docker compose up -d
```

O Docker vai:
1. Baixar as imagens do Docker Hub (`victormachado38/bip-backend:latest` e `bip-frontend:latest`)
2. Subir o PostgreSQL e aguardar ele ficar saudável
3. Subir o Keycloak, importar o realm `bip-realm` automaticamente e aguardar ele ficar pronto
4. Subir o Backend (aguarda Postgres + Keycloak)
5. Subir o Frontend

> O primeiro `up` pode demorar ~2 minutos enquanto o Keycloak inicializa.

### 3. Acompanhe os logs (opcional)

```bash
# Todos os serviços
docker compose logs -f

# Só o Keycloak (costuma ser o mais lento)
docker compose logs -f keycloak
```

Aguarde a linha:
```
Keycloak 24.0.5 ... started in XX.XXXs. Listening on: http://0.0.0.0:8080
```

---

## Acessando a aplicação

| URL | O que é |
|-----|---------|
| `http://localhost:4200` | Frontend (aplicação principal) |
| `http://localhost:8080/swagger-ui.html` | Documentação interativa da API |
| `http://localhost:8180` | Painel de administração do Keycloak |

### Usuários de teste

| Usuário | Senha | Perfil |
|---------|-------|--------|
| `admin` | `admin123` | Administrador — acesso total |
| `usuario` | `user123` | Usuário padrão |

### Acesso ao painel Keycloak

```
URL:   http://localhost:8180
Login: admin
Senha: admin123
```

---

## Stack técnica detalhada

### Backend — Spring Boot

```
back/
├── Dockerfile
└── bip-teste-integrado/
    ├── pom.xml
    └── src/main/java/br/com/bip/
        ├── controller/     ← BeneficioController (API REST)
        ├── service/        ← regras de negócio
        ├── repository/     ← Spring Data JPA
        ├── model/          ← entidade Beneficio
        └── config/         ← SecurityConfig, WebConfig (CORS)
```

**Dependências principais:**

| Biblioteca | Finalidade |
|-----------|-----------|
| Spring Boot Web | API REST |
| Spring Data JPA + PostgreSQL | persistência |
| Flyway | versionamento do banco (migrations em `db/migration/`) |
| Spring Security OAuth2 Resource Server | validação JWT emitido pelo Keycloak |
| Springdoc OpenAPI | Swagger UI em `/swagger-ui.html` |

**Endpoints disponíveis:**

| Método | Rota | Descrição |
|--------|------|-----------|
| `GET` | `/api/v1/beneficios` | Lista todos |
| `GET` | `/api/v1/beneficios/{id}` | Busca por ID |
| `POST` | `/api/v1/beneficios` | Cria benefício |
| `PUT` | `/api/v1/beneficios/{id}` | Atualiza |
| `DELETE` | `/api/v1/beneficios/{id}` | Remove |
| `POST` | `/api/v1/beneficios/transfer?fromId=&toId=&amount=` | Transfere saldo |

> Todos os endpoints exigem token JWT válido (Bearer token emitido pelo Keycloak).

**Configuração local** (`application.properties`):
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/bipdb
spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8180/realms/bip-realm
app.cors.allowed-origin=http://localhost:4200
```
No Docker, essas propriedades são sobrescritas por variáveis de ambiente no `docker-compose.yml`.

---

### Frontend — Angular

```
front/
├── Dockerfile
├── nginx.conf
└── bip/
    └── src/app/
        ├── core/
        │   ├── services/
        │   │   ├── keycloak.service.ts   ← autenticação
        │   │   └── beneficio.service.ts  ← chamadas à API
        │   ├── interceptors/
        │   │   └── auth.interceptor.ts   ← injeta Bearer token
        │   └── guards/
        │       └── auth.guard.ts         ← protege rotas
        ├── features/
        │   └── beneficios/
        │       └── beneficio-list/       ← tela principal (dashboard)
        └── shared/
            └── components/toast/         ← notificações
```

**URLs configuradas por ambiente:**

| Arquivo | Keycloak | API |
|---------|---------|-----|
| `environment.ts` (local) | `http://localhost:8180` | `http://localhost:8080/api/v1` |
| `environment.prod.ts` (prod) | `https://auth.victor-dev.tech` | `https://api.victor-dev.tech/api/v1` |

---

### Keycloak — Configuração do Realm

O arquivo `docker/local/bip-realm.json` é importado automaticamente na primeira vez que o container sobe. Ele configura:

- **Realm**: `bip-realm`
- **Cliente**: `bip-frontend` (público, PKCE)
  - Redirect URI permitida: `http://localhost:4200/*`
- **Roles**: `admin`, `user`
- **Usuários**: `admin` e `usuario` (já com senhas e roles atribuídas)
- **Idioma**: Português (pt-BR)
- **SSL**: desabilitado para ambiente local

---

## Comandos úteis

```bash
# Ver status dos containers
docker compose ps

# Parar tudo (mantém os dados)
docker compose down

# Parar tudo e apagar volumes (banco zerado + Keycloak resetado)
docker compose down -v

# Reiniciar um serviço específico
docker compose restart backend

# Ver logs de um serviço
docker compose logs -f backend

# Acessar o banco de dados
docker exec -it bip-postgres psql -U bip -d bipdb
```

---

## Solução de problemas

### Frontend não abre / tela em branco
Aguarde o backend e o Keycloak terminarem de inicializar. O frontend depende dos dois.

### Keycloak retorna 400 Bad Request no login
O container Keycloak pode ter dados de uma inicialização anterior com configurações diferentes. Recrie-o:
```bash
docker compose down keycloak
docker compose up -d keycloak
```

### Backend retorna 401 Unauthorized
O token JWT foi emitido pelo Keycloak mas o backend não consegue validar. Verifique se o Keycloak está saudável:
```bash
docker compose ps
# bip-keycloak deve estar "healthy"
```

### Porta já em uso
Algum serviço local já usa a porta 5432, 8080 ou 8180. Pare o conflito ou ajuste as portas no `docker/local/docker-compose.yml`.

### Resetar tudo do zero
```bash
docker compose down -v
docker compose up -d
```
