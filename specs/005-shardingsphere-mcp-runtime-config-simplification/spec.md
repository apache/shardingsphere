# Feature Specification: ShardingSphere MCP Runtime Configuration Simplification

**Feature Branch**: `[005-shardingsphere-mcp-runtime-config-simplification]`  
**Created**: 2026-03-24  
**Status**: Draft  
**Input**: User description: "收敛 runtime 配置结构，去掉手工能力布尔值，配置模型与运行时一一对应"

## Scope Statement

本 follow-up 只收敛 MCP direct JDBC runtime 的 operator-facing 配置契约，不重写
MCP V1 公共协议，不引入新的 transport，也不改变 direct multi-database 的公共
`database` 路由语义。

本特性的核心是：

- 让 `runtimeDatabases` 成为 single-db 与 multi-db 的统一 canonical 入口
- 去掉 `runtime` 包裹层和 `runtime.databaseDefaults` 这类 YAML-only sugar
- 移除 `schemaPattern` 等 operator-facing schema 发现配置，改由 JDBC metadata 自动发现
- 把 `driverClassName` 降为 optional override
- 把 `supportsCrossSchemaSql` 与 `supportsExplainAnalyze` 从常规 operator 配置中移除
- 对 legacy `runtime.*` keys 和 legacy capability booleans 提供明确拒绝诊断

## User Scenarios & Testing *(mandatory)*

### User Story 1 - 用一套 canonical 配置表达 direct runtime (Priority: P1)

作为运行方，我希望 single-db 和 multi-db 都通过同一套 `runtimeDatabases`
结构配置，而不是在 `runtime.props`、`runtime.databases` 和其他 YAML 包裹层之间切换，
这样我可以更稳定地维护发行包配置、示例文档和运维自动化。

**Why this priority**: 当前 direct runtime 的主要问题不是“字段不够多”，而是
“YAML 模型和运行时模型不一致”；把 canonical 结构收成 `runtimeDatabases`
可以直接消除这层抽象错位。

**Independent Test**: 用 canonical `runtimeDatabases` 分别配置 1 个和 2 个
logical databases，完成配置加载、启动和 discovery smoke，不再依赖任何 `runtime.*`
别名或 shared-defaults 机制。

**Acceptance Scenarios**:

1. **Given** canonical YAML 只使用 `runtimeDatabases`，**When** 加载配置，
   **Then** 运行时得到稳定的 logical database 拓扑。
2. **Given** 同一个服务只接入一个数据库，**When** 运维使用
   `runtimeDatabases.logic_db`，**Then** 服务行为与旧 single-db direct runtime
   路径等价。
3. **Given** 旧配置仍使用 `runtime.props`，**When** 配置加载，
   **Then** 系统 fail fast 并明确提示改用 `runtimeDatabases`。
4. **Given** 旧配置仍使用 `runtime.databaseDefaults` 或 `runtime.databases`，
   **When** 配置加载，**Then** 系统 fail fast 并明确提示改用顶级 `runtimeDatabases`。

---

### User Story 2 - 数据库能力自动推导，配置只保留部署输入 (Priority: P2)

作为运行方，我希望 `supportsCrossSchemaSql`、`supportsExplainAnalyze` 这类数据库
能力由系统根据 database type、数据库版本和运行时 metadata 自动推导，而不是由我手填，
这样 capability 结果和实际行为更一致，也更不容易被配置写错。

**Why this priority**: 这两个字段本质上是数据库能力，不是普通连接参数；
把它们暴露在 operator 配置里会让 capability contract 变成手工声明，而不是运行时事实。

**Independent Test**: 在未配置 capability booleans 的情况下启动 direct runtime，
验证 `get_capabilities(database)` 仍返回稳定的 capability 结果，且
`EXPLAIN ANALYZE` 的执行拦截使用自动推导值。

**Acceptance Scenarios**:

1. **Given** operator 配置未显式提供 `supportsCrossSchemaSql` 与
   `supportsExplainAnalyze`，**When** 客户端调用 `get_capabilities(database)`，
   **Then** 返回的 capability 来自确定性的自动推导，而不是空值或缺失字段。
2. **Given** 某 database type 或 database version 不支持
   `EXPLAIN ANALYZE`，**When** 客户端通过 `execute_query` 提交该语句，
   **Then** 服务按自动推导结果返回 `unsupported`，而不是要求 operator 预先手填布尔值。
