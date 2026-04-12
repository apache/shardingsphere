# Contract: Execute Query Schema Semantics

## Purpose

定义 `execute_query` 中 `database` 与 `schema` 的正式产品语义，
并把 capability、metadata 与执行契约绑定到同一套解释规则上。

## Tool Signature

```text
execute_query(database, schema?, sql, max_rows?, timeout_ms?)
```

## Request Fields

### `database`

- 必填
- 表示 MCP 对外 logical database 标识
- 是每次执行的唯一强边界

### `schema`

- 可选
- 表示 database 内部命名空间 hint
- 主要影响未限定对象名的目标命名空间意图
- 不是第二个强路由键
- 不是第二个鉴权边界

### `sql`

- 必填
- 只允许单条 SQL
- 当 SQL 自身包含显式限定名时，SQL 文本优先决定对象解析关系

## Capability Coupling

调用方在使用 `schema` 之前，应先读取：

```text
get_capabilities(database)
```

并解释以下两个字段：

- `defaultSchemaSemantics`
- `schemaExecutionSemantics`

### `defaultSchemaSemantics`

描述 metadata/discovery 侧的统一 schema 语义。

当前已有值：

- `NATIVE_SCHEMA`
- `DATABASE_AS_SCHEMA`

### `schemaExecutionSemantics`

描述 `execute_query.schema` 的执行语义。

V1 值：

- `FIXED_TO_DATABASE`
- `BEST_EFFORT`

## Schema Semantics Matrix

### `FIXED_TO_DATABASE`

#### Meaning

- 公共 contract 中存在 `schema` 维度
- 但 execution boundary 仍固定在 `database`
- request-level `schema` 不能被解释为独立切换执行命名空间

#### Typical Use

- MySQL-like 或其他 `DATABASE_AS_SCHEMA` 数据库
- metadata 会暴露统一 `schema` 名称以保持 URI 与对象模型稳定

#### Caller Guidance

- 可以直接省略 `schema`
- 如果传入 `schema`，应把它理解为与 metadata 对齐的 public namespace label
- 不能因为传入 `schema` 就假设 MCP 承诺了独立 schema switch

### `BEST_EFFORT`

#### Meaning

- 数据库具有原生 schema 概念
- MCP 可以尝试把 request-level `schema` 作为未限定对象名的命名空间 hint
- 但不承诺 strict guarantee

#### Caller Guidance

- 可以省略 `schema`，让数据库默认 schema / `search_path` 生效
- 需要 deterministic targeting 时，应使用 SQL 自身显式限定名

## Metadata Alignment Rules

- 当数据库没有独立 schema 概念时，
  MCP 对外暴露的 `schema` 名称使用 logical `database` 名称
- 这个名字是 MCP 公共命名空间的一部分，
  不自动等价于底层 SQL 可直接使用的真实 qualifier
- metadata 中看到的 `schema` 名称必须与 capability 的 `schemaExecutionSemantics`
  一起解读

## Request Resolution Rules

1. 先按 `database` 选择唯一 logical database  
2. 再按 `schemaExecutionSemantics` 解释 request-level `schema`  
3. 若 SQL 已显式限定对象名，则 SQL 文本优先  
4. `schema` 不得把请求扩展为跨 `database` 执行  

## Non-Requirements

- 本 contract 不要求系统因为 `schema` 无法严格生效而 fail-fast
- 本 contract 不要求系统自动重写 SQL 以补齐 schema 限定名
- 本 contract 不要求 metadata 暴露出来的统一 schema 名称必然等同于底层 SQL qualifier

## Example Responses

### MySQL-like capability

```json
{
  "database": "logic_db",
  "databaseType": "MySQL",
  "defaultSchemaSemantics": "DATABASE_AS_SCHEMA",
  "schemaExecutionSemantics": "FIXED_TO_DATABASE"
}
```

### PostgreSQL-like capability

```json
{
  "database": "logic_db",
  "databaseType": "PostgreSQL",
  "defaultSchemaSemantics": "NATIVE_SCHEMA",
  "schemaExecutionSemantics": "BEST_EFFORT"
}
```

## Reviewer Checklist

- `execute_query` tool descriptor 是否已把 `schema` 描述为 optional namespace hint
- capability response 是否新增 `schemaExecutionSemantics`
- no-native-schema 数据库是否把空 schema 归一化为 logical `database` 名称
- 文档是否仍然错误地把 `schema` 描述成 strict selector
