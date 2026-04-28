# Implementation Plan: MCP Feature SPI Simplification

**Branch**: `[001-shardingsphere-mcp]` | **Date**: 2026-04-29 | **Spec**: `/.specify/specs/003-mcp-feature-spi/spec.md`
**Input**: Feature specification from `/.specify/specs/003-mcp-feature-spi/spec.md`

## Summary

本次规划不是继续扩张 `mcp/features/spi`，而是反过来把它收缩成真正的纯 SPI 边界：

- `mcp/features/spi` 只保留稳定扩展接口和最小签名附属类型；
- 当前混在 `mcp/features/spi` 里的 shared DTO、descriptor、request/response、metadata/protocol model、exception 等共享非 SPI 合约迁入独立 shared API 模块，优先方案为 `mcp/api`；
- 当前混在 `mcp/features/spi` 里的 shared concrete workflow helper 不允许迁入新的 support 模块，而是必须拆分到 `mcp/core`、feature 模块，或收敛为 `mcp/api` / SPI 合约；
- 只被 shared runtime 使用的 registry、parser、context store implementation 和其他 core-private helper 迁入 `mcp/core`；
- encrypt / mask feature 继续通过纯 SPI 和 shared contract 接入，不允许因为简化 `feature-spi` 而反向依赖 core implementation；
- `MCPFeatureProvider` 作为顶层 feature SPI 入口保留在 `mcp/features/spi`。

这个规划的关键不是“往 core 塞更多东西”，而是**把每类类型放到与其 ownership 相匹配的位置**，并防止 `mcp/features/spi` 再次变成 shared dumping ground。

## Current Baseline

- `mcp/features/spi` 当前共有 `6128` 行生产代码。
- `tool/service/workflow`、`tool/model/workflow`、`tool/handler/workflow`、`tool/descriptor`、`tool/request`、`tool/response` 合计 `3932` 行，约占 `64.2%`，说明模块的“重”主要来自通用 workflow 运行时和相关 DTO。
- `MCPFeatureContext` 当前直接返回 `WorkflowContextStore`，这不是纯 SPI 签名，而是 concrete support 实现泄漏。
- `WorkflowExecutionService` 当前承担 apply 生命周期推进、artifact 执行和响应 payload 组装，明显属于 concrete runtime service。
- `MCPUriPattern` 被 `mcp/core` 的 `ResourceHandlerRegistry` 使用来做 registry 级 pattern 解析与 overlap 校验，更符合 core-private utility。
- encrypt / mask 当前都直接实例化 `WorkflowExecutionToolHandler` 和 `WorkflowValidationToolHandler`，这表明 `mcp/features/spi` 还承担着共享 concrete helper 的角色，而这类代码后续必须被拆散，不再整体迁入新模块。

## Technical Context

**Language/Version**: Java 17  
**Primary Dependencies**:
- `shardingsphere-infra-spi` / `ShardingSphereServiceLoader`
- current `mcp/core` runtime infrastructure
- current `mcp/features/spi` mixed contract/runtime surface
- encrypt and mask feature modules under `mcp/features`
**Storage**:
- in-memory workflow state owned by runtime or feature-owned layers
- no new external persistence
**Testing**:
- JUnit 5
- Mockito
- module-scoped Maven tests and style checks
**Target Platform**:
- ShardingSphere MCP runtime
- bootstrap packaging for STDIO and Streamable HTTP transport
**Project Type**: Multi-module Java refactor inside existing `mcp` reactor
**Performance Goals**:
- no regression in handler discovery or runtime dispatch
- no additional hot-path dependency indirection beyond interface-only seams
- startup-time classification and registry validation remain deterministic
**Constraints**:
- no branch switching
- keep scope on module purity and dependency boundaries
- do not redesign external tool names or resource URIs in this work
- avoid feature-to-core reverse dependency
- keep changes minimal and traceable
**Scale/Scope**:
- covers `mcp/features/spi`, `mcp/core`, `mcp/features/encrypt`, `mcp/features/mask`, and any newly introduced shared module needed to keep SPI pure
- does not expand encrypt or mask business behavior

## Constitution Check

*GATE: 通过；本次为内部架构收口，不改变 Proxy-first 产品边界。*

- **Proxy-first logical abstraction**
  - 本次不改变 Proxy logical view，也不改变业务工作流的用户视角。
- **Explicit operator control**
  - 不新增或删除 plan / review / apply / validate 的操作控制语义。
- **Minimal safe automation**
  - 不借本次边界收口引入新的自动化能力。
- **Deterministic naming and transparent changes**
  - 模块归属与依赖方向必须可审计；每类类型都要有清晰归位理由。
- **Complete verification before completion**
  - 完成标准包括模块边界、依赖图、编译与 registry 行为验证，而不仅是文件搬迁。

## Project Structure

### Documentation (this feature)

