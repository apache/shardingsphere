# Research: ShardingSphere MCP Remote HTTP Access Token

## Decision 1: 采用部署方预置的静态共享 Bearer Token

- **Decision**: 使用 `transport.http.accessToken`
  作为部署方预置的共享访问密钥，
  请求侧通过 `Authorization: Bearer <token>` 提交。
- **Rationale**:
  - 这是最小可行 admission gate，
    能直接关闭 remote HTTP 匿名访问问题。
  - 不需要引入用户体系、会话签发、token 生命周期管理。
  - 与现有 servlet transport 和 YAML 配置模型高度契合，
    代码改动面最小。
- **Alternatives considered**:
  - 复用数据库用户名密码：拒绝，因为这会把 backend datasource credential
    与 MCP caller credential 混为一谈。
  - 运行时自动生成 token：拒绝，因为服务重启后凭证会漂移，
    不利于稳定部署和自动化运维。
  - 登录后动态签发 token：拒绝，因为这会把范围扩展成完整认证系统。

## Decision 2: remote 场景下 token 必填，loopback 场景下 token 可选

- **Decision**: 非 loopback 暴露必须配置 token；
  loopback 场景继续保留 “无 token 也可本地调试” 的默认体验。
- **Rationale**:
  - 远程匿名访问是当前 blocker，必须在 remote 场景关闭。
  - V1 顶层边界仍然是 local-first；
    不能为了 remote hardening 把所有本地调试都变成重配置流程。
- **Alternatives considered**:
  - 所有 HTTP 场景都强制 token：可以更统一，
    但会显著提高本地调试摩擦。
  - 只在 remote 场景建议配置 token、不强制：无法真正解决匿名 remote HTTP。

## Decision 3: 一旦配置 token，所有 HTTP 请求都统一要求认证

- **Decision**: 当 `accessToken` 存在时，
  initialize、follow-up session request、resource read、tool call 与 DELETE
  都必须带匹配 token。
- **Rationale**:
  - admission gate 应该保护整个 HTTP surface，
    而不是只保护 `initialize`。
  - 如果后续请求不再验 token，
    拿到或猜到 `sessionId` 的请求仍可能绕过接入边界。
- **Alternatives considered**:
  - 只校验 initialize：拒绝，因为后续 stateful session surface
    仍然会暴露给未认证调用方。

## Decision 4: 认证校验先于 session / protocol 校验

- **Decision**: transport 层先做 token admission check，
  再做 loopback `Origin`、session existence 和 protocol version 校验。
- **Rationale**:
  - 未认证调用方不应该获得 “session 是否存在”
    或 “protocol 是否匹配” 这类额外运行时信息。
  - 这符合 admission boundary 应优先于业务校验的常见设计。
- **Alternatives considered**:
  - 维持现有 session 校验优先：实现改动更小，
    但会产生信息泄露和语义顺序不完整的问题。

## Decision 5: 使用最小披露的 `401 Unauthorized`

- **Decision**: token 缺失、格式非法或值不匹配时统一返回 `401 Unauthorized`，
  使用稳定、最小披露的错误消息。
- **Rationale**:
  - 本轮的目标是 admission gate，不是给未认证调用方做详细诊断。
  - 统一错误语义可以减少实现分支和潜在信息暴露。
- **Alternatives considered**:
  - 区分 “header missing / invalid scheme / token mismatch”：
    调试更友好，但会额外暴露细节，且不是本轮重点。
  - 返回 `403 Forbidden`：不够贴合“未通过认证门槛”的语义。

## Decision 6: 保留外部网关建议，不把内建 token 包装成完整安全方案

- **Decision**: 即使新增内建 token，
  产品仍然要求对外暴露场景放在 trusted network、
  gateway 或 reverse proxy 后面。
- **Rationale**:
  - 共享 token 只是最小 admission gate，
    不是公网级完整安全体系。
  - TLS、请求审计、IP 过滤、限流和多身份治理仍应由外围基础设施承担。
- **Alternatives considered**:
  - 宣称内建 token 已足以替代外部边界：拒绝，
    因为这会夸大当前实现能力。

## Recommended Product Position

- remote HTTP 可以保留，
  但不再是 anonymous control plane
- `allowRemoteAccess` 表达暴露意图，
  `accessToken` 表达内建 admission gate
- 共享 token 是 deployment secret，
  不是 login token
- 内建 token + 外部 trusted network / gateway
  才是 V1 对 remote exposure 的诚实定位
