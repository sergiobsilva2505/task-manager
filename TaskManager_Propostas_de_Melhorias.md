# TaskManager --- Propostas de Melhorias Técnicas

## Objetivo

Este documento consolida as principais alterações propostas após a
análise do projeto **TaskManager**, com foco em:

-   segurança;
-   arquitetura hexagonal;
-   regras de negócio;
-   persistência e ownership;
-   testes;
-   configuração de produção;
-   CI/CD;
-   observabilidade;
-   qualidade de código;
-   posicionamento do projeto como portfólio profissional.

As propostas estão organizadas por prioridade e incluem a motivação
técnica de cada uma.

------------------------------------------------------------------------

# 1. Resumo executivo

O projeto já apresenta uma base bastante sólida para um projeto de
portfólio profissional, principalmente pela combinação de:

-   Java 21;
-   Spring Boot;
-   arquitetura hexagonal;
-   separação entre domínio, aplicação e adapters;
-   PostgreSQL;
-   Liquibase;
-   JWT;
-   autenticação local e Google;
-   testes unitários e de integração;
-   Testcontainers;
-   ArchUnit;
-   JaCoCo;
-   Docker;
-   Bruno;
-   documentação OpenAPI;
-   controle de ownership das tarefas.

As alterações propostas abaixo não têm como objetivo "refazer" o
projeto. A ideia é **endurecer pontos específicos**, reduzir riscos e
deixar as decisões arquiteturais ainda mais defensáveis em uma
entrevista técnica.

------------------------------------------------------------------------

# 2. Prioridade das alterações

  ------------------------------------------------------------------------------
  Prioridade              Alteração                      Motivo principal
  ----------------------- ------------------------------ -----------------------
  🔴 Alta                 Restringir rotas públicas de   Reduzir superfície de
                          usuários                       ataque

  🔴 Alta                 Reforçar ownership na          Segurança e
                          persistência                   consistência

  🔴 Alta                 Ampliar testes de segurança    Evitar falhas em
                          JWT                            autenticação

  🔴 Alta                 Ajustar Actuator em produção   Evitar exposição de
                                                         informações

  🟡 Média                Endurecer configuração de CORS Reduzir superfície de
                                                         ataque

  🟡 Média                Melhorar pipeline CI/CD        Aumentar confiabilidade
                                                         das entregas

  🟡 Média                Ampliar testes de              Aumentar confiança no
                          integração/segurança           comportamento real

  🟢 Evolução             Observabilidade                Melhorar operação e
                                                         diagnóstico

  🟢 Evolução             Revisar                        Manter arquitetura
                          complexidade/overengineering   proporcional ao
                                                         problema
  ------------------------------------------------------------------------------

------------------------------------------------------------------------

# 3. 🔴 Restringir as rotas públicas de `/api/users/**`

## Situação identificada

A configuração de segurança permite acesso público a:

``` text
/api/users/**
```

Pelo desenho atual, a funcionalidade que precisa ser pública é
principalmente o cadastro de usuário.

## Proposta

Substituir uma regra ampla:

``` text
/api/users/**
```

por endpoints públicos explicitamente definidos, por exemplo:

``` text
/api/users/register
```

ou o endpoint equivalente adotado pelo projeto.

A ideia é utilizar uma whitelist explícita de endpoints públicos.

### Exemplo conceitual

Em vez de:

``` java
.requestMatchers("/api/users/**").permitAll()
```

preferir algo como:

``` java
.requestMatchers("/api/users/register").permitAll()
```

Os demais endpoints de usuário devem permanecer protegidos.

## Motivação

Uma regra como:

``` text
/api/users/**
```

pode liberar futuramente novos endpoints sem que alguém perceba que eles
também ficaram públicos.

Por exemplo, se futuramente forem adicionados:

``` text
GET /api/users/profile
PUT /api/users/profile
DELETE /api/users/{id}
```

eles poderiam acabar herdando a permissão pública.

Isso cria uma **superfície de ataque desnecessariamente ampla**.

## Benefício

A segurança passa a seguir o princípio:

> "Tudo é protegido por padrão; somente endpoints explicitamente
> públicos são liberados."

Essa é uma decisão mais fácil de defender tecnicamente em uma
entrevista.

## Prioridade

