# ✅ Task Manager

<div align="center">

![Java](https://img.shields.io/badge/Java-21-ED8800?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.1.0-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)
![Maven](https://img.shields.io/badge/Apache_Maven-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)
![Liquibase](https://img.shields.io/badge/Liquibase-2962FF?style=for-the-badge&logo=liquibase&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white)
![OpenAPI](https://img.shields.io/badge/OpenAPI-Swagger-85EA2D?style=for-the-badge&logo=swagger&logoColor=white)
![Testcontainers](https://img.shields.io/badge/Testcontainers-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![JUnit5](https://img.shields.io/badge/JUnit5-25A162?style=for-the-badge&logo=junit5&logoColor=white)
![Lombok](https://img.shields.io/badge/Lombok-BC0836?style=for-the-badge&logoColor=white)
![MapStruct](https://img.shields.io/badge/MapStruct-1.6.3-orange?style=for-the-badge&logoColor=white)

*API de gerenciamento de tarefas construída com Java 21 + Spring Boot, seguindo Arquitetura Hexagonal (Ports &
Adapters)*

</div>

---

## Sumário

- [Pré-requisitos](#-pré-requisitos)
- [Como executar](#-como-executar)
- [Arquitetura](#-arquitetura-e-decisões-técnicas)
- [Tecnologias](#-tecnologias)
- [Ferramentas](#-ferramentas)
- [API Reference](#-guia-de-uso-da-api)
- [Roadmap](#-roadmap)

---

## ✅ Pré-requisitos

- Java 21+
- Docker e Docker Compose (para o PostgreSQL)
- Maven 3.x

---

## 🚀 Como executar

1. Suba o PostgreSQL com Docker Compose (arquivo na raiz do projeto):

```bash
docker compose up -d
```

2. Execute a aplicação:

```bash
./mvnw spring-boot:run
```

A API estará disponível em `http://localhost:8080`.

3. Teste rapidamente:

```bash
curl http://localhost:8080/actuator/health
```

---

## 🏗️ Arquitetura e decisões técnicas

O projeto segue **Arquitetura Hexagonal (Ports & Adapters)**, organizada em três camadas principais, com a regra de que
**as dependências sempre apontam para dentro** — o domínio nunca conhece Spring, JPA ou qualquer outro detalhe de
infraestrutura.

```
src/
└── main/
    └── java/
        └── br.com.forjacode.taskmanager/
            ├── domain/                     # Núcleo: entidades e regras de negócio puras
            │   ├── model/                  # Task, enums (Status, Priority)
            │   └── exception/              # Exceções de regra de negócio
            │
            ├── application/                # Casos de uso (orquestração)
            │   ├── port/
            │   │   ├── in/                 # Interfaces dos casos de uso (UseCases) + Commands
            │   │   └── out/                # Interfaces que a aplicação precisa (ex: RepositoryPort)
            │   └── service/                # Implementação dos casos de uso
            │
            └── adapters/                   # Infraestrutura: implementações concretas
                ├── config/                 # Fiação neutra (conecta portas de entrada/saída)
                ├── input/
                │   └── rest/                # Controllers, DTOs, mappers, exception handler, security
                └── output/
                    └── persistence/         # Entidade JPA, Spring Data Repository, mapper, adapter
```

**Regra de dependência entre camadas:**

`adapters` → `application` → `domain`

Nunca o inverso. O `domain` não conhece `application`; o `application` não conhece `adapters`.

### Decisões de design

- **Entidade de domínio (`Task`) imutável por fora:** só é possível criar uma instância via `Task.create(...)` (nova
  tarefa) ou `Task.reconstruct(...)` (reidratação a partir do banco). Não existe construtor público nem setters — todas
  as invariantes de negócio (título obrigatório, prazo não pode ser anterior à criação, transições de status válidas)
  são garantidas dentro da própria classe.
- **Máquina de estados de status:** transições de `Status` (`TODO → IN_PROGRESS → DONE`, com `CANCELLED` como estado
  terminal alternativo) são validadas no próprio enum (`canTransitionTo`), impedindo pulos de etapa inválidos.
- **Command Pattern na porta de entrada:** casos de uso recebem um objeto `Command` (ex: `CreateTaskCommand`) em vez de
  parâmetros soltos, facilitando evolução sem quebrar assinaturas.
- **Registro manual de beans dos casos de uso:** para manter a camada `application` livre de anotações do Spring, os
  `Service`s **não** são anotados com `@Service`. Em vez disso, uma classe `@Configuration` neutra (`UseCaseConfig`, em
  `adapters/config`) registra cada caso de uso como `@Bean` manualmente.
- **DTOs desacoplados do domínio:** a API REST nunca expõe `Task` nem os `Command`s diretamente — usa
  `CreateTaskRequest`/`TaskResponse`, convertidos via `TaskRestMapper` (MapStruct).
- **Erros padronizados com RFC 7807:** o `GlobalExceptionHandler` (`@RestControllerAdvice`) usa `ProblemDetail` do
  Spring para uniformizar respostas de erro, cobrindo exceções de domínio, falhas de validação (`@Valid`) e um fallback
  genérico para erros não previstos.
- **Segurança liberada temporariamente:** com `Spring Security` no classpath mas sem autenticação implementada ainda, as
  rotas atuais estão liberadas via `permitAll()` como dívida técnica documentada — a implementação de autenticação (JWT)
  está prevista no roadmap.
- **Arquitetura protegida por teste (ArchUnit):** as regras de dependência entre camadas, o isolamento do domínio em
  relação a frameworks e as convenções de nomenclatura (`UseCase`/`Port` como interfaces) são verificadas
  automaticamente a cada build, evitando regressão arquitetural silenciosa.
- **Paginação desacoplada do Spring Data:** a camada `application` não conhece `Pageable`/`Page` — usa tipos próprios
  (`PageQuery`, `PagedResult<T>`, em `application/ports/shared`), com uma whitelist de campos ordenáveis por entidade
  (`TaskSortField`). A conversão para `Pageable`/`Page` do Spring Data acontece só dentro do `TaskRepositoryAdapter`,
  mantendo a aplicação livre de framework mesmo nesse ponto.
- **Swagger e Bruno coexistindo (não é redundância):** os dois servem propósitos diferentes, então nenhum substitui o
  outro:
    - **Swagger** é gerado automaticamente a partir do código (`springdoc-openapi`) — reflete sempre o contrato *real*
      da API no momento, sem esforço manual de manutenção. É a fonte de verdade para "o que existe e qual o formato
      exato", útil para explorar endpoints novos ou consultar rapidamente sem sair do navegador.
    - **Bruno** guarda **exemplos de uso reais e cenários específicos** que o Swagger não descreve: fluxos encadeados
      (ex: criar uma tarefa e já reaproveitar o `id` retornado para buscar essa mesma tarefa, via `vars:post-response`),
      casos de erro documentados com o resultado esperado (`sortField` inválido, UUID malformado, validação de campo), e
      environments por ambiente (`Local`, e futuramente `Staging`/`Production`). Como fica versionado em `bruno/` no
      repositório, também serve de documentação viva de "como esse endpoint costuma ser chamado na prática" — algo que a
      especificação OpenAPI, por natureza, não descreve.
    - Na prática: Swagger responde "o que a API aceita e devolve"; Bruno responde "como usar a API para resolver uma
      tarefa específica, incluindo os erros esperados". Manter os dois evita que a coleção Bruno vire uma cópia manual
      (e desatualizável) da especificação, e evita que o Swagger precise carregar exemplos de fluxo/erro que não são sua
      responsabilidade.

---

## 🧪 Estratégia de testes

O projeto segue uma pirâmide de testes com separação explícita entre testes rápidos (sem infraestrutura) e testes de
integração (com Docker):

| Camada                                               | Tipo                          | Ferramenta                      | Comando      |
|------------------------------------------------------|-------------------------------|---------------------------------|--------------|
| Domínio (`Task`, `Status`)                           | Unitário                      | JUnit 6 + AssertJ               | `mvn test`   |
| Serviços (`CreateTaskService`, `GetTaskByIdService`) | Unitário, mocks               | Mockito                         | `mvn test`   |
| Persistência (`TaskRepositoryAdapter`)               | Integração, Postgres real     | Testcontainers + `@DataJpaTest` | `mvn verify` |
| API REST (`TaskController`)                          | Integração, contexto completo | MockMvc + Testcontainers        | `mvn verify` |
| Arquitetura                                          | Estático                      | ArchUnit                        | `mvn test`   |

- `mvn test` roda apenas os testes rápidos (Surefire), sem exigir Docker.
- `mvn verify` inclui os testes de integração (Failsafe), que sobem um PostgreSQL real via Testcontainers.
- Cobertura de código gerada pelo JaCoCo em relatórios separados por tipo de teste (`jacoco.exec` para unitários,
  `jacoco-it.exec` para integração), visível diretamente na IDE ou via `target/site/jacoco`.

---

## 🛠️ Tecnologias

| Tecnologia                                           | Uso                                                                   |
|------------------------------------------------------|-----------------------------------------------------------------------|
| **Java 21 LTS**                                      | Linguagem base                                                        |
| **Spring Boot 4.1.0**                                | Framework principal                                                   |
| **Spring Web MVC**                                   | Mapeamento de rotas HTTP e camada de apresentação                     |
| **Spring Data JPA**                                  | Persistência de dados                                                 |
| **PostgreSQL**                                       | Banco de dados relacional                                             |
| **Liquibase**                                        | Versionamento e migração de schema do banco                           |
| **Spring Security**                                  | Autenticação e autorização (em configuração inicial)                  |
| **Spring Actuator**                                  | Health checks e observabilidade                                       |
| **Bean Validation (spring-boot-starter-validation)** | Validação de dados de entrada da API (`@Valid`)                       |
| **MapStruct**                                        | Mapeamento entre domínio, entidades JPA e DTOs                        |
| **Lombok**                                           | Redução de boilerplate (getters/setters em classes de infraestrutura) |
| **springdoc-openapi**                                | Documentação da API (Swagger UI)                                      |
| **springboot4-dotenv**                               | Carregamento de variáveis de ambiente a partir de arquivo `.env`      |
| **Testcontainers**                                   | Testes de integração com PostgreSQL real via Docker                   |
| **Mockito**                                          | Mocks para testes unitários de serviços                               |
| **ArchUnit**                                         | Testes de arquitetura (regras de camadas e convenções)                |
| **JaCoCo**                                           | Relatórios de cobertura de testes (unitário e integração separados)   |

> Dependências de teste (`spring-boot-starter-data-jpa-test`, `spring-boot-starter-liquibase-test`,
> `spring-boot-starter-security-test`, `spring-boot-starter-webmvc-test`) já estão configuradas no `pom.xml`, junto com o
> goal `build-info` do `spring-boot-maven-plugin` (expõe metadados de build via Actuator) e o plugin **Failsafe**,
> responsável por rodar os testes de integração separadamente dos unitários.

---

## 🔧 Ferramentas

| Ferramenta                         | Finalidade                                                        |
|------------------------------------|-------------------------------------------------------------------|
| IntelliJ IDEA                      | IDE                                                               |
| Git                                | Controle de versão                                                |
| Maven                              | Build e dependências                                              |
| [Bruno](https://www.usebruno.com/) | Cliente de API para testes manuais (coleções versionáveis em Git) |

> A coleção de requisições do Bruno fica versionada na pasta `bruno/` na raiz do repositório — basta abrir a pasta no
> Bruno ("Open Collection") e selecionar o environment **Local** para testar a API.

---

## 📖 Guia de uso da API

### 💓 Status da aplicação

<details>
<summary>💓 Health Check</summary>

**GET** `/actuator/health`

**Resposta:** `200 OK` — status agregado da aplicação, banco de dados e disco.

</details>

---

### 📋 Tarefas

<details>
<summary>➕ Criar uma tarefa</summary>

**POST** `/api/tasks`

```json
{
    "title": "Minha primeira tarefa",
    "description": "Descrição da tarefa",
    "priority": "HIGH",
    "dueDate": "2026-08-01T10:00:00"
}
```

**Resposta:** `201 Created` (com header `Location` apontando para o recurso criado)

```json
{
    "id": "83509a61-0df4-4629-b172-0870f5190d37",
    "title": "Minha primeira tarefa",
    "description": "Descrição da tarefa",
    "status": "TODO",
    "priority": "HIGH",
    "dueDate": "2026-08-01T10:00:00",
    "createdAt": "2026-07-18T01:35:06.740981200Z",
    "updatedAt": "2026-07-18T01:35:06.740981200Z"
}
```

**Erros possíveis:** `400 Bad Request` (formato `application/problem+json`, RFC 7807) para campos obrigatórios ausentes,
prazo inválido ou falha de validação de formato.

</details>

<details>
<summary>🔍 Buscar tarefa por ID</summary>

**GET** `/api/tasks/{id}`

**Resposta:** `200 OK`

```json
{
    "id": "83509a61-0df4-4629-b172-0870f5190d37",
    "title": "Minha primeira tarefa",
    "description": "Descrição da tarefa",
    "status": "TODO",
    "priority": "HIGH",
    "dueDate": "2026-08-01T10:00:00",
    "createdAt": "2026-07-18T01:35:06.740981200Z",
    "updatedAt": "2026-07-18T01:35:06.740981200Z"
}
```

**Erros possíveis:** `404 Not Found` (formato `application/problem+json`) quando o ID não existe. `400 Bad Request`
quando o `id` informado não é um UUID válido.

</details>

<details>
<summary>📋 Listar tarefas (paginado)</summary>

**GET** `/api/tasks`

**Query params (todos opcionais):**

| Parâmetro       | Padrão       | Valores aceitos                               |
|-----------------|--------------|-----------------------------------------------|
| `page`          | `0`          | inteiro >= 0                                  |
| `size`          | `20`         | inteiro > 0                                   |
| `sortField`     | `CREATED_AT` | `TITLE`, `CREATED_AT`, `DUE_DATE`, `PRIORITY` |
| `sortDirection` | `DESC`       | `ASC`, `DESC`                                 |

**Resposta:** `200 OK`

```json
{
    "content": [
        {
            "id": "83509a61-0df4-4629-b172-0870f5190d37",
            "title": "Minha primeira tarefa",
            "description": "Descrição da tarefa",
            "status": "TODO",
            "priority": "HIGH",
            "dueDate": "2026-08-01T10:00:00",
            "createdAt": "2026-07-18T01:35:06.740981200Z",
            "updatedAt": "2026-07-18T01:35:06.740981200Z"
        }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1
}
```

**Erros possíveis:** `400 Bad Request` quando `sortField`/`sortDirection` recebem um valor fora da whitelist (ex:
`?sortField=NAOEXISTE`).

</details>

> Os demais endpoints (atualizar status, remover) estão em desenvolvimento — veja o [Roadmap](#-roadmap). A documentação
> interativa completa está disponível em `/swagger-ui/index.html` com a aplicação em execução.

---

## 🗺️ Roadmap

- [x] Setup do projeto e estrutura hexagonal
- [x] Health check (Actuator) e Security básica
- [x] Entidade de domínio `Task` com regras de transição de status
- [x] Caso de uso: criar tarefa (`POST /api/tasks`)
- [x] Persistência via JPA + PostgreSQL
- [x] Tratamento global de erros com `ProblemDetail` (RFC 7807)
- [x] Documentação OpenAPI/Swagger, sincronizada com o README
- [x] Testes unitários de domínio e serviço (100% linha/branch no domínio)
- [x] Testes de integração (persistência e API) com Testcontainers
- [x] Testes de arquitetura com ArchUnit
- [x] Caso de uso: buscar tarefa por ID (`GET /api/tasks/{id}`)
- [x] Caso de uso: listar tarefas com paginação e ordenação (`GET /api/tasks`)
- [ ] Caso de uso: alterar status da tarefa
- [ ] Cobertura de teste do handler de exceções de domínio no `GlobalExceptionHandler` (pendente até `ChangeTaskStatus`)
- [ ] Multiusuário (`ownerId`, autenticação JWT)
- [ ] Deploy (Docker + cloud)
- [ ] Avaliar SonarQube/SonarCloud no CI (complementar ao Qodana já configurado) — retomar ao final do projeto

---

<div align="center">

*Projeto em desenvolvimento contínuo, construído como estudo de Arquitetura Hexagonal com Spring Boot.*

</div>