# BIP - Desafio Fullstack Integrado

Solução completa para o **Desafio Fullstack Integrado** proposto pelo **Sicoob / Travel Cash**.

---

## Tecnologias

| Camada        | Tecnologia                              |
|---------------|-----------------------------------------|
| Banco de dados | PostgreSQL 16 + Flyway (migrations)    |
| Backend        | Java 17, Spring Boot 3.2, Spring Security + OAuth2 Resource Server |
| Autenticação   | Keycloak 24 (Identity Provider, JWT)   |
| Frontend       | Angular 19, Keycloak-js                |
| Containerização | Docker + Docker Compose               |
| Documentação   | Springdoc OpenAPI (Swagger UI)         |

---

## Arquitetura em camadas

```
┌─────────────────────────────────┐
│         Frontend (Angular)       │  :4200
│  KeycloakService / AuthGuard     │
│  BeneficioService (HttpClient)   │
└────────────────┬────────────────┘
                 │ HTTP + Bearer JWT
┌────────────────▼────────────────┐
│   Backend (Spring Boot REST)     │  :8080
│   BeneficioController            │
│   BeneficioServiceImpl (EJB)     │
│   BeneficioRepository (JPA)      │
└────────────────┬────────────────┘
                 │
┌────────────────▼────────────────┐
│       PostgreSQL 16              │  :5432
│   (migrations via Flyway)        │
└─────────────────────────────────┘
         autenticação via
┌─────────────────────────────────┐
│       Keycloak 24                │  :8180
│   Realm bip-realm / JWT issuer   │
└─────────────────────────────────┘
```

---

## Rodando localmente

### Pré-requisitos

- [Docker](https://www.docker.com/) e Docker Compose instalados
- Porta `5432`, `8080`, `8180` e `4200` livres na máquina

### 1. Clone o repositório

```bash
git clone https://github.com/VictorMachado38/bip-teste-integrado.git
cd bip-teste-integrado
```

### 2. Suba todos os serviços com Docker Compose

```bash
cd docker/local
docker compose up -d
```

Isso irá subir:

| Serviço    | URL local                         | Credenciais                  |
|------------|-----------------------------------|------------------------------|
| Frontend   | http://localhost:4200             | —                            |
| Backend    | http://localhost:8080             | —                            |
| Swagger UI | http://localhost:8080/swagger-ui/index.html | —               |
| Keycloak   | http://localhost:8180             | admin / admin123             |
| PostgreSQL | localhost:5432                    | bip / bip123 (db: bipdb)     |

> O Keycloak pode levar até ~90 s para ficar pronto (health check configurado). Aguarde o backend subir antes de acessar o frontend.

### 3. Acesse o sistema

1. Abra http://localhost:4200
2. Clique em **Login** — você será redirecionado ao Keycloak
3. Use as credenciais do usuário configurado no realm `bip-realm`

---

## Endpoints da API

A API REST está documentada via Swagger UI:

- **Local:** http://localhost:8080/swagger-ui/index.html
- **Producao:** https://api.victor-dev.tech/swagger-ui/index.html

Principais endpoints (`/api/v1/beneficios`):

| Metodo   | Rota                  | Descricao                         |
|----------|-----------------------|-----------------------------------|
| `GET`    | `/beneficios`         | Lista todos os beneficios          |
| `GET`    | `/beneficios/{id}`    | Busca beneficio por ID             |
| `POST`   | `/beneficios`         | Cria novo beneficio                |
| `PUT`    | `/beneficios/{id}`    | Atualiza beneficio existente       |
| `DELETE` | `/beneficios/{id}`    | Remove beneficio                   |
| `POST`   | `/beneficios/transfer`| Transfere valor entre beneficios   |

> Todos os endpoints exigem Bearer JWT (Keycloak). No Swagger UI em producao, clique em **Authorize** e informe o token.

---

## Ambientes em producao

| Servico    | URL                                                                  |
|------------|----------------------------------------------------------------------|
| Aplicacao  | https://app.victor-dev.tech                                          |
| API / Swagger | https://api.victor-dev.tech/swagger-ui/index.html                |
| Keycloak   | https://auth.victor-dev.tech                                         |

---

## Criterios de avaliacao (Desafio Sicoob)

| Criterio                      | Peso | Status         | Detalhes                                                                                         |
|-------------------------------|------|----------------|--------------------------------------------------------------------------------------------------|
| Arquitetura em camadas        | 20%  | Atendido      | Controller → Service (EJB pattern) → Repository (JPA) → PostgreSQL; Frontend Angular desacoplado |
| Correcao do bug no EJB        | 20%  | Atendido      | `BeneficioServiceImpl.transfer()`: valida saldo, usa bloqueio pessimista (`SELECT FOR UPDATE`) e `@Transactional` com rollback automatico |
| CRUD + Transferencia          | 15%  | Atendido      | CRUD completo via REST + endpoint `/transfer` com validacao de saldo e consistencia transacional  |
| Qualidade de codigo           | 10%  | Atendido      | Separacao de responsabilidades, imutabilidade via `@Version` (optimistic locking no modelo), sem logica de negocio no controller |
| Testes                        | 15%  | Em andamento  | Dependencias de teste ja configuradas (`spring-boot-starter-test`, `spring-security-test`)       |
| Documentacao                  | 10%  | Atendido      | Swagger UI disponivel local e em producao; README com instrucoes completas de execucao           |
| Frontend Angular              | 10%  | Atendido      | SPA Angular com autenticacao Keycloak, listagem, criacao, edicao, exclusao e transferencia de beneficios |

---

## Estrutura do repositorio

```
bip-teste-integrado/
├── back/
│   └── bip-teste-integrado/      # Spring Boot (Java 17)
│       ├── src/main/java/...
│       └── src/main/resources/
│           └── db/migration/     # Flyway: V1__schema.sql, V2__seed.sql
├── front/
│   └── bip/                      # Angular 19
├── docker/
│   ├── local/                    # docker-compose para desenvolvimento local
│   └── prod/                     # docker-compose para producao
├── docs/
│   └── desafio.md                # Especificacao original do desafio
└── themes/
    └── bip-theme/                # Tema customizado do Keycloak
```