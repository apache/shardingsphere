# Feature Specification: ShardingSphere MCP Remote HTTP Access Token

**Feature Branch**: `[020-shardingsphere-mcp-remote-http-access-token]`  
**Created**: 2026-04-13  
**Status**: Draft  
**Input**: User description: "按照方案2，先给我一个设计，注意要用最小的修改代码的方式" and "好了，开始去设计实现文档吧，使用Speckit去整理需求"

## Scope Statement

本特性为 MCP V1 的 Streamable HTTP transport
增加一层最小内建接入门槛，
用于关闭 remote HTTP 的匿名访问问题，
但不把当前 PR 扩展成完整认证授权系统。

本轮产品目标只有一个：

- 非 loopback 暴露的 HTTP runtime 不再是匿名入口

本轮要明确的产品原则：

- `transport.http.allowRemoteAccess` 只表达远程暴露意图，不代表访问已受保护
- `transport.http.accessToken` 是部署方预置的共享访问密钥
- 这是 service-level admission gate，不是用户登录令牌
- 这是 transport boundary，不是 SQL capability boundary
- 本轮只解决 “谁能接入这个 HTTP runtime”，
  不解决 “接入后不同调用方能做什么”

本轮重点覆盖：

- `transport.http.accessToken` 的配置契约
- remote 场景下 token 必填规则
- `Authorization: Bearer <token>` 请求头契约
- 认证失败的 HTTP 错误语义
- 认证校验与现有 loopback `Origin` 校验、session / protocol 校验的先后顺序
- loopback 本地调试兼容性

本轮不处理：

- 多用户身份体系
- role / permission / tool-level authorization
- token 签发、刷新、过期、吊销
- OAuth / OIDC / SSO
- TLS 终止或证书管理
- 外部网关集成协议
- `execute_query`、`schema`、statement classification 语义

## Problem Statement

当前产品已经允许通过：

- 非 loopback `bindHost`
- `allowRemoteAccess: true`

把 Streamable HTTP 暴露到非本机网络。

但当前实现里，
远程访问一旦被允许，
系统并没有再提供任何内建 admission gate。

这会带来三个产品问题：

1. **远程暴露意图与访问边界脱节**  
   `allowRemoteAccess` 只是“我愿意暴露”的声明，
   不是“我已经保护好入口”的声明。
2. **HTTP runtime 仍然是匿名入口**  
   当前请求只做 loopback `Origin`、session id 和 protocol version 校验；
   在 remote 场景下，这些都不能回答 “调用方是否被允许接入”。
3. **产品承诺不够完整**  
   文档虽然建议把远程暴露放在 trusted network / gateway / reverse proxy 后面，
   但运行时本身没有最小接入门槛，
   容易让 reviewer 认为这是匿名 SQL control plane。

本特性的目标不是替代网关或做完整 IAM，
而是给内置 runtime 补上一道最小、诚实、可验证的内建边界。

## User Scenarios & Testing *(mandatory)*

### User Story 1 - 运维在 remote 场景下必须显式配置共享访问密钥 (Priority: P1)

作为部署 MCP runtime 的运维，
我希望当 HTTP 服务绑定到非 loopback 地址时，
系统强制要求我提供一个共享访问密钥，
这样我不会在“已远程暴露但未设接入门槛”的状态下启动服务。

**Why this priority**: 这是关闭匿名 remote HTTP 的第一道门；
如果 remote 启动仍不要求凭证，后续请求级校验就没有意义。

**Independent Test**: 使用非 loopback `bindHost`
验证缺失 token 启动失败、存在 token 启动成功。

**Acceptance Scenarios**:

1. **Given** `bindHost = 0.0.0.0` 且 `allowRemoteAccess = true`，  
   **When** 未配置 `transport.http.accessToken`，  
   **Then** 配置加载必须失败。
2. **Given** `bindHost = 192.168.1.10` 且 `allowRemoteAccess = true`，  
   **When** `transport.http.accessToken` 为空字符串或空白，  
   **Then** 配置加载必须失败。
3. **Given** `bindHost = 0.0.0.0` 且 `allowRemoteAccess = true`，  
   **When** 配置了非空 `transport.http.accessToken`，  
   **Then** 运行时可以正常启动。

---

### User Story 2 - HTTP 客户端必须带 Bearer Token 才能接入 runtime (Priority: P1)

作为接入 MCP 的 HTTP client，
我希望系统要求我在请求头里显式提供共享访问密钥，
这样 remote runtime 就不再是匿名入口。

**Why this priority**: 这是 reviewer 关心的核心问题；
必须让未持有凭证的调用方在接入层就被拒绝。

**Independent Test**: 用 initialize、tools/call、resources/read 和 DELETE
验证无 token、错误 token、正确 token 的行为差异。

**Acceptance Scenarios**:

1. **Given** runtime 已配置 `transport.http.accessToken`，  
   **When** 客户端未携带 `Authorization` 请求头调用 `initialize`，  
   **Then** 服务端返回 `401 Unauthorized`。
2. **Given** runtime 已配置 `transport.http.accessToken`，  
   **When** 客户端携带错误 token 调用 `tools/call`，  
   **Then** 服务端返回 `401 Unauthorized`，
   且不得继续暴露 session 是否存在。
3. **Given** runtime 已配置 `transport.http.accessToken`，  
   **When** 客户端携带正确 token 调用 `resources/read`、`tools/call` 或 `DELETE`，  
   **Then** 请求继续进入后续 session / protocol 校验链。

---

