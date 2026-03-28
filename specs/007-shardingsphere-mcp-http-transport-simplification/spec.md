# Feature Specification: ShardingSphere MCP HTTP Transport Simplification After SDK Reuse

**Feature Branch**: `[007-shardingsphere-mcp-http-transport-simplification]`  
**Created**: 2026-03-28  
**Status**: Implementation-aligned draft  
**Input**: User description: "好的，用speckit梳理任务"

## Scope Statement

本 follow-up 建立在 `006` 已完成的 HTTP SDK reuse 收敛之上，目标不再是继续“多拆几个 helper”，
而是把 MCP HTTP transport 的**总复杂度**继续压下去：

- 保持官方 MCP Java SDK `HttpServletStreamableServerTransportProvider` 作为 transport core
- 进一步区分哪些能力已经是 SDK 自己的，哪些是独立的本地策略，哪些必须留在 ShardingSphere
- 清理只被单点调用、且不构成稳定抽象的 one-off helper，减少文件跳转和认知负担
- 保持现有 public contract、兼容行为、runtime cleanup 与 classloader 行为零损失

本特性的核心不是再做一次协议改造，而是把 `006` 的实现从“边界正确”继续推进到“结构更简单、更稳定”。

## User Scenarios & Testing *(mandatory)*

### User Story 1 - HTTP transport 的总复杂度继续下降，而不是转移到更多小文件里 (Priority: P1)

作为维护者，我希望 `StreamableHttpMCPServlet` 及其周边 helper 只保留真正有稳定抽象价值的类型，
这样后续阅读和修改 HTTP transport 时，不需要在多个 one-off 文件之间频繁跳转。

**Why this priority**: `006` 已经完成“SDK 复用优先”的目标，但当前实现里仍有几个只服务单一调用点的 helper；
如果这些 helper 不能表达稳定的概念，拆出来反而增加总认知成本。

**Independent Test**: 在行为完全不变的前提下，HTTP transport 生产代码中的单点 helper 数量下降，
而现有 HTTP regression matrix 保持通过。

**Acceptance Scenarios**:

1. **Given** 当前 HTTP transport 已完成 SDK reuse，**When** 继续做结构收敛，
   **Then** 只被单一 servlet 调用、且不构成稳定概念边界的 helper 应优先被合回更合适的位置。
2. **Given** 某个 helper 表达的是稳定的本地策略或复用边界，**When** 评估是否保留独立类，
   **Then** 该 helper 可以继续独立存在，但需要有明确的概念理由。
3. **Given** 某个 helper 只是为了包装 1 个 request/response 或 1 次 initialize 归一化，
   **When** 它没有跨类复用价值，**Then** 不应为了“看起来更薄”而单独成文件。

---

### User Story 2 - SDK 能力边界与 ShardingSphere glue 边界进一步清晰 (Priority: P1)

作为维护者，我希望每一段 HTTP transport 逻辑都能明确归类为“SDK native”、“standalone local policy”
或 “ShardingSphere-owned glue”，这样后续做删改时不需要反复判断该不该留。

**Why this priority**: 当前争议的根源不是单个类长不长，而是哪些逻辑应该继续留在本地实现中；
只有边界真正清楚，结构简化才不会变成误删能力。

**Independent Test**: 形成一份清楚的 keep / inline / do-not-inline 矩阵，并让设计文档与任务拆分同步反映这些边界。

**Acceptance Scenarios**:

1. **Given** 某项能力已由 SDK 原生覆盖，**When** 评估本地代码，**Then** 不再保留重复 wrapper 或重复 transport logic。
2. **Given** 某项能力不能零损失吸收到 SDK、但又形成稳定本地概念，**When** 保留本地实现，**Then** 其职责应收敛为最薄的本地策略类型。
3. **Given** 某项能力是 ShardingSphere 当前 contract 或 runtime lifecycle 的一部分，
   **When** 进行简化，**Then** 该能力必须继续保留在 ShardingSphere 本地，不因“追求纯 SDK”而丢失。

---

### User Story 3 - 结构简化不能引入任何行为回退 (Priority: P1)

作为客户端和运行方，我希望这轮结构简化不会改变 initialize 响应头、follow-up protocol fallback、
missing/blank `Accept`、loopback `Origin`、managed session cleanup 和 classloader 兼容行为。

**Why this priority**: 这轮 follow-up 的价值完全来自“更简单但不变更行为”；只要行为回退，
它就不再是结构优化。

**Independent Test**: 复用 `006` 已建立的 HTTP regression matrix，并把它作为本次简化的最终验收门槛。

**Acceptance Scenarios**:

1. **Given** initialize 成功，**When** 结构调整完成，**Then** `MCP-Session-Id` 和 `MCP-Protocol-Version` 响应头继续保持不变。
2. **Given** follow-up 请求缺失 protocol header，**When** session 合法，**Then** 服务继续回退到协商版本。
3. **Given** 客户端缺失或留空 `Accept`，**When** 请求进入 HTTP runtime，**Then** 当前兼容行为继续保持。
4. **Given** runtime 执行 `DELETE` 或 shutdown，**When** 存在 managed session，**Then** ShardingSphere runtime cleanup 继续发生。
5. **Given** driver 仅通过 classpath 可见，**When** HTTP runtime 处理真实请求，**Then** classloader 行为不退化。

### Edge Cases

- 不允许为了减少文件数而把 SDK SPI adapter 和 ShardingSphere runtime glue 混成一个模糊概念。
- 不允许依赖 SDK package-private internal helper 来减少本地代码量。
- 不允许用 SDK 默认 security validator 替代当前 loopback host-only 语义，除非零损失验证证明其完全等价。
- 不允许把 `006` 中的零损失兼容 shim 误判为“纯冗余”并直接删除。

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: 系统 MUST 继续以官方 `HttpServletStreamableServerTransportProvider` 作为 HTTP transport core。
- **FR-002**: 系统 MUST 将当前 HTTP transport 代码按三类明确归档：SDK native、standalone local policy、ShardingSphere-owned glue。
- **FR-003**: 系统 MUST 优先合并只被单一生产调用点使用、且不构成稳定抽象的 helper。
- **FR-004**: 系统 MUST 保留真正代表稳定本地概念边界的独立类型，前提是其独立存在能表达清晰职责。
- **FR-005**: 系统 MUST NOT 使用 MCP SDK 的 package-private internal helper 作为实现依赖。
- **FR-006**: 系统 MUST NOT 用 `DefaultServerTransportSecurityValidator` 替换当前 loopback `Origin` 语义，除非验证证明状态码、错误语义和匹配规则零损失。
- **FR-007**: 系统 MUST 继续保留 initialize `MCP-Protocol-Version` 响应头行为。
- **FR-008**: 系统 MUST 继续保留 follow-up protocol fallback 与 mismatch contract。
- **FR-009**: 系统 MUST 继续保留 missing/blank `Accept` 兼容行为，除非有等价承接方案和验证证据。
- **FR-010**: 系统 MUST 继续保留 managed session cleanup 与 shutdown cleanup。
- **FR-011**: 系统 MUST 继续保留 classpath-driver / classloader 兼容性。
- **FR-012**: 本特性 MUST NOT 改变现有 public endpoint path、HTTP method surface、status code contract、session header contract 和 stateful session model。
- **FR-013**: 本特性 MUST 用现有 HTTP regression matrix 作为最终验收，而不是仅用编译通过或局部单测代替。

### Key Entities *(include if feature involves data)*

- **SDK native transport mechanic**: SDK 已直接负责的 routing、Accept 校验、session map、`DELETE`、SSE、graceful close。
- **Standalone local policy**: 不能零损失吸收到 SDK、但又值得独立表达的本地策略类型。
- **ShardingSphere-owned glue**: 固定协议版本、initialize 响应头、follow-up protocol/session contract、managed session cleanup、classloader guard。
- **One-off helper**: 只服务单一调用点、且不构成稳定概念边界的小型包装类或工具类。

### Assumptions

- `006` 已经完成 HTTP SDK reuse 的主收敛目标，本次 follow-up 不重新打开其协议结论。
- 当前争议点主要在“总复杂度是否真的下降”，而不是 transport core 是否仍由 SDK 提供。
- 若某类代码不能被 SDK 零损失吸收，就应以更简单的本地结构保留，而不是为了“类更少”而牺牲可读性。

## Non-Goals

- 不修改 MCP tools/resources/public protocol contract。
- 不切换 HTTP transport 技术栈或 servlet 容器。
- 不把这次结构简化扩展成新的安全策略或新的运行时生命周期设计。
- 不为了减少文件数而牺牲职责边界的清晰性。

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% 的现有 HTTP regression tests 在结构简化后保持通过。
- **SC-002**: 100% 的剩余生产类都能明确映射到 SDK native、standalone local policy 或 ShardingSphere-owned glue 之一。
- **SC-003**: 当前 HTTP transport 中仅单点使用、且无稳定抽象价值的 helper 文件数量低于 `006` 完成后的基线。
- **SC-004**: 保留下来的独立 helper 必须要么表达稳定本地概念，要么被多个生产调用点复用。
- **SC-005**: 设计文档与任务清单中对“可继续内化到 SDK 的能力”和“必须保留在 ShardingSphere 的能力”给出一致结论。
