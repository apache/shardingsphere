# Implementation Plan: MCP Workflow Shared Module Extraction

**Branch**: `[001-shardingsphere-mcp]` | **Date**: 2026-05-01 | **Spec**: `/.specify/specs/007-mcp-api-core-boundary/spec.md`
**Input**: Feature specification from `/.specify/specs/007-mcp-api-core-boundary/spec.md`

## Summary

这次规划的目标不是继续在 `mcp/api` 和 `mcp/core` 之间硬挤边界，而是把已经客观存在的 workflow 共享层显式抽出来：

- 新增 `shardingsphere-mcp-workflow`
- `mcp/api` 保持基础共享契约
- `mcp/workflow` 承担 workflow 专属共享 helper 和 workflow-specific SPI seam
- `mcp/core` 保持 runtime-only ownership
- `encrypt` / `mask` 依赖 `mcp/api + mcp/workflow`，不再直接依赖 `mcp/core`

这次计划的关键不是“批量搬文件”，而是**先把 workflow 共享代码和 runtime-only 代码切开，再处理 feature-facing seam 上的 generic helper 泄漏**。

## Current Baseline

- `mcp/api` 当前 `76` 个生产 Java 文件，约 `4100` 行生产代码。
- `mcp/core` 当前 `80` 个生产 Java 文件，约 `8246` 行生产代码。
- `mcp/features/encrypt` 当前 `20` 个生产 Java 文件，约 `2245` 行生产代码。
- `mcp/features/mask` 当前 `14` 个生产 Java 文件，约 `1112` 行生产代码。
- `encrypt` / `mask` 对 MCP 共享层的 import 分布里：
  - `tool.model.workflow`: `59`
  - `tool.service.workflow`: `42`
  - `feature.spi`: `22`
  - `tool.descriptor`: `10`
  - `resource.handler`: `8`
  - `context`: `8`
- 当前最核心的边界问题不是 workflow model，而是 shared helper 被放在 `mcp/core` 后，feature 被迫直接依赖 core。
- 当前最明显的命名 / 边界风险点是 `MCPToolArguments`：
  - workflow plan handler 在用
  - generic execute / metadata handler 也在用
  - 它不是纯 workflow helper

## Technical Context

**Language/Version**: Java 21  
**Primary Dependencies**:
- `mcp/api`
- `mcp/workflow` (new)
- `mcp/core`
- `mcp/bootstrap`
- `mcp/features/encrypt`
- `mcp/features/mask`
- `shardingsphere-infra-spi`
- `shardingsphere-infra-exception`
- `shardingsphere-infra-util`
**Storage**:
- in-memory workflow session state remains core-owned
- no new persistence
**Testing**:
- JUnit 5
- Mockito
- module-scoped Maven tests
- scoped Checkstyle / Spotless verification
**Target Platform**:
- ShardingSphere MCP runtime
- STDIO and Streamable HTTP transport paths
**Project Type**: Multi-module Java architectural boundary refactor
**Performance Goals**:
- no regression in handler discovery or workflow tool dispatch
- startup wiring remains deterministic
- no new request-path indirection beyond module ownership cleanup
**Constraints**:
- no branch switching
- new module name is fixed as `shardingsphere-mcp-workflow`
- feature modules must not depend directly on `mcp/core`
- `mcp/workflow` must stay workflow-specific
- this iteration prefers minimal API redraw
- no redesign of external MCP semantics
**Scale/Scope**:
- covers `mcp/api`, `mcp/core`, `mcp/bootstrap`, `mcp/features/encrypt`, `mcp/features/mask`
- adds one new module under `mcp/`
- does not redesign encrypt / mask business behavior

## Constitution Check

*GATE: 通过；本次为内部模块边界收口，不改变 MCP 外部产品语义。*

- **Proxy-first logical abstraction**
  - 不改变逻辑视图，不触碰 feature 业务语义。
- **Explicit operator control**
  - 不改变 plan / apply / validate 的用户控制方式。
- **Minimal safe automation**
  - 不新增自动迁移、自动执行或回滚机制。
- **Deterministic naming and transparent changes**
  - 新模块名字、类归属、依赖方向都必须明确且可审计。
- **Complete verification before completion**
  - 完成标准包括模块依赖、编译、测试、Checkstyle、Spotless，而不只是“代码搬过去了”。

## Project Structure

### Documentation (this feature)

```text
specs/007-mcp-api-core-boundary/
|-- spec.md
|-- plan.md
|-- research.md
`-- checklists/
    `-- requirements.md
```

### Source Code (repository root)

```text
mcp/
|-- api/
|-- workflow/
|-- core/
|-- bootstrap/
`-- features/
    |-- encrypt/
    `-- mask/
```

**Structure Decision**:
新增 `mcp/workflow`，但不进一步拆成多个 support 子模块。通过职责边界而不是继续加层数来控制复杂度。

## Design Focus

### Architectural direction

