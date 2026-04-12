# Feature Specification: ShardingSphere MCP Execute Query Schema Semantics

**Feature Branch**: `[018-shardingsphere-mcp-execution-schema-semantics]`  
**Created**: 2026-04-12  
**Status**: Draft  
**Input**: User description: "按照推荐方案使用方案 A，把 execute_query 的 schema 语义一次性梳理清楚；对于没有独立 schema 概念的数据库，统一暴露的 schema 名称使用逻辑 database 名称；不新增 fail-fast 校验"

## Scope Statement

本特性只收敛 MCP V1 中 `schema` 的产品语义与契约表达，
重点解决 metadata 暴露、capability 声明和 `execute_query` 输入之间的断层。

本轮明确采用方案 A：

- 通过 machine-readable capability 显式声明 execution-time schema 语义
- 保持 `database` 作为唯一强边界和唯一必填执行目标
- 把 `schema` 定义为可选命名空间 hint，而不是第二个强路由键
- 对没有独立 `schema` 概念的数据库，
  统一暴露的 `schema` 名称使用 MCP 对外逻辑 `database` 名称
- 不新增 “schema 无法严格生效时直接 fail-fast” 的校验路径

本轮不处理：

- 远程 HTTP 鉴权
- `WITH` 语句分类
- SQL 重写或 SQL 自动补全限定名
- 跨 `database` 执行
- 通过 parser 推断并改写所有未限定对象名

## Problem Statement

当前 `schema` 在 MCP 产品面上处于 “字段存在，但边界未说清” 的状态：

- metadata 已统一暴露 `schema`
- `execute_query` 实现已接受 `schema`
- `001` 契约文档却还没有把 `execute_query.schema` 正式写进 contract
- capability 目前只描述 `defaultSchemaSemantics`，
  还不能告诉调用方 “执行时这个 schema 到底能不能独立生效”

这会导致三类直接问题：

1. Agent 无法判断 `schema` 是强选择器、弱 hint，还是只是一个统一命名空间标签  
2. 对 MySQL 这类没有独立 schema 概念的数据库，
   metadata 中暴露的 `schema` 名称如果采用统一命名，可能并不是底层 SQL 可直接引用的真实 qualifier  
3. 当前实现即使尝试 `setSchema(...)`，也没有把这种行为提升为正式产品承诺，
   导致调用方容易把 best-effort 行为误解为 deterministic guarantee

本特性的目标不是让所有数据库都强行表现成同一种 schema 能力，
而是把差异明确、诚实、可被 Agent 程序化消费地表达出来。

## User Scenarios & Testing *(mandatory)*

### User Story 1 - 调用方先看到 schema 的执行语义 (Priority: P1)

作为接入 ShardingSphere MCP 的 Agent 开发者，
我希望在真正调用 `execute_query` 之前，
就能从 capability 明确知道当前数据库的 `schema` 到底是固定随 `database`、
还是只能 best-effort 生效，
这样我不会把一个统一 metadata 名称误当成严格的执行边界。

**Why this priority**: 这是所有后续行为的前提；
如果 capability 不先说清楚，调用方就无法正确决定是否传 `schema`。

**Independent Test**: 通过 `get_capabilities(database)`，
分别验证 MySQL-like 与 PostgreSQL-like 数据库返回不同的
`schemaExecutionSemantics`，
且返回值足以指导调用方使用 `execute_query`。

**Acceptance Scenarios**:

1. **Given** 一个没有独立 schema 概念的 logical database，  
   **When** 调用 `get_capabilities(database)`，  
   **Then** 返回中必须显式包含 `schemaExecutionSemantics = FIXED_TO_DATABASE`。
2. **Given** 一个有原生 schema 概念的 logical database，  
   **When** 调用 `get_capabilities(database)`，  
   **Then** 返回中必须显式包含 `schemaExecutionSemantics = BEST_EFFORT`，
   且不能宣传为 strict guarantee。
3. **Given** Agent 先读取 capability，  
   **When** 决定如何构造 `execute_query` 参数，  
   **Then** 它必须能据此判断 `schema` 是固定标签还是可选 execution hint。

---

### User Story 2 - `execute_query.schema` 的产品语义被正式定义 (Priority: P1)