3. **Given** operator 未配置 `driverClassName`，且 JDBC driver 已通过
   `lib/` 或 `ext-lib/` 进入 classpath，**When** 服务启动，**Then** direct
   runtime 仍能通过 JDBC 自动发现建立连接。
4. **Given** operator 显式配置了错误的 `driverClassName`，**When** 服务启动，
   **Then** 系统快速失败并输出明确的 driver 诊断，而不是退回到模糊的连接错误。

---

### User Story 3 - 迁移错误提示明确，文档与默认配置统一 (Priority: P3)

作为维护者，我希望旧 `runtime.*` 写法和 legacy capability booleans 都能得到
清晰的拒绝诊断，并且默认 `mcp.yaml`、README 与技术设计只展示新的 canonical 写法，
这样我可以避免新用户继续学到已经废弃的结构。

**Why this priority**: 这次变更触及的是 operator-facing YAML 契约；如果文档和
实现继续对旧结构含糊其辞，reviewer 和用户都很难判断什么才是现行模型。

**Independent Test**: 分别验证 canonical 配置、legacy `runtime.*` 和 legacy
capability booleans 的加载行为与诊断，并确认默认发行配置与 README 只展示
`runtimeDatabases`。

**Acceptance Scenarios**:

1. **Given** legacy `runtime.props`、`runtime.defaults`、`runtime.databaseDefaults`
   或 `runtime.databases` 被使用，**When** 配置加载，**Then** 系统 fail fast 并输出明确迁移提示。
2. **Given** canonical keys 与 legacy `runtime.*` keys 在同一配置中混用，
   **When** 配置加载，**Then** 系统快速失败并指出冲突来源。
3. **Given** 默认发行包配置、README 和 follow-up quickstart 已更新，
   **When** 审阅者按文档配置 direct runtime，**Then** 看到的唯一推荐写法是
   `runtimeDatabases`。
4. **Given** legacy capability booleans 仍出现在某个 runtime database entry 中，
   **When** 配置加载，**Then** 系统 fail fast 并明确标注能力字段改为自动推导。

### Edge Cases

- `runtime.props` 与 `runtimeDatabases` 同时出现时，系统必须失败，而不是隐式选择优先级。
- `runtime.defaults`、`runtime.databaseDefaults` 或 `runtime.databases` 任何一个出现时，系统必须失败。
- `driverClassName` 为空时，系统必须允许走 JDBC 自动发现，而不是把它当成缺失必填项。
- 如果 JDBC metadata 无法可靠提供数据库版本，capability 推导必须回退到安全的
  type-level defaults，而不是重新要求 operator 手工提供 capability booleans。
- legacy capability booleans 在 `runtimeDatabases` 下出现时必须失败，而不是 silently 覆盖自动推导结果。
- 如果 JDBC 连接无法暴露当前 schema，系统必须回退到 `DatabaseMetaData.getSchemas()`
  或空 schema 发现，而不是重新要求 operator 配置 schema 过滤条件。

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: 系统 MUST 使用 `runtimeDatabases` 作为 transport 之外的 direct runtime 顶级配置入口。
- **FR-002**: 对 direct JDBC runtime 而言，`runtimeDatabases` MUST 成为 single-db 与 multi-db 的统一 canonical 配置入口。
- **FR-003**: `runtime.props`、`runtime.defaults`、`runtime.databaseDefaults` 和 `runtime.databases`
  MUST NOT 继续作为 canonical 或 migration alias 被加载；系统 MUST fail fast 并输出明确诊断。
- **FR-004**: 每个 logical database binding MUST 至少显式声明 `databaseType` 与 `jdbcUrl`。
- **FR-005**: 每个 logical database binding MAY 显式声明 `username`、`password` 和 `driverClassName`。
- **FR-006**: `driverClassName` MUST 为 optional override；若未配置，系统 MUST 尝试依赖 classpath 与
  `DriverManager` 自动发现 JDBC driver。
- **FR-007**: 若显式配置了 `driverClassName` 且该类不可用，系统 MUST fail fast 并输出 driver-specific 诊断。
- **FR-008**: `schemaPattern` MUST NOT 继续作为 canonical direct runtime operator 配置字段。
- **FR-009**: direct runtime loader MUST 通过 JDBC metadata 自动推导 schema 范围，而不是要求 operator
  继续声明额外的 schema 发现配置。
