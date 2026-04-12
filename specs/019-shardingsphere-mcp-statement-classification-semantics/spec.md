# Feature Specification: ShardingSphere MCP Statement Classification Semantics

**Feature Branch**: `[019-shardingsphere-mcp-statement-classification-semantics]`  
**Created**: 2026-04-12  
**Status**: Draft  
**Input**: User description: "用产品语义把位分类问题梳理清楚，并用 speckit 规划；不切换分支"

## Scope Statement

本特性只收敛 MCP V1 中 `execute_query` 的语句分类语义，
重点解决 `WITH` / CTE 语句在产品契约、执行治理和返回结果之间的语义断层。

本轮要明确的产品原则：

- `WITH` 是 SQL 语法前导，不是产品动作类型
- `statement class` 表示这条 SQL 的治理语义和副作用级别
- `statement type` 表示用户看到的主要语句类型
- `result kind` 表示这次执行最终返回的数据形状
- 写操作语义和返回结果形状必须解耦；
  一条语句可以是 `DML`，但仍返回 `result_set`

本轮重点覆盖：

- 普通 `WITH ... SELECT`
- SQL Server 风格 `WITH ... INSERT/UPDATE/DELETE/MERGE`
- PostgreSQL / openGauss 风格 data-modifying CTE
- `execute_query` 成功 payload 对分类语义的表达
- capability gate、audit marker、后置处理对语义分类的消费方式

本轮不处理：

- remote HTTP 鉴权
- `schema` 语义
- SQL 重写
- 新增跨 `database` 语义
- 扩充 V1 允许的 SQL 范围

## Problem Statement

当前 `StatementClassifier` 用首关键字前缀做分类，
把所有以 `WITH` 开头的 SQL 一律归为 `QUERY`。

这在产品上不是一个“解析细节问题”，
而是把三个本来不同的概念混成了一个字段：

1. 这条 SQL 会不会产生副作用  
2. 这条 SQL 属于哪一类治理动作  
3. 这次执行最终返回的是结果集、更新计数还是确认消息

这种混淆会直接带来三类问题：

1. **治理语义错误**  
   SQL Server `WITH ... UPDATE` 明明是写操作，
   却会被当成查询类语句进入能力校验、审计标记和执行分支。
2. **产品反馈不诚实**  
   PostgreSQL data-modifying CTE 可能一边修改数据，
   一边返回结果集；
   现有模型无法诚实表达 “这是写操作，但返回的是 result set”。
3. **契约无法指导 Agent**  
   调用方如果只看到 `WITH -> QUERY`，
   会把语法前缀误当成产品动作类型，
   进而在安全判断、读写策略和结果解释上做错决策。

本特性的目标不是发明新的 SQL 类别，
而是把 `statement class`、`statement type` 和 `result kind`
重新拆开并说清楚。

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Agent 能正确识别 `WITH` 语句是不是写操作 (Priority: P1)

作为接入 MCP 的 Agent，
我希望无论 SQL 是否以 `WITH` 开头，
都能得到与实际副作用一致的 `statement class`，
这样我才能正确做读写判断、能力校验和审计解释。

**Why this priority**: 这是整个分类体系的第一原则；
如果写操作先被误判成查询，其它返回形状和审计语义都会跟着错。

**Independent Test**: 用 SQL Server CTE-prefixed DML、
普通 `WITH ... SELECT` 和无 CTE 的 DML 三组样例，
验证分类结果与副作用一致。

**Acceptance Scenarios**:

1. **Given** `WITH cte AS (SELECT * FROM orders) SELECT * FROM cte`,  
   **When** 调用分类器，  
   **Then** `statement_class` 必须为 `QUERY`，
   且 `statement_type` 必须表达查询语义而不是 `WITH`。
2. **Given** `WITH cte AS (...) UPDATE orders SET status = 'DONE' FROM cte WHERE ...`,  
   **When** 调用分类器，  
   **Then** `statement_class` 必须为 `DML`，
   且不能因为首关键字是 `WITH` 就走查询路径。
