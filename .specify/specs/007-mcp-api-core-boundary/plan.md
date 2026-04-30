# Implementation Plan: MCP API Boundary Slimming

**Branch**: `[001-shardingsphere-mcp]` | **Date**: 2026-04-30 | **Spec**: `/.specify/specs/007-mcp-api-core-boundary/spec.md`
**Input**: Feature specification from `/.specify/specs/007-mcp-api-core-boundary/spec.md`

## Summary

这次规划的目标不是再给 `mcp/api` 找一个“更合理的杂物间”，而是把它收紧成最小共享边界：

- `mcp/api` 只保留 shared contract、DTO、protocol model；
- 所有 generic runtime helper、workflow execution/validation/store/payload/binder 等 concrete behavior 下沉到 `mcp/core`；
- workflow model 能拆就拆，planning / execution / diagnostics / lifecycle / mutable runtime state 尽量收口到 `mcp/core`；
- encrypt / mask feature 继续只依赖 `mcp/api`，不允许为了瘦身 `api` 而直接依赖 `mcp/core`；
- 不引入新的 `support` / `workflow` 中间模块，现有边界只在 `mcp/api` 和 `mcp/core` 之间重新收束。

这次计划的关键不是“简单搬文件”，而是**先把每类类型的 ownership 讲清楚，再按 ownership 拆 contract 和 runtime implementation**。

## Current Baseline

- `mcp/api` 当前共有 `6048` 行生产代码、`86` 个 Java 文件。
- 当前 workflow 相关代码已经占到 `mcp/api` 的主体：
  - `tool/service/workflow`: `1930` 行
  - `tool/model/workflow`: `1273` 行
  - `tool/handler/workflow`: `134` 行
  - 再加上 `WorkflowToolDescriptors` 与 `MCPToolArguments`，合计约 `3623` 行、`37` 个文件，约占 `mcp/api` 生产代码的 `60%`。
- `WorkflowContextStore` 当前在 API 层暴露了静态工厂，并直接绑定 `InMemoryWorkflowContextStore`，说明 `api` 已经承载了 concrete runtime implementation factory。
- `MCPRuntimeContext` 当前通过 `WorkflowContextStore.newInstance()` 获取默认 store，进一步放大了 API-to-runtime implementation 泄漏。
- `RuntimeDatabaseProfile` 当前虽然放在 `mcp/api`，但实际只被 `mcp/core` 使用，是明显的 core-only runtime descriptor。
- encrypt / mask 当前都直接实例化 `WorkflowExecutionToolHandler` 和 `WorkflowValidationToolHandler`，说明 feature 模块仍在消费 API 层的 shared concrete runtime helper。
- `MCPFeatureContext` 当前直接暴露 `WorkflowContextStore`，使 feature-facing contract 带上了 core-owned runtime state concern。

## Technical Context

**Language/Version**: Java 17  
**Primary Dependencies**:
- `mcp/api`, `mcp/core`, `mcp/bootstrap`, `mcp/features/encrypt`, `mcp/features/mask`
- `shardingsphere-infra-spi`
- `shardingsphere-infra-exception`
- `shardingsphere-infra-util`
- current MCP workflow, metadata, resource, tool, and capability paths
**Storage**:
- in-memory runtime state owned by `mcp/core`
- no new persistence
- no new cross-process storage
**Testing**:
- JUnit 5
- Mockito
- module-scoped Maven tests
- scoped Checkstyle verification for touched modules
**Target Platform**:
- ShardingSphere MCP runtime
- STDIO and Streamable HTTP transport paths
**Project Type**: Multi-module Java architectural boundary cleanup
**Performance Goals**:
- no regression in handler discovery or runtime dispatch
- no new avoidable request-path indirection
- startup wiring remains deterministic
**Constraints**:
- no branch switching
- `mcp/api` must keep only shared contract, DTO, and protocol model surface
- workflow model families must sink core state to `mcp/core` whenever they can be split
- encrypt / mask must continue to depend only on `mcp/api`
- no new shared support or workflow module
- no redesign of external MCP semantics in this work
**Scale/Scope**:
- covers `mcp/api`, `mcp/core`, `mcp/bootstrap`, `mcp/features/encrypt`, and `mcp/features/mask`
- does not expand encrypt / mask business behavior
- does not redesign external tool names, resource URIs, or protocol semantics

