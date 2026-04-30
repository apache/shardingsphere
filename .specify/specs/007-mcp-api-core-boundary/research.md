# Research: MCP API Boundary Slimming

## Fixed Constraints

- 本次分析和后续实现都只能在当前分支完成，不切换分支。
- `mcp/api` 的目标形态是最小 shared contract / DTO / protocol model 模块。
- workflow model 能拆就拆，planning / execution / diagnostics / lifecycle / mutable runtime state 尽量下沉到 `mcp/core`。
- 不接受新增 `support` / `workflow` 中间模块。
- `encrypt` / `mask` 后续仍只能依赖 `mcp/api`，不能直接依赖 `mcp/core`。

## Baseline

本轮实现级分析确认了一件很关键的事：前面那版“可直接下沉列表”里，有一部分类确实应该离开 `mcp/api`，但还有一部分已经长在 feature 编译面上，不能直接搬。

当前最明显的编译耦合点有三类：

- feature provider 直接实例化 generic workflow handler
  - `EncryptFeatureProvider`
  - `MaskFeatureProvider`
- feature planning / validation / response 直接依赖 `mcp/api` 里的 workflow helper
  - `WorkflowPlanningSupport`
  - `WorkflowRequestBinder`
  - `WorkflowValidationSupport`
  - `WorkflowPlanPayloadBuilder`
  - `WorkflowArtifactPayloadUtils`
- feature planning / validation / intent resolver 直接依赖 workflow model
  - `WorkflowRequest`
  - `WorkflowContextSnapshot`
  - `ClarifiedIntent`
  - `ValidationReport`
  - `ValidationSection`
  - `WorkflowFeatureData`

这意味着下一步不是“把所有 workflow 代码一起搬去 core”，而是要先把 feature 和 runtime 之间的 shared seam 做出来。

## 1. `feature -> api -> core` 接缝怎么落

### Current state

- `EncryptFeatureProvider` 和 `MaskFeatureProvider` 当前都直接 new `WorkflowExecutionToolHandler` / `WorkflowValidationToolHandler`。
- `ToolHandlerRegistry` 只知道从 `MCPFeatureProvider#getToolHandlers()` 收集 `ToolHandler` 实例。
- `ToolHandlerRegistry` 是静态注册表，启动时就要拿到最终 handler 集合。

所以一旦把 generic apply / validate handler 挪到 `mcp/core`：

- feature 模块不能 import core class；
- core 也不能靠运行时反射猜测“这个 feature 应该补一个 apply/validate handler”；
- 直接搬类会让 tool 注册链路断掉。

### Recommended seam

推荐引入一个新的 API-level workflow contribution seam，由 `MCPFeatureProvider` 暴露，core 负责 materialize：

- feature 继续自己提供 planning `ToolHandler`
- feature 不再直接 new generic apply / validate handler
- feature 只提供 workflow contribution definition，例如：
  - `applyToolName`
  - `validateToolName`
  - feature-owned validation callback
  - 未来如果需要，也可以附加 plan response enrichment callback
- core 在加载 `MCPFeatureProvider` 后，把这些 contribution 展开成 core-owned generic apply / validate handler，再交给 `ToolHandlerRegistry`

### Why this is the safest option

- 保住 `encrypt` / `mask` 只依赖 `mcp/api`
- generic apply / validate handler 真正回到 `mcp/core`
- registry 仍然保持显式注册，不引入按命名推断的隐式规则
- bootstrap / tool descriptor 仍然通过现有 registry 工作，影响面可控

### What should not be done

- 不要让 feature 通过字符串约定让 core “猜” 出 apply / validate tool
- 不要在 core 里硬编码 encrypt / mask 特判
- 不要为了图省事把 generic apply / validate handler 继续留在 `mcp/api`

## 2. workflow model 到底怎么拆

### Revised classification

经过实现面复核，workflow model 不能按“整包搬迁”处理，至少要分成三层。

### A. 更适合继续留在 `mcp/api` 的 shared DTO / vocabulary

这批类型本身主要是数据载体，feature 代码直接编译依赖它们，短期内不适合搬走：

- `WorkflowRequest`
- `WorkflowFeatureData`
- `WorkflowPropertySource`
- `WorkflowIssue`
- `WorkflowIssueCode`
- `WorkflowLifecycle`
- `AlgorithmCandidate`
- `AlgorithmPropertyRequirement`
- `DDLArtifact`
- `RuleArtifact`
- `IndexPlan`
- `DerivedColumnPlan`

保留原因：

- 它们是 feature planning / validation / response builder 直接消费的 shared data vocabulary
- 它们本身不负责 runtime store、registry、factory、session ownership

### B. 必须先拆再决定归宿的 aggregate state

