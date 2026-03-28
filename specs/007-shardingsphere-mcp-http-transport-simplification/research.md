# Research: ShardingSphere MCP HTTP Transport Simplification After SDK Reuse

## Decision 1: 用“总复杂度”而不是“servlet 文件长度”衡量结构是否更简单

- **Decision**: 后续简化以“减少认知跳转”和“减少不稳定抽象”为目标，不再把“再拆几个 helper”当成默认方向。
- **Rationale**:
  - `006` 已经正确收敛了 SDK 与 ShardingSphere 的职责边界，但把若干 one-off helper 单独成文件后，
    总体阅读路径并没有变短。
  - 一个类更短，不代表整个功能更简单；如果为了局部薄而引入更多文件跳转，会违背
    `Readability / Simplicity / Abstraction`。
- **Alternatives considered**:
  - 继续拆更多小类: rejected，因为当前问题已经不是“单个类太长”，而是“总结构太碎”。
  - 把所有 helper 都塞回一个超大 servlet: rejected，因为独立本地策略类和 runtime glue
    仍需要清晰边界。

## Decision 2: 只有稳定本地概念或多点复用 helper 才值得独立成类

- **Decision**: 独立 helper 只保留两类：
  - 稳定的本地策略或概念边界
  - 被多个生产调用点复用的稳定抽象
- **Rationale**:
  - 独立类的价值来自“稳定边界”，不是来自“把代码挪出去”。
  - 当前最符合独立类条件的是 loopback `Origin` 策略类，
    因为它表达的是独立且稳定的本地运行边界概念。
  - 只服务一次 initialize 或一次 request/response 包装的小型 helper，独立成文件的收益低。
- **Alternatives considered**:
  - 保留全部已拆 helper: rejected，因为它们多数只服务单一调用点。
  - 合并所有 helper，包括独立安全策略类: rejected，因为会模糊本地策略边界。

## Decision 3: loopback `Origin` 继续独立，但只保留一条本地校验路径

- **Decision**: loopback `Origin` 继续保留独立策略类，但由 servlet 前置校验直接调用；
  delegate 不再重复执行同一 `securityValidator`。
- **Rationale**:
  - 同一安全规则在 servlet 和 SDK delegate 中各跑一遍，会增加阅读负担，也没有带来额外语义价值。
  - 当前实现还需要保留现有 JSON 错误外观和校验顺序，本地前置校验更直接。
  - `DefaultServerTransportSecurityValidator` 是 allowlist 式 origin/host 校验，
    与当前 “只要 origin host 是 loopback 即可” 的 host-only 语义并不完全一致。
  - 默认实现还会引入 `Host` header 约束和不同的错误消息，不满足当前“零损失”目标。
- **Alternatives considered**:
  - 继续把同一个 validator 传给 delegate: rejected，因为会造成重复校验。
  - 改用 SDK 默认 validator: rejected，因为当前看不到零损失等价路径。
  - 把策略类也合回 servlet: rejected，因为会弱化独立的本地安全概念。

## Decision 4: 不依赖 SDK package-private internal helper

- **Decision**: 不尝试复用 SDK 内部的 `HttpServletRequestUtils` 等 package-private helper。
- **Rationale**:
  - 这类 helper 不属于公开扩展面，无法作为稳定依赖。
  - 为了省几行 header 提取代码去依赖 internal utility，会把结构简化变成脆弱耦合。
- **Alternatives considered**:
  - 复制粘贴 SDK internal helper 逻辑: rejected，因为收益极低且会制造来源分叉。

## Decision 5: 以下能力继续保留在 ShardingSphere，本轮只做更简单的组织

- **Decision**: 继续在 ShardingSphere 本地保留以下能力：
  - loopback `Origin` 本地运行边界策略
  - initialize `MCP-Protocol-Version` 响应头
  - follow-up protocol fallback / mismatch contract
  - missing/blank `Accept` 兼容
  - managed session cleanup
  - classloader guard
- **Rationale**:
  - 这些能力要么是当前对外 contract，要么是 ShardingSphere runtime lifecycle，
    SDK 当前并没有零损失的现成承载方式。
  - 本轮优化的重点是让这些能力的组织更简单，而不是错误地把它们“硬塞回 SDK”。
- **Alternatives considered**:
  - 为了更少代码强行删除这些 glue: rejected，因为会造成行为回退。
  - 维持 `006` 完成后的所有独立 helper 结构不动: rejected，因为还有简化空间。

## Decision 6: missing/blank `Accept` 目前不能零损失下沉到 SDK

- **Decision**: 当前 SDK 1.1.0 下，missing/blank `Accept` 兼容继续由 ShardingSphere 本地 shim 承担，
  不把该能力进一步交给 SDK。
- **Rationale**:
  - `HttpServletStreamableServerTransportProvider` 在 `GET` / `POST` 中直接硬编码校验
    `Accept`，且当前没有暴露自定义 Accept 校验策略的扩展点。
  - 现有兼容目标要求缺失或空值仍能成功进入 runtime，这与 SDK 默认严格校验不等价。
  - 使用 request wrapper 预补 header 仍然是“借 SDK 执行”，但不是“由 SDK 原生提供兼容语义”。
- **Alternatives considered**:
  - 直接删除本地 Accept shim，完全跟 SDK 默认行为走: rejected，因为会带来行为回退。
  - 依赖 SDK internal utility 或非公开钩子重写 Accept 流程: rejected，因为不稳定且不可维护。
