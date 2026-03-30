# Feature Specification: ShardingSphere MCP Direct Multi-Database Runtime

**Feature Branch**: `[004-shardingsphere-mcp-direct-multi-db]`  
**Created**: 2026-03-23  
**Status**: Draft  
**Input**: User description: "mcp 不通过 shardingsphere 也能处理多个数据库"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - 统一接入多个独立数据库 (Priority: P1)

作为 MCP 接入方，我希望一个 MCP 服务实例可以直接接入多个彼此独立的数据库，
并继续通过统一的 `database` 参数完成发现与访问，而不要求这些数据库先接入
ShardingSphere 作为中间层。

**Why this priority**: 这是这次特性的核心用户价值；如果仍然要求先经过
ShardingSphere，当前需求就没有被满足。

**Independent Test**: 配置同一个 MCP 服务直连至少两个独立数据库，
完成 `initialize`、`list_databases`、`list_tables`、`describe_table`
和 `get_capabilities(database)`，验证调用方无需改变公共契约即可区分和访问多个目标库。

**Acceptance Scenarios**:

1. **Given** MCP 服务已配置两个或以上独立数据库，**When** 客户端调用
   `list_databases`，**Then** 返回全部已成功装载的 logical database 名称。
2. **Given** 不同 logical database 对应不同底层数据库实例或不同数据库类型，
   **When** 客户端分别调用 `list_schemas`、`list_tables` 或 `describe_table`，
   **Then** 返回与目标 logical database 对应的统一元数据结果。
3. **Given** 某个 logical database 未声明支持 `index`，
   **When** 客户端调用 `list_indexes` 或读取 index 相关 resource，
   **Then** 返回 `unsupported`，而不是空成功结果或数据库专有行为。
4. **Given** 客户端调用 `search_metadata` 且未指定 `database`，
   **When** 服务处理该请求，**Then** 在全部已装载的 logical databases 范围内搜索。

---

### User Story 2 - 按数据库路由执行并保持事务隔离 (Priority: P2)

作为使用数据库能力的 Agent，我希望 `execute_query` 继续以一个 logical
`database` 为边界执行单条 SQL，并在多数据库场景下保持统一结果模型和单数据库事务语义，
而不是引入跨数据库事务或联邦执行。

**Why this priority**: 多库接入如果破坏了现有统一执行契约，上层调用方反而会新增路由和容错分支。

**Independent Test**: 在同一 MCP 服务中，对两个不同 logical database 分别执行
`SELECT`、DML、`BEGIN / COMMIT`、`SAVEPOINT` 或不支持场景，验证路由、结果模型和事务边界符合统一契约。

**Acceptance Scenarios**:

1. **Given** 客户端提交 `execute_query(database, sql)` 到某个 logical database，
   **When** SQL 属于允许执行的单语句，**Then** 返回来自该数据库的统一结果对象。
2. **Given** 当前会话已在 database A 上进入事务态，**When** 后续请求尝试切换到
   database B 执行 SQL 或事务控制语句，**Then** 返回 `conflict` 或
   `transaction_state_error`，且不会跨数据库执行。
3. **Given** 目标 database 不支持事务控制或 savepoint，
   **When** 客户端执行对应语句，**Then** 返回 `unsupported`。
4. **Given** 客户端提交多语句 SQL，或提交 `USE`、`SET`、`COPY`、`LOAD`、
   `CALL` 等 V1 明确不支持的命令，**When** `execute_query` 被调用，
   **Then** 返回统一拒绝错误，不进入目标数据库执行。

---

### User Story 3 - 明确运行边界与单库故障行为 (Priority: P3)

作为平台团队或运行方，我希望多数据库直连模式的启动校验、单库故障处理、元数据刷新边界和审计脱敏都被明确规定，
这样我可以把它作为一个可运维的 MCP 服务部署，而不会因为部分歧义导致实现和验收各自解释。

**Why this priority**: 直连多个真实数据库后，最大的风险不再是协议，而是运维边界和故障行为不清楚。

**Independent Test**: 验证启动时配置错误会 fail fast；验证运行中单个数据库暂时不可用时其他数据库仍可服务；
验证 DDL / DCL 只影响目标数据库的可见性刷新；验证审计与诊断不泄露凭据。

**Acceptance Scenarios**:

1. **Given** 启动配置中任一 logical database 缺少必要连接信息、数据库类型不受支持、
   驱动不可用或初次元数据装载失败，**When** 服务启动，**Then** 启动快速失败，
   且不会发布可用的 MCP endpoint。