🔴 **Alta**

------------------------------------------------------------------------

# 4. 🔴 Reforçar ownership diretamente na persistência

## Situação identificada

O projeto já possui uma preocupação correta com ownership.

Por exemplo, a busca de tarefa utiliza uma lógica equivalente a:

``` java
repositoryPort
    .findById(taskId)
    .filter(task -> currentUserId.equals(task.getOwnerId()))
```

O `DeleteTaskService` também verifica o proprietário antes da exclusão.

A regra está correta do ponto de vista funcional, mas pode ser
melhorada.

## Proposta

Criar operações de persistência que já considerem o usuário
proprietário.

Por exemplo:

``` java
Optional<Task> findByIdAndOwnerId(
    UUID taskId,
    UUID ownerId
);
```

e:

``` java
boolean deleteByIdAndOwnerId(
    UUID taskId,
    UUID ownerId
);
```

No adapter de persistência, essas informações seriam convertidas para
consultas que contenham as duas condições.

Conceitualmente:

``` sql
SELECT ...
FROM tasks
WHERE id = :taskId
  AND owner_id = :ownerId
```

e:

``` sql
DELETE FROM tasks
WHERE id = :taskId
  AND owner_id = :ownerId
```

## Motivação

A regra de segurança deixa de depender apenas de uma verificação feita
no service.

Isso traz duas vantagens:

1.  reduz a possibilidade de esquecer a verificação de ownership em
    algum fluxo futuro;
2.  faz a própria operação de persistência respeitar a fronteira de
    propriedade.

Além disso, no caso de exclusão, evita o padrão:

``` text
SELECT
↓
verifica owner
↓
DELETE
```

quando a operação poderia ser realizada diretamente como:

``` text
DELETE WHERE id AND owner_id
```

## Exemplo de fluxo

Em vez de:

``` java
findById(taskId)
    .filter(owner)
    .ifPresent(task -> deleteById(taskId));
```

usar:

``` java
boolean deleted =
    repository.deleteByIdAndOwnerId(taskId, currentUserId);
```

A aplicação pode então decidir o comportamento quando
`deleted == false`.

## Benefício arquitetural

A regra de acesso passa a ficar mais próxima da operação que
efetivamente altera os dados.

Isso também facilita demonstrar em entrevista que o sistema foi pensado
para evitar **IDOR (Insecure Direct Object Reference)**.

## Prioridade

🔴 **Alta**

------------------------------------------------------------------------

# 5. 🔴 Ampliar os testes de segurança JWT

## Situação identificada

O projeto já possui testes para:

-   `JwtAuthenticationFilter`;
-   `JwtTokenGeneratorAdapter`;
-   autenticação;
-   segurança;
-   integração.

Isso é positivo.

A proposta é ampliar a matriz de cenários negativos.

## Proposta

Adicionar testes para:

### Token expirado

``` text
JWT expirado
→ acesso negado
```

### Token inválido

``` text
JWT com assinatura inválida
→ acesso negado
```

### Token malformado

``` text
Authorization: Bearer abc
→ acesso negado
```

### Authorization ausente

``` text
sem Authorization header
→ acesso negado
```

### Subject ausente

Token válido criptograficamente, mas sem o identificador esperado:

``` text
sub = null
→ acesso negado
```

### Subject inválido

Por exemplo:

``` text
sub = "abc"
```

quando o sistema espera UUID.

Resultado esperado:

``` text
acesso negado
```

### Token de outro usuário

Garantir que a identidade extraída do JWT seja corretamente utilizada no
controle de ownership.

## Testes de autorização

Também é interessante garantir explicitamente:

``` text
Usuário A cria tarefa
Usuário B tenta consultar
→ 404/negação conforme contrato da API
```

e:

``` text
Usuário A cria tarefa
Usuário B tenta excluir
→ operação não permitida
```

## Motivação

Autenticação e autorização são pontos críticos do projeto.

Não basta testar:

``` text
JWT válido → acesso permitido
```

É importante testar também:

``` text
JWT inválido → acesso negado
JWT expirado → acesso negado
JWT adulterado → acesso negado
usuário errado → acesso negado
```

Isso transforma a segurança em uma característica verificável do
sistema, e não apenas em configuração.

## Benefício

