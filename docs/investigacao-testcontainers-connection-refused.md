# Investigação: `Connection refused` entre `TaskControllerIT` e `UserControllerIT`

**Data:** 26/07/2026 **Sintoma inicial:** `UserControllerIT` falhando com `500 Internal Server Error` em todos os testes
que tocavam o banco, ao rodar `mvn verify`. **Causa raiz:** uso de `@Container`/`@Testcontainers` (gerenciamento de
ciclo de vida pelo JUnit 5) em vez do padrão *Singleton Container* — o JUnit encerrava o container Postgres ao final de
`TaskControllerIT`, e o `ApplicationContext` cacheado do Spring Test (compartilhado com `UserControllerIT`, por ter
configuração idêntica) continuava referenciando o pool de conexões antigo.

---

## Linha do tempo da investigação

### 1. Sintoma reportado

Rodando `mvn verify`, `UserControllerIT` falhava com `500` em todos os testes de sucesso (os que efetivamente gravam no
banco). Os testes de validação (`WithError`) passavam normalmente.

**Primeira tentativa de correção, feita antes da investigação:** adicionar `@DirtiesContext` na classe. Isso "resolveu"
o sintoma (forçando o Spring a recriar o `ApplicationContext` do zero), mas sem explicar a causa — e com custo de
performance (contexto inteiro recriado a cada execução).

**Decisão:** investigar a causa raiz em vez de aceitar `@DirtiesContext` como solução definitiva.

---

### 2. Coletando a stack trace real

Comando usado para capturar o log completo em arquivo (Windows/PowerShell):

```powershell
mvn verify 2>&1 | Out-File -Encoding utf8 build-log.txt
```

> Nota: a primeira tentativa usou `>` puro do PowerShell, que grava em UTF-16LE por padrão — exigiu conversão manual
> (`iconv -f UTF-16LE -t UTF-8`) para conseguir ler o arquivo. O uso de `-Encoding utf8` explícito nas tentativas
> seguintes evitou esse problema.

**Achado:** a stack trace real revelou:

```
org.springframework.transaction.CannotCreateTransactionException: Could not open JPA EntityManager for transaction
Caused by: ... Connection to localhost:PORT refused
Caused by: java.net.ConnectException: Connection refused: getsockopt
```

Cada teste falho esperava exatamente ~30 segundos (o timeout padrão do HikariCP) antes de desistir — indicando que,
durante toda a tentativa, nada estava escutando na porta em questão (não era lentidão, era ausência real de serviço).

---

### 3. Hipótese 1 (descartada): múltiplos forks/JVMs do Maven

**Verificação:** inspeção do `pom.xml`, seção `maven-failsafe-plugin` — sem `forkCount`, `reuseForks` ou `parallel`
configurados, portanto usando o padrão do Maven (`forkCount=1`, `reuseForks=true`, uma única JVM para toda a suíte de
integração).

**Confirmação adicional:** o PID do processo Java (`25644`, depois `14560`, depois `17480` em execuções diferentes)
permanecia **idêntico** do início ao fim de cada execução — descartando a hipótese de múltiplos forks.

**Conclusão:** hipótese descartada.

---

### 4. Hipótese 2 (descartada): dois containers rodando simultaneamente

**Verificação:** monitoramento em tempo real do Docker durante a execução, usando dois terminais em paralelo.

Terminal 1 (build):

```powershell
mvn verify 2>&1 | Out-File -Encoding utf8 build-log.txt
```

Terminal 2 (monitoramento, polling a cada 5s):

```powershell
while ($true) {
    docker ps -a --filter "ancestor=postgres:16" --format "table {{.ID}}\t{{.Ports}}\t{{.Status}}\t{{.CreatedAt}}"
    Write-Host "---"
    Start-Sleep -Seconds 5
}
```

**Achado:** em nenhum momento dois containers apareceram simultaneamente. Em vez disso, uma sequência de containers
**distintos** (IDs diferentes) nascendo e morrendo um após o outro ao longo da mesma execução — um deles vivendo ~3
minutos (coincidindo com a janela de falha), sendo substituído por outro logo em seguida.

**Conclusão:** hipótese descartada — não havia dois containers coexistindo, mas sim substituição sequencial.

---

### 5. Hipótese 3 (descartada): Ryuk derrubando o container por perda de heartbeat

O Testcontainers usa um container auxiliar (**Ryuk**) que monitora a conexão com a JVM cliente; se essa conexão for
interrompida, o Ryuk assume que o processo "morreu" e derruba os containers associados, para evitar containers órfãos.
Em ambientes Windows/WSL2, instabilidades na rede virtual interna podem causar esse tipo de desconexão falsa.