3. **Given** `WITH cte AS (...) MERGE INTO archive_orders ...`,  
   **When** 调用分类器，  
   **Then** `statement_class` 必须为 `DML`，
   并保留 `MERGE` 这类具体动作类型。

---

### User Story 2 - 写操作与返回结果形状被分开表达 (Priority: P1)

作为 MCP 使用者，
我希望系统能同时告诉我
“这条 SQL 是不是写操作” 和 “这次返回了什么形状”，
这样我在看到结果集时不会误以为这条 SQL 是只读查询。

**Why this priority**: PostgreSQL / openGauss 的 data-modifying CTE
天然会暴露这个问题；
如果不拆开，产品永远无法诚实表示 “写操作 + 结果集”。

**Independent Test**: 用 PostgreSQL 风格
`WITH updated AS (UPDATE ... RETURNING ...) SELECT * FROM updated`
样例验证执行结果可以同时表达 DML 语义和 `result_set`。

**Acceptance Scenarios**:

1. **Given** `WITH updated AS (UPDATE orders SET status = 'DONE' RETURNING *) SELECT * FROM updated`,  
   **When** 执行 `execute_query`，  
   **Then** 这条 SQL 必须按写操作语义治理，
   且返回结果允许是 `result_set`。
2. **Given** 一条 `DML` 语义的语句实际返回了结果集，  
   **When** 系统生成成功 payload，  
   **Then** payload 必须能够同时表达写操作分类与结果形状，
   而不能把二者折叠成单一字段。
3. **Given** 一条查询语义语句返回了结果集，  
   **When** 系统生成成功 payload，  
   **Then** 返回仍然保持 `QUERY + result_set`，
   不能为了兼容 data-modifying CTE 而把所有 `WITH` 都提升成写操作。

---

### User Story 3 - 契约、审计和执行后处理都消费语义分类，而不是首关键字 (Priority: P2)

作为 MCP 维护者，
我希望 capability gate、audit marker 和执行后处理
都基于语义分类消费 SQL，
而不是继续基于字符串前缀分支，
这样后续新增方言或复杂语法时不会重复踩坑。

**Why this priority**: 如果只改分类器而不改消费点，
问题会换一种形式残留在审计和执行链里。

**Independent Test**: 用 facade 级测试验证 capability 校验、
audit marker 和执行路径都使用语义分类结果。

**Acceptance Scenarios**:

1. **Given** 一条 `WITH` 前缀的写操作，  
   **When** 执行 `execute_query`，  
   **Then** capability 校验必须按 `DML` 处理，
   不能按 `QUERY` 放行或拒绝。
2. **Given** 一条 data-modifying CTE，  
   **When** 记录 audit，  
   **Then** audit 语义必须体现写操作分类，而不是 `WITH` 或 `QUERY`。
3. **Given** 产品文档、顶层 spec 和 tool contract，  
   **When** 审阅 `WITH` 语句说明，  
   **Then** 必须统一表达
   “`WITH` 只是前导语法，真正分类依据是语义副作用和结果形状”。

### Edge Cases

- 一个 `WITH` 语句包含多个 CTE，其中至少一个 CTE 有数据修改副作用。
- 外层主语句是 `SELECT`，但某个 CTE 内部是 `INSERT` / `UPDATE` / `DELETE`。
- SQL Server 的 CTE 前缀主语句是 `MERGE`。
- PostgreSQL / openGauss 的 data-modifying CTE 使用 `RETURNING` 返回结果。
- 普通查询 CTE 必须继续被归类为 `QUERY`。
- 当前数据库 capability 不支持某类语句时，仍由 capability 拒绝，而不是让 `WITH` 前缀绕过校验。

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: 系统 MUST NOT 把 `WITH` 作为独立产品语句类别，也 MUST NOT 把所有 `WITH` 默认映射为 `QUERY`。
- **FR-002**: 系统 MUST 根据整条 SQL 的真实副作用确定 `statement class`，
  而不是只看首关键字。
