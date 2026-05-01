# Research: MCP Workflow Shared Module Extraction

## Fixed Constraints

- 本次需求梳理和后续实现都只能在当前分支完成，不切换分支。
- 新共享模块名字固定为 `shardingsphere-mcp-workflow`。
- `encrypt` / `mask` 后续允许依赖 `mcp/api + mcp/workflow`，但不能直接依赖 `mcp/core`。
- `mcp/workflow` 必须保持 workflow 专属，不能退化成新的通用 support 杂物间。
- 这轮优先做最小安全重构，不主动重画整个 MCP SPI。

## Baseline

这轮重新核对后的关键事实有 5 个：

1. 问题不是 `api` 和 `core` 两层，而是现在实际混了三层职责：
   - 基础 MCP 契约层
   - workflow 共享契约 / 共享 helper 层
   - runtime core 层
2. `mcp/api` 当前主要承担基础契约，同时还承载了一批 workflow model / bridge contract。
3. `mcp/core` 当前除了 runtime registry / handler / execution 之外，还承载了一批被 feature 直接复用的 workflow helper。
4. `encrypt` / `mask` 对 `mcp` 共享层的 import 里，`tool.model.workflow` 有 `59` 个，`tool.service.workflow` 有 `42` 个，说明 workflow 已经是一级领域层，不再只是几个 helper。
5. 如果只做“把 support 从 api 搬到 core”，feature 就会被迫继续依赖 core；如果把 support 塞回 api，又会让 api 重新变重。

所以合理的答案不是合并 `api + core`，也不是把所有东西塞回 `api`，而是把 workflow 共享层显式独立出来。

## 1. 为什么 `mcp-workflow` 合理

### Current split

- `mcp/api`：
  - `ToolHandler`
  - `ResourceHandler`
  - `MCPFeatureProvider`
  - protocol / metadata DTO
  - workflow model / bridge contract
- `mcp/core`：
  - `ToolHandlerRegistry`
  - `ResourceHandlerRegistry`
  - `MCPContributionRegistry`
  - `WorkflowExecutionService`
  - `WorkflowExecutionToolHandler`
  - `WorkflowValidationToolHandler`
  - `InMemoryWorkflowSessionContext`
- feature：
  - 直接依赖 workflow helper 做 planning / validation / response building

这说明 `api` 和 `core` 的骨架其实已经存在，但 workflow 共享层被夹在中间，没有自己的归属。

### Why not merge

如果合并 `api + core`：

- SPI 契约和默认 runtime 实现会继续揉在一起；
- 未来第三方 feature 的扩展边界会更虚；
- `core` 的实现细节会更容易变成事实公共 API。

所以合并能减少模块数，但不能解决抽象层级问题。

### Why not keep feature -> api only

如果要求 `encrypt` / `mask` 只依赖 `api`，最直接的实现方式往往会把 workflow helper 再塞回 `api`。

这会让 `api` 再次承担：

- 契约
- workflow helper
- workflow-specific SPI extension

也就是重新变成“半契约半实现”的混层模块。

## 2. `mcp-workflow` 该放什么

### Shared workflow helpers

这些类同时被 feature 和 core 使用，而且明显不是 runtime-only：

- `WorkflowPlanningSupport`
- `WorkflowValidationSupport`
- `WorkflowRequestBinder`
- `WorkflowSqlUtils`
- `WorkflowIntentResolverSupport`
- `WorkflowPlanPayloadBuilder`
- `WorkflowArtifactPayloadUtils`
- `WorkflowArtifactMaskUtils`
- `WorkflowArtifactBundle`
- `WorkflowLifecycleUtils`
- `WorkflowRuleValueUtils`
- `WorkflowToolDescriptors`

这批类应该进入 `mcp/workflow`。

### Workflow-specific SPI

下面两类虽然现在在 `mcp/api`，但它们并不是“基础 MCP 通用 SPI”，而是 workflow 特化扩展：

- `MCPWorkflowToolContribution`
- `MCPWorkflowValidationHandler`

它们更适合进入 `mcp/workflow`，这样 workflow 扩展语义会更完整。

### Workflow model / bridge contracts

这批类型本轮不建议强拆：

- `WorkflowRequest`
- `WorkflowContextSnapshot`
- `ValidationReport`
- `ValidationSection`
- `ClarifiedIntent`
- `WorkflowLifecycle`
- `WorkflowSessionContext`
- `WorkflowPropertySource`

