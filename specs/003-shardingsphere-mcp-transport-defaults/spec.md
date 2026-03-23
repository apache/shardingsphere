# Feature Specification: ShardingSphere MCP Transport Default Realignment

**Feature Branch**: `[003-shardingsphere-mcp-transport-defaults]`  
**Created**: 2026-03-22  
**Status**: Draft  
**Input**: User description: "默认开启 stdio，HTTP 默认关闭，但两个协议都必须可以使用"

## Scope Statement

本特性只重定义 MCP 发行包与默认启动路径的 transport 默认值与运维文档，不重写现有 wire protocol，不新增第三种 transport，也不移除 HTTP 能力。

本特性的核心是：

- 发行包默认改为 `stdio = true`
- 发行包默认改为 `http = false`
- `stdio` 与 `Streamable HTTP` 两条 transport 继续保留并可独立启用
- 允许双开，但不再把双开或 HTTP 作为默认启动形态

## User Scenarios & Testing *(mandatory)*

### User Story 1 - 默认启动走 STDIO (Priority: P1)

作为本地 Agent、IDE 或 CLI 集成方，我希望 MCP 发行包默认启动时只开启 STDIO，而不是同时打开 HTTP 监听，这样我可以直接以本地子进程模式接入，而不必先处理网络端口、网关或本地 HTTP 面暴露。

**Why this priority**: 当前数据库类 MCP 生态中，本地集成场景大量以 `stdio` 作为默认 transport；ShardingSphere 的默认发行包更应先匹配本地 Agent 接入，再把 HTTP 作为显式启用能力。

**Independent Test**: 使用发行包默认 `conf/mcp.yaml` 启动 MCP runtime，确认 STDIO 路径可完成 `initialize`、tool 调用与 session 关闭，同时本地不存在 `/mcp` HTTP 监听。

**Acceptance Scenarios**:

1. **Given** 使用发行包默认配置启动 MCP runtime，**When** 读取 transport 开关，**Then** `transport.stdio.enabled = true` 且 `transport.http.enabled = false`。
2. **Given** 默认配置启动成功，**When** 本地客户端通过 STDIO 建立会话并调用 tool，**Then** 调用成功且不依赖 HTTP listener。
3. **Given** 默认配置启动成功，**When** 运维尝试访问默认 `http://127.0.0.1:18088/mcp`，**Then** 不应存在由 MCP runtime 打开的 HTTP endpoint。

---

### User Story 2 - HTTP 保持可显式启用 (Priority: P2)

作为需要远程 MCP 服务入口的运维或平台团队，我希望仍然可以通过配置显式启用 HTTP transport，并继续获得既有的 session、header、SSE 与 follow-up 行为，这样默认值调整不会破坏远程 host 接入。

**Why this priority**: 默认值改为 `stdio` 不能演变成对远程部署能力的回退；HTTP 仍然是远程 host、网关与独立部署拓扑的必要入口。

**Independent Test**: 将 `transport.http.enabled` 设为 `true`、`transport.stdio.enabled` 设为 `false` 后启动发行包，验证 `/mcp` Streamable HTTP endpoint、session 协商与 follow-up 请求全部继续可用。

**Acceptance Scenarios**:

1. **Given** 配置中显式启用 HTTP、关闭 STDIO，**When** 启动发行包，**Then** `/mcp` 可以完成 `initialize` 与 follow-up 调用。
2. **Given** HTTP 模式已启用，**When** 客户端打开 `GET /mcp` SSE 流或发送 `DELETE /mcp`，**Then** 行为与现有 HTTP contract 保持一致。
3. **Given** HTTP 模式已启用，**When** 客户端省略必需 session header 或传入非法 origin，**Then** 继续返回既有拒绝行为，而不是出现默认值切换导致的 contract 漂移。

---

### User Story 3 - 双协议都可用且文档一致 (Priority: P3)

作为维护者，我希望 `stdio only`、`http only`、`stdio + http` 三种有效启动矩阵都被文档化并具备验证路径，同时保留 `both false` 的 fail-fast 约束，这样 transport 能力、默认值与文档不会继续分叉。

**Why this priority**: 当前仓库中的设计文档、README 与默认配置已出现不一致；如果只改默认 YAML 而不补齐设计与验证矩阵，后续实现与文档还会再次漂移。