Maior confiança contra regressões e melhor demonstração de maturidade
técnica.

## Prioridade

🔴 **Alta**

------------------------------------------------------------------------

# 6. 🔴 Ajustar exposição do Actuator em produção

## Situação identificada

A configuração atual utiliza:

``` yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info
```

e:

``` yaml
management:
  endpoint:
    health:
      show-details: always
```

## Proposta

Manter o endpoint de health disponível para infraestrutura, mas evitar
exposição desnecessária de detalhes em produção.

Uma possibilidade:

``` yaml
management:
  endpoint:
    health:
      show-details: never
```

ou utilizar:

``` yaml
show-details: when_authorized
```

dependendo da estratégia de autenticação adotada.

## Motivação

Informações detalhadas de health podem revelar detalhes sobre
componentes internos da aplicação e suas dependências.

Em desenvolvimento isso pode ser extremamente útil.

Em produção, entretanto, a regra deve ser:

> expor somente o necessário.

## Sugestão de separação

### Desenvolvimento

Mais informações:

``` yaml
show-details: always
```

### Produção

Mais restritivo:

``` yaml
show-details: never
```

## Benefício

Redução da exposição de informações internas sem perder a capacidade de
monitorar a aplicação.

## Prioridade

🔴 **Alta**

------------------------------------------------------------------------

# 7. 🟡 Endurecer a configuração de CORS

## Situação identificada

A configuração possui:

``` yaml
allowed-headers: "*"
```

O projeto já possui configuração de CORS por ambiente, o que é positivo.

## Proposta

Avaliar quais headers realmente são utilizados pela aplicação e declarar
somente os necessários.

Por exemplo, conceitualmente:

``` text
Authorization
Content-Type
Accept
```

em vez de permitir qualquer header.

## Motivação

`*` é conveniente durante desenvolvimento, mas uma configuração mais
restritiva é preferível quando os requisitos reais são conhecidos.

Isso segue o princípio:

> permitir somente o necessário.

## Observação

Essa alteração não precisa ser tratada como uma falha crítica.

É principalmente uma melhoria de hardening.

## Prioridade

🟡 **Média**

------------------------------------------------------------------------

# 8. 🟡 Melhorar o pipeline CI/CD

## Situação identificada

O projeto já possui GitHub Actions.

A proposta é garantir que o pipeline represente uma sequência completa
de validação.

## Proposta

Um pipeline ideal para este projeto poderia seguir aproximadamente:

``` text
Checkout
   ↓
Setup Java 21
   ↓
Compile
   ↓
Unit Tests
   ↓
Integration Tests
   ↓
Architecture Tests
   ↓
JaCoCo
   ↓
Sonar
   ↓
Docker Build
   ↓
Docker Image Validation
   ↓
Publish
```

## Motivação

Quanto mais regras arquiteturais e testes o projeto possui, mais
importante é impedir que uma alteração quebre essas garantias.

Por exemplo:

``` text
developer altera código
↓
testes passam
↓
ArchUnit falha
↓
pipeline bloqueia merge
```

Isso transforma arquitetura e qualidade em **quality gates**, e não
apenas em boas intenções.

## Sugestão

Caso Sonar já esteja sendo utilizado, estabelecer thresholds coerentes
para:

-   cobertura;
-   bugs;
-   vulnerabilidades;
-   code smells;
-   duplicação.

## Benefício

Aumenta a qualidade das entregas e aproxima o projeto de um fluxo
utilizado em ambientes profissionais.

## Prioridade

🟡 **Média**

------------------------------------------------------------------------

# 9. 🟡 Ampliar testes de integração

## Situação identificada

O projeto já possui:

-   testes unitários;
-   testes de integração;
-   Testcontainers;
-   testes de controllers;
-   testes de repository;
-   testes de segurança;
-   ArchUnit.

A base de testes é um dos pontos fortes do projeto.

## Proposta

Ampliar principalmente os cenários de segurança e comportamento entre
camadas.

### Exemplos

``` text
Usuário A → cria tarefa
Usuário A → consulta tarefa
Usuário B → consulta tarefa de A
Usuário B → tenta alterar tarefa de A
Usuário B → tenta excluir tarefa de A
```

Também testar:

``` text
paginação
ordenação
filtros
status inválido
transição inválida
recurso inexistente
JWT inválido
JWT expirado
```

