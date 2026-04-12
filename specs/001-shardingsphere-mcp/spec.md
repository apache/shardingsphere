# Feature Specification: ShardingSphere MCP V1 Unified Database Contract

**Feature Branch**: `001-shardingsphere-mcp`  
**Created**: 2026-03-21  
**Status**: Implementation-aligned draft  
**Input**: User description: "基于 `docs/mcp/ShardingSphere-MCP-PRD.md` 创建 ShardingSphere MCP V1 baseline specification"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - 统一元数据发现 (Priority: P1)

作为 Agent 开发者或接入平台，我希望只接入一个 ShardingSphere MCP 服务，
就能用统一方式发现 logical database、schema、table、view、column、index 与 capability，
从而不再为每种数据库单独维护对象发现逻辑。

**Why this priority**: 统一发现能力是所有后续查询、执行与能力判断的入口；
如果这一层不能稳定统一，上层仍然需要写数据库分支，产品目标无法成立。

**Independent Test**: 在同一个 MCP 服务后挂接至少两种正式支持数据库，
调用统一的 resources 与 metadata tools，验证调用方能够只凭公共契约完成对象发现，
并在不支持 `index` 时收到 `unsupported`。

**Acceptance Scenarios**:

1. **Given** metadata catalog 中存在多个 logical database 与 schema，
   **When** 调用 metadata resources 或 `search_metadata`，
   **Then** 系统返回统一对象语义、支持搜索与分页。
2. **Given** 某个 database 的 capability 声明支持 `index`，
   **When** 读取 index 相关 resource，
   **Then** 系统返回与父 table 关联的统一 index 信息。
3. **Given** 某个 database 的 capability 未声明支持 `index`，
   **When** 调用 index 相关 resource，
   **Then** 系统返回 `unsupported`，而不是数据库专有行为。

---

### User Story 2 - 统一 SQL 执行与事务边界 (Priority: P2)

作为使用数据库能力的 Agent，我希望通过一个统一的 `execute_query` 契约执行单条 SQL，
并在不同数据库之间获得一致的结果模型、错误语义、事务控制语义与 savepoint 语义，
从而减少数据库特判和失败处理分支。

**Why this priority**: 元数据发现之后，执行能力是产品的核心价值；
如果执行结果、错误码或事务边界不一致，上层仍然需要数据库特化适配。

**Independent Test**: 使用同一组公共 tools，在不同正式支持数据库上分别执行允许语句、
不允许语句、事务控制语句与 savepoint 语句，验证返回统一结果模型与统一错误语义。

**Acceptance Scenarios**:

1. **Given** 调用方显式指定一个 logical database，并提交一条允许执行的单语句 SQL，
   **When** 调用 `execute_query`，
   **Then** 系统返回且只返回一个统一结果对象，类型为 `result_set`、
   `update_count` 或 `statement_ack` 之一。
2. **Given** 调用方提交多语句 SQL，或提交 `USE`、`SET`、`COPY`、`LOAD`、`CALL`
   等 V1 明确不支持的语句，
   **When** 调用 `execute_query`，
   **Then** 系统返回统一错误语义，而不是透传数据库专有错误。
3. **Given** 当前会话已在某个 database 上进入事务态，
   **When** 后续调用尝试切换到其他 database，或在无效事务状态下执行
   `COMMIT`、`ROLLBACK`、`SAVEPOINT` 相关语句，
   **Then** 系统返回 `conflict`、`unsupported` 或 `transaction_state_error`。

---

### User Story 3 - 运行边界、审计与变化可见性 (Priority: P3)

作为平台团队或运行方，我希望当前 MCP runtime 能明确自己的运行边界，
提供基础审计，并对 DDL / DCL 变化提供稳定的可见性标记，
从而在保持最小运行面的前提下可运维、可观测。

**Why this priority**: 当前实现的内置 runtime 只提供最小运行边界，
如果边界说明、审计基线与变化可见性不清晰，外部接入方很容易误判其安全能力。