这批类型是当前真正的“大状态包”，不适合原样继续留在 `mcp/api`，但也不能直接整体搬走：

- `WorkflowContextSnapshot`
- `ClarifiedIntent`
- `InteractionPlan`
- `ValidationReport`
- `ValidationSection`

原因分别是：

- `WorkflowContextSnapshot`
  - 同时装了 request、featureData、interaction state、issues、algorithm candidates、property requirements、artifacts、validation report
  - 这是典型 runtime aggregate，不是纯 DTO
- `ClarifiedIntent`
  - 既有 intent 推断结果，也有 pending questions / unresolved fields 这种交互过程状态
- `InteractionPlan`
  - 既有展示给用户的 steps，也有 `currentStep`、delivery/execution mode 这种 runtime lifecycle state
- `ValidationReport` / `ValidationSection`
  - 逻辑上偏 runtime validation state，但 feature validation service 当前还直接构造和消费它们

这批类型的正确方向不是“整类移动”，而是：

- 先把 user-facing contract data 和 core-owned mutable state 拆开
- feature 继续拿 contract DTO
- core 拿 aggregate state 和 storage lifecycle ownership

### C. 可以直接认定为 core-owned state helper 的类型

- `WorkflowPlanningDiagnostics`
- `WorkflowContextSnapshots`

这两个类几乎完全是 runtime state aggregation / defensive copy helper，没有保留在 API 面的必要。

### Important correction

这轮分析也修正了一个实现判断：

- `WorkflowContextSnapshot` 现在虽然“看上去应该下沉到 core”，但它已经被 planning service、validation service、response builder、test fixture 全链路直接使用
- 所以它不能作为第一刀直接搬迁对象
- 它必须排在“先做 seam，再做 decomposition”的后面

## 3. `MCPFeatureContext` 和 `WorkflowContextStore` 的边界怎么改

### Current leakage

当前有两个边界泄漏点最明显：

- `MCPFeatureContext` 直接暴露 `WorkflowContextStore`
- `WorkflowContextStore` 在 API 层自带 `newInstance(...)` 静态工厂，并直接返回 `InMemoryWorkflowContextStore`

这相当于把：

- runtime store contract
- default implementation choice
- object construction responsibility

一起暴露给了 `mcp/api`。

### Recommended boundary adjustment

#### Short-term

短期建议先做最小收口，不强行一次重命名所有接口：

- `WorkflowContextStore` 可以暂时继续作为 API contract 暴露给 feature
- 但它必须退化为纯 contract
- 去掉所有 `newInstance(...)`
- `InMemoryWorkflowContextStore` 下沉到 `mcp/core`
- store 创建责任回到 `MCPRuntimeContext`

这样先解决“API 直接 new runtime implementation”的问题。

#### Medium-term

中期再考虑进一步收紧 `MCPFeatureContext`：

- 如果 feature planning 仍然自己持有 workflow 状态，则可以暂时保留 `getWorkflowContextStore()`
- 但它应当被视为 repository-style contract，而不是 runtime implementation entry point
- 如果后续 planning / validation 进一步 core-owned，则 `MCPFeatureContext` 可以继续缩成更窄的 workflow session / plan access seam

### What this means for tests

目前大量测试直接调用 `WorkflowContextStore.newInstance()`，包括：

- `mcp/api` 下的 workflow helper tests
- `mcp/features/encrypt` 下的 planning / validation / handler tests
- `mcp/features/mask` 下的 planning / validation / handler tests

所以 factory removal 不是不能做，而是不能作为第一刀来做。

## 4. bootstrap 和发现链路会不会受影响

### What is actually sensitive

真正敏感的不是 transport 层，而是下面 4 个静态拼装点：

- `MCPFeatureProviderRegistry`
- `ToolHandlerRegistry`
- `ResourceHandlerRegistry`
- `MCPToolSpecificationFactory`

其中最关键的是：

- `ToolHandlerRegistry` 在类初始化时就固定 `REGISTERED_HANDLERS` 和 `SUPPORTED_TOOL_DESCRIPTORS`
- `MCPToolSpecificationFactory` 只是消费 registry 的最终 descriptor 集

### Risk analysis

如果 generic apply / validate handler 的归属发生变化：

- 只要最终注册到 `ToolHandlerRegistry` 的 tool name 和 descriptor 不变，bootstrap 层基本不用动
- 真正需要改的是 handler materialization 的前置阶段

也就是说，风险集中在：

- feature provider load 阶段
- handler collection 阶段
- duplicate tool name check 阶段

而不是：

- STDIO transport
- Streamable HTTP transport
- MCP server factory

### Safe wiring rule

generic workflow handler 即使迁到 core，也必须满足下面两个条件：

