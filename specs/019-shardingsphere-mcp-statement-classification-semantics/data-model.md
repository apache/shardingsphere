# Data Model: ShardingSphere MCP Statement Classification Semantics

## Core Domain Entities

### StatementSemanticProfile

- **Purpose**: 描述一条 SQL 在 MCP 产品面的完整语义画像。
- **Fields**:
  - `statementClass`
  - `statementType`
  - `normalizedSql`
  - `containsDataModifyingCte`
  - `targetObjectName`
  - `savepointName`
- **Validation rules**:
  - `statementClass` 表达治理语义和副作用级别。
  - `statementType` 表达用户可读的主要语句类型，
    不能替代 `statementClass`。
  - `containsDataModifyingCte = true` 时，
    `statementClass` 必须至少提升为 `DML`。
  - `statementType` 不允许退化为 `WITH`，
    因为 `WITH` 不是业务动作。

### StatementClass

- **Purpose**: 表达 MCP V1 中的治理语义类别。
- **Values**:
  - `QUERY`
  - `DML`
  - `DDL`
  - `DCL`
  - `TRANSACTION_CONTROL`
  - `SAVEPOINT`
  - `EXPLAIN_ANALYZE`
- **Validation rules**:
  - `StatementClass` 必须与 capability 中 `supportedStatementClasses` 对齐。
  - `StatementClass` 用于能力校验、执行分支、审计标记和后置处理。

### StatementType

- **Purpose**: 向调用方展示更具体的语句类型。
- **Examples**:
  - `SELECT`
  - `INSERT`
  - `UPDATE`
  - `DELETE`
  - `MERGE`
  - `CREATE`
  - `ALTER`
  - `DROP`
  - `COMMIT`
- **Validation rules**:
  - `StatementType` 可以与 `StatementClass` 不同维度共存。
  - `StatementType=SELECT` 时，`StatementClass` 仍然允许为 `DML`，
    只要整条语句包含写副作用。

### ExecuteQuerySuccessPayload

- **Purpose**: 描述一次成功的 `execute_query` 返回。
- **Fields**:
  - `statementClass`
  - `statementType`
  - `resultKind`
  - `columns`
  - `rows`
  - `affectedRows`
  - `status`
  - `message`
  - `truncated`
- **Validation rules**:
  - `statementClass` 与 `resultKind` 必须并存，
    不允许只保留其一来推断另一方。
  - `resultKind=RESULT_SET` 时，
    `statementClass` 仍然可能是 `DML`。
  - `resultKind=UPDATE_COUNT` 时，
    `statementClass` 不得为 `QUERY`。

### AuditExecutionMarker

- **Purpose**: 描述执行审计中记录的语义标记。
- **Fields**:
  - `statementClass`
  - `statementType`
  - `success`
  - `errorCode`
- **Validation rules**:
  - Audit 必须记录语义分类，
    不能只记录字符串首关键字。

## Relationships

- 一个 `StatementSemanticProfile` 驱动一次 capability 校验。
- 一个 `StatementSemanticProfile` 决定一次审计与执行分支的治理语义。
- 一次 `ExecuteQuerySuccessPayload` 必须引用一次 `StatementSemanticProfile`
  的 `statementClass` 与 `statementType`。
- `resultKind` 来源于实际执行结果，
  但必须与 `StatementSemanticProfile` 共同表达。

## Derived Rules

- `WITH` 只影响写法，不直接决定 `statementClass`。
- 一条 SQL 只要包含数据修改副作用，就不再是纯 `QUERY`。
- `statementClass=DML` 不等于一定返回 `UPDATE_COUNT`。
- `statementType=SELECT` 不等于一定是只读查询。
- capability gate、audit 与执行后处理都应该消费 `statementClass`，
  不应该消费 `WITH` 前缀。

## Compatibility Notes

- `SupportedMCPStatement` 枚举可继续复用；
  本轮不新增 `WITH` 相关类别。
- `ExecuteQueryResultKind` 枚举可以保持现状；
  需要调整的是它与 `statementClass` 的组合语义。
- 当前只在 payload 中返回 `statement_type`；
  为完整表达 data-modifying CTE，建议增加 `statement_class`。