## Motivação

Testes unitários demonstram comportamento isolado.

Testes de integração demonstram que:

``` text
HTTP
→ Controller
→ Application
→ Domain
→ Port
→ Adapter
→ PostgreSQL
```

funciona corretamente como sistema.

## Benefício

Aumenta a confiança no comportamento real da aplicação.

## Prioridade

🟡 **Média**

------------------------------------------------------------------------

# 10. 🟢 Adicionar observabilidade

## Situação identificada

O projeto já possui Actuator.

Isso fornece uma boa base para evolução da observabilidade.

## Proposta

Em uma próxima etapa, considerar:

-   correlation/request ID;
-   logs estruturados;
-   métricas;
-   tempo de resposta;
-   quantidade de erros;
-   métricas de autenticação;
-   métricas de operações de tarefas.

## Exemplo

Uma requisição poderia carregar:

``` text
X-Request-ID: 8f7d...
```

E esse identificador aparecer nos logs:

``` text
requestId=8f7d...
userId=...
operation=CREATE_TASK
status=SUCCESS
duration=42ms
```

## Motivação

Quando o sistema cresce, uma das primeiras dificuldades operacionais é
responder:

> "O que aconteceu com essa requisição?"

Um request/correlation ID facilita rastrear uma operação através dos
diferentes componentes.

## Benefício

Melhor diagnóstico de problemas e demonstração de preocupação com
operação, e não apenas desenvolvimento.

## Prioridade

🟢 **Evolução**

------------------------------------------------------------------------

# 11. 🟢 Revisar pontos de possível overengineering

## Situação identificada

O projeto possui uma arquitetura relativamente completa para o tamanho
atual do domínio.

Isso é positivo para aprendizado e portfólio, mas existe um risco:

> a arquitetura ficar mais complexa do que o problema exige.

## Proposta

Não remover a arquitetura hexagonal.

Em vez disso, revisar continuamente se cada abstração possui uma
justificativa.

Para cada componente, perguntar:

``` text
Qual problema essa abstração resolve?
```

e:

``` text
Se eu removesse essa abstração, o que perderia?
```

## Exemplos de pontos a observar

-   quantidade de interfaces;
-   quantidade de adapters;
-   quantidade de DTOs;
-   quantidade de services;
-   abstrações que possuem apenas uma implementação sem necessidade
    real;
-   configurações que poderiam ser mais simples.

## Motivação

Em uma entrevista técnica, é possível que o entrevistador pergunte:

> "Por que você criou essa interface?"

A melhor resposta não é:

> "Porque estou usando arquitetura hexagonal."

A melhor resposta é:

> "Essa abstração existe para separar X de Y e permitir Z."

## Benefício

A arquitetura deixa de ser apenas estrutural e passa a ser
**intencional**.

## Prioridade

🟢 **Evolução**

------------------------------------------------------------------------

# 12. Melhorias que NÃO recomendo fazer agora

É importante também definir o que **não** precisa entrar no projeto
neste momento.

O objetivo não deve ser adicionar tecnologia apenas para aumentar a
quantidade de itens no README.

Não vejo necessidade imediata de adicionar, somente para "mostrar
conhecimento":

-   Kafka;
-   Redis;
-   Elasticsearch;
-   Kubernetes;
-   microsserviços;
-   CQRS;
-   Event Sourcing;
-   GraphQL;
-   service mesh;
-   arquitetura distribuída complexa.

## Motivação

O TaskManager já possui complexidade suficiente para demonstrar:

``` text
Java
Spring Boot
Arquitetura
Domínio
Segurança
Banco
Testes
Docker
CI/CD
Documentação
```

Adicionar tecnologias sem necessidade pode prejudicar a clareza
arquitetural.

A pergunta principal deve ser:

> "Qual problema real essa tecnologia resolve?"

------------------------------------------------------------------------

# 13. Ordem recomendada de implementação

Sugiro implementar as alterações nesta ordem:

## Etapa 1 --- Segurança

### 1. Restringir `/api/users/**`

Definir explicitamente quais endpoints são públicos.

### 2. Ownership

Criar operações como:

``` java
findByIdAndOwnerId()
deleteByIdAndOwnerId()
```

