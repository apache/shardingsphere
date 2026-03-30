# Feature Specification: ShardingSphere MCP HTTP SDK Reuse Without Capability Loss

**Feature Branch**: `[006-shardingsphere-mcp-http-sdk-reuse]`  
**Created**: 2026-03-28  
**Status**: Implementation-aligned draft  
**Input**: User description: "使用speckit继续做，尽量复用官方的SDK能覆盖到的能力，但是在复用官方SDK的时候，要确保现有的能力没有任何的损失"

## Scope Statement

本 follow-up 只重构 MCP HTTP transport 的实现边界：尽量把已经被官方 MCP Java SDK
`HttpServletStreamableServerTransportProvider` 覆盖的能力交还 SDK，自定义代码只保留
ShardingSphere 当前 contract 和 runtime lifecycle 真正需要的部分。

本特性的核心是：

- 复用官方 SDK 已覆盖的 HTTP routing、Accept 校验、SDK session 管理、
  `DELETE` 语义和 graceful close 行为
- 把本地 loopback `Origin` 校验优先收敛到 SDK 的 `securityValidator`
  标准扩展点
- 保留 ShardingSphere 独有的 managed session lifecycle、协议版本 contract、
  initialize 响应头和零损失兼容行为
- 删除没有实际收益的重复包装或冗余配置
- 不改变现有 MCP HTTP 对外 contract、状态码、header 语义和会话资源释放语义

## User Scenarios & Testing *(mandatory)*

### User Story 1 - 能交给官方 SDK 的 HTTP transport 逻辑尽量交回 SDK (Priority: P1)

作为维护者，我希望 `StreamableHttpMCPServlet` 不再重复实现官方 SDK
已经稳定覆盖的 transport mechanics，而是优先复用 SDK 的 endpoint dispatch、
Accept 校验、session map、`DELETE` 和 graceful close，这样 HTTP runtime 的维护面更小，
也更容易跟上 SDK 行为演进。

**Why this priority**: 当前 servlet 里既有 ShardingSphere 真正独有的 contract glue，
也有和 SDK 重叠的 transport 逻辑；如果不先收敛边界，后续协议升级和 bugfix 都会继续双份维护。

**Independent Test**: 在不改变现有对外行为的前提下，HTTP runtime 仍通过现有 initialize、
follow-up、SSE、`DELETE` 和 shutdown 验收，但 transport 级重复逻辑减少，`Origin`
校验优先走 SDK `securityValidator`。

**Acceptance Scenarios**:

1. **Given** `/gateway` HTTP runtime 已启动，**When** 客户端发送 `POST` / `GET` /
   `DELETE` 请求，**Then** endpoint dispatch、Accept 校验和 SDK session 行为优先由
   官方 SDK provider 负责，而不是由 ShardingSphere 重复实现。
2. **Given** runtime 绑定 loopback host，**When** 客户端显式携带非 loopback
   `Origin`，**Then** 请求仍被拒绝，且该边界校验优先通过 SDK `securityValidator`
   或等价官方扩展点承载。
3. **Given** 当前实现中显式传入空 transport context extractor，**When** 重构完成，
   **Then** 不再保留等价但冗余的空配置。

---

### User Story 2 - 对外 HTTP contract 与现有兼容行为零损失 (Priority: P1)

作为 MCP 客户端和运维方，我希望这次 refactor 不改变任何已经存在的 HTTP contract
和兼容行为，这样我可以得到更薄的实现，但不需要修改现有 client、gateway、
smoke 脚本或运维认知。

**Why this priority**: 本次目标是“SDK 复用优先”，不是“趁机改协议”；只要行为丢一项，
这次收敛就会从实现优化变成对外变更。

**Independent Test**: 复用现有 HTTP integration smoke，并新增缺省 `Accept`、
lowercase headers、missing protocol header fallback 和 loopback `Origin`
的零损失回归用例。

**Acceptance Scenarios**:

1. **Given** 客户端发送 initialize 请求，**When** 初始化成功，
   **Then** 响应仍返回 `MCP-Session-Id`，并继续通过响应头返回
   `MCP-Protocol-Version`。
2. **Given** follow-up 请求携带 `MCP-Session-Id`，**When** `MCP-Protocol-Version`
   与当前协商版本不一致，**Then** 服务仍返回 `400 Bad Request`。
