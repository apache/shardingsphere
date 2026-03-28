# Research: ShardingSphere MCP HTTP SDK Reuse Without Capability Loss

## Decision 1: 直接复用官方 SDK 已覆盖的 transport mechanics

- **Decision**: endpoint dispatch、Accept 校验、SDK session map、`DELETE`
  基础语义和 graceful close 继续优先交给官方
  `HttpServletStreamableServerTransportProvider`，不再在 servlet 里重复实现同类逻辑。
- **Rationale**:
  - 官方 provider 已覆盖 GET/POST/DELETE、SSE、session map 和 `DELETE`
    的 transport 级语义。
  - 重复实现这些逻辑会扩大维护面，并让 SDK 升级时出现行为漂移风险。
  - 本次目标是“把能交回 SDK 的交回 SDK”，而不是继续保留双份 transport 行为。
- **Alternatives considered**:
  - 保持当前 wrapper 结构不变: rejected，因为 transport 级重复逻辑仍然存在。
  - 完全去掉 wrapper: rejected，因为会丢失 ShardingSphere 自己的 contract glue
    与 lifecycle bridge。

## Decision 2: loopback `Origin` 边界优先走 SDK `securityValidator`

- **Decision**: 将当前 local-mode `Origin` 校验优先迁移到 SDK 的
  `securityValidator` 标准扩展点，只在 SDK hook 无法承接零损失语义时保留最薄本地补充。
- **Rationale**:
  - `securityValidator` 是官方 provider 已提供的安全扩展点，比 servlet 前置手工校验更贴合 SDK 边界。
  - loopback `Origin` 校验本质上属于 transport security，而不是 ShardingSphere
    domain lifecycle。
  - 把这部分放回 SDK hook，可以减少 `StreamableHttpMCPServlet`
    的职责密度。
- **Alternatives considered**:
  - 继续保留在 request validator 中: rejected，因为它重复承载了 SDK 已有的扩展责任。
  - 直接使用 `DefaultServerTransportSecurityValidator`: rejected，因为默认实现偏向
    静态 origin/host allowlist，未必完整表达当前 host-only loopback 规则。

## Decision 3: 保留最薄的 ShardingSphere contract glue

- **Decision**: 自定义 HTTP transport 代码只保留以下几类能力：
  固定协议版本 policy、initialize 响应头补充、follow-up session/protocol contract、
  managed session lifecycle 和必要的 compatibility shim。
- **Rationale**:
  - 这些能力和 ShardingSphere 当前 public contract 或 runtime resource cleanup
    强相关，不能完全交给 SDK session map 解决。
  - 当前 `DELETE /mcp` 不只是 transport session close，还要求清理 metadata、
    database runtime 和 session manager state。
  - initialize 响应头 `MCP-Protocol-Version` 是当前 ShardingSphere 明确承诺的外部行为。
- **Alternatives considered**:
  - 继续把所有前置校验都留在 request validator: rejected，因为其中相当一部分属于 SDK 已覆盖或可通过官方 hook 覆盖的责任。
  - 把 managed session cleanup 也交给 SDK: rejected，因为 SDK 不知道
    ShardingSphere 的 metadata refresh 和 database runtime 生命周期。

## Decision 4: 兼容行为按“零损失”处理，而不是按“低价值”直接删除

- **Decision**: 对当前实现里已有、但尚未完全被 contract 文档化的兼容行为，
  例如缺省 `Accept` 兼容和可能的 classloader safety bridge，先纳入零损失回归矩阵，
  只有在存在等价承接方案和验证证据时才移除。
- **Rationale**:
  - 用户已明确要求“复用 SDK 时不能有任何能力损失”。
  - 有些行为即使价值不高，也可能已经被现有 smoke 或某些集成环境隐式依赖。
  - 先补回归，再决定是否下沉或移除，风险最小。
- **Alternatives considered**:
  - 直接删除低价值兼容层: rejected，因为这会把 refactor 变成无计划的行为收缩。
  - 无限制保留全部兼容层: rejected，因为这会抵消 SDK 复用带来的收敛收益。

## Decision 5: classloader bridge 视为运行时兼容守卫，不视为协议能力

- **Decision**: `serviceWithApplicationClassLoader` 只有在 HTTP runtime
  classpath-driver / optional-driver 场景下被证明不可缺少时才保留；否则优先寻找更局部、
  更标准的承接方式。
- **Rationale**:
  - 它不是 MCP 协议 contract 的一部分，而是 embedded Tomcat + JDBC driver
    兼容性的运行时守卫。
  - 每请求切换 TCCL 的代价和可读性都不低，应避免在没有证据时长期保留。
  - 但在 classpath-driver 场景未被回归证明前，也不能贸然删除。
- **Alternatives considered**:
  - 永久保留 TCCL bridge: rejected，因为这会把潜在的运行时 workaround 固化为长期架构。
  - 立即移除 TCCL bridge: rejected，因为当前还没有覆盖 HTTP runtime +
    optional-driver 的零损失回归。
