# Research: MCP Public API Flattening

## Fixed Constraints

- 本次需求梳理和后续实现都必须留在当前分支完成，不切换分支。
- `mcp/api` 必须保持纯 API 语义，不允许保留生产实现类。
- 公开抽象必须同层；`tool` 和 `resource` 是公开能力分类，`workflow contribution` 不是。
- workflow 仍然可以作为共享层存在，但它是内部架构层，不是公开 API 顶层分类。
- 兼容当前外部 workflow tool 名称不是本轮需求约束。
- 公开 workflow apply / validate 允许收敛成 generic platform tools。
- 保留 `mcp/workflow` 作为内部共享模块，不在本轮移除。
- 本轮不继续把 `mcp/api` 细拆成多个 API 子模块。

## Baseline

重新梳理后，当前问题不只是模块摆放，而是同时存在 3 个结构性问题：

1. `mcp/api` 混了 API 契约和实现辅助类。
2. 公开名词体系混了同层能力概念和内部装配概念。
3. workflow 共享行为真实存在，但被错误地表现成公开顶层能力类别。

当前典型症状包括：

- `MCPDirectToolContribution`、`MCPDirectResourceContribution` 是公开具体类，但本质上只是内部装配辅助。
- `MCPWorkflowToolContribution` 也是公开概念，但最终仍然会被展开成普通 tool handler。
- `ToolHandler` / `ResourceHandler` 已经是稳定抽象，却又被 `descriptor + invoker`、`uriPattern + reader` 再拆一层。

所以根因不是“实现类放错模块”这么简单，而是 **API 泄漏了内部装配模型**。

## What the public API should look like

### Same-level public nouns

公开 API 顶层应该只保留两类能力：

- public tool
- public resource

这两类概念：

- 对应 MCP 最终暴露给调用方的能力面；
- 在语义层级上对齐；
- 足以覆盖 feature 公开行为。

### What should not be public nouns

下面这些都不应作为公开 API 顶层概念存在：

- contribution
- direct contribution
- workflow contribution
- invoker
- reader
- materializer
- default wrapper

它们不是调用方视角的能力，而是框架内部装配语言。

## Why workflow is not a public top-level category

workflow 是真实存在的共享能力，但它不应该和 tool/resource 并列为公开 API 分类。

原因有三点：

1. 当前 workflow 最终暴露给外部的仍然是 tools。
2. workflow 更像“工具之间的编排模式”，不是新的协议实体。
3. 把 workflow 做成公开顶层分类，会导致公开抽象层级失衡。

因此更合理的公共模型是：

- feature-specific planning 仍然是 tool
- platform-scoped workflow apply 是 generic tool
- platform-scoped workflow validate 是 generic tool

这样 workflow 仍然存在，但它只以 tool 的形式出现在公开 API 里。

## Internal architecture that still remains necessary

即使公开 API 被压平，内部仍然需要 workflow 专属共享层。

这个共享层需要承载的不是公开能力分类，而是内部 workflow 运行定义：

- workflow kind
- workflow snapshot
- workflow validation strategy
- workflow apply synchronization strategy
- workflow-shared binding and helper contracts

这里的关键是：**这些概念可以是内部共享架构的一等概念，但不能是公开 API 的一等概念。**

## Why explicit workflow identity is required

一旦 apply / validate 收敛成 generic public tools，runtime 就不能再靠工具名或 Java 类型猜测 workflow 家族。

因此需要显式的 workflow identity：

- workflow plan 创建时写入 `workflowKind`
- apply / validate 通过 `plan_id` 读出 `workflowKind`
- runtime 再用 `workflowKind` 找到对应内部 workflow definition

如果没有这条显式链路，generic public workflow tools 会退化成“表面统一、内部靠猜”。

## Recommended ownership split

### `mcp/api`

只保留：

- public tool contracts
- public resource contracts
- shared descriptors
- protocol DTOs
- context capability contracts
- exception contracts

不保留：

- direct contribution
- workflow contribution
- invoker / reader
- materializer-like helper
- default wrapper implementations

### `mcp/workflow`

保留：

- workflow-shared contracts
- workflow runtime definition seams
- workflow planning and validation shared helpers
- workflow-specific context capability or workflow-scoped binding seams

### `mcp/core`

保留：

- runtime registry
- dispatch
- session store
- generic workflow apply / validate runtime implementation
- generic tool and resource registration runtime

## Key implication

这条路线的真正代价不是模块数，而是：

- 需要把 public API 和 internal workflow architecture 明确分开；
- 需要为 generic apply / validate 建立显式 workflow kind dispatch；
- 需要把 feature integration 从“公开 contribution 类”改为“公开 tool/resource + 内部 workflow definition”。

但这是符合用户目标的代价，因为用户的目标不是“少改代码”，而是“API 纯净且同层”。

## Conclusion

本轮 Speckit 研究结论是：

- `mcp/api` 不应继续承载任何生产实现类；
- public API 顶层只应保留 `tool` 和 `resource`；
- workflow 不应作为公开顶层能力分类；
- 更彻底的设计是保留 feature-specific planning tools，并把 workflow apply / validate 收敛成 generic public tools；
- 为了支撑这套公开 API，需要在内部引入显式 `workflowKind` 和 internal workflow definition，而这些都应离开 `mcp/api`。