- **FR-003**: `statement class` MUST 继续表示治理语义，
  并保持在现有 `QUERY / DML / DDL / DCL / TRANSACTION_CONTROL / SAVEPOINT / EXPLAIN_ANALYZE`
  这些 MCP V1 类别内。
- **FR-004**: `statement type` MUST 与 `statement class` 分离；
  它用于表达更具体的用户可读语句类型，
  但不能替代副作用分类。
- **FR-005**: `result kind` MUST 与 `statement class` 分离；
  一条 `DML` 语义语句 MAY 返回 `result_set`。
- **FR-006**: SQL Server 风格 `WITH ... INSERT/UPDATE/DELETE/MERGE`
  MUST 被归类为 `DML`。
- **FR-007**: PostgreSQL / openGauss 风格 data-modifying CTE
  MUST 在存在数据修改副作用时被归类为 `DML`，
  即使外层主语句为 `SELECT`。
- **FR-008**: capability gate、audit marker 和执行分支
  MUST 消费语义分类结果，
  而不是继续消费 `WITH` 前缀或首关键字字符串。
- **FR-009**: `execute_query` 成功 payload MUST 能同时表达
  `statement class` 与 `result kind`，
  以避免把 “写操作 + 结果集” 错表述成普通查询。
- **FR-010**: 普通 `WITH ... SELECT` MUST 继续稳定归类为 `QUERY`，
  不得因为引入 data-modifying CTE 支持而整体抬升风险等级。
- **FR-011**: 语句分类实现 MUST 使用 AST / parser 级别或同等可靠性的语义检测；
  prefix-only 规则不足以满足产品契约。
- **FR-012**: 本轮 MUST 不新增 V1 支持语句范围，
  只修正现有允许范围内的语义解释。
- **FR-013**: 顶层 spec、PRD、technical design、detailed design 与 tool contract
  MUST 对 `WITH` 语句语义保持一致。
- **FR-014**: 已有非 `WITH` 语句的分类行为 MUST 保持兼容，
  除非它们同样存在“副作用语义与返回形状被错误耦合”的问题。

### Key Entities *(include if feature involves data)*

- **StatementClass**: MCP V1 的治理语义类别，
  用于 capability、审计和执行分支。
- **StatementType**: 用户可读的主要语句类型，
  如 `SELECT`、`UPDATE`、`MERGE`。
- **StatementSemanticProfile**: 一次分类输出的完整语义画像，
  至少包含 `statementClass`、`statementType`、副作用信息与标准化 SQL。
- **ExecuteQuerySuccessPayload**: 一次成功执行的响应对象，
  需要同时表达分类语义和结果形状。

### Assumptions

- `SupportedMCPStatement` 现有枚举仍可覆盖 V1 所需治理语义，
  本轮不新增 `WITH_QUERY`、`CTE_WRITE` 之类新类别。
- PostgreSQL / openGauss data-modifying CTE
  在产品语义上属于写操作，即使最终返回结果集。
- `result_kind` 是执行结果形状，不是权限等级或副作用等级。
- `statement_type` 单独存在时不足以表达 data-modifying CTE 的完整语义。

## Non-Goals

- 不通过简单字符串扫描补一个 “看下一个关键字” 的临时规则就结束。
- 不新增 V1 未承诺的 SQL 类型。
- 不解决 remote HTTP 暴露问题。
- 不重新设计 `schema` 契约。
- 不做 SQL 重写、SQL 自动补全限定名或跨方言 SQL 转换。

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 所有 `WITH` 前缀语句不再默认落入 `QUERY`；
  分类结果与真实副作用保持一致。
- **SC-002**: SQL Server CTE-prefixed DML
  在单元测试中 100% 归类为 `DML`。
- **SC-003**: PostgreSQL / openGauss data-modifying CTE
  在契约与测试中都能表达为 “`DML` 语义 + `result_set` 结果形状”。
- **SC-004**: capability gate、audit marker 和执行路径
  不再直接依赖 `WITH` 前缀作出产品决策。
- **SC-005**: 顶层 spec、PRD、technical design、detailed design 和 Speckit
  对 `WITH` 语句的产品语义描述 100% 一致。
