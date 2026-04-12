# BIP — Beneficios Integrados de Pagamento

Solucao completa para o **Desafio Fullstack Integrado** proposto pelo **Sicoob / Travel Cash**.

Desenvolvido por **Victor Augusto**.

---

## Ambiente em producao

O sistema esta no ar e pode ser acessado agora:

| Servico | URL |
|---------|-----|
| Aplicacao (Frontend) | https://app.victor-dev.tech |
| API — Swagger UI | https://api.victor-dev.tech/swagger-ui/index.html |
| Keycloak (Identity Provider) | https://auth.victor-dev.tech |

> Para testar a API via Swagger UI em producao, clique em **Authorize** e informe um Bearer token valido emitido pelo Keycloak.

---

## Rodando localmente

Consulte o guia completo de execucao local, incluindo pre-requisitos, comandos, usuarios de teste e solucao de problemas:

**[docs/rodar-local.md](docs/rodar-local.md)**

Em resumo:

```bash
git clone https://github.com/VictorMachado38/bip-teste-integrado.git
cd bip-teste-integrado/docker/local
docker compose up -d
```

Servicos disponiveis apos subir:

| Servico | URL local |
|---------|-----------|
| Frontend | http://localhost:4200 |
| Swagger UI | http://localhost:8080/swagger-ui/index.html |
| Keycloak Admin | http://localhost:8180 |

---

## Tecnologias

| Camada | Tecnologia |
|--------|-----------|
| Banco de dados | PostgreSQL 16 + Flyway (migrations versionadas) |
| Backend | Java 17, Spring Boot 3.2, Spring Data JPA |
| Autenticacao | Keycloak 24 — OAuth2 / OIDC / JWT |
| Seguranca | Spring Security OAuth2 Resource Server |
| Documentacao | Springdoc OpenAPI (Swagger UI) |
| Frontend | Angular 19 + Keycloak-js + Nginx |
| Containerizacao | Docker + Docker Compose |

---

## Criterios de avaliacao — Desafio Sicoob

| # | Criterio | Peso | Status |
|---|----------|------|--------|
| 1 | **Arquitetura em camadas** | 20% | ✅ Atendido |
| 2 | **Correcao do bug no EJB** | 20% | ✅ Atendido |
| 3 | **CRUD + Transferencia** | 15% | ✅ Atendido |
| 4 | **Qualidade de codigo** | 10% | ✅ Atendido |
| 5 | **Testes** | 15% | 🔄 Em andamento |
| 6 | **Documentacao** | 10% | ✅ Atendido |
| 7 | **Frontend Angular** | 10% | ✅ Atendido |

## Estrutura do repositorio

```
bip-teste-integrado/
├── back/
│   └── bip-teste-integrado/        # Spring Boot (Java 17)
│       ├── src/main/java/br/com/bip/
│       │   ├── controller/         # BeneficioController
│       │   ├── service/            # BeneficioService + impl (EJB corrigido)
│       │   ├── repository/         # BeneficioRepository (JPA + findByIdForUpdate)
│       │   ├── model/              # Beneficio (com @Version)
│       │   └── config/             # SecurityConfig, WebConfig (CORS)
│       └── src/main/resources/
│           └── db/migration/       # V1__schema.sql, V2__seed.sql (Flyway)
├── front/
│   └── bip/                        # Angular 19
│       └── src/app/
│           ├── core/               # services, interceptors, guards
│           ├── features/beneficios # tela principal (CRUD + transferencia)
│           └── shared/             # componentes reutilizaveis
├── docker/
│   ├── local/                      # docker-compose para desenvolvimento local
│   └── prod/                       # docker-compose para producao
├── docs/
│   ├── desafio.md                  # Especificacao original do desafio
│   └── COMO-RODAR-LOCAL.md        # Guia detalhado de execucao local
└── themes/
    └── bip-theme/                  # Tema customizado do Keycloak (pt-BR)
```