2. **Given** 服务已成功启动且某一个 logical database 在运行中暂时不可用，
   **When** 客户端继续访问其他 logical database，**Then** 其他数据库的 discovery
   与 execution 行为继续可用。
3. **Given** 服务已成功启动且某一个 logical database 在运行中暂时不可用，
   **When** 客户端访问该 database 的实时执行路径，**Then** 返回 `unavailable`；
   对只读 discovery 请求，服务继续返回该 database 最后一次成功装载的 metadata snapshot。
4. **Given** 某个 database 成功提交了 DDL 或 DCL 变更，**When** 当前会话再次读取该 database
   相关 metadata，**Then** 当前会话立即可见，且全局可见性在 60 秒窗口内满足 SLA；
   其他 databases 的 metadata snapshot 不因本次变更被整体替换。
5. **Given** 服务生成启动诊断、运行诊断或审计记录，**When** 运维查看这些输出，
   **Then** 能看到 logical database、失败阶段与建议动作，但看不到原始密码或带凭据的连接串。

### Edge Cases

- 当两个配置项声明了相同的 logical database 名称时，系统必须启动失败。
- 当 logical database 名称与底层数据库原生 catalog 或 schema 名称不同，系统仍以配置的
  logical database 名称作为 MCP 公共面的一级目标标识。
- 当某个数据库没有独立 schema 概念时，系统仍需暴露统一 schema 语义，
  且公共契约不额外引入 schema 名称推导字段。
- 当 `schema` 被提供但 `database` 缺失时，系统必须返回 `invalid_request`。
- 当服务已经成功启动，但单个数据库的后续 refresh 失败时，系统必须保留该数据库最近一次成功的 metadata snapshot，
  而不是把其降级成空 catalog。
- 当一个会话在活动事务中被关闭时，系统必须自动回滚该会话在当前绑定 database 上的未提交工作。
- 当一个数据库类型不在 V1 正式支持范围内时，系统必须拒绝该绑定，而不是以未知 capability 启动。
- 当查询结果超过 `max_rows` 或 capability 默认返回行数限制时，系统必须显式标记 `truncated=true`。

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: 系统 MUST 允许一个 MCP 服务实例直接接入多个彼此独立的数据库，
  而不要求这些数据库先通过 ShardingSphere 聚合后再接入。
- **FR-002**: 每个被接入的数据库 MUST 通过显式且全局唯一的 logical `database`
  名称对外暴露，作为 MCP discovery 和 execution 的一级目标标识。
- **FR-003**: 系统 MUST 保持现有 V1 公共 resources、tools、统一结果模型和统一错误模型，
  不因直连多数据库而引入新的客户端必选参数或新的结果形状。
- **FR-004**: `list_databases` MUST 返回当前服务中全部成功装载的 logical databases。
- **FR-005**: `list_schemas`、`list_tables`、`list_views`、`list_columns`、
  `list_indexes`、`search_metadata`、`describe_table`、`describe_view` 与
  `get_capabilities(database)` MUST 按请求中的 logical `database`
  路由到对应的数据库绑定。
- **FR-006**: `execute_query` MUST 将每次 SQL 请求绑定到一个且仅一个 logical
  `database`，并只在该目标数据库范围内执行。
- **FR-007**: 系统 MUST 不支持跨数据库事务、跨数据库 savepoint 和跨数据库 SQL 联邦执行；
  多数据库直连只提供按库路由，而不提供跨库语义聚合。
- **FR-008**: 系统 MUST 保持活动事务绑定到单一 logical `database`；
  在事务存续期间若后续请求切换到其他 `database`，系统 MUST 返回 `conflict`
  或 `transaction_state_error`。
- **FR-009**: 每个 logical database MUST 能独立声明或继承其 database type、
  schema 语义、对象暴露边界和 capability 覆盖，不得要求所有已配置数据库共享同一组运行时属性。
- **FR-010**: 当某个目标数据库不支持事务控制、savepoint、`index` 或
  `EXPLAIN ANALYZE` 时，系统 MUST 按统一 capability 契约返回 `unsupported`。
- **FR-011**: 服务启动时 MUST 对全部已配置的 logical databases 执行一致的启动校验；
  当任一数据库绑定缺少必要信息、数据库类型不受支持、依赖不可用或初次 metadata
  装载失败时，启动 MUST fail fast。
- **FR-012**: 在服务已成功启动之后，单个 logical database 的运行时暂时不可用
  MUST NOT 阻止其他已装载 logical databases 继续服务。
- **FR-013**: 在服务已成功启动之后，当某个 logical database 暂时不可用时，
  对该数据库需要实时后端访问的请求 MUST 返回 `unavailable`。