作为 MCP 使用者，
我希望 `execute_query.schema` 的意义被正式定义为
“对未限定对象名生效的可选命名空间 hint”，
而不是一个隐含的第二执行边界，
这样我能知道什么时候可以省略它、什么时候要依赖 SQL 自身的限定名。

**Why this priority**: 当前 review 问题的根源不是单点实现，而是公共契约不清晰。

**Independent Test**: 用同一个 `execute_query` 契约，
分别验证省略 `schema`、传入 `schema`、以及 SQL 自带限定名三种场景的文档语义一致。

**Acceptance Scenarios**:

1. **Given** `schemaExecutionSemantics = BEST_EFFORT`，  
   **When** 调用方省略 `schema`，  
   **Then** 执行必须回到数据库默认/当前 schema 语义，而不是要求强制传入。
2. **Given** `schemaExecutionSemantics = BEST_EFFORT` 且 SQL 使用未限定对象名，  
   **When** 调用方传入 `schema`，  
   **Then** MCP 可以尝试把它应用到执行上下文，
   但契约不能把这种行为表述为严格保证。
3. **Given** SQL 文本已经包含显式限定名，  
   **When** 调用方同时传入 `schema`，  
   **Then** SQL 自身的限定关系优先于 request-level `schema`。
4. **Given** `schemaExecutionSemantics = FIXED_TO_DATABASE`，  
   **When** 调用方传入 `schema`，  
   **Then** 该参数不能被解释为创建新的独立执行边界，
   执行仍然只命中当前 `database`。

---

### User Story 3 - metadata 暴露与执行语义保持一致 (Priority: P2)

作为 MCP 维护者，
我希望 metadata 中暴露出来的 `schema` 名称、
capability 中声明的 schema 语义、
以及 `execute_query` 的输入说明保持一致，
这样外部看到的名字和内部真实承诺不会互相打架。

**Why this priority**: 如果 metadata 名称和 execute 语义不一致，
调用方就会把 discoverability label 错当成 executable qualifier。

**Independent Test**: 对没有独立 schema 概念的数据库，
验证 metadata 暴露的 `schema` 为逻辑 `database` 名称；
对有原生 schema 的数据库，
验证 capability 与 tool 契约对 `schema` 的说明一致。

**Acceptance Scenarios**:

1. **Given** 一个没有独立 schema 概念的数据库，  
   **When** metadata 被统一暴露，  
   **Then** 公共 `schema` 名称必须使用逻辑 `database` 名称，而不是空字符串。
2. **Given** `execute_query` tool descriptor、`001` contract、PRD 与 technical design，  
   **When** 审阅这些文档，  
   **Then** 对 `schema` 的输入定义必须一致，
   都明确它是 optional namespace hint。
3. **Given** metadata 中暴露的 `schema` 名称只是统一 public label，  
   **When** capability 返回 `FIXED_TO_DATABASE`，  
   **Then** 产品文档必须明确该名称不等价于底层 SQL 的独立执行 qualifier。

### Edge Cases

- 逻辑 `database` 名称与底层物理 database / catalog 名称不同。
- `schemaExecutionSemantics = FIXED_TO_DATABASE` 时，调用方传入了与逻辑 `database` 名称不同的 `schema`。
- `schemaExecutionSemantics = BEST_EFFORT` 时，调用方省略 `schema`，数据库依赖默认 schema 或 `search_path`。
- SQL 已显式写出限定名时，request-level `schema` 不得改写 SQL 自身含义。
- 事务中的 `database` 绑定规则保持不变；本轮不引入跨 `database` 或跨 session 的新语义。

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: `database` MUST 继续作为 MCP SQL 执行的唯一强边界和唯一必填目标。
- **FR-002**: `execute_query.schema` MUST 被正式定义为 optional namespace hint，
  用于表达对未限定对象名的目标命名空间意图，
  MUST NOT 被定义为第二个强路由键或独立鉴权边界。
- **FR-003**: 系统 MUST 在数据库级 capability 中新增 `schemaExecutionSemantics`
  字段，用 machine-readable 方式表达 execution-time schema 语义。
- **FR-004**: `schemaExecutionSemantics` V1 MUST 至少支持 `FIXED_TO_DATABASE`
  与 `BEST_EFFORT` 两种取值。
- **FR-005**: `defaultSchemaSemantics` MUST 继续描述 metadata/discovery 侧的统一 schema 语义；
  `schemaExecutionSemantics` MUST 专门描述 `execute_query.schema` 的执行语义。
