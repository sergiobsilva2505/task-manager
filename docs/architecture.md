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
- **Multiusuário com JWT (a autenticação simulada via header foi substituída):** cada `Task` pertence a um `User`
  (`ownerId`, com `FOREIGN KEY` no banco). O "usuário atual" é resolvido a partir de um token JWT validado
  (`Authorization: Bearer <token>`), extraído por um `HandlerMethodArgumentResolver` customizado
  (`CurrentUserIdArgumentResolver`, acionado via a anotação `@CurrentUserId`) que lê o `userId` do
  `SecurityContextHolder`, populado por um `JwtAuthenticationFilter`. **Nota histórica:** antes do JWT existir, esse
  mesmo mecanismo lia um header temporário `X-User-Id` diretamente — a decisão de isolar a resolução do usuário atual
  atrás de `@CurrentUserId`/`HandlerMethodArgumentResolver` desde o início se provou correta: a migração do header para
  o JWT não exigiu alterar nenhum Controller ou caso de uso, só o filtro e o próprio resolver.
- **`AuthIdentity` como entidade separada de `User`, preparada para múltiplos providers:** a senha (e, futuramente,
  credenciais de provedores externos como Google) não fica em `User` — fica em `AuthIdentity` (`userId`, `provider` [
  `LOCAL`/`GOOGLE`], `passwordHash` nullable, `providerUserId` nullable), com uma constraint
  `UNIQUE (user_id, provider)` no banco. Isso separa "identidade" (`User`) de "método de autenticação" (`AuthIdentity`),
  permitindo um usuário ter múltiplos métodos de login vinculados à mesma conta no futuro, sem exigir migração de schema
  quando um novo provider for adicionado.
- **Registro de usuário como transação atômica entre `User` e `AuthIdentity`:** para evitar um `User` "órfão" (sem
  credencial) caso a segunda gravação falhe, existe uma porta dedicada (`UserRegistrationPort`) cujo adapter
  (`UserRegistrationAdapter`) demarca a transação com `@Transactional`, salvando os dois agregados juntos. Essa é a
  única concessão de anotação Spring fora da camada `adapters` estrita — decisão consciente de criar uma porta nova em
  vez de anotar o `RegisterUserService` diretamente, mantendo a camada `application` livre de Spring mesmo nesse caso.
- **Hash de senha via porta própria (`PasswordHasherPort`):** a camada `application` não conhece `BCryptPasswordEncoder`
  diretamente — só a interface `hash`/`matches`. O adapter (`BCryptPasswordHasherAdapter`) mora em
  `adapters/output/security`, um pacote irmão de `adapters/output/persistence`, já que hash de senha é infraestrutura de
  segurança, não persistência.
- **Login com mensagem de erro genérica, sempre idêntica:** e-mail inexistente, `AuthIdentity` ausente e senha incorreta
  lançam a mesma `InvalidCredentialsException`, sem parâmetro de mensagem customizável (o construtor não aceita
  `message`, de propósito) — evita que qualquer chamador vaze acidentalmente qual dos três motivos causou a falha,
  prevenindo enumeração de e-mails cadastrados.
- **Token JWT: access token curto, sem refresh, assinado com HMAC:** expiração de 1 hora, sem mecanismo de renovação
  automática — decisão consciente de simplicidade para o estágio atual do projeto (usuário precisa logar novamente ao
  expirar). A claim do token carrega só o `userId` (`sub`), nada de e-mail/nome, já que o payload de um JWT não é
  criptografado, apenas assinado — qualquer dado ali é legível por quem tiver o token.
- **Filtro de autenticação nunca bloqueia a requisição diretamente:** `JwtAuthenticationFilter` sempre chama
  `filterChain.doFilter(...)`, independente do token ser válido ou não — ele só popula (ou não) o
  `SecurityContextHolder`. Quem decide se a ausência de autenticação é um problema é o `SecurityConfig`
  (`.anyRequest().authenticated()`), mantendo a responsabilidade de autorização centralizada em um único lugar.
- **`401`, não `403`, para falha de autenticação:** o Spring Security usa `403 Forbidden` como fallback padrão para
  qualquer falha de autenticação quando nenhum `AuthenticationEntryPoint` é configurado — semanticamente incorreto
  (deveria ser `401 Unauthorized`, já que é ausência de identidade, não falta de permissão). Um
  `JwtAuthenticationEntryPoint` customizado corrige o status e também garante que a resposta segue o mesmo formato
  `ProblemDetail` (RFC 7807) do resto da API, mesmo esse erro nascendo fora do alcance do `GlobalExceptionHandler` (no
  filtro de segurança, antes do `DispatcherServlet`).
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
- **Login social (Google) via fluxo "frontend-driven", não o `oauth2Login()` clássico do Spring Security:** o frontend
  (Angular) autentica o usuário diretamente com o Google Identity Services (JS) e recebe um ID Token; esse token é
  enviado ao backend (`POST /api/auth/google`), que apenas **valida** a assinatura/emissor/audiência (via
  `GoogleTokenVerifierPort`, implementada com a biblioteca oficial `google-api-client`) e emite **seu próprio JWT** — a
  mesma `TokenGeneratorPort` usada no login local. O fluxo clássico (`spring-boot-starter-oauth2-client`,
  redirecionamento via sessão) foi descartado por entrar em conflito direto com `SessionCreationPolicy.STATELESS`, já
  adotada para o JWT.