```text
specs/003-mcp-feature-spi/
|-- spec.md
|-- plan.md
|-- research.md
|-- data-model.md
|-- quickstart.md
|-- contracts/
|   `-- feature-spi.md
`-- checklists/
    `-- requirements.md
```

### Source Code (repository root)

```text
mcp/
|-- pom.xml
|-- core/
|-- bootstrap/
|-- features/
|   |-- pom.xml
|   |-- spi/
|   |-- encrypt/
|   `-- mask/
`-- api/                  # new shared non-SPI contract module
```

**Structure Decision**:
主方案引入 `mcp/api` 作为 shared non-SPI contract 归宿，不引入 `mcp/features/support`。当前 shared concrete helper 必须通过拆分归位到 core-private implementation、feature-owned implementation，或抽出 shared API / SPI contract。`mcp/features/spi` 本身不再承载 concrete runtime helper。

## Design Focus

### Architectural direction

- 先做 `mcp/features/spi` 当前生产类型分类矩阵，再决定迁移顺序。
- 纯 SPI、shared API、core runtime、feature-owned 四个最终桶必须边界清晰；当前混合 concrete helper 家族先做拆分，再进入最终桶。
- 核心设计目标是消除 `feature-spi` 的 concrete weight，而不是把所有共享代码简单塞进 core。
- `MCPFeatureContext` 等 feature-facing seam 若保留，必须是 interface-only。
- `MCPFeatureProvider` 保留为顶层 SPI，不作为本次收缩对象。

### First-pass classification seeds

- `capability/*`、`feature/spi/*`、`ToolHandler`、`ResourceHandler`、清理签名后的 `MCPFeatureContext` 优先视为 pure SPI。
- `metadata/model/*`、`protocol/*`、`tool/request/*`、`tool/response/*`、descriptor DTO、`MCPUriVariables` 优先视为 shared API。
- `tool/service/workflow/*`、`tool/model/workflow/*`、`tool/handler/workflow/*`、`WorkflowToolDescriptors` 先按共享 concrete helper 拆分候选处理，再判断其 shared contract、core-private implementation、feature-owned implementation 各自归宿。
- `MCPUriPattern` 优先视为 core runtime。

### Behavior preservation

- encrypt / mask 现有业务语义保持不变。
- 外部 tool names、resource URIs、workflow lifecycle naming 不在本规划中重开。
- runtime discovery 机制应保持可工作，但其 contract 与 implementation 的模块归属需要收口。

### Cleanup expectation

- 删除 `mcp/features/spi` 中所有 concrete service / helper / store / binder / parser / registry 类。
- 删除把 shared DTO 或 descriptor 长期放在 `mcp/features/spi` 的做法。
- 删除任何因为图省事而让 feature 反向依赖 core implementation 的迁移方案。

## Implementation Slices

### Slice 1 - Classification and target module decisions

- 列出 `mcp/features/spi` 当前每个类型家族。
- 为每个家族给出最终归宿，或给出必须先拆分的原因。
- 决定 `mcp/api` 中承载的 shared contract 范围，并锁定“不引入 `mcp/features/support`”。

### Slice 2 - Shared API extraction

- 创建 `mcp/api`。
- 迁出 descriptor、request/response、metadata/protocol model、shared exception 等 shared non-SPI contract。
- 调整 core 和 features 的编译依赖到 shared API。

### Slice 3 - Core-private runtime extraction

- 将 registry-only utility、URI parser、core runtime context implementation、core-owned store implementation 等迁入 `mcp/core`。
- 保留 feature-facing interface seam，但不保留其 concrete implementation 在 `mcp/features/spi`。

### Slice 4 - Shared concrete helper decomposition

- 对当前 generic workflow helper 做二次判断：
  - 若 core-private，迁 core
  - 若 feature-owned，迁 feature
  - 若只是在 contract 层共享，抽出到 `mcp/api` 或 SPI
- 不允许以 `mcp/features/support` 作为兜底归宿。

### Slice 5 - Feature dependency cleanup

- 让 encrypt / mask 只依赖 pure SPI 和 shared API。
- 删除 feature 对 core implementation package 的任何直接依赖。
- 验证 handler discovery 仍然成立。

## Verification Strategy

- `./mvnw -pl mcp/api -am test -DskipITs -Dspotless.skip=true` if `mcp/api` is introduced
- `./mvnw -pl mcp/features/spi -am test -DskipITs -Dspotless.skip=true`
- `./mvnw -pl mcp/features/encrypt -am test -DskipITs -Dspotless.skip=true`
- `./mvnw -pl mcp/features/mask -am test -DskipITs -Dspotless.skip=true`
- `./mvnw -pl mcp/core -am test -DskipITs -Dspotless.skip=true`
- `./mvnw checkstyle:check -pl <touched-module> -am -Pcheck`

## Complexity Tracking

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| No `mcp/features/support` escape hatch | Shared concrete helpers must be decomposed instead of parked in a new shared module | Introducing support would preserve the same ownership blur under a different name |
