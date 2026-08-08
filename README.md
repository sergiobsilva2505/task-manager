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

Backend do TaskManager — Java 21 + Spring Boot com arquitetura hexagonal (Ports & Adapters), consumido
pelo [TaskManager Web](https://github.com/sergiobsilva2505/taskmanager-web)

</div>

---

## Sumário

- [Pré-requisitos](#-pré-requisitos)
- [Como executar](#-como-executar)
- [Tecnologias](#-tecnologias)
- [Ferramentas](#-ferramentas)
- [Documentação](#-documentação)

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

## 📚 Documentação

A documentação detalhada do projeto vive na **[Wiki deste repositório](../../wiki)**:

- **[Architecture & Design Decisions](../../wiki/Architecture)** — estrutura de pacotes, regras de dependência entre
  camadas, e todas as decisões de design registradas (imutabilidade, Command Pattern, paginação desacoplada, CORS,
  multiusuário, JWT, login social, etc.).
- **[API Guide](../../wiki/API-Guide)** — comportamento e decisões por trás de cada endpoint (o contrato exato fica no
  Swagger UI).
- **[Testing Strategy](../../wiki/Testing-Strategy)** — pirâmide de testes, ferramentas por camada, e lições aprendidas
  de bugs reais de infraestrutura de teste (Testcontainers, datas fixas, `application.yml` de teste).
- **[Investigação: Testcontainers Connection Refused](../../wiki/Testcontainers-Connection-Refused)** — relatório de
  investigação de um bug real de infraestrutura de teste.
- **[Relatório: SonarCloud Local Auth Bug](../../wiki/SonarCloud-Local-Auth-Bug)** — depuração de uma falha de
  autenticação na análise local do SonarCloud.

O progresso e as pendências do projeto são acompanhados via **[Issues](../../issues)**. A documentação interativa da
API (Swagger UI) está disponível em `/swagger-ui/index.html` com a aplicação em execução.

---

<div align="center">

*Projeto em desenvolvimento contínuo, construído como estudo de Arquitetura Hexagonal com Spring Boot.*

</div>