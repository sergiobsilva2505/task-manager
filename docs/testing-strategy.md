# Testing Strategy

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

## Lições aprendidas (bugs de infraestrutura de teste)

- **Container Postgres compartilhado via padrão "Singleton Container" (sem `@Container`/`@Testcontainers`):**
  `IntegrationTestSupport` inicia o `PostgreSQLContainer` manualmente, num bloco estático, em vez de usar as anotações
  padrão do JUnit 5. Motivo: `@Container` em campo `static` faz o **JUnit** (não o Testcontainers) encerrar o container
  automaticamente ao final da última classe de teste que o usa — a garantia de "container único compartilhado" desse
  padrão vale de forma confiável só *dentro* de uma classe, não necessariamente *entre* classes diferentes que estendem
  a mesma base. Isso causava falhas intermitentes de conexão (`Connection refused`) especificamente na transição entre
  duas classes `@SpringBootTest` consecutivas (`TaskControllerIT` → `UserControllerIT`), porque o `ApplicationContext`/
  `HikariPool` cacheado pelo Spring Test continuava apontando para um container que o JUnit já tinha derrubado.
  `@ServiceConnection` continua presente (é o mecanismo do Spring Boot que lê a conexão do container, independente de
  quem gerencia seu ciclo de vida). Investigação completa, com todos os comandos e hipóteses testadas, documentada em [
  `investigacao-testcontainers-connection-refused.md`](investigacao-testcontainers-connection-refused.md).
- **Nunca usar datas fixas absolutas em testes — sempre relativas a `LocalDateTime.now()`:** alguns testes de `Task`
  fixavam `dueDate` com uma data absoluta (ex: `LocalDateTime.of(2026, Month.AUGUST, 1, ...)`), inicialmente introduzida
  para evitar o "magic number" apontado pelo SonarQube (`Month.AUGUST` em vez do inteiro `8`). O problema: uma data fixa
  no futuro deixa de ser válida assim que o calendário a ultrapassa — o que aconteceu literalmente um dia após
  `2026-08-01`, quebrando 14 testes em cascata (`TaskTest`, `CreateTaskServiceTest`, `ChangeTaskStatusServiceTest`) com
  `InvalidInputException: Due date cannot be before creation date`. A correção (e a regra adotada daqui em diante) é
  usar sempre `LocalDateTime.now().plusDays(N)` — o uso de constantes de enum (`Month.AUGUST`) resolve a legibilidade do
  número mágico sem exigir uma data fixa; as duas preocupações são independentes.
- **`application.yml` de teste não herda automaticamente do principal — checklist obrigatório ao adicionar propriedade
  nova:** esse projeto já foi pego de surpresa três vezes pelo mesmo mecanismo (CORS, `owner_id`/schema do Liquibase, e
  a introdução do `app.jwt.*`): o `application.yml` em `src/test/resources` **substitui** o de `src/main/resources` no
  classpath de teste, em vez de fazer merge — qualquer propriedade nova adicionada só no principal simplesmente não
  existe durante `mvn test`/`mvn verify`, geralmente resultando em `PlaceholderResolutionException` ou, pior, num bean
  sendo construído com campo `null` sem erro explícito de configuração ausente (caso do `JwtProperties.secret()`, que só
  falhou dentro do construtor do `JwtTokenGeneratorAdapter` com `Decode argument cannot be null`, mascarando a causa
  real). **Regra adotada:** toda vez que uma propriedade nova for adicionada em `app.*` no `application.yml` principal,
  replicar (com valor equivalente/de teste) no `application.yml` de teste no mesmo commit — nunca depois, como correção
  reativa. Vale a mesma atenção para Secrets do GitHub Actions: uma propriedade referenciada via `${VARIAVEL}` precisa
  existir tanto localmente (`.env`) quanto como Secret do repositório, e a esteira só reflete um Secret criado a partir
  da execução seguinte à sua criação.

---

Ver também: [Architecture](architecture.md) · [API Guide](api-guide.md) · [Roadmap](roadmap.md)