原因不是它们最终一定属于 `api`，而是它们已经漏进现有 feature-facing API：

- `MCPFeatureContext` 直接暴露 `WorkflowSessionContext`
- `WorkflowSessionContext` 又直接引用 `WorkflowContextSnapshot`

如果本轮连它们一起迁，就不再是“抽 shared workflow layer”，而是开始重画 SPI。

## 3. `mcp-workflow` 不该放什么

### Runtime-only classes stay in core

下面这些仍应留在 `mcp/core`：

- `WorkflowExecutionService`
- `WorkflowProxyQueryService`
- `WorkflowExecutionToolHandler`
- `WorkflowValidationToolHandler`
- `InMemoryWorkflowSessionContext`
- contribution / handler / resource registries
- runtime materializer

它们是典型 runtime-owned 实现，不属于共享 workflow 层。

### Generic non-workflow helper should not be dragged in

`MCPToolArguments` 是这轮最容易误放的类。

它不仅被 workflow plan handler 用，还被：

- `ExecuteSQLToolHandler`
- `SearchMetadataToolHandler`

这类非 workflow handler 使用。

所以它不是“workflow helper”，不该因为当前模块拆分方便就直接搬进 `mcp/workflow`。

## 4. 方案三还能怎么优化

### Key optimization

方案三最值得优化的一点是：

- 不要把 `MCPToolArguments` 直接搬进 `mcp/workflow`
- 要把 feature 对它的直接编译依赖切断

当前耦合点在：

- `WorkflowRequestBinder` 公开使用 `MCPToolArguments`
- `PlanEncryptRuleToolHandler`
- `PlanMaskRuleToolHandler`

这会导致只要 feature 要用 workflow binder，就必须 import 一个非 workflow 通用 helper。

### Better seam

更合理的做法是二选一：

1. 在 `mcp/workflow` 引入更小的 workflow-scoped argument accessor
2. 或者把 `WorkflowRequestBinder` 改成只对外暴露 raw `Map<String, Object>` 和 workflow-specific binding seam

两种方式的共同目标都是：

- feature 不再直接 import `MCPToolArguments`
- `mcp/workflow` 的边界保持 workflow-pure
- `MCPToolArguments` 若仍有价值，只留给 core 的通用 handler 使用

### Recommended choice

推荐第二种方向优先：

- 从 feature-facing API 上隐藏 `MCPToolArguments`
- 如果之后仍需要提炼，再在 `mcp/workflow` 引入一个更小的 workflow-scoped accessor

这样比直接复制一个 `WorkflowToolArguments` 更稳，也更符合“最少代码解决问题”。

## 5. 最终边界建议

### Module graph

- `mcp/api`
- `mcp/workflow` -> `mcp/api`
- `mcp/core` -> `mcp/api + mcp/workflow`
- `mcp/features/encrypt` -> `mcp/api + mcp/workflow`
- `mcp/features/mask` -> `mcp/api + mcp/workflow`
- `mcp/bootstrap` -> `mcp/core`

### Responsibility summary

- `mcp/api`
  - foundational contracts
  - base SPI
  - protocol / metadata DTO
  - retained workflow bridge / model contracts
- `mcp/workflow`
  - workflow-specific shared helper
  - workflow-specific SPI contribution seam
  - workflow descriptor / binder helper
- `mcp/core`
  - runtime registry
  - runtime materializer
  - runtime execution / validation / query service
  - workflow session-store implementation

## 6. What is explicitly not solved in this iteration

- 不重画 `MCPFeatureContext` 的全部层次
- 不把 workflow model 全量从 `api` 拿走
- 不把基础 `feature/spi` 全部拆成 base SPI 和 workflow SPI 两层子模块
- 不重新命名外部 MCP tool / resource / protocol

## Conclusion

这轮最稳的 Speckit 结论是：

- 保留 `api` 和 `core`
- 新增 `mcp/workflow`
- 让 feature 依赖 `api + workflow`
- 让 core 依赖 `api + workflow`
- 通过更小的 workflow-facing binder seam 避免把 `MCPToolArguments` 这类非 workflow 通用 helper 错搬进新模块

这样既能解决当前 feature -> core 的编译耦合，又不会把 `mcp/workflow` 做成另一个语义含混的 support 杂物间。