### User Story 3 - loopback 本地调试继续保持低摩擦 (Priority: P2)

作为本地调试 MCP runtime 的开发者，
我希望 loopback 绑定默认仍然可直接使用，
这样本地调试体验不会因为 remote hardening 被整体抬高门槛。

**Why this priority**: V1 仍以本地默认安全为基础，
不能为了 remote 场景把所有本地 HTTP 调试都变成重配置流程。

**Independent Test**: 验证 loopback 场景下未配置 token 仍可用，
且 loopback `Origin` 校验保持不变。

**Acceptance Scenarios**:

1. **Given** `bindHost = 127.0.0.1` 且未配置 `transport.http.accessToken`，  
   **When** 本地 client 调用 `initialize`，  
   **Then** 服务端继续允许请求进入现有处理流程。
2. **Given** `bindHost = 127.0.0.1` 且配置了 `transport.http.accessToken`，  
   **When** 客户端未携带 token，  
   **Then** 服务端返回 `401 Unauthorized`。
3. **Given** `bindHost = 127.0.0.1`，  
   **When** 请求显式携带非 loopback `Origin`，  
   **Then** 服务端仍返回 `403 Forbidden`，
   不得因为引入 token 校验而取消现有本地边界规则。

### Edge Cases

- `Authorization` 头不存在
- `Authorization` 头存在但不是 `Bearer` 方案
- `Authorization: Bearer` 后 token 为空
- 配置了 token，但请求是 `GET` / `POST` / `DELETE` 任一 HTTP 方法
- 请求同时违反 token 校验与 session 校验时，必须优先返回认证失败
- loopback 场景显式配置 token 后，所有请求仍应统一要求认证
- token 错误时不得泄露 “session 是否存在” 或 “protocol 是否匹配”

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: 系统 MUST 为 Streamable HTTP runtime 提供最小内建 admission gate，
  用于防止 configured remote exposure 仍保持匿名访问。
- **FR-002**: `transport.http` 配置 MUST 新增 `accessToken` 字段，
  其类型为字符串，表示部署方预置的共享访问密钥。
- **FR-003**: 当 `bindHost` 为非 loopback 时，
  runtime MUST 继续要求 `allowRemoteAccess = true`。
- **FR-004**: 当 `bindHost` 为非 loopback 且 `allowRemoteAccess = true` 时，
  `transport.http.accessToken` MUST 为非空、非空白字符串；
  否则配置加载 MUST 失败。
- **FR-005**: 当 runtime 配置了 `transport.http.accessToken` 时，
  所有进入 HTTP transport 的请求 MUST 提供
  `Authorization: Bearer <token>` 请求头。
- **FR-006**: 当请求缺失 `Authorization` 请求头、
  Bearer 方案不合法或 token 不匹配时，
  服务端 MUST 返回 `401 Unauthorized`。
- **FR-007**: token 校验 MUST 发生在 session existence、
  protocol version 和会话后续校验之前。
- **FR-008**: 当 `bindHost` 为 loopback 且未配置 `accessToken` 时，
  本地 HTTP 调试 MUST 继续可用，
  不得被 remote hardening 规则强制要求提供 token。
- **FR-009**: 当 `bindHost` 为 loopback 且配置了 `accessToken` 时，
  runtime MUST 要求所有请求继续携带匹配 token，
  以支持本地显式加固场景。
- **FR-010**: loopback `Origin` 校验 MUST 继续保留，
  且不得被 token 校验替代。
- **FR-011**: `accessToken` MUST 被定义为部署级共享密钥，
  而不是登录后动态签发的 session token。
- **FR-012**: 本轮 MUST NOT 引入用户身份、角色、权限模型、
  token 刷新、过期、吊销或签发机制。
- **FR-013**: 本轮 MUST NOT 改变 MCP tool surface、
  SQL execution contract 或 capability 语义；
  改动范围限定在 HTTP transport admission boundary。
- **FR-014**: README、top-level spec 与 Streamable HTTP contract
  MUST 统一说明：
  远程暴露除了 trusted network / gateway / reverse proxy 之外，
  还需要内建 shared bearer token admission gate。
- **FR-015**: 认证失败响应 SHOULD 使用稳定、最小披露的错误消息，
  避免向未认证调用方暴露额外运行时信息。

### Key Entities *(include if feature involves data)*

- **HttpTransportConfiguration**: HTTP transport 的运行时配置对象；
  本轮新增 `accessToken` 字段。
- **SharedAccessToken**: 部署方预置的共享访问密钥；
  用于决定调用方能否接入当前 runtime。
- **AuthorizationHeader**: 请求级 Bearer token 载体；
  形如 `Authorization: Bearer <token>`。
- **AccessDecision**: transport 层的 admission check 结果；
  只回答请求能否继续进入 MCP session / protocol 流程。
- **RemoteExposureIntent**: `allowRemoteAccess` 表达的远程暴露意图；
  它不是认证能力本身。

### Assumptions

- 当前 V1 的目标是补齐最小内建边界，而不是替代外部网关。
- 现有 embedded Tomcat + servlet transport 足以承载 header-level admission check。
- 部署方能够为 remote 场景提供一段随机、高熵的共享字符串。
- 共享 token 方案在 V1 只服务于单租户或受控网络场景。

### Non-Requirements

- 系统不负责自动生成并持久化 token
- 系统不负责登录、换取或刷新 token
- 系统不负责将 token 暴露给 capability、resource 或 tool response
- 系统不负责多 token 管理或按客户端区分身份