- **Verificação do ID Token exige `setAudience` com o Client ID do próprio app:** sem essa checagem, um token genuíno
  emitido pelo Google para *qualquer outro* aplicativo que use "Sign in with Google" seria aceito pela sua API. O client
  secret do Google **não é usado** nesse fluxo (client-side/ID Token) — só o Client ID, que não é segredo por natureza.
- **Vinculação automática de conta ao invés de exigir fluxo manual:** se o e-mail retornado pelo Google já pertence a um
  `User` registrado via `LOCAL`, um `AuthIdentity` novo (`GOOGLE`) é criado e vinculado a esse mesmo usuário — sem
  exigir confirmação adicional. Isso é considerado seguro porque o Google já garante a posse do e-mail (via verificação
  própria) antes de emitir o token; a alternativa (exigir vinculação manual) foi avaliada e descartada por adicionar
  fricção sem ganho de segurança proporcional neste estágio do projeto.
- **`AuthIdentity` resolvida em duas etapas: por vínculo existente, depois por e-mail:** `GoogleLoginService` primeiro
  busca `AuthIdentity` por `(provider, providerUserId)` — se encontrar, o login é imediato. Só na ausência desse vínculo
  é que o e-mail entra em jogo (busca de `User`, vinculação ou criação). Isso garante que, uma vez vinculada, uma conta
  Google nunca dependa de o e-mail continuar o mesmo no `User` para autenticar novamente.
- **Nome derivado do e-mail quando o Google não fornece (`deriveNameFromEmail`):** a claim `name` do ID Token não é
  garantida pelo Google em todos os casos. Quando ausente/vazia, o nome é derivado da parte local do e-mail
  (substituindo caracteres fora de `\p{L}\s'-` por espaço), com fallback para `"Google User"` caso o resultado fique
  abaixo do tamanho mínimo exigido por `User.name`. A função vive como método privado de `GoogleLoginService` — é uma
  regra específica desse fluxo de login, não uma responsabilidade do domínio `User`.
- **`InvalidGoogleTokenException` sem parâmetro de mensagem, mesmo padrão de `InvalidCredentialsException`:** token do
  Google ausente na verificação (assinatura inválida, expirado, emitido para outro Client ID) sempre resulta na mesma
  mensagem fixa e no mesmo status (`401`), sem detalhar o motivo exato — mesma filosofia de não vazar informação de
  diagnóstico ao cliente da API.
- **Dashboard agregado em memória, não via query SQL dedicada:** `GET /api/tasks/dashboard` busca todas as tarefas do
  usuário (`findAllByOwnerId`, sem paginação) e calcula todas as métricas (total, contagem por status/prioridade,
  atrasadas, vencendo em breve) com Java Streams sobre essa lista. Uma query agregada (`GROUP BY` no banco) seria mais
  eficiente, mas foi deliberadamente descartada nesse estágio do projeto — o volume de tarefas por usuário não justifica
  a complexidade adicional, e a implementação em memória é mais simples de testar e alterar.
- **`Status.isTerminal()` como regra de domínio reutilizável:** "atrasada" e "vencendo em breve" só se aplicam a tarefas
  com status não terminal (`TODO`/`IN_PROGRESS`) — uma tarefa `DONE`/`CANCELLED` com `dueDate` no passado nunca conta
  como atrasada. Essa checagem foi extraída para o próprio enum `Status` (`isTerminal()`), ao lado de `canTransitionTo`,
  em vez de reimplementada como uma comparação solta dentro do serviço do dashboard — evita duplicar a mesma regra em
  múltiplos lugares se outro caso de uso precisar da mesma distinção no futuro.
- **Contagens por enum mantidas como tipos fortes (`Map<Status, Long>`/`Map<Priority, Long>`) até a borda REST:**
  seguindo o mesmo princípio já aplicado em toda a camada `application` (nunca vazar formato de serialização pra dentro
  do domínio/aplicação), a conversão para `Map<String, Long>` (o formato que efetivamente aparece no JSON de resposta)
  acontece exclusivamente no `TaskRestMapper`, nunca no `GetDashboardService`.

---

Ver também: [Testing Strategy](testing-strategy.md) · [API Guide](api-guide.md) · [Roadmap](roadmap.md)