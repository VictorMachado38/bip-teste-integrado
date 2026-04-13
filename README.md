# BIP — Beneficios Integrados de Pagamento

> Solucao completa para o **Desafio Fullstack Integrado** proposto pelo **Sicoob / Travel Cash**.  
> Desenvolvido por **Victor Augusto**.

---

## 🎬 Demonstracao em video

<div align="center">
  <a href="https://youtu.be/pZvFQhR7U8o">
    <img src="https://img.youtube.com/vi/pZvFQhR7U8o/maxresdefault.jpg" alt="Assista a demonstracao do BIP" width="220">
  </a>
</div>

---

## 🌐 Ambiente em producao

O sistema esta no ar e pode ser acessado agora:

| Servico | URL |
|---|---|
| Aplicacao | [app.victor-dev.tech](https://app.victor-dev.tech) |
| API / Swagger UI | [api.victor-dev.tech/swagger-ui/index.html](https://api.victor-dev.tech/swagger-ui/index.html) |
| Keycloak | [auth.victor-dev.tech](https://auth.victor-dev.tech) |

> Para testar a API no Swagger UI de producao, clique em **Authorize** e informe um Bearer token valido gerado pelo Keycloak.

---

## 🚀 Rodando localmente

Consulte o guia completo — pre-requisitos, usuarios de teste, comandos e solucao de problemas:

📄 **[docs/COMO-RODAR-LOCAL.md](docs/COMO-RODAR-LOCAL.md)**

Em resumo:

```bash
git clone https://github.com/VictorMachado38/bip-teste-integrado.git
cd bip-teste-integrado/docker/local
docker compose up -d
```

Apos subir (~2 min na primeira vez):

| Servico | URL |
|---|---|
| Frontend | http://localhost:4200 |
| Swagger UI | http://localhost:8080/swagger-ui/index.html |
| Keycloak Admin | http://localhost:8180 |

---

## 🛠️ Tecnologias

| Camada | Tecnologia |
|---|---|
| Banco de dados | PostgreSQL 16 + Flyway |
| Backend | Java 17 · Spring Boot 3.2 · Spring Data JPA |
| Autenticacao | Keycloak 24 — OAuth2 / OIDC / JWT |
| Seguranca | Spring Security OAuth2 Resource Server |
| Documentacao | Springdoc OpenAPI (Swagger UI) |
| Frontend | Angular 19 · Keycloak-js · Nginx |
| Infraestrutura | Docker · Docker Compose |

---

## ✅ Criterios de avaliacao — Desafio Sicoob

| Criterio | Peso | Status          |
|---|---|-----------------|
| Arquitetura em camadas | 20% | ✅               |
| Correcao do bug no EJB | 20% | ✅               |
| CRUD + Transferencia | 15% | ✅               |
| Qualidade de codigo | 10% | ✅               |
| Testes | 15% | ✅ Atendido |
| Documentacao | 10% | ✅               |
| Frontend Angular | 10% | ✅               |

<details>
<summary>Ver detalhes de cada criterio</summary>

**Arquitetura em camadas**
Controller → Service → Repository → PostgreSQL. Frontend Angular desacoplado, consume a API via HTTP + JWT.

**Correcao do bug no EJB**
`BeneficioServiceImpl.transfer()` corrigido com validacao de valor positivo, bloqueio pessimista via `SELECT FOR UPDATE`, verificacao de saldo e `@Transactional` com rollback automatico.

**CRUD + Transferencia**
CRUD completo (`GET / POST / PUT / DELETE`) em `/api/v1/beneficios` + endpoint `/transfer` com consistencia transacional garantida.

**Qualidade de codigo**
Separacao de responsabilidades, `@Version` no modelo (optimistic locking), zero logica de negocio no controller, configuracoes externalizadas via variaveis de ambiente.

**Testes**
22 testes passando — unit tests do `BeneficioServiceImpl` (toda a logica de negocio, incluindo os cenarios do bug EJB) e slice tests do `BeneficioController` (HTTP, JWT e tratamento de erros via `GlobalExceptionHandler`).

**Documentacao**
Swagger UI disponivel local e em producao. README principal + guia detalhado `COMO-RODAR-LOCAL.md`.

**Frontend Angular**
SPA com autenticacao Keycloak (PKCE), AuthGuard nas rotas, interceptor de token, CRUD completo e transferencia de beneficios.

</details>

---

## 📁 Estrutura do repositorio

```
bip-teste-integrado/
├── back/
│   └── bip-teste-integrado/        # Spring Boot (Java 17)
│       ├── src/main/java/br/com/bip/
│       │   ├── controller/         # BeneficioController
│       │   ├── service/            # BeneficioService + impl (EJB corrigido)
│       │   ├── repository/         # BeneficioRepository (JPA + findByIdForUpdate)
│       │   ├── model/              # Beneficio (@Version — optimistic locking)
│       │   └── config/             # SecurityConfig, WebConfig (CORS)
│       └── src/main/resources/
│           └── db/migration/       # V1__schema.sql, V2__seed.sql (Flyway)
├── front/
│   └── bip/                        # Angular 19
│       └── src/app/
│           ├── core/               # services, interceptors, guards
│           ├── features/beneficios # tela principal — CRUD + transferencia
│           └── shared/             # componentes reutilizaveis (toast, etc.)
├── docker/
│   ├── local/                      # Docker Compose para desenvolvimento local
│   └── prod/                       # Docker Compose para producao
├── docs/
│   ├── desafio.md                  # Especificacao original do desafio
│   └── COMO-RODAR-LOCAL.md        # Guia detalhado de execucao local
└── themes/
    └── bip-theme/                  # Tema customizado do Keycloak (pt-BR)
```