**Correção testada, baseada nessa hipótese:**

```java
static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
        .withReuse(true);
```

Habilitado também globalmente em `%USERPROFILE%\.testcontainers.properties`:

```properties
testcontainers.reuse.enable=true
```

Containers em modo *reuse* não são registrados no Ryuk — se a causa fosse essa, o problema desapareceria.

**Resultado:** o bug se reproduziu **de forma idêntica**, mesmo com essa mudança — mesmos 7 testes falhando, mesmo
padrão de tempo (~210s).

**Conclusão:** hipótese descartada. A reprodução idêntica também revelou um fato importante: o problema era
**determinístico** (sempre no mesmo ponto exato), não uma falha intermitente de rede — o que já enfraquecia a teoria do
Ryuk antes mesmo do teste confirmar.

---

### 6. Isolando a variável: `TaskControllerIT` é a causa?

**Teste A — `UserControllerIT` sozinho, sem nada rodando antes:**

```powershell
mvn verify "-Dit.test=UserControllerIT" 2>&1 | Out-File -Encoding utf8 build-log-isolated.txt
```

**Resultado:** `BUILD SUCCESS`, 20/20 testes passando.

**Teste B — `TaskControllerIT` seguido de `UserControllerIT`, isolando as outras classes:**

```powershell
mvn verify "-Dit.test=TaskControllerIT,UserControllerIT" 2>&1 | Out-File -Encoding utf8 build-log-both.txt
```

**Resultado:** falha reproduzida, exatamente no mesmo padrão da suíte completa.

**Conclusão:** confirmado — o problema é causado especificamente pela **execução de `TaskControllerIT` imediatamente
antes de `UserControllerIT`**, não por qualquer outra classe da suíte, nem por `UserControllerIT` isoladamente.

---

### 7. Causa raiz identificada

Analisando o log da Etapa 6 (Teste B) em detalhe, com timestamps e PIDs:

- O container usado por `TaskControllerIT` (`84b94b6...`, porta `49648`) funcionou perfeitamente durante toda a execução
  dessa classe (~30s).
- **Assim que `TaskControllerIT` termina**, um **novo** container é criado (`f896a53...`, porta `49670`) — mesmo PID de
  JVM, confirmando que não é um novo fork.
- O novo container sobe com sucesso — mas os testes de `UserControllerIT` continuam falhando com `Connection refused`.

**Explicação:** a documentação do Testcontainers (ver referência abaixo) esclarece que, ao usar `@Container` em um campo
`static`, o **JUnit 5** (via extensão `@Testcontainers`) assume o controle do ciclo de vida do container, incluindo
pará-lo automaticamente ao final da execução da (s) classe (s) que o utilizam. A garantia de "container único
compartilhado" desse padrão é confiável **dentro de uma mesma classe** — mas não há garantia documentada de que o mesmo
container sobreviva **entre classes de teste diferentes**, mesmo que herdem o campo de uma superclasse comum.

Como `TaskControllerIT` e `UserControllerIT` têm configuração de contexto Spring **idêntica** (`@SpringBootTest` +
`@AutoConfigureMockMvc`), o Spring Test **reaproveita o mesmo `ApplicationContext`** do cache entre as duas classes —
incluindo o `HikariPool` (pool de conexões) já construído, que continuava apontando para a porta do container antigo
(`49648`), agora encerrado. O container novo (`49670`) nunca chegou a ser usado, porque o Spring nunca recriou o
`DataSource`.

---

### 8. Correção aplicada — padrão "Singleton Container"

Documentado oficialmente pelo Testcontainers como a forma correta de compartilhar um único container entre **múltiplas
classes de teste**: iniciar o container manualmente, sem delegar o ciclo de vida ao JUnit.

**Antes:**

```java

@Testcontainers
public abstract class IntegrationTestSupport {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16");
}
```

**Depois:**

```java
public abstract class IntegrationTestSupport {

    @ServiceConnection
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withReuse(true);

    static {
        postgres.start();
    }
}
```

**O que mudou e por quê:**

- Removidas `@Testcontainers` (classe) e `@Container` (campo) — elimina o gerenciamento de ciclo de vida pelo JUnit, que
  era a causa da parada prematura entre classes.
- Bloco estático (`static { postgres.start(); }`) garante que o container sobe uma única vez, no carregamento da classe,
  antes de qualquer contexto Spring precisar dele — e só é encerrado quando a **JVM inteira** termina (via shutdown hook
  padrão do Testcontainers/Ryuk).