- 以“基础契约 / workflow 共享 / runtime 核心”三层结构重画边界。
- 任何被 feature 和 core 同时编译依赖、且明显属于 workflow 共享逻辑的类，优先进入 `mcp/workflow`。
- 任何 runtime-only registry、handler、session-store、execution/query/service，继续留在 `mcp/core`。
- `mcp/api` 本轮不再追求极致瘦身，而是优先保持基础契约稳定；workflow model / bridge contract 只在必须时调整。
- `mcp/workflow` 必须 workflow-pure；遇到 generic helper 泄漏时，优先缩 seam，而不是把 generic helper 一起搬过去。

### First-pass family directions

- **优先保留在 `mcp/api` 的家族**
  - `feature/spi/*` 中的基础 SPI
  - `tool/handler/ToolHandler`
  - `resource/handler/ResourceHandler`
  - `protocol/*`
  - `metadata/model/*`
  - `tool/model/workflow/*`（本轮先保留）
  - `tool/service/workflow/WorkflowSessionContext`
  - `tool/service/workflow/WorkflowPropertySource`
  - `context/MCPFeatureContext`

- **优先迁入 `mcp/workflow` 的家族**
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
  - `MCPWorkflowToolContribution`
  - `MCPWorkflowValidationHandler`

- **优先保留在 `mcp/core` 的家族**
  - `WorkflowExecutionService`
  - `WorkflowProxyQueryService`
  - `WorkflowExecutionToolHandler`
  - `WorkflowValidationToolHandler`
  - `InMemoryWorkflowSessionContext`
  - tool / resource registry
  - feature contribution materializer / registry
  - generic execute / metadata handler

- **必须先收 seam 再决定具体迁法的点**
  - `MCPToolArguments`
  - feature planning handler 的 binder API

### Behavior preservation

- encrypt / mask 对外行为保持不变
- workflow plan / apply / validate 外部语义保持不变
- registry discovery 与 bootstrap wiring 保持不变

### Cleanup expectation

- 去掉 feature 对 `mcp/core` 的直接依赖
- 去掉 `mcp/workflow` 对 generic non-workflow helper 的错误吸纳
- 去掉 workflow binder 对 `MCPToolArguments` 的 feature-facing泄漏
- 让 `mcp/api` 不再承载 workflow shared helper 实现

## Implementation Slices

### Slice 1 - Create module and dependency graph

- 新增 `mcp/workflow` 模块和 POM
- 调整 `mcp/pom.xml` reactor 顺序
- 建立依赖图：
  - `workflow -> api`
  - `core -> api + workflow`
  - `features/encrypt -> api + workflow`
  - `features/mask -> api + workflow`

### Slice 2 - Move workflow-shared helpers and workflow-specific SPI

- 迁移 workflow 共享 helper 到 `mcp/workflow`
- 迁移 workflow-specific SPI seam 到 `mcp/workflow`
- 同步迁移对应单元测试

### Slice 3 - Seal the argument-binding seam

- 处理 `WorkflowRequestBinder` 对 `MCPToolArguments` 的直接暴露
- 目标是 feature planning handler 不再直接 import generic non-workflow helper
- 优先通过缩 seam 解决，不优先复制一个新的通用参数工具类

### Slice 4 - Rewire core runtime ownership

- 让 `mcp/core` 从 `mcp/workflow` 取 workflow-shared helper
- 保持 runtime handler、materializer、registry、session-store 仍在 core
- 校验 bootstrap / discovery 不回退

### Slice 5 - Clean feature dependencies and verify

- 删除 feature POM 对 `mcp/core` 的直接依赖
- 清理 import 和 dead code
- 执行模块级编译、测试、Checkstyle、Spotless

## Verification Strategy

- `./mvnw -pl mcp/workflow -am test -DskipITs -Dspotless.skip=true`
- `./mvnw -pl mcp/core -am test -DskipITs -Dspotless.skip=true`
- `./mvnw -pl mcp/features/encrypt -am test -DskipITs -Dspotless.skip=true`
- `./mvnw -pl mcp/features/mask -am test -DskipITs -Dspotless.skip=true`
- `./mvnw -pl mcp/bootstrap -am test -DskipITs -Dspotless.skip=true`
- `./mvnw -pl mcp/api,mcp/workflow,mcp/core,mcp/features/encrypt,mcp/features/mask,mcp/bootstrap -am checkstyle:check spotless:check -Pcheck`

## Complexity Tracking

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| Add one new module | Workflow shared behavior has become a distinct architectural layer | Keeping only `api` and `core` forces feature-to-core coupling or makes `api` heavy again |
| Keep workflow model in `api` for now | Avoid reopening a larger SPI redesign in the same change | Full workflow API split is cleaner long-term but larger than this iteration |
| Add a smaller workflow-facing binding seam | Prevent `MCPToolArguments` from polluting `mcp/workflow` | Moving `MCPToolArguments` into `mcp/workflow` would break workflow-only module scope |
