# Implementation Plan: MCP Public API Flattening

**Branch**: `[001-shardingsphere-mcp]` | **Date**: 2026-05-01 | **Spec**: `/.specify/specs/007-mcp-api-core-boundary/spec.md`
**Input**: Feature specification from `/.specify/specs/007-mcp-api-core-boundary/spec.md`

## Summary

这次计划不再围绕“把 shared helper 从哪里搬到哪里”打转，而是直接按新的公开 API 原则重构：

- `mcp/api` 只保留纯 API 契约
- 公开顶层能力只保留 `tool` 和 `resource`
- workflow 不再作为公开顶层分类出现
- workflow planning 继续以普通 tool 暴露
- workflow apply / validate 收敛成 generic public tools
- 内部通过显式 `workflowKind` 和 internal workflow definition 支撑 generic dispatch

## Technical Context

**Language/Version**: Java 21  
**Primary Dependencies**:
- `mcp/api`
- `mcp/workflow`
- `mcp/core`
- `mcp/bootstrap`
- `mcp/features/encrypt`
- `mcp/features/mask`
**Storage**:
- workflow session state remains runtime-owned
- no new persistent storage requirement beyond workflow identity carried in workflow snapshot
**Testing**:
- JUnit 5
- Mockito
- module-scoped Maven tests
- scoped Checkstyle / Spotless verification
**Project Type**: Multi-module Java API and runtime boundary redesign
**Constraints**:
- no branch switching
- `mcp/api` must contain no production implementation classes
- public top-level capability categories must remain same-level
- workflow is internal orchestration, not a public top-level category
- generic workflow apply / validate replace public feature-specific apply / validate categories
- `mcp/workflow` remains as the internal shared workflow module
- this iteration does not further split `mcp/api` into separate API submodules
**Scale/Scope**:
- covers `mcp/api`, `mcp/workflow`, `mcp/core`, `mcp/bootstrap`, `mcp/features/encrypt`, `mcp/features/mask`
- includes public API redraw plus internal workflow routing redesign

## Constitution Check

*GATE: 通过；本次是公开 API 分层与 runtime 内部路由的重画，不改变 Proxy 逻辑视图本身。*

- **Proxy-first logical abstraction**
  - 不改变 workflow 面向逻辑库、逻辑表、逻辑列的核心业务语义。
- **Explicit operator control**
  - planning / apply / validate 仍然是显式工具调用，不引入隐式自动执行。
- **Minimal safe automation**
  - 泛化 apply / validate 只收口公开 API，不增加新的自动迁移或回滚机制。
- **Deterministic naming and transparent changes**
  - 公开 API 分类、workflow identity、internal definition 都必须可读且可审计。
- **Complete verification before completion**
  - 完成标准必须覆盖模块依赖、编译、测试和风格检查。

## Design Focus

### Public API shape

- `mcp/api` 公开名词只保留：
  - public tool contracts
  - public resource contracts
  - descriptors / DTOs / exceptions / context capability contracts
- 移除公开装配概念：
  - contribution
  - direct contribution
  - workflow contribution
  - invoker / reader
  - materializer-like wrapper

### Workflow shape

- feature-specific planning remains public tools
- generic workflow apply is a platform public tool
- generic workflow validate is a platform public tool
- runtime dispatch uses explicit `workflowKind`
- internal workflow definitions live outside `mcp/api`

### Ownership split

- `mcp/api`
  - pure public API contracts only
- `mcp/workflow`
  - workflow-shared contracts
  - workflow-shared helpers
  - internal workflow definition seams
- `mcp/core`
  - runtime registries
  - runtime dispatch
  - generic workflow apply / validate implementation
  - workflow session-store implementation

## Implementation Slices

### Slice 1 - Purify `mcp/api`

- remove public production implementation classes from `mcp/api`
- remove public contribution-style abstractions from `mcp/api`
- keep only API-level contracts, DTOs, descriptors, exceptions, and context capability contracts

### Slice 2 - Redraw public feature contribution shape

- redesign `MCPFeatureProvider` or equivalent feature entry contracts so public contribution is expressed only through tool and resource categories
- remove public workflow contribution shape from the contribution surface

### Slice 3 - Introduce internal workflow identity and definitions

- add explicit `workflowKind` to workflow snapshot
- define internal workflow runtime definition seams
- wire workflow kind to validation and post-apply synchronization behavior

### Slice 4 - Generic workflow apply and validate

- replace feature-specific public apply / validate tools with generic public workflow apply / validate tools
- move feature-specific runtime routing behind internal workflow definitions
- keep planning feature-specific where intent collection differs

### Slice 5 - Rewire feature modules and runtime

- rewire encrypt and mask to contribute planning tools plus internal workflow definitions
- keep feature modules on `mcp/api + mcp/workflow`
- keep `mcp/core` as runtime-only owner

### Slice 6 - Verification and cleanup

- remove dead contribution wrappers and duplicate abstractions
- update tests and docs
- run module-scoped verification

## Verification Strategy

- `./mvnw -pl mcp/api -am test -DskipITs -Dspotless.skip=true`
- `./mvnw -pl mcp/workflow -am test -DskipITs -Dspotless.skip=true`
- `./mvnw -pl mcp/core -am test -DskipITs -Dspotless.skip=true`
- `./mvnw -pl mcp/features/encrypt -am test -DskipITs -Dspotless.skip=true`
- `./mvnw -pl mcp/features/mask -am test -DskipITs -Dspotless.skip=true`
- `./mvnw -pl mcp/bootstrap -am test -DskipITs -Dspotless.skip=true`
- `./mvnw -pl mcp/api,mcp/workflow,mcp/core,mcp/features/encrypt,mcp/features/mask,mcp/bootstrap -am checkstyle:check spotless:check -Pcheck`

## Complexity Tracking

| Decision | Why Needed | Simpler Alternative Rejected Because |
|----------|------------|--------------------------------------|
| Keep `mcp/workflow` as a separate module | Workflow shared behavior remains real and cross-layer | Pushing it back into `mcp/api` breaks the pure API goal |
| Generic public apply / validate | Keeps public categories same-level and avoids public workflow contribution | Keeping feature-specific public apply / validate preserves duplicate workflow public surface |
| Add explicit `workflowKind` | Generic workflow dispatch needs stable internal identity | Inferring workflow family from Java types or tool names is brittle and hidden |
| Preserve feature-specific planning tools | Intent collection differs substantially by workflow family | Forcing generic planning too early would over-generalize user intent handling |