### 3. Testes JWT

Adicionar cenários:

``` text
expirado
inválido
malformado
sem subject
subject inválido
usuário diferente
```

### 4. Actuator

Separar comportamento de desenvolvimento e produção.

### 5. CORS

Reduzir headers permitidos ao necessário.

------------------------------------------------------------------------

# 14. Etapa 2 --- Qualidade

Depois das alterações de segurança:

``` text
Unit Tests
↓
Integration Tests
↓
Security Tests
↓
ArchUnit
↓
JaCoCo
```

Garantir que tudo continue passando.

------------------------------------------------------------------------

# 15. Etapa 3 --- CI/CD

Ajustar o pipeline para bloquear alterações que violem:

-   testes;
-   cobertura;
-   arquitetura;
-   qualidade estática;
-   build Docker.

------------------------------------------------------------------------

# 16. Etapa 4 --- Observabilidade

Somente depois das etapas anteriores:

-   request ID;
-   logs estruturados;
-   métricas;
-   indicadores de aplicação.

Essa etapa é importante, mas não deve atrasar as correções de segurança.

------------------------------------------------------------------------

# 17. Checklist final

## Segurança

-   [ ] Restringir `/api/users/**`
-   [ ] Criar whitelist explícita de endpoints públicos
-   [ ] Implementar `findByIdAndOwnerId`
-   [ ] Implementar `deleteByIdAndOwnerId`
-   [ ] Validar ownership no banco/persistência
-   [ ] Testar JWT expirado
-   [ ] Testar JWT inválido
-   [ ] Testar JWT malformado
-   [ ] Testar JWT sem subject
-   [ ] Testar subject inválido
-   [ ] Testar acesso entre usuários
-   [ ] Revisar Actuator em produção
-   [ ] Revisar CORS

## Testes

-   [ ] Unit tests
-   [ ] Integration tests
-   [ ] Security tests
-   [ ] Ownership tests
-   [ ] Architecture tests
-   [ ] JaCoCo

## CI/CD

-   [ ] Compile
-   [ ] Unit tests
-   [ ] Integration tests
-   [ ] ArchUnit
-   [ ] JaCoCo
-   [ ] Sonar
-   [ ] Docker build
-   [ ] Quality gate
-   [ ] Publish

## Observabilidade

-   [ ] Request ID
-   [ ] Logs estruturados
-   [ ] Métricas
-   [ ] Monitoramento de erros
-   [ ] Monitoramento de latência

## Arquitetura

-   [ ] Revisar abstrações
-   [ ] Justificar interfaces
-   [ ] Evitar complexidade desnecessária
-   [ ] Documentar decisões arquiteturais importantes

------------------------------------------------------------------------

# 18. Resultado esperado

Após essas alterações, o projeto tende a ficar mais forte principalmente
em quatro dimensões:

### Segurança

Menor superfície de ataque e ownership mais robusto.

### Arquitetura

Regras de negócio e fronteiras entre camadas mais claras.

### Qualidade

Mais cenários críticos protegidos por testes automatizados.

### Profissionalização

CI/CD, observabilidade e configuração de produção mais próximas de um
ambiente real.

------------------------------------------------------------------------

# 19. Critério para decidir novas alterações

Antes de adicionar qualquer nova tecnologia, biblioteca ou camada,
aplicar estas quatro perguntas:

1.  **Qual problema isso resolve?**
2.  **Por que a solução atual não é suficiente?**
3.  **Qual custo de manutenção isso adiciona?**
4.  **Como vou testar essa decisão?**

Se a resposta não for clara, provavelmente a alteração não é necessária.

------------------------------------------------------------------------

# 20. Conclusão

O objetivo destas propostas não é transformar o TaskManager em um
projeto artificialmente complexo.

A direção recomendada é o contrário:

> **Manter a arquitetura atual, fortalecer os pontos críticos e tornar
> cada decisão técnica justificável.**

O projeto já possui uma base suficientemente forte para funcionar como
demonstração de conhecimentos em Java, Spring Boot, arquitetura
hexagonal, segurança, persistência, testes e engenharia de software.

As próximas alterações devem priorizar **segurança e robustez**, depois
**qualidade e automação**, e somente posteriormente funcionalidades ou
tecnologias adicionais.
