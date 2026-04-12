# Research: ShardingSphere MCP Statement Classification Semantics

## Decision 1: `WITH` 不是产品语句类别

- **Decision**: 把 `WITH` 定义为 SQL 语法前导，而不是 MCP 的 `statement class`。
- **Rationale**:
  - `WITH` 只能说明语句采用了 CTE 写法，不能说明它是不是写操作。
  - 同样以 `WITH` 开头的 SQL，可能是纯查询，也可能是写操作。
  - 如果产品层继续把 `WITH` 当分类条件，语法形式就会覆盖真实业务语义。
- **Alternatives considered**:
  - 保留 `WITH -> QUERY`：实现最简单，但对 SQL Server CTE-prefixed DML 和 PostgreSQL data-modifying CTE 都错误。
  - 引入新的 `WITH_QUERY` / `WITH_DML` 类别：会把语法形式抬升成产品 API 概念，增加不必要复杂度。

## Decision 2: 把 `statement class`、`statement type`、`result kind` 分开

- **Decision**: 使用三个正交维度表达执行语义。
- **Rationale**:
  - `statement class` 解决治理和副作用问题。
  - `statement type` 解决用户看到的具体动作问题。
  - `result kind` 解决 payload 形状问题。
  - PostgreSQL data-modifying CTE 证明了一条语句可以同时满足
    “写操作语义” 与 “结果集返回”。
- **Alternatives considered**:
  - 继续只保留 `statement_type + result_kind`：
    无法表达 “外层 `SELECT`，但整条语句会写数据”。
  - 只保留 `statement_class`：
    会损失对 `UPDATE`、`MERGE` 等具体动作的可读性。

## Decision 3: 语义分类应由 parser / AST 驱动，而不是首关键字启发式

- **Decision**: 实现层采用 ShardingSphere 现有 parser 能力或同等级 AST 语义检测。
- **Rationale**:
  - prefix-only 规则无法识别 data-modifying CTE。
  - “找 `WITH` 后面的第一个关键字” 只能解决 SQL Server 的一部分场景，
    仍然会漏掉 PostgreSQL / openGauss 外层 `SELECT`、内层 DML 的写操作语义。
  - ShardingSphere 已经拥有多数据库 parser 资产，
    与其不断堆积字符串规则，不如复用已有语义层。
- **Alternatives considered**:
  - 只扫描 `WITH` 之后的主语句关键字：
    会把 `WITH updated AS (UPDATE ... RETURNING *) SELECT * FROM updated`
    继续错归为 `QUERY`。
  - 一律把 `WITH` 提升成 `DML`：
    会把大量普通查询 CTE 错判成写操作。

## Decision 4: 副作用优先于外层返回形状

- **Decision**: 当任一 CTE 或主语句存在数据修改副作用时，
  `statement class` 采用 `DML`。
- **Rationale**:
  - 产品上的安全、审计和治理首先关心“有没有写”。
  - 外层主语句是不是 `SELECT`，并不能抹去 CTE 内部已经发生的写操作。
  - 这一定义能覆盖 SQL Server CTE-prefixed DML
    和 PostgreSQL / openGauss data-modifying CTE。
- **Alternatives considered**:
  - 只看外层主语句：
    对 data-modifying CTE 不诚实。
  - 只看最后返回结果：
    会把“写操作 + result set”误当成只读查询。

## Decision 5: 返回形状由实际执行结果决定，不再绑定语义类别

- **Decision**: `result_kind` 由实际执行结果决定，
  `statement class` 只用于治理和副作用表达。
- **Rationale**:
  - JDBC `execute(...)` 本身就能区分是否有结果集。
  - 让 `DML` 语义语句返回 `result_set`
    是 data-modifying CTE 的必要表达能力。
  - 这能避免在协议层制造“写操作必须只能返回 update_count”的假规则。
- **Consequences**:
  - `SQLExecutionResponse` 需要把 `statement class` 与 `result kind` 同时表达。
  - 执行分支不能再简单写死为 `QUERY -> result_set`、`DML -> update_count`。

## Decision 6: 成功 payload 建议新增 `statement_class`

- **Decision**: 在成功 payload 中显式返回 `statement_class`。
- **Rationale**:
  - `statement_type=SELECT` 无法单独表达 data-modifying CTE 的写语义。
  - 调用方不应该反向解析 SQL 文本去判断读写。
  - 这是对现有响应的加法增强，兼容性风险可控。
- **Alternatives considered**:
  - 只在内部使用 `statement class`：
    会让外部 Agent 仍然无法区分“查询”与“写后返回结果集”。
  - 把 `statement_type` 强行改成 `DML`：
    会损失用户对具体 SQL 类型的理解。

## Recommended Product Position

- `WITH` 是写法，不是产品动作。
- 产品首先要回答 “这条 SQL 有没有副作用”。
- 返回结果集不等于只读查询。
- 对外契约应该显式告诉调用方：
  - `statement_class` 负责副作用与治理语义
  - `statement_type` 负责具体动作展示
  - `result_kind` 负责 payload 形状
