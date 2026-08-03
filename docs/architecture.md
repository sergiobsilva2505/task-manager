# Architecture & Design Decisions

## Visão geral

O projeto segue **Arquitetura Hexagonal (Ports & Adapters)**, organizada em três camadas principais, com a regra de que
**as dependências sempre apontam para dentro** — o domínio nunca conhece Spring, JPA ou qualquer outro detalhe de
infraestrutura.

```
src/
└── main/
    └── java/
        └── br.com.forjacode.taskmanager/
            ├── domain/                     # Núcleo: entidades e regras de negócio puras
            │   ├── model/                  # Task, User, enums (Status, Priority)
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

---

## Decisões de design

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
- **CORS configurável, sem origem coringa:** liberação de CORS (`CorsConfig`, em `adapters/input/rest/config`) para o
  futuro frontend, com origem, métodos e headers permitidos externalizados via `application.yml` (`app.cors.*`) em vez
  de hardcoded. `allowed-origins` é sempre uma origem específica (nunca `*`), já que uma origem coringa combinada com
  `allowCredentials(true)` é rejeitada pelo próprio navegador e enfraqueceria a defesa que complementa a decisão de
  desabilitar CSRF.
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
- **`DELETE` verdadeiramente idempotente:** remover uma tarefa retorna sempre `204 No Content`, independentemente de o
  `id` existir ou não — em vez de `404` para IDs inexistentes (padrão usado em `GetTaskById`/`ChangeTaskStatus`), o
  endpoint segue a semântica de idempotência da spec HTTP: "o recurso não existe" e "o recurso foi removido" resultam no
  mesmo estado final observável. `DeleteTaskService` delega direto para
  `TaskRepositoryPort.deleteByIdAndOwnerId(id, ownerId)`, sem buscar a tarefa antes — evitando uma consulta
  desnecessária e mantendo o comportamento realmente livre de ramificação de erro, mesmo com isolamento por dono.
- **Multiusuário com autenticação simulada (etapa intermediária antes do JWT):** cada `Task` pertence a um `User`
  (`ownerId`, com `FOREIGN KEY` no banco). Como a autenticação real (JWT) ainda não existe, o "usuário atual" é
  resolvido a partir do header **`X-User-Id`** — obrigatório em todo endpoint de `Task`, extraído por um
  `HandlerMethodArgumentResolver` customizado (`CurrentUserIdArgumentResolver`, acionado via a anotação
  `@CurrentUserId`) que injeta o valor diretamente como parâmetro do Controller. O header ausente ou malformado resulta
  em `400 Bad Request`. Esse mecanismo é deliberadamente temporário: quando o JWT for implementado, só o resolver muda
  (passa a decodificar o token em vez de ler o header) — nenhum Controller ou caso de uso precisa ser alterado.
- **`404`, não `403`, quando a tarefa não pertence ao usuário atual:** `GetTaskById` e `ChangeTaskStatus` tratam "tarefa
  não existe" e "tarefa existe mas pertence a outro usuário" com a **mesma resposta** (`404 Not Found`, mesma mensagem),
  usando `Optional.filter(...)` para colapsar os dois casos antes do `orElseThrow`. Isso evita vazar a existência de
  recursos que o usuário não deveria nem saber que existem — prática comum em APIs que levam segurança a sério, mesmo
  custando um pouco de "transparência" sobre o real motivo da falha.
- **Isolamento por dono sem SELECT extra:** `ListTasks` filtra por `ownerId` diretamente na consulta paginada
  (`findAllByOwnerId`), e `DeleteTask` usa `deleteByIdAndOwnerId` — os dois evitam buscar a tarefa antes de agir,
  deixando o próprio banco resolver "existe e é do usuário" em uma única operação.
- **`User.name` validado por formato, não só por tamanho:** além do intervalo de 3-160 caracteres, o nome precisa conter
  apenas letras (incluindo acentuadas, via `\p{L}`), espaços, hífens ou apóstrofos — barrando entradas como `"98765"`. A
  regra é duplicada por design (defesa em profundidade): validada no domínio (`User`, sempre) e replicada como
  `@Pattern` em `CreateUserRequest` (borda da API, mensagem de erro mais específica por campo).
- **Testes de serviço padronizados como "delegação pura" (sem duplicar validação de domínio):** todo `*Service` de caso
  de uso tem cobertura de teste unitário proporcional à sua **própria** responsabilidade — orquestração, ramificação de
  negócio (`TaskNotFoundException`, checagem de ownership) — nunca revalidando invariantes que já são garantidas e
  testadas em `Task`/`User` (domínio). Um serviço de delegação pura (`GetTaskByIdService`, `DeleteTaskService`,
  `ListTasksService`, `RegisterUserService`) recebe um teste de sucesso enxuto; um serviço com lógica de orquestração
  real (`ChangeTaskStatusService`) recebe testes proporcionais a essa lógica. Esse padrão foi unificado deliberadamente:
  as primeiras versões de `CreateTaskServiceTest`/`RegisterUserServiceTest` replicavam todos os cenários de erro de
  validação do domínio, criando duplicação — se a mensagem de uma invariante mudasse no domínio, dois arquivos de teste
  precisariam ser atualizados. Enxugar para o padrão único reduz manutenção sem perder cobertura real, já que a garantia
  de invariante permanece 100% coberta em `TaskTest`/`UserTest`.

---

Ver também: [Testing Strategy](testing-strategy.md) · [API Guide](api-guide.md) · [Roadmap](roadmap.md)