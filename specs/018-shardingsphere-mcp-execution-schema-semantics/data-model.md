# Data Model: ShardingSphere MCP Execute Query Schema Semantics

## Core Domain Entities

### DatabaseCapability

- **Purpose**: 描述一个 logical database 的 metadata 与 execution 边界。
- **Fields**:
  - `database`
  - `databaseType`
  - `supportedObjectTypes`
  - `supportedStatementClasses`
  - `supportsTransactionControl`
  - `supportsSavepoint`
  - `defaultSchemaSemantics`
  - `schemaExecutionSemantics`
  - `supportsCrossSchemaSql`
  - `supportsExplainAnalyze`
- **Validation rules**:
  - `defaultSchemaSemantics` 与 `schemaExecutionSemantics` 必须同时存在。
  - 当 `defaultSchemaSemantics = DATABASE_AS_SCHEMA` 时，
    `schemaExecutionSemantics` 必须为 `FIXED_TO_DATABASE`。
  - `schemaExecutionSemantics` 只描述 `execute_query.schema` 的语义，
    不能取代 `database` 这个强边界。

### SchemaExecutionSemantics

- **Purpose**: 声明 `execute_query.schema` 在执行时可被 MCP 正式承诺的能力边界。
- **Values**:
  - `FIXED_TO_DATABASE`
  - `BEST_EFFORT`
- **Validation rules**:
  - `FIXED_TO_DATABASE` 表示 `schema` 只是与 metadata 对齐的统一命名空间标签，
    或至多是弱 hint，执行边界仍固定在 `database`。
  - `BEST_EFFORT` 表示运行时可以尝试将 `schema` 应用于未限定对象名的命名空间选择，
    但不能对外宣传为 strict guarantee。

### NormalizedSchemaMetadata

- **Purpose**: 对外暴露的 schema 命名空间对象，用于 resource、tool 和 metadata search。
- **Fields**:
  - `database`
  - `schema`
  - `schemaOrigin`
- **Validation rules**:
  - 当数据库没有独立 schema 概念时，`schema` 必须使用逻辑 `database` 名称。
  - `schemaOrigin` 允许后续区分 `native` 与 `normalized`，
    但本轮不要求把它暴露为公共字段。

### ExecuteQueryRequest

- **Purpose**: 描述一次 MCP `execute_query` 请求。
- **Fields**:
  - `sessionId`
  - `database`
  - `schema`
  - `sql`
  - `maxRows`
  - `timeoutMs`
- **Validation rules**:
  - `database` 必填。
  - `schema` 可选。
  - `schema` 只影响未限定对象名的目标命名空间意图，
    不改变 `database` 的绑定边界。
  - 当 SQL 已包含显式限定名时，SQL 自身限定关系优先。

### ExecutionNamespaceResolution

- **Purpose**: 记录一次 `execute_query` 中命名空间的解析规则。
- **Fields**:
  - `database`
  - `schema`
  - `schemaExecutionSemantics`
  - `usesExplicitSqlQualification`
- **Validation rules**:
  - `usesExplicitSqlQualification = true` 时，request-level `schema` 只能作为补充上下文，
    不可覆盖 SQL 自身含义。
  - `schemaExecutionSemantics = FIXED_TO_DATABASE` 时，
    `database` 是唯一正式执行边界。

## Relationships

- 一个 `DatabaseCapability` 约束许多 `NormalizedSchemaMetadata` 与 `ExecuteQueryRequest`。
- 一个 `ExecuteQueryRequest` 在执行前必须引用且只引用一个 `DatabaseCapability`。
- 一个 `ExecutionNamespaceResolution` 由一个 `ExecuteQueryRequest` 与一个 `DatabaseCapability` 共同推导。
- 对没有独立 schema 概念的数据库，
  一个 logical database 至少对应一个 `NormalizedSchemaMetadata`，
  且该 `schema` 名称与 logical `database` 名称一致。

## Derived Rules

- `database` 是强边界，`schema` 不是。
- `defaultSchemaSemantics` 解决的是 “metadata 怎么统一暴露”，
  `schemaExecutionSemantics` 解决的是 “execute_query.schema 怎么解释”。
- `FIXED_TO_DATABASE` 不是 “没有 schema 字段”，
  而是 “有统一 schema 命名空间，但不承诺独立 execution switch”。
- `BEST_EFFORT` 不是 “严格切换成功”，
  而是 “可以尝试把 schema 当作未限定对象名的命名空间 hint”。
- 当调用方需要 deterministic object targeting 时，
  应优先依赖 SQL 自身的显式限定关系，而不是 request-level `schema`。

## Compatibility Notes

- 现有 `SchemaSemantics` 枚举继续保留，
  负责 metadata/discovery 语义。
- 新增的 `SchemaExecutionSemantics` 是 execution-time 补充维度，
  不是对 `SchemaSemantics` 的替代。
- `supportsCrossSchemaSql` 保持现有定义；
  本轮不把它与 `schemaExecutionSemantics` 混成一个字段。
