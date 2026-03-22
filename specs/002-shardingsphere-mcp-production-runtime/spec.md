# Feature Specification: ShardingSphere MCP Production Runtime Integration

**Feature Branch**: `[002-shardingsphere-mcp-production-runtime]`  
**Created**: 2026-03-22  
**Status**: Draft  
**Input**: User description: "补齐 ShardingSphere MCP 当前未完成的真实 runtime integration，使默认发行包兑现 PRD 愿景"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - 真实元数据发现默认可用 (Priority: P1)

作为 MCP 接入方，我希望默认发行包启动后就能从真实 ShardingSphere 运行时读取 logical database、schema、table、view、column 与 capability，
而不是只得到空结果或依赖测试 fixture，这样我接入一个 MCP 服务就能完成真实对象发现。

**Why this priority**: 没有真实 metadata，`list_databases`、`list_tables`、`search_metadata` 的产品价值无法成立，PRD 的统一发现目标无法落地。

**Independent Test**: 使用一个接入真实 ShardingSphere metadata 源的独立 MCP 发行包，执行 `initialize`、`list_databases`、`list_tables`、`describe_table` 与 `get_capabilities(database)`，
确认结果来自真实 logical database，而不是空 catalog 或内存 fixture。

**Acceptance Scenarios**:

1. **Given** MCP 发行包已通过配置接入一个包含多个 logical database 的真实 ShardingSphere 运行时，**When** 客户端调用 `list_databases`，**Then** 返回非空 logical database 列表。
2. **Given** 某个 logical database 暴露了真实 schema 与 table，**When** 客户端调用 `list_tables(database, schema)` 与 `describe_table(database, schema, table)`，**Then** 返回真实对象与字段信息。
3. **Given** 某个 database 的运行时对象能力与静态矩阵共同组装 capability，**When** 客户端调用 `get_capabilities(database)`，**Then** 返回与该 database 真实对象暴露边界一致的数据库级 capability。
4. **Given** 某个 database 未声明支持 `index`，**When** 客户端调用 `list_indexes` 或读取 index 相关 resource，**Then** 返回 `unsupported`，而不是空成功结果。

---

### User Story 2 - 真实 SQL 执行与事务语义默认可用 (Priority: P2)

作为 Agent 或平台调用方，我希望 `execute_query` 走真实 ShardingSphere parse / route / execute 链路，并继续保持统一结果模型、事务边界和 savepoint 语义，
这样同一个 MCP 服务才能真正承接查询、变更与事务控制。

**Why this priority**: 真实执行是 PRD 的核心承诺；如果 `execute_query` 只对内存 runtime 生效，MCP 只能演示协议，不能承接生产场景。

**Independent Test**: 在接入真实运行时的 MCP 服务上，对至少两种正式支持数据库分别执行 `SELECT`、DML、`BEGIN / COMMIT`、`SAVEPOINT` 或 `unsupported` 场景，
确认统一结果模型、错误模型和单数据库事务绑定行为符合契约。

**Acceptance Scenarios**:

1. **Given** MCP 已接入真实 ShardingSphere 执行链路，**When** 客户端调用 `execute_query(database, sql)` 执行单条 `SELECT`，**Then** 返回真实查询结果并遵守统一 `result_set` 模型。
2. **Given** 同一会话已在 database A 上进入事务态，**When** 后续 `execute_query` 指向 database B，**Then** 返回 `conflict`，而不是跨 database 执行。
3. **Given** 目标 database 不支持事务控制或 `savepoint`，**When** 客户端执行相关语句，**Then** 返回 `unsupported` 并与 database capability 保持一致。
4. **Given** 客户端提交 `USE`、`SET`、`COPY`、`LOAD`、`CALL` 或多语句输入，**When** `execute_query` 被调用，**Then** 返回统一拒绝错误，不进入真实执行链路。

---

### User Story 3 - 发行包与验收路径兑现 PRD 终态 (Priority: P3)

作为运维和平台团队，我希望 `distribution/mcp` 的默认启动路径、配置、文档与 E2E 验收都基于真实 runtime integration，
而不是空运行时骨架，这样发行包才能被部署、注册并供大模型稳定调用。

**Why this priority**: 即使核心代码支持真实 provider，如果默认发行路径仍然启动空 runtime，PRD 的产品交付面依然不成立。

**Independent Test**: 使用发行包启动真实 runtime integration 模式，完成 `initialize`、`list_databases`、`execute_query(SELECT)`、DDL/DCL 可见性验证、`DELETE` 关闭会话，
并确认文档提供的部署与注册步骤可以复现。

**Acceptance Scenarios**:

1. **Given** 发行包配置缺少真实 runtime provider 所需配置，**When** `bin/start.sh` 启动，**Then** 服务快速失败并输出明确错误，而不是以空 metadata/runtime 成功启动。
2. **Given** 发行包配置已接入真实 runtime provider，**When** 运维按 README 启动 MCP 服务，**Then** Quick Start 示例可返回真实 logical database 与查询结果。
3. **Given** 客户端通过 MCP 成功执行 DDL 或 DCL，**When** 当前会话再次读取相关 metadata，**Then** 当前会话立即可见，且全局可见性在 60 秒内满足 SLA。
4. **Given** 一个支持 MCP 的 host 已注册该 HTTP endpoint，**When** host 连接并完成 `initialize`，**Then** 能自动发现非空 tools/resources，并对真实 runtime 发起 follow-up 调用。