## Constitution Check

*GATE: 通过；本次为内部架构收口，不改变 MCP 产品边界。*

- **Proxy-first logical abstraction**
  - 不改变 Proxy-first 逻辑视图，不触碰业务工作流语义。
- **Explicit operator control**
  - 不改变 plan / apply / validate 的用户可见控制语义。
- **Minimal safe automation**
  - 不新增自动执行、迁移、回滚或额外自动化能力。
- **Deterministic naming and transparent changes**
  - 模块归位、依赖方向、contract/runtime 拆分都必须可审计、可复核。
- **Complete verification before completion**
  - 完成标准包括模块边界、依赖关系、编译与测试验证，不只是“代码移动成功”。

## Project Structure

### Documentation (this feature)

```text
specs/007-mcp-api-core-boundary/
|-- spec.md
|-- plan.md
|-- research.md
|-- data-model.md
`-- checklists/
    `-- requirements.md
```

### Source Code (repository root)

```text
mcp/
|-- api/src/main/java/org/apache/shardingsphere/mcp/
|   |-- capability/
|   |-- context/
|   |-- feature/
|   |-- metadata/
|   |-- protocol/
|   |-- resource/
|   `-- tool/
|-- core/src/main/java/org/apache/shardingsphere/mcp/
|   |-- capability/
|   |-- context/
|   |-- feature/
|   |-- metadata/
|   |-- resource/
|   `-- tool/
|-- bootstrap/
`-- features/
    |-- encrypt/
    `-- mask/
```

**Structure Decision**:
不新增任何中间 shared support 模块。`mcp/api` 只保留最小共享 contract surface，`mcp/core` 承接 generic runtime implementation，feature 模块只依赖 `mcp/api` 暴露的 contract seam。

## Design Focus

### Architectural direction

- 先做 `mcp/api` 当前类型家族分类矩阵，再决定迁移顺序。
- 任何“不确定是不是 contract”的类型，默认先按 runtime-suspect 处理，只有证明是多层共享 contract 才能留在 `mcp/api`。
- feature-facing seam 必须保持 interface-only；feature 不得因为 generic runtime helper 下沉而反向依赖 `mcp/core`。
- 当前直接由 feature 模块实例化的 generic workflow execution / validation helper，后续必须改成 core-owned runtime materialization，而 feature 只保留 API-level contribution seam。
- 当前 workflow model family 不应整体留在 `mcp/api`；必须先拆出 minimal contract DTO，再把 mutable runtime state、diagnostics、lifecycle、artifact accumulation 等 core concern 下沉。

### First-pass family directions

- **优先保留在 `mcp/api` 的家族**
  - `capability/*` 中的 contract / SPI / capability model
  - `metadata/model/*`
  - `protocol/*`
  - `protocol/response/*`
  - `protocol/exception/*`
  - `tool/descriptor/*` 中的 DTO contract
  - `tool/request/*` 与 `tool/response/*` 中的 shared DTO
  - `resource/handler/ResourceHandler`
  - `tool/handler/ToolHandler`
  - `resource/uri/MCPUriVariables`
  - `feature/spi/*`

- **优先下沉到 `mcp/core` 的家族**
  - `metadata/jdbc/RuntimeDatabaseProfile`
  - `tool/handler/workflow/*`
  - `tool/service/workflow/*` 中的 concrete helper、service、builder、binder、masking / payload / execution helper
  - `tool/service/workflow/InMemoryWorkflowContextStore`
  - `tool/service/workflow/WorkflowContextStore` 的 runtime factory responsibility
  - `tool/descriptor/WorkflowToolDescriptors`
  - `tool/request/MCPToolArguments`