**Independent Test**: 验证 resource 读取、metadata tool 调用与 SQL 执行都会生成审计记录；
验证 DDL / DCL 成功提交后的会话级与全局可见性标记；验证本地模式下非 loopback `Origin`
被拒绝。

**Acceptance Scenarios**:

1. **Given** 调用方成功执行了结构变更或 DCL 变更，
   **When** 当前会话再次读取相关 metadata，
   **Then** 当前会话在下一次相关 metadata 读取时看到刷新标记，
   且全局 MCP 公共面在 60 秒窗口内可见该变化。
2. **Given** 发生 resource 读取、metadata tool 调用或 SQL 执行，
   **When** 平台检查审计记录，
   **Then** 每条记录都包含 `sessionId`、`database`、`operationClass`、
   `operationDigest`、`successOrFailure`、`errorCode`、`transactionMarker`
   与 `timestamp`。
3. **Given** 服务绑定在 loopback 地址上，
   **When** 请求显式携带非 loopback `Origin`，
   **Then** 服务端返回 `403 Forbidden`；
   对外暴露场景应由受信网络、上游网关或反向代理承担接入边界控制。

### Edge Cases

- 当调用方指定 `schema` 但未指定 `database` 时，系统必须返回 `invalid_request`。
- 当底层数据库没有独立 `schema` 概念时，系统仍需暴露统一 `schema` 语义，
  并使用 logical `database` 名称作为公共 `schema` 名称；
  `schema` 在执行时的含义由 database capability 的 `schemaExecutionSemantics` 解释。
- 当目标 database 不支持事务控制或 savepoint 时，相关语句必须统一返回 `unsupported`。
- 当事务已绑定一个 database 后再次指定其他 database 时，系统必须返回 `conflict`。
- 当查询结果超过 `max_rows` 或 database capability 的默认返回行数限制时，
  系统必须显式标记 `truncated=true`。
- 当 `index` 不属于当前 database 的 `supported_object_types` 时，
  index 相关 tools 与 resources 不得静默返回空结果，必须返回 `unsupported`。
- 当 follow-up 请求携带的 `MCP-Protocol-Version` 与会话协商版本不一致时，
  系统必须返回 `400`。

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: 系统 MUST 对外统一暴露 `database`、`schema`、`table`、`view`、
  `column`、`index`、可选 `sequence` 与 `capability` 的对象语义，适用于所有 V1 正式支持数据库。
- **FR-002**: 系统 MUST 将每次 SQL 执行请求绑定到一个且仅一个 logical `database` 参数。
- **FR-003**: 系统 MUST 提供正式公共 resources，用于表达服务级 capability、
  databases、database capability、schemas、可选 sequences、tables、views、columns 与可选 indexes。
- **FR-004**: 系统 MUST 将 `index` 作为 database-level 可选公共对象，
  且仅当目标 database capability 明确声明支持时才暴露其 resources。
- **FR-004A**: 系统 MUST 将 `sequence` 作为 database-level 可选公共对象，
  且仅当目标 database capability 明确声明支持时才暴露其 resources。
- **FR-005**: 系统 MUST 为 V1 提供正式公共 tools：`search_metadata` 与 `execute_query`。
- **FR-006**: 系统 MUST 让 metadata resources 与 `search_metadata` 支持统一对象发现；
  其中 `search_metadata` MUST 支持关键字搜索与分页，并 MUST 返回下一页标识或等价机制，
  避免客户端通过完整枚举发现大型元数据集合。
- **FR-007**: 系统 MUST 支持 `search_metadata` 按对象类型过滤；
  允许值限定为 `database`、`schema`、`table`、`view`、`column`、`index`、`sequence`；
  其他值 MUST 返回 `invalid_request`；
  当省略 `database` 时在当前 metadata catalog 中的全部 logical databases 范围内搜索；
  当指定 `schema` 但未指定 `database` 时 MUST 返回 `invalid_request`。
- **FR-008**: 系统 MUST 让 `execute_query` 每次只接受一条 SQL 语句，
  并 MUST 对多语句输入返回 `invalid_request`；
  `execute_query` 的公共输入签名为 `execute_query(database, schema?, sql, max_rows?, timeout_ms?)`。
