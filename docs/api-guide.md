# API Guide

> ⚠️ **Todo endpoint de `Task` exige autenticação** via header `Authorization: Bearer <token>` — obtido em [
`POST /api/auth/login`](#-autenticação). Sem um token válido, a API responde `401 Unauthorized`.
> Ver [decisões de design](architecture.md#decisões-de-design) para o histórico (esse mecanismo substituiu um header
> `X-User-Id` temporário usado antes do JWT estar implementado).

## 💓 Status da aplicação

<details>
<summary>💓 Health Check</summary>

**GET** `/actuator/health`

**Resposta:** `200 OK` — status agregado da aplicação, banco de dados e disco.

</details>

---

## 👤 Usuários

<details>
<summary>➕ Registrar um usuário</summary>

**POST** `/api/users`

```json
{
    "name": "Sergio Bezerra da Silva",
    "email": "sergio@exemplo.com",
    "password": "SenhaForte123!"
}
```

**Regras de senha:** mínimo 8 caracteres (máximo 72, limite do algoritmo BCrypt), com pelo menos uma letra maiúscula, um
número e um caractere especial.

**Resposta:** `201 Created` (com header `Location` apontando para o recurso criado)

```json
{
    "id": "1911d14f-85b1-47a7-a414-f25dca18c1c2",
    "name": "Sergio Bezerra da Silva",
    "email": "sergio@exemplo.com",
    "createdAt": "2026-07-25T18:45:32.436477200Z"
}
```

> A senha nunca é retornada em nenhuma resposta da API — nem em texto puro, nem como hash.

**Erros possíveis:**

- `400 Bad Request` quando `name`/`email`/`password` estão ausentes ou fora do formato exigido (ver regras de senha
  acima; `name` fora do intervalo de 3-160 caracteres ou contendo caracteres além de letras/espaços/hífen/apóstrofo;
  `email` com formato inválido).
- `409 Conflict` quando o `email` já está em uso por outro usuário.

</details>

---

## 🔐 Autenticação

<details>
<summary>🔑 Login</summary>

**POST** `/api/auth/login`

```json
{
    "email": "sergio@exemplo.com",
    "password": "SenhaForte123!"
}
```

**Resposta:** `200 OK`

```json
{
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "expiresAt": "2026-08-03T02:40:19.949398100Z",
    "userId": "ff593b42-f4fc-4466-bab7-c7513bbcd665"
}
```

O `token` retornado deve ser enviado no header `Authorization: Bearer <token>` em todo endpoint de `Task`. O token
expira em **1 hora** — não há renovação automática (refresh token); expirado, é necessário logar novamente.

**Erros possíveis:** `401 Unauthorized` quando o e-mail não existe **ou** a senha está incorreta — a API retorna sempre
a mesma mensagem genérica (`"Invalid email or password"`) nos dois casos, para não revelar quais e-mails estão
cadastrados no sistema.

</details>

<details>
<summary>🔵 Login com Google</summary>

**POST** `/api/auth/google`

```json
{
    "idToken": "eyJhbGciOiJSUzI1NiIsImtpZCI6..."
}
```

O `idToken` é obtido no **frontend**, via [Google Identity Services](https://developers.google.com/identity/gsi/web) — a
API nunca lida diretamente com credenciais do Google, apenas valida o token recebido.

**Resposta:** `200 OK` — mesmo formato do login local:

```json
{
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "expiresAt": "2026-08-06T16:05:49.166756800Z",
    "userId": "ae4bd18d-ba43-4b18-a4ad-291e61155fb8"
}
```

**Comportamento:**

- Se a conta Google já estiver vinculada a um usuário (login anterior), retorna o token para esse usuário.
- Se o e-mail da conta Google já pertence a um usuário registrado via senha (`LOCAL`), a conta Google é **vinculada
  automaticamente** a esse usuário existente — sem exigir confirmação adicional.
- Se não existir usuário nenhum com esse e-mail, um novo `User` é criado. O nome vem da conta Google; se o Google não
  fornecer um nome, é derivado a partir do e-mail (ex: `maria.silva@exemplo.com` → `"maria silva"`), com um nome
  genérico (`"Google User"`) como último recurso.

**Erros possíveis:**

- `401 Unauthorized` quando o token do Google é inválido, expirado, ou não foi emitido para este aplicativo
  (`title: "Invalid Google Token"`).
- `400 Bad Request` quando `idToken` está ausente/em branco.

> **Testando manualmente sem um frontend:** use
> o [Google OAuth 2.0 Playground](https://developers.google.com/oauthplayground/) com suas próprias credenciais (ícone de
> engrenagem ⚙️ → "Use your own OAuth credentials"), ou uma página HTML mínima com o script do Google Identity Services,
> servida em `http://localhost:4200` (mesma origem autorizada no Google Cloud Console) para gerar um ID Token real.

</details>

---

## 📋 Tarefas

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
prazo inválido, falha de validação de formato, ou token ausente/inválido/expirado.

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

**Erros possíveis:** `404 Not Found` (formato `application/problem+json`) quando o `id` não existe **ou pertence a outro
usuário** (mesma resposta nos dois casos, por segurança). `400 Bad Request` quando o `id` não é um UUID válido.
`401 Unauthorized` quando o token está ausente/inválido/expirado.

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

**Resposta:** `200 OK` — contém apenas tarefas do usuário identificado pelo token.

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
`?sortField=NAOEXISTE`). `401 Unauthorized` quando o token está ausente/inválido/expirado.

</details>

<details>
<summary>🔄 Alterar status de uma tarefa</summary>

**PATCH** `/api/tasks/{id}/status`

```json
{
    "status": "IN_PROGRESS"
}
```

**Transições válidas:** `TODO → IN_PROGRESS`, `TODO → CANCELLED`, `IN_PROGRESS → DONE`, `IN_PROGRESS → CANCELLED`.
`DONE` e `CANCELLED` são estados terminais — nenhuma transição é permitida a partir deles.

**Resposta:** `200 OK`

```json
{
    "id": "83509a61-0df4-4629-b172-0870f5190d37",
    "title": "Minha primeira tarefa",
    "description": "Descrição da tarefa",
    "status": "IN_PROGRESS",
    "priority": "HIGH",
    "dueDate": "2026-08-01T10:00:00",
    "createdAt": "2026-07-18T01:35:06.740981200Z",
    "updatedAt": "2026-07-19T14:20:11.120981200Z"
}
```

**Erros possíveis:**

- `404 Not Found` quando o `id` não existe **ou pertence a outro usuário** (mesma resposta nos dois casos).
- `400 Bad Request` quando a transição de status é inválida (ex: pular etapa), quando o campo `status` está ausente,
  quando o valor enviado não corresponde a nenhum status válido (corpo JSON malformado). `401 Unauthorized` quando o
  token está ausente/inválido/expirado.

</details>

<details>
<summary>🗑️ Remover tarefa</summary>

**DELETE** `/api/tasks/{id}`

**Resposta:** `204 No Content` — sempre, independentemente de o `id` existir, pertencer a outro usuário, ou não existir
(operação idempotente; ver [decisão de design](architecture.md#decisões-de-design)).

**Erros possíveis:** `400 Bad Request` quando o `id` informado não é um UUID válido. `401 Unauthorized` quando o token
está ausente/inválido/expirado.

</details>

<details>
<summary>📊 Dashboard (métricas agregadas)</summary>

**GET** `/api/tasks/dashboard`

**Resposta:** `200 OK` — métricas calculadas exclusivamente sobre as tarefas do usuário autenticado.

```json
{
    "totalTasks": 4,
    "countByStatus": {
        "TODO": 1,
        "IN_PROGRESS": 1,
        "DONE": 1,
        "CANCELLED": 1
    },
    "countByPriority": {
        "HIGH": 2,
        "MEDIUM": 1,
        "LOW": 1
    },
    "overdueCount": 0,
    "dueSoonCount": 1
}
```

**Definições:**

- `overdueCount`: tarefas com `dueDate` no passado **e** status não terminal (`TODO`/`IN_PROGRESS`) — uma tarefa `DONE`/
  `CANCELLED` nunca conta como atrasada, mesmo com prazo vencido.
- `dueSoonCount`: tarefas com `dueDate` entre agora e os próximos 7 dias, também restrito a status não terminal.
- `countByStatus`/`countByPriority`: só aparecem no mapa os status/prioridades que o usuário efetivamente possui — sem
  tarefas de um tipo, a chave correspondente simplesmente não existe no JSON (nunca `0` explícito).
- Sem nenhuma tarefa, todos os campos numéricos vêm `0` e os mapas vêm vazios (`{}`).

**Erros possíveis:** `401 Unauthorized` quando o token está ausente/inválido/expirado.

</details>

---

> A documentação interativa completa está disponível em `/swagger-ui/index.html` com a aplicação em execução.

Ver também: [Architecture](architecture.md) · [Testing Strategy](testing-strategy.md) · [Roadmap](roadmap.md)