3. **Given** follow-up 请求携带合法 session 但省略 `MCP-Protocol-Version`，
   **When** 请求被处理，**Then** 服务仍按当前 contract 回退到协商版本，而不是拒绝。
4. **Given** 客户端使用小写 `mcp-session-id` / `mcp-protocol-version` header，
   **When** 发起 follow-up 请求，**Then** 请求仍可成功处理。
5. **Given** 客户端省略或留空 `Accept` header，**When** 发送 initialize 或
   follow-up 请求，**Then** 服务行为仍与当前实现兼容，不因 SDK 复用而退化。
6. **Given** runtime 绑定 loopback host，**When** 请求显式携带非法 `Origin`，
   **Then** 服务仍返回 `403 Forbidden`。
7. **Given** 客户端调用 `DELETE /mcp`，**When** 请求成功，
   **Then** session 仍被关闭，且后续 `GET` / follow-up 请求继续得到
   `404 Not Found`。

---

### User Story 3 - ShardingSphere runtime lifecycle 与 driver/classloader 兼容性不退化 (Priority: P2)

作为运行方，我希望 HTTP transport refactor 不会破坏 ShardingSphere 的事务、
metadata refresh、database runtime 清理，也不会让 embedded Tomcat + classpath
driver 的运行兼容性退化，这样 transport 代码更薄，但生产行为不变。

**Why this priority**: 真正不能交给 SDK 的部分是 ShardingSphere 自己的 runtime
state 和资源清理；另外当前实现有 TCCL bridge，哪怕它只是兼容层，也必须先用回归测试证明
“可以删”，不能凭感觉删除。

**Independent Test**: `DELETE` 与 server shutdown 路径继续触发 metadata/session/db
清理；HTTP runtime 在 `driverClassName` 为空、driver 位于 classpath 时仍可完成一次
真实 initialize + tool call。

**Acceptance Scenarios**:

1. **Given** 已建立的 HTTP session 触发 `DELETE /mcp`，**When** 请求返回 `200`，
   **Then** `metadataRefreshCoordinator`、`databaseExecutionBackend` 和 `sessionManager`
   仍完成当前会话的清理。
2. **Given** HTTP runtime 关闭或 embedded Tomcat stop，**When** 存在未显式
   `DELETE` 的活跃 session，**Then** 服务仍关闭全部 managed sessions，
   而不是只关闭 SDK transport session。
3. **Given** `driverClassName` 为空且 JDBC driver 已通过 classpath 可见，
   **When** HTTP runtime 启动并处理第一次真实请求，**Then** 运行行为不因 transport
   refactor 或 classloader 调整而失败。
4. **Given** 某个当前兼容行为无法完全由 SDK 直接覆盖，**When** 进行实现收敛，
   **Then** 必须保留一层最薄的 ShardingSphere compatibility shim，而不是牺牲行为。

### Edge Cases

- initialize 请求的 `protocolVersion` 为空或缺失时，服务必须继续协商到固定基线版本。
- 若 `Accept` 兼容仍由本地 shim 承担，显式错误值和缺失值的处理边界必须保持可解释。
- 若 runtime 绑定非 loopback host，本地 `Origin` 特殊边界不应被误应用到远程部署模式。
- shutdown 先于客户端显式 `DELETE` 发生时，managed session 仍必须完成资源释放。
- 如果移除 `serviceWithApplicationClassLoader`，必须有等价回归覆盖证明 HTTP runtime
  在 classpath driver 场景下无能力损失。

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: 系统 MUST 优先复用官方 `HttpServletStreamableServerTransportProvider`
  已覆盖的 endpoint dispatch、Accept 校验、SDK session 生命周期、
  `DELETE` 语义和 graceful close 行为。
- **FR-002**: 自定义 HTTP transport 代码 MUST 只保留 SDK 无法直接覆盖的能力：
  固定协议版本 policy、initialize 响应头补充、follow-up session/protocol contract、
  ShardingSphere managed session lifecycle，以及零损失兼容 shim。
- **FR-003**: loopback 模式下的 `Origin` 校验 MUST 优先通过 SDK
  `securityValidator` 或等价官方扩展点实现，而不是继续在 servlet 中重复前置校验。