- **FR-006**: 对没有独立 schema 概念的数据库，
  系统 MUST 把公共暴露的 `schema` 名称归一化为逻辑 `database` 名称。
- **FR-007**: 当 `defaultSchemaSemantics = DATABASE_AS_SCHEMA` 时，
  `schemaExecutionSemantics` MUST 为 `FIXED_TO_DATABASE`。
- **FR-008**: 当 `schemaExecutionSemantics = FIXED_TO_DATABASE` 时，
  系统 MUST NOT 对外承诺 request-level `schema` 可以独立切换执行命名空间；
  该字段 MAY 被视为与 metadata 对齐的 public label。
- **FR-009**: 当 `schemaExecutionSemantics = BEST_EFFORT` 时，
  系统 MAY 尝试把 request-level `schema` 应用到执行上下文，
  但公共契约 MUST NOT 把这种行为描述为 strict guarantee。
- **FR-010**: 当 SQL 文本已经包含显式限定名时，
  request-level `schema` MUST NOT 覆盖 SQL 自身的限定含义。
- **FR-011**: `execute_query` 的公共契约、tool descriptor、PRD、technical design、
  detailed design 与 Speckit 文档 MUST 对 `schema` 的定义保持一致。
- **FR-012**: 本特性 MUST 不通过新增 fail-fast 校验来解决 schema 语义不清问题；
  语义分歧应优先通过 capability 和 contract 明确表达。
- **FR-013**: `supportsCrossSchemaSql` 的现有意义 MUST 保持稳定；
  本轮不借由 `schemaExecutionSemantics` 改写其定义。
- **FR-014**: metadata、capability 与 `execute_query` 三者中出现的 `schema` 名称，
  MUST 可被统一解释，不允许一个地方是 public label、另一个地方却被暗中当成 strict selector。
- **FR-015**: 本轮实现 MUST 提供 dedicated tests，
  覆盖 capability 发布、无独立 schema 数据库的 metadata 归一化、
  `execute_query` 的 best-effort 边界、以及文档/descriptor 对齐。

### Key Entities *(include if feature involves data)*

- **SchemaExecutionSemantics**: 数据库级 capability 中的新枚举，
  用于声明 `execute_query.schema` 在执行时的可承诺语义。
- **DatabaseCapability**: 数据库级能力对象；
  除现有字段外，新增 `schemaExecutionSemantics`。
- **ExecuteQueryRequest**: SQL 执行请求；
  `database` 是强边界，`schema` 是可选命名空间 hint。
- **NormalizedSchemaMetadata**: MCP 对外统一暴露的 schema 命名空间；
  对没有独立 schema 概念的数据库，名称使用逻辑 `database` 名称。

### Assumptions

- `database` 指 MCP 对外逻辑数据库标识，不等同于底层物理 database / catalog 名称。
- 对没有独立 schema 概念的数据库，公共 `schema` 名称可以是统一 contract label，
  不保证等价于底层 SQL 的真实限定名。
- V1 采用方案 A；
  调用方应该先读 capability，再决定是否传 `schema`。
- 本轮不引入 schema strictness 探测、SQL parser rewrite 或 vendor-specific qualifier 映射。

## Non-Goals

- 不把 `schema` 升级为第二个强执行边界。
- 不新增 schema-related fail-fast 校验或强制 request rejection。
- 不承诺所有有原生 schema 的数据库都具备 strict schema switching。
- 不解决 remote HTTP 鉴权问题。
- 不解决 `WITH` 语句分类和 metadata refresh 问题。

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: `get_capabilities(database)` 对每个正式支持数据库都返回 `schemaExecutionSemantics`。
- **SC-002**: 对没有独立 schema 概念的数据库，
  metadata 暴露的默认 `schema` 名称 100% 使用逻辑 `database` 名称，而不是空字符串。
- **SC-003**: `execute_query` 的 tool descriptor、`001` contract、PRD、technical design、
  detailed design 与本 spec 对 `schema` 的定义 100% 一致。
- **SC-004**: 调用方仅凭 capability 即可区分
  “固定随 database 的 schema 标签” 与 “best-effort execution hint”。
- **SC-005**: 本轮不新增因 schema 无法严格生效而直接返回 `invalid_request`
  或等价错误的新路径。