- **FR-008A**: 系统 MUST 将 `execute_query.schema` 定义为 optional namespace hint，
  用于表达未限定对象名的目标命名空间意图；
  `schema` MUST NOT 作为第二个强执行边界。
- **FR-009**: 系统 MUST 支持 capability 声明允许的 SQL 语句类别，
  包括查询、DML、DDL、DCL、事务控制、savepoint 与 `EXPLAIN ANALYZE`；
  但当目标 database 未声明支持时 MUST 返回 `unsupported`。
- **FR-010**: 系统 MUST 统一拒绝 `USE`、`SET`、`COPY`、`LOAD`、`CALL`
  与各数据库专有高风险元命令，并返回统一错误语义。
- **FR-011**: 系统 MUST 维持状态型 MCP 会话模型，不支持会话恢复；
  follow-up HTTP 请求 MUST 使用 `MCP-Session-Id`，并遵守会话协商的协议版本。
- **FR-012**: 系统 MUST 让会话默认行为为 `autocommit = true`；
  对原生支持事务控制的 database，仅在显式执行 `BEGIN` 或
  `START TRANSACTION` 后进入事务态。
- **FR-013**: 系统 MUST 将活动事务绑定到单一 logical `database`；
  在事务存续期间若后续调用指定其他 `database`，系统 MUST 返回 `conflict`。
- **FR-014**: 系统 MUST 对原生支持事务控制的 database 统一支持
  `BEGIN`、`START TRANSACTION`、`COMMIT` 与 `ROLLBACK` 语义。
- **FR-015**: 系统 MUST 对原生支持 savepoint 的 database 统一支持
  `SAVEPOINT`、`ROLLBACK TO SAVEPOINT` 与 `RELEASE SAVEPOINT` 语义；
  对不支持 savepoint 的 database MUST 返回 `unsupported`。
- **FR-016**: 系统 MUST 在 MCP 会话结束时自动回滚未提交事务。
- **FR-017**: 系统 MUST 为每次 `execute_query` 返回且只返回一个统一结果对象，
  结果类型限定为 `result_set`、`update_count` 或 `statement_ack`。
- **FR-018**: 系统 MUST 让查询类结果至少包含 `result_kind`、`columns`、
  `rows` 与 `truncated`；DML 至少包含 `result_kind` 与 `affected_rows`；
  非结果集语句至少包含 `result_kind`、`statement_type`、`status` 与 `message`。
- **FR-019**: 系统 MUST 以统一契约表达列名、统一逻辑类型、底层原生类型、
  可空性、`null` 值与截断语义，避免调用方自行解析方言差异。
- **FR-020**: 系统 MUST 提供 service-level capability，
  用于声明协议公共支持的 resources、tools 与 statement classes。
- **FR-021**: 系统 MUST 提供 database-level capability，
  用于声明对象类型、statement classes、事务能力、savepoint 能力、
  默认 autocommit、默认 schema 语义、schema 执行语义、跨 schema SQL 能力、返回行数和超时默认值，
  以及 DDL、DCL、`EXPLAIN ANALYZE` 的事务与结果边界行为。
- **FR-022**: 系统 MUST 对 metadata 读取、metadata tool 调用与 SQL 执行执行审计，
  审计记录至少包含 `sessionId`、`database`、`operationClass`、
  `operationDigest`、`successOrFailure`、`errorCode`、`transactionMarker`
  与 `timestamp`。
- **FR-023**: 系统 MUST 在结构变化与 DCL 变化成功提交后的 60 秒内
  将变化反映到 MCP 公共面的全局可见性标记；对当前 MCP 会话成功提交的变化，
  系统 MUST 在同一会话的下一次相关 metadata 读取时反映该变化。
- **FR-024**: 系统 MUST 使用统一错误码集合：
  `invalid_request`、`not_found`、`unsupported`、`conflict`、`timeout`、
  `unavailable`、`transaction_state_error` 与 `query_failed`。
- **FR-025**: 系统 MUST 在无活动事务时执行 `COMMIT`、`ROLLBACK`
  或引用不存在的保存点时返回 `transaction_state_error`。