- **FR-010**: `supportsCrossSchemaSql` 与 `supportsExplainAnalyze` MUST NOT 继续作为 canonical direct runtime
  operator 配置字段。
- **FR-011**: direct runtime 的 `supportsCrossSchemaSql` 与 `supportsExplainAnalyze` MUST 由系统根据
  capability matrix defaults、数据库版本与运行时 metadata 事实自动推导。
- **FR-012**: 若无法可靠获得数据库版本事实，系统 MUST 回退到 deterministic type-level defaults，而不是要求
  operator 手填 capability booleans。
- **FR-013**: `get_capabilities(database)` MUST 返回自动推导后的 capability，而不是用户手工布尔值的直接映射。
- **FR-014**: `execute_query` 对 `EXPLAIN ANALYZE` 的接受或拒绝 MUST 使用自动推导后的 capability。
- **FR-015**: legacy capability booleans 在 canonical 配置中 MUST fail fast，并输出明确诊断。
- **FR-016**: loader 与 YAML swappers MUST 在进入 runtime launch 之前完成 legacy runtime keys 与
  legacy capability booleans 的 validation and rejection。
- **FR-017**: 默认发行配置、README、中文 README 和新 follow-up quickstart MUST 只展示 `runtimeDatabases`
  canonical runtime 配置结构。
- **FR-018**: direct runtime 的配置收敛 MUST 不改变 MCP V1 的公共 resources、tools、结果模型、错误模型与
  `database` 路由键。
- **FR-019**: 相关测试 MUST 覆盖 canonical config、legacy rejection、optional driver、derived capability、
  `EXPLAIN ANALYZE` gating 和 migration diagnostics。

### Key Entities *(include if feature involves data)*

- **Canonical Direct Runtime Configuration**: 顶级 `runtimeDatabases` 组成的 direct runtime canonical YAML 结构。
- **Logical Database Binding Configuration**: 一个 logical database 的 direct JDBC binding，包含连接输入与
  optional driver override。
- **Runtime Schema Discovery Facts**: direct runtime 在启动时通过 JDBC metadata 识别出的 schema 列表。
- **Derived Database Capability Facts**: 系统根据 database type、database version 和运行时 metadata
  生成的 capability 事实集合。
- **Legacy Runtime Alias Input**: `runtime.props`、`runtime.defaults`、`runtime.databaseDefaults`、
  `runtime.databases` 和 legacy capability booleans 等被显式拒绝的历史输入。

### Assumptions

- 本特性只重构 direct JDBC runtime 配置契约，不扩展 transport 或 MCP 公共协议面。
- 一一对应模型的目标是减少 YAML 和运行时对象之间的抽象层，而不是继续保留兼容转换。
- `DatabaseType` SPI 目前不负责暴露 JDBC driver class，因此 `driverClassName`
  不能简单从 `databaseType` 接口直接推导。
- capability 自动推导以 deterministic 和 reviewable 为优先，不采用会产生副作用的
  “试执行 SQL 探测”。

## Non-Goals

- 不修改 `transport.http` / `transport.stdio` 契约或默认值。
- 不引入新的 runtime provider 抽象或新的 MCP public tools/resources。
- 不在本特性中引入跨数据库事务、SQL federation 或跨数据库 capability 聚合。
- 不继续保留 legacy `runtime.*` 写法的兼容加载行为。

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% 的新 direct runtime 示例和默认发行配置都只使用 `runtimeDatabases`。
- **SC-002**: 100% 的 canonical single-db 和 multi-db 配置样例都可通过同一条 default launch path 成功加载。
- **SC-003**: 100% 的 `EXPLAIN ANALYZE` 验收路径由系统自动 capability 推导决定，不依赖 operator 手工布尔值。
- **SC-004**: 100% 的 legacy `runtime.*` 和 legacy capability booleans 验收样例都能得到明确拒绝诊断，
  不存在 silent fallback。
- **SC-005**: 100% 的 optional-driver 验收样例在 driver 位于 classpath 时可正常启动，显式错误 driver class
  的样例都能快速失败并输出可识别诊断。