- **必须先拆再决定归宿的家族**
  - `context/MCPFeatureContext`
  - `tool/model/workflow/*`
  - `tool/service/workflow/WorkflowContextStore`
  - 任何既被 feature 编译依赖、又持有 core lifecycle / execution state 的 workflow family

### Behavior preservation

- encrypt / mask 外部行为保持不变。
- MCP tool names、resource URIs、protocol behavior 不在本计划中重开。
- handler discovery 和 runtime feature loading 行为必须保持可用。

### Cleanup expectation

- 删除 `mcp/api` 中所有 concrete runtime workflow helper、store implementation、builder、binder、registry-like helper。
- 删除 API 层通过静态工厂直接 new runtime implementation 的做法。
- 删除 feature 对 API 层 shared concrete runtime helper 的直接实例化依赖。
- 删除当前只是历史遗留、实际只服务 `mcp/core` 的 API-side runtime descriptor。

## Implementation Slices

### Slice 1 - Classification matrix and ownership freeze

- 盘点 `mcp/api` 当前所有生产类型家族。
- 为每个家族给出 retain / move / split 的唯一结论。
- 明确哪些类型满足“shared contract / DTO / protocol model”标准，哪些不满足。
- 单独标出 workflow family 的 contract data 与 runtime state 边界。

### Slice 2 - Feature-facing seam reduction

- 收紧 `MCPFeatureContext` 与相关 feature-facing seam，只暴露 API-owned contract。
- 取消 feature 对 `WorkflowContextStore`、generic runtime helper、core concern 的直接持有或依赖。
- 设计 API-level workflow contribution seam，让 core 可以接管 generic apply / validate runtime materialization，而 feature 仍只依赖 `mcp/api`。

### Slice 3 - Core runtime migration

- 将 core-only runtime descriptor、generic workflow handler、execution / validation service、payload / binder / parser helper、store implementation 等迁入 `mcp/core`。
- 把默认 runtime store 创建职责从 API 层移回 core runtime context。
- 保证 `mcp/core` 自己拥有 runtime wiring，不再依赖 API 层 concrete factory。

### Slice 4 - Workflow model decomposition

- 拆分 `tool/model/workflow/*` 中的 minimal contract DTO 与 core-owned mutable state。
- 让 planning / execution / diagnostics / lifecycle / artifact accumulation / validation state 尽量归到 `mcp/core`。
- 仅保留 feature 编译真正需要的 minimal shared workflow contract data 在 `mcp/api`。

### Slice 5 - Module cleanup and verification

- 调整 `mcp/api`, `mcp/core`, `mcp/bootstrap`, `mcp/features/encrypt`, `mcp/features/mask` 的依赖与 imports。
- 确保 encrypt / mask 继续只依赖 `mcp/api`。
- 删除 API 中的 dead helper、过渡性 runtime factory 和已拆空的 runtime holder。
- 验证 discovery、编译和测试链路不回退。

## Verification Strategy

- `./mvnw -pl mcp/api -am test -DskipITs -Dspotless.skip=true`
- `./mvnw -pl mcp/core -am test -DskipITs -Dspotless.skip=true`
- `./mvnw -pl mcp/bootstrap -am test -DskipITs -Dspotless.skip=true`
- `./mvnw -pl mcp/features/encrypt -am test -DskipITs -Dspotless.skip=true`
- `./mvnw -pl mcp/features/mask -am test -DskipITs -Dspotless.skip=true`
- `./mvnw checkstyle:check -pl <touched-modules> -am -Pcheck`

## Complexity Tracking

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| No new support module allowed | Ownership must be clarified inside existing `mcp/api` and `mcp/core` boundary | Adding `support` would preserve the same architectural blur under another name |