- handler 本身是无状态或仅持有 feature 提供的 callback
- handler 的创建不依赖 `MCPRuntimeContext`

原因很简单：

- registry 是静态的
- `MCPRuntimeContext` 是请求/启动运行时对象
- 如果把 handler 注册依赖 runtimeContext，就会和当前 registry/bootstrap 时序冲突

### Practical implication

推荐把新的 workflow contribution expansion 放在 registry 装配之前，例如：

- 在 `MCPFeatureProviderRegistry.loadToolHandlers()` 内展开
- 或在 `ToolHandlerRegistry.createRegisteredHandlers(...)` 的输入侧展开

但不要把这件事放到 transport factory 或 request context 里做。

## 5. 迁移顺序和测试面怎么切

### Main observation

如果直接从 store 或 snapshot 下手，测试爆炸面会很大；如果先从 registry seam 下手，收益高、风险低。

### Recommended implementation order

#### Slice A - 先补 shared seam，不搬大状态

- 在 `mcp/api` 新增 workflow contribution SPI
- `MCPFeatureProvider` 增加对应 default method
- feature provider 改成返回 planning handler + workflow contribution，而不是直接 new generic apply / validate handler

这是最适合第一刀落地的地方。

#### Slice B - 把 generic apply / validate handler 下沉到 core

- `WorkflowExecutionToolHandler`
- `WorkflowValidationToolHandler`

迁到 `mcp/core`

- core 根据 workflow contribution 创建它们
- `ToolHandlerRegistry` 继续看到同样的 tool name / descriptor

#### Slice C - 收回 runtime store ownership

- `InMemoryWorkflowContextStore` 下沉到 `mcp/core`
- `WorkflowContextStore` 去掉静态工厂
- `MCPRuntimeContext` 自己负责默认 store 创建
- feature tests 改成 mock / local test double，不再依赖 API factory

#### Slice D - 处理当前仍长在 feature 编译面的 helper

这一批目前不能直接搬，因为 feature 生产代码还在 import：

- `WorkflowPlanningSupport`
- `WorkflowRequestBinder`
- `WorkflowValidationSupport`
- `WorkflowPlanPayloadBuilder`
- `WorkflowArtifactPayloadUtils`
- `WorkflowToolDescriptors`
- `MCPToolArguments`

这一层要么：

- 继续作为 API-side shared helper 暂留

要么：

- 先用新的 API contract 替代，再搬到 core

它们不应该和 generic apply / validate handler 一起在第一刀中强搬。

#### Slice E - 最后拆 `WorkflowContextSnapshot` 这类 aggregate state

等 handler ownership、store ownership、feature seam 都稳定后，再动：

- `WorkflowContextSnapshot`
- `ClarifiedIntent`
- `InteractionPlan`
- `ValidationReport`
- `ValidationSection`

这是最后一刀，不是第一刀。

### Test blast radius

这次最该预判的回归面有 4 组：

- `mcp/api` workflow helper 单测
- `mcp/core` registry / controller / request context 单测
- `mcp/features/encrypt` planning / validation / handler 单测
- `mcp/features/mask` planning / validation / handler 单测

特别需要注意的现状依赖有：

- 大量测试直接调用 `WorkflowContextStore.newInstance()`
- feature handler tests 直接实例化 `WorkflowExecutionToolHandler` / `WorkflowValidationToolHandler`
- plan handler tests 直接依赖 `WorkflowToolDescriptors` / `MCPToolArguments` / `WorkflowRequestBinder`

所以测试顺序应该跟代码迁移顺序一致，不能等全部迁完再补。

## Recommended coding starting point

如果下一步要开始写代码，最合理的起点不是 snapshot split，也不是 store removal，而是：

1. 先引入 workflow contribution SPI
2. 把 generic apply / validate handler 的创建责任从 feature provider 挪到 core registry
3. 保持 tool name、descriptor、external MCP behavior 全不变

这样能先拿到最大的边界收益，同时不立刻引爆 workflow model 和测试面。

## Final conclusion

这 5 个实现级问题已经有明确答案：

- 可以开始写代码
- 但第一刀不应该直接碰 `WorkflowContextSnapshot` 这种 aggregate state
- 最先落地的应该是 feature-to-core workflow contribution seam
- `WorkflowContextStore` 的 factory 下沉应排在第二批
- `WorkflowPlanningSupport` / `WorkflowRequestBinder` / `WorkflowValidationSupport` / `WorkflowPlanPayloadBuilder` / `WorkflowArtifactPayloadUtils` 这批类需要先解耦 feature compile surface，再决定是否下沉

换句话说，后续实现应该是“先收所有权，再拆状态”，而不是“先搬状态，再补接缝”。