**Independent Test**: 执行 transport 启动矩阵测试，覆盖 `stdio only`、`http only`、`dual enabled`、`both disabled` 四个分支。

**Acceptance Scenarios**:

1. **Given** `stdio = true` 且 `http = true`，**When** 启动 runtime，**Then** 两条 transport 都可使用。
2. **Given** `stdio = false` 且 `http = false`，**When** 启动 runtime，**Then** 服务 fail fast，并输出 `At least one transport must be enabled.` 或等价诊断。
3. **Given** 仓库 README、技术设计与 quickstart 被更新，**When** 审阅者按文档操作，**Then** 能区分默认模式、HTTP 远程模式与双开模式。

---

### Edge Cases

- 默认 `stdio only` 启动时，日志必须清晰输出 HTTP 未启用，而不是沉默地“没起服务”。
- `http only` 启动时，STDIO runtime 不应被错误地标记为运行中。
- `stdio + http` 双开时，必须继续共享同一套 runtime wiring，而不是复制两套不一致的 tool/resource 装配。
- `both false` 的失败必须发生在启动早期，而不是部分 transport 已启动后再回滚。
- 本特性不改变 `Streamable HTTP` 的 header、session、SSE、origin 校验与 close 语义。

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: `distribution/mcp/src/main/resources/conf/mcp.yaml` 的默认 transport 配置 MUST 设为 `stdio.enabled = true`、`http.enabled = false`。
- **FR-002**: 默认启动路径 MUST 只启动 STDIO runtime，不创建 HTTP listener。
- **FR-003**: 系统 MUST 继续支持通过显式配置启用 `Streamable HTTP` transport。
- **FR-004**: 系统 MUST 继续支持 `stdio` 与 `Streamable HTTP` 同时启用。
- **FR-005**: 当两个 transport 开关同时为 `false` 时，系统 MUST fail fast。
- **FR-006**: 默认值变更 MUST 不修改现有 `Streamable HTTP` contract、STDIO API surface 或 session 语义。
- **FR-007**: 启动日志 MUST 输出 effective transport state，至少包含 `stdio` 与 `http` 的 enablement 状态。
- **FR-008**: README、技术设计与 quickstart MUST 明确区分三种合法启动模式：`stdio only`、`http only`、`dual enabled`。
- **FR-009**: 文档 MUST 将 `stdio only` 标为默认发行模式，将 `http only` 标为远程部署/host 接入模式。
- **FR-010**: 文档 MUST 明确说明“默认关闭 HTTP”不等于“移除 HTTP 能力”。
- **FR-011**: transport 启动矩阵测试 MUST 覆盖 `stdio only`、`http only`、`dual enabled` 与 `both disabled` 四个分支。
- **FR-012**: 包装脚本与配置加载路径 MUST 保持兼容现有 `transport.http.enabled` 与 `transport.stdio.enabled` 字段，不引入破坏性配置重命名。
- **FR-013**: 若运维显式启用 HTTP，现有 `/mcp` endpoint 路径、session-header 规则与 follow-up 行为 MUST 保持不变。

### Key Entities *(include if feature involves data)*

- **Transport Switch**: `transport.http.enabled` 与 `transport.stdio.enabled` 两个布尔开关。
- **Launch Profile**: `stdio only`、`http only`、`dual enabled`、`both disabled` 四种 transport 启动组合。
- **Effective Transport State**: 启动后实际生效的 transport 组合及其日志输出。

## Non-Goals

- 不引入 WebSocket、gRPC、named pipe 等新 transport。
- 不修改 `Streamable HTTP` 的 `/mcp` contract。
- 不改变 HTTP 鉴权、网关与外部网络边界策略。
- 不把 MCP 直接嵌入 Proxy 或 JDBC 进程。

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 默认发行配置可被审阅为 `stdio = true`、`http = false`，且与 README/设计文档一致。
- **SC-002**: 默认发行配置启动后，STDIO smoke 路径 100% 可用，且 MCP runtime 不暴露 HTTP listener。
- **SC-003**: 显式启用 HTTP 的启动路径 100% 通过既有 HTTP integration smoke。
- **SC-004**: `stdio + http` 双开路径 100% 可启动并完成至少一次 STDIO 与 HTTP 调用。
- **SC-005**: `both false` 路径 100% fail fast，且诊断信息可被运维直接识别。