- **FR-014**: 在服务已成功启动之后，当某个 logical database 暂时不可用时，
  对该数据库的只读 metadata discovery 请求 MUST 继续返回最后一次成功装载的
  metadata snapshot，直到下一次成功刷新替换它。
- **FR-015**: DDL 和 DCL 成功提交后的 metadata 可见性刷新 MUST 以 logical
  database 为粒度生效；一次变更不得强制替换其他 databases 的 metadata snapshot。
- **FR-016**: 系统 MUST 对 metadata 读取、metadata tool 调用、SQL 执行、
  启动校验失败和运行时数据库不可用场景提供可追溯诊断与审计记录。
- **FR-017**: 公共 resources、tools、审计记录和诊断输出 MUST NOT 暴露原始密码、
  secret 明文或带凭据的连接串。
- **FR-018**: 当数据库没有独立 schema 概念时，系统 MUST 通过统一 schema
  语义对外暴露 discovery 结果，而不引入额外的 schema 名称推导契约。
- **FR-019**: `search_metadata` 在省略 `database` 时 MUST 在全部已装载的
  logical databases 范围内搜索；当指定 `schema` 但未指定 `database` 时 MUST
  返回 `invalid_request`。
- **FR-020**: 系统 MUST 继续遵守 V1 的单语句限制、禁用命令集合、统一错误码集合、
  自动回滚未提交事务和 60 秒全局可见性 SLA。
- **FR-021**: 系统 MUST 将正式支持的数据库范围继续限定在 V1 已定义的正式支持集合内，
  并允许同一 MCP 服务同时接入其中的多种数据库类型。
- **FR-022**: 当一个 logical database 绑定使用的名称与其底层物理数据库名称不同，
  系统 MUST 始终以 logical database 名称作为公共 discovery 和 execution 的路由键。

### Key Entities *(include if feature involves data)*

- **Direct Runtime Topology**: MCP 服务当前装载的全部 logical database 绑定及其统一运行边界。
- **Logical Database Binding**: 一个 logical `database` 名称与其目标数据库类型、连接目标、
  schema 语义、对象暴露边界和 capability 覆盖之间的绑定关系。
- **Database Availability State**: 某个 logical database 当前是否完成启动校验、是否暂时不可用、
  最近一次成功 metadata 装载时间以及最近一次失败原因的聚合状态。
- **Metadata Snapshot**: 某个 logical database 最近一次成功装载得到的可读 discovery 视图。
- **Session Routing Context**: 一个 MCP 会话当前绑定的 logical database、事务状态、
  savepoint 集合与自动提交状态的组合。
- **Redacted Diagnostic Record**: 面向运维的启动或运行诊断条目，包含逻辑库标识、
  失败阶段、状态、建议动作和经过脱敏的上下文。

### Assumptions

- `001-shardingsphere-mcp` 已定义的 V1 公共资源、工具、结果模型和错误语义继续生效；
  本特性不重写公共协议面，只扩展运行时装配范围。
- V1 对多数据库直连采用显式 logical database 名称和 fail-fast 启动策略，
  不引入“部分配置成功也照常启动”的默认行为。
- 服务成功启动后，单个数据库的短时不可用视为运行时故障，而不是启动配置错误。
- V1 仍以统一公共面和安全保守原则为先，不提供跨数据库 Join、跨数据库事务、
  分布式事务或自动 failover。
- 凭据来源可以由运行方决定，但公共输出和审计必须始终脱敏。

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 在验收环境中，同一个 MCP 服务成功接入至少 2 个独立数据库后，
  `list_databases` 返回 100% 已成功装载的 logical database 名称。
- **SC-002**: 在至少 2 种 V1 正式支持数据库类型上，调用方都能仅通过同一套
  MCP 公共 tools 完成对象发现和 `execute_query`，无需新增数据库特化客户端分支。
- **SC-003**: 100% 的启动错误验收样例在 endpoint 发布前 fail fast，
  包括重复 logical database 名称、不支持的数据库类型、缺失依赖和初次 metadata 装载失败。
- **SC-004**: 100% 的单库运行时不可用验收样例中，其他已装载 databases 继续可用，
  且目标库的实时访问请求统一返回 `unavailable`。
- **SC-005**: 100% 的 DDL / DCL 验收样例满足“当前会话立即可见、全局 60 秒内可见”，
  且不会导致无关 databases 的 metadata snapshot 被整体替换。
- **SC-006**: 100% 的审计与诊断验收样例都包含 logical database、
  操作类别和失败上下文，且 0 个样例泄露密码明文或带凭据的连接串。