- **FR-004**: initialize 成功响应 MUST 继续返回 `MCP-Session-Id` 和
  `MCP-Protocol-Version` 响应头。
- **FR-005**: follow-up HTTP 请求 MUST 继续校验 session existence、
  协商协议版本和 local-mode `Origin` 边界。
- **FR-006**: 当 follow-up 请求省略 `MCP-Protocol-Version` 且 session 合法时，
  系统 MUST 继续回退到协商版本，而不是要求强制显式传入。
- **FR-007**: 当前固定协议基线 `2025-11-25` MUST 保持不变。
- **FR-008**: `DELETE /mcp` 成功后，系统 MUST 继续释放 ShardingSphere
  session 相关资源，包括 metadata refresh state、database runtime state 和
  session manager state。
- **FR-009**: runtime shutdown 时，系统 MUST 继续关闭全部 managed sessions，
  而不仅是 SDK 自己持有的 transport sessions。
- **FR-010**: 当前已存在的缺省 `Accept` 兼容行为 MUST 保持不变，除非有等价兼容层
  明确承接该行为。
- **FR-011**: lowercase HTTP header 的可接受性 MUST 保持不变。
- **FR-012**: 重构后 MUST 删除无实际收益的冗余 transport context 空配置。
- **FR-013**: 若某项当前能力不能由 SDK 直接覆盖，系统 MUST 保留最薄的本地 shim，
  而不是为了“纯 SDK”而放弃行为。
- **FR-014**: 若要移除当前 classloader bridge，系统 MUST 先通过 HTTP runtime
  的 driver/classpath 回归用例证明不存在能力损失。
- **FR-015**: 本特性 MUST NOT 改变现有 MCP HTTP public endpoint path、
  method surface、status code contract、session headers 和 stateful session model。
- **FR-016**: 相关测试 MUST 同时覆盖“SDK 复用路径”与“零损失回归矩阵”。

### Key Entities *(include if feature involves data)*

- **SDK-covered HTTP transport capability**: 官方 SDK 已直接提供的 routing、
  Accept 校验、session map、`DELETE`、graceful close 与 security hook。
- **ShardingSphere-specific HTTP contract glue**: 固定协议版本、initialize
  响应头、follow-up session/protocol 校验和 local-mode 边界。
- **Managed HTTP session lifecycle bridge**: transport session 与
  `metadataRefreshCoordinator`、`databaseExecutionBackend`、`sessionManager` 的清理桥接。
- **Compatibility shim**: 为保持现有行为而保留的最薄本地适配层，如缺省 `Accept`
  或 classloader safety bridge。

### Assumptions

- 本特性不改变 `specs/001` 已定义的 public Streamable HTTP contract，只重构实现边界。
- 官方 MCP Java SDK 1.1.0 已经覆盖 routing、Accept 校验、SDK session map、
  `DELETE` 和 `securityValidator` 扩展点。
- 当前代码中已经存在但尚未被明确回归覆盖的兼容行为，也应视为“零损失”范围的一部分。

## Non-Goals

- 不引入新的 transport，不切换到 WebFlux/WebMVC provider，不改 embedded Tomcat 形态。
- 不修改 MCP tools/resources/prompts 的 public contract。
- 不改变当前 stateful session model、sticky-session 假设或 session storage 拓扑。
- 不把这次 refactor 扩展成新的鉴权、网关或生产安全体系设计。

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% 的现有 HTTP integration tests 在 refactor 后保持通过。
- **SC-002**: 100% 的新增零损失回归用例覆盖 initialize 响应头、follow-up
  protocol fallback、lowercase headers、loopback `Origin` 和 `DELETE` cleanup。
- **SC-003**: 100% 的 loopback `Origin` 校验由 SDK `securityValidator`
  或等价官方扩展点承接，而不是继续由重复的 servlet 前置逻辑承载。
- **SC-004**: `StreamableHttpMCPServlet` 中只保留 ShardingSphere 独有 contract glue
  与 compatibility shim，不再保留无收益的空配置和重复 transport mechanics。
- **SC-005**: 100% 的 HTTP runtime classpath-driver 验收样例在 refactor 后保持可用，
  不因 classloader 调整导致 regressions。
