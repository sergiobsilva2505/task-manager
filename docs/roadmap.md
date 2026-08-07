# Roadmap

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
- [x] Caso de uso: alterar status da tarefa (`PATCH /api/tasks/{id}/status`)
- [x] Cobertura de teste do handler de exceções de domínio no `GlobalExceptionHandler`
  (`InvalidStatusTransitionException` e `TaskNotFoundException` exercitadas via HTTP real; `InvalidInputException`/
  `MissingRequiredFieldException` permanecem cobertas apenas via validação de borda, já que essa camada intercepta antes
  de alcançar o domínio nos fluxos atuais)
- [x] Caso de uso: remover tarefa (`DELETE /api/tasks/{id}`, idempotente) — fecha o CRUD completo
- [x] Multiusuário: entidade `User`, `ownerId` em `Task`, isolamento por dono em todos os casos de uso, autenticação
  simulada via header `X-User-Id` (etapa intermediária antes do JWT)
- [x] Cobertura de teste completa para `User` (domínio, serviço, persistência, API) e validação de formato de nome;
  padronização de todos os `*ServiceTest` no mesmo estilo (sem duplicar validação de domínio)
- [x] CI (GitHub Actions) rodando build + testes + Qodana a cada push/PR
- [x] Autenticação JWT completa: `AuthIdentity` (multi-provider, `LOCAL`/`GOOGLE`), registro com senha (hash BCrypt,
  transação atômica User+AuthIdentity), login com mensagem de erro genérica, emissão e validação de token (HS256, 1h de
  expiração, sem refresh), filtro de autenticação (`JwtAuthenticationFilter`), `CurrentUserIdArgumentResolver` migrado
  do header `X-User-Id` para o `SecurityContextHolder`, `401` consistente via `AuthenticationEntryPoint` customizado
- [x] Login social (Google): fluxo frontend-driven (Google Identity Services no cliente, backend valida o ID Token via
  `GoogleTokenVerifierPort`/`google-api-client`), vinculação automática a `User` existente pelo e-mail, criação de
  usuário novo com nome derivado do e-mail quando o Google não fornece (`deriveNameFromEmail`), emissão do mesmo JWT
  usado no login local
- [x] Endpoint de agregação/dashboard (`GET /api/tasks/dashboard`): total de tarefas, contagem por status e prioridade,
  tarefas atrasadas e vencendo em breve — agregação em memória a partir de `findAllByOwnerId`, isolado por usuário
- [ ] Deploy (Docker + cloud)
- [ ] Avaliar SonarQube/SonarCloud no CI (complementar ao Qodana já configurado) — retomar ao final do projeto
- [ ] Definir estratégia e padrão de logging (o que logar, em qual nível, formato) — task dedicada

---

Ver também: [Architecture](architecture.md) · [Testing Strategy](testing-strategy.md) · [API Guide](api-guide.md)