---

### Edge Cases

- 当真实运行时存在 logical database，但 metadata provider 暂时无法读取 schema / table 明细时，服务应返回清晰错误，而不是 silently 返回空列表。
- 当静态事务能力矩阵与运行时 metadata 暴露边界冲突时，服务应按固定组装顺序生成 capability，并留下可排查证据。
- 当 provider 配置了不受支持的 topology、缺少 JDBC driver 或 mode repository 无法连接时，发行包必须快速失败。
- 当真实执行链路返回数据库专有错误时，MCP 层必须映射到统一错误模型，不能把方言细节直接泄漏成公共契约。
- 当会话在活动事务中被 `DELETE` 或服务关闭时，未提交事务必须自动回滚，且会话不可恢复。

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: 默认发行包的生产启动路径 MUST 不再依赖空 `MetadataCatalog` 与空 `DatabaseRuntime` 作为成功启动的默认行为。
- **FR-002**: 系统 MUST 提供真实 runtime provider 装配路径，用于从 ShardingSphere 运行时读取 logical database、schema、table、view、column 与 database type 信息。
- **FR-003**: `list_databases`、`list_schemas`、`list_tables`、`list_views`、`list_columns`、`search_metadata`、`describe_table`、`describe_view` MUST 基于真实 metadata provider 返回结果。
- **FR-004**: 系统 MUST 提供真实执行适配层，使 `execute_query` 通过 ShardingSphere parse / route / execute 链路执行，而不是通过内存 map 模拟结果。
- **FR-005**: 系统 MUST 继续维持 PRD 约定的单语句限制、禁用命令集合、统一错误模型与统一结果模型。
- **FR-006**: database-level capability MUST 按固定顺序由静态事务矩阵、运行时 metadata 与部署覆盖共同组装，而不是只依赖静态矩阵。
- **FR-007**: 默认发行包在 provider 配置不完整、运行时不可达或依赖缺失时 MUST fail fast，并输出明确诊断信息。
- **FR-008**: 发行包 README 与 quickstart MUST 说明真实 runtime integration 的部署方式、配置要求、MCP host 注册方式与最小 smoke 流程。
- **FR-009**: 至少一个默认支持的生产 topology MUST 通过真实 E2E 验收闭环；首个正式 topology 为独立 MCP runtime 读取共享 ShardingSphere metadata 来源并调用真实执行链路。
- **FR-010**: 系统 MUST 保持 MCP 独立 runtime 形态，不把 MCP 直接嵌入 Proxy 或 JDBC 进程。
- **FR-011**: 结构变化与 DCL 变化的可见性 MUST 通过真实 metadata 刷新链路兑现“当前会话立即可见、全局 60 秒内可见”的契约。
- **FR-012**: 真实 runtime integration 的 E2E 验收 MUST 不依赖测试 fixture 直接构造 `MetadataCatalog` 与 `DatabaseRuntime` 作为唯一成功路径。
- **FR-013**: 若 host 通过 HTTP 连接该 MCP 服务，完成 `initialize` 后 MUST 能自动发现非空 tools/resources，并成功执行至少一个真实 metadata tool 与一个真实 `execute_query` 场景。

### Key Entities *(include if feature involves data)*

- **Runtime Provider Configuration**: 描述 MCP 发行包如何定位并接入真实 ShardingSphere metadata 来源、执行链路与依赖项的配置模型。
- **Runtime Metadata Provider**: 将真实 ShardingSphere metadata 投影为 MCP 所需 `MetadataCatalog` 的适配器。
- **Runtime Execution Adapter**: 将 MCP `execute_query` 请求转换为真实 ShardingSphere parse / route / execute 调用并映射统一结果模型的适配器。
- **Capability Assembly Input**: 由静态事务矩阵、运行时 metadata 与部署覆盖组成的 capability 组装输入。
- **Production Launch Mode**: 指发行包通过 provider 配置启动真实 runtime integration 的运行模式，与空骨架或测试 fixture 模式区分。

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 在真实运行时验收环境中，默认发行包启动后的 `list_databases` 返回至少 1 个 logical database，而不是空列表。
- **SC-002**: 在至少 2 种正式支持数据库上，`execute_query(SELECT)`、DML 与事务控制验收样例均通过，不依赖内存 fixture 注入。
- **SC-003**: 100% 的 follow-up 真实验收样例继续满足统一错误模型与统一结果模型要求，不因接入真实 runtime 回退为数据库专有行为。
- **SC-004**: 100% 的生产启动失败场景在缺少 provider 配置、驱动或 metadata 来源不可达时都以 fail-fast 方式暴露，而不是空启动成功。
- **SC-005**: 通过 MCP 成功提交的 DDL / DCL 验收样例中，当前会话立即可见率达到 100%，全局 60 秒内可见率达到 100%。
- **SC-006**: 支持 MCP 的 host 按 README 注册 HTTP endpoint 后，首次接入即可自动发现非空 tools/resources，并成功完成至少一个真实 metadata 调用和一个真实 SQL 调用。