- `@ServiceConnection` foi mantida — é o mecanismo do **Spring Boot** (não do JUnit) que lê os dados de conexão do
  container e os registra como propriedades do `DataSource`. Ela não depende de `@Container`/`@Testcontainers` para
  funcionar, apenas exige que o container já esteja em execução no momento em que o contexto for montado — o que o bloco
  estático garante.
- `withReuse(true)` foi mantido como reforço adicional (não era a causa raiz, mas não é prejudicial).

---

### 9. Validação da correção

```powershell
mvn verify 2>&1 | Out-File -Encoding utf8 build-log.txt
```

**Resultado:** `BUILD SUCCESS`, 52 testes de integração, 0 falhas — incluindo a transição `TaskControllerIT` →
`UserControllerIT`, que antes falhava consistentemente e agora completa em menos de 1 segundo (antes: ~210s de timeouts
acumulados).

---

## Comandos de referência usados na investigação

| Comando                                                                                                          | Propósito                                                                                     |
|------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------|
| `mvn verify 2>&1 \| Out-File -Encoding utf8 build-log.txt`                                                       | Capturar log completo de build em UTF-8, evitando o problema de encoding UTF-16 do PowerShell |
| `mvn verify "-Dit.test=NomeDaClasse"`                                                                            | Rodar uma única classe de teste de integração via Failsafe, isolando variáveis                |
| `mvn verify "-Dit.test=ClasseA,ClasseB"`                                                                         | Rodar duas classes específicas, na ordem informada                                            |
| `docker ps -a --filter "ancestor=postgres:16" --format "table {{.ID}}\t{{.Ports}}\t{{.Status}}\t{{.CreatedAt}}"` | Inspecionar containers Postgres ativos/recentes no momento da execução                        |

---

## Documentação oficial consultada

- **Testcontainers — Manual JUnit 5 lifecycle (`@Container`/
  `@Testcontainers`):** [https://java.testcontainers.org/test_framework_integration/junit_5/](https://java.testcontainers.org/test_framework_integration/junit_5/) —
  descreve o comportamento de containers `static` vs. de instância, e as garantias (e limites) de compartilhamento entre
  métodos/classes de teste.
- **Testcontainers — Singleton Containers
  pattern:** [https://java.testcontainers.org/test_framework_integration/manual_lifecycle_control/](https://java.testcontainers.org/test_framework_integration/manual_lifecycle_control/) —
  padrão oficial recomendado para compartilhar um único container entre múltiplas classes de teste, controlando o ciclo
  de vida manualmente em vez de delegar ao framework de teste.
- **Testcontainers — Reusable
  Containers:** [https://java.testcontainers.org/features/reuse/](https://java.testcontainers.org/features/reuse/) —
  mecanismo `withReuse(true)` e a propriedade `testcontainers.reuse.enable=true`, incluindo a explicação de que
  containers reutilizáveis não são gerenciados pelo Ryuk.
- **Spring Boot — Testcontainers at development time (
  `@ServiceConnection`):** [https://docs.spring.io/spring-boot/reference/testing/testcontainers.html](https://docs.spring.io/spring-boot/reference/testing/testcontainers.html) —
  comportamento da anotação `@ServiceConnection` e sua independência do ciclo de vida gerenciado pelo JUnit.
- **Spring Framework — Context caching in the TestContext
  framework:** [https://docs.spring.io/spring-framework/reference/testing/testcontext-framework/ctx-management/caching.html](https://docs.spring.io/spring-framework/reference/testing/testcontext-framework/ctx-management/caching.html) —
  explica como o Spring Test reaproveita `ApplicationContext`s entre classes de teste com configuração idêntica,
  mecanismo central para entender por que o `HikariPool` antigo persistiu entre `TaskControllerIT` e `UserControllerIT`.

---

## Lições para o projeto

1. **`@Container`/`@Testcontainers` em campo `static` não garante, por si só, compartilhamento entre classes de teste
   diferentes** — apenas dentro da mesma classe. Para compartilhamento real entre múltiplas classes (o cenário mais
   comum em suítes de integração maiores), o padrão correto é iniciar o container manualmente.
2. **Sintomas de conexão recusada em testes de integração devem ser isolados por classe antes de qualquer outra
   investigação** — rodar `-Dit.test=ClasseX` isoladamente e depois em combinação (`-Dit.test=ClasseA,ClasseB`) é o
   jeito mais rápido de confirmar se o problema depende de ordem de execução.
3. **Soluções que "escondem" o sintoma (`@DirtiesContext`) têm custo real de performance e não substituem entender a
   causa raiz** — vale investir tempo na investigação antes de aceitar esse tipo de correção como definitiva.
