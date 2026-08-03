# API Guide

> ⚠️ **Todo endpoint de `Task` exige o header `X-User-Id`** (um UUID de usuário já registrado). Ele identifica o
> "usuário atual" enquanto a autenticação real (JWT) não é implementada — sem ele, a API responde `400 Bad Request`.
> Ver [decisões de design](architecture.md#decisões-de-design) para o motivo.

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
    "email": "sergio@exemplo.com"
}
```

**Resposta:** `201 Created` (com header `Location` apontando para o recurso criado)

```json
{
    "id": "1911d14f-85b1-47a7-a414-f25dca18c1c2",
    "name": "Sergio Bezerra da Silva",
    "email": "sergio@exemplo.com",
    "createdAt": "2026-07-25T18:45:32.436477200Z"
}
```

**Erros possíveis:**

- `400 Bad Request` quando `name`/`email` estão ausentes, `name` fora do intervalo de 3-160 caracteres ou contendo
  caracteres além de letras/espaços/hífen/apóstrofo (ex: números), ou `email` com formato inválido.
- `409 Conflict` quando o `email` já está em uso por outro usuário.

> O `id` retornado aqui é o valor a ser usado no header `X-User-Id` em todos os endpoints de `Task`.

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
prazo inválido, falha de validação de formato, ou header `X-User-Id` ausente/malformado.

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
usuário** (mesma resposta nos dois casos, por segurança). `400 Bad Request` quando o `id` não é um UUID válido, ou o
header `X-User-Id` está ausente/malformado.

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

**Resposta:** `200 OK` — contém apenas tarefas do usuário identificado pelo header `X-User-Id`.

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
`?sortField=NAOEXISTE`), ou o header `X-User-Id` está ausente/malformado.

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
  quando o valor enviado não corresponde a nenhum status válido (corpo JSON malformado), ou o header `X-User-Id` está
  ausente/malformado.

</details>

<details>
<summary>🗑️ Remover tarefa</summary>

**DELETE** `/api/tasks/{id}`

**Resposta:** `204 No Content` — sempre, independentemente de o `id` existir, pertencer a outro usuário, ou não existir
(operação idempotente; ver [decisão de design](architecture.md#decisões-de-design)).

**Erros possíveis:** `400 Bad Request` quando o `id` informado não é um UUID válido, ou o header `X-User-Id` está
ausente/malformado.

</details>

---

> A documentação interativa completa está disponível em `/swagger-ui/index.html` com a aplicação em execução.

Ver também: [Architecture](architecture.md) · [Testing Strategy](testing-strategy.md) · [Roadmap](roadmap.md)