- **FR-026**: 系统 MUST 在 database 不支持事务控制或 savepoint 时，
  对相关语句返回 `unsupported`，而不是透传数据库专有错误。
- **FR-027**: 系统 MUST 将 V1 正式支持数据库范围限定为 MySQL、PostgreSQL、
  openGauss、SQL Server、MariaDB、Oracle、ClickHouse、Doris、Hive、Presto、
  Firebird 与 H2，并使其满足统一契约的最低能力基线。
- **FR-028**: 系统 MUST 使 database transaction capability matrix
  与实际 database-level capability 返回保持一致。
- **FR-029**: 系统 MUST 不把 `materialized view`、
  `function / procedure`、`trigger`、`event`、`synonym` 与其他数据库专有对象
  纳入 V1 统一公共对象基线。
- **FR-030**: 当前内置 runtime MUST 明确自身运行边界：
  本地模式默认绑定 `127.0.0.1`；
  loopback 绑定下若请求显式携带 `Origin`，其 host MUST 仍为 loopback / localhost；
  对外暴露场景依赖受信网络、上游网关或反向代理承担接入边界控制。

### Key Entities *(include if feature involves data)*

- **Logical Database**: MCP 对外暴露的一级访问目标，也是每次 SQL 执行必须显式指定的目标；
  它代表逻辑数据库标识，不等同于底层物理实例或原生 catalog。
- **Schema Namespace**: 逻辑 database 下的命名空间；
  即使底层数据库没有独立 schema 概念，也必须以统一 schema 语义对外暴露。
- **Metadata Object**: 包括 table、view、column 与可选 index / sequence；
  通过统一字段表达对象身份与父子关系。
- **Capability Profile**: 服务级或 database 级能力声明，
  用于公开当前契约支持的对象类型、工具、语句类别与事务边界。
- **Session Context**: 一个 MCP 会话对应的 `autocommit` 状态、
  事务状态、保存点集合与当前绑定 database 的组合。
- **Execution Result**: `result_set`、`update_count` 或 `statement_ack`
  之一，是每次 `execute_query` 的唯一返回对象。
- **Audit Record**: 对 resource 读取、metadata tool 调用或 SQL 执行的统一审计条目。
- **Refresh Visibility State**: 对结构变化与 DCL 变化的会话级与全局可见性标记。

### Assumptions

- 本规格以 `docs/mcp/ShardingSphere-MCP-PRD.md` 中定义的 V1 范围为准，
  其他类 SQL 或测试型方言不纳入正式验收。
- 上层调用方通过一个 ShardingSphere MCP 服务接入，
  且使用 logical `database` 作为目标标识，而不是直接面向物理数据库实例。
- V1 以统一公共面和安全保守原则为先，不要求抹平所有数据库专有对象和专有语义。
- 当前内置 runtime 聚焦 session contract、协议协商与运行边界校验；
  如需对外暴露，依赖外部受信网络、网关或反向代理承担接入边界控制。

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% 的 V1 正式支持数据库在验收中都满足统一对象模型、
  统一公共 resources / tools、统一结果模型与统一错误语义的最低能力基线。
- **SC-002**: 在验收演示中，调用方能够仅通过一个 ShardingSphere MCP 服务，
  使用同一套公共 resources 与 tools 完成至少两种正式支持数据库上的对象发现与 SQL 执行。
- **SC-003**: 100% 的事务冲突、无效事务状态、不支持语句、多语句输入、
  缺失 session header、协议版本不匹配与不支持 `index` 场景，在验收中都返回定义好的统一错误码或 HTTP 状态。
- **SC-004**: 100% 的结构变化与 DCL 变化在 60 秒窗口内反映到 MCP 公共面的全局可见性标记，
  且当前会话成功提交的变更在同一会话的下一次相关读取中可见。
- **SC-005**: 100% 的 database transaction capability matrix 条目
  与对应 database-level capability 实际返回保持一致。
- **SC-006**: 100% 的 metadata 读取、metadata tool 调用与 SQL 执行验收样例
  都能生成包含必填字段的审计记录。
