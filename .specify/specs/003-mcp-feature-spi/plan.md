# Implementation Plan: MCP Feature SPI Modularization

**Branch**: `[001-shardingsphere-mcp]` | **Date**: 2026-04-18 | **Spec**: `/.specify/specs/003-mcp-feature-spi/spec.md`
**Input**: Feature specification from `/.specify/specs/003-mcp-feature-spi/spec.md`

## Summary

本次规划要把当前 `mcp/core` 中混合存在的 encrypt / mask MCP 业务语义拆成真正对等的 feature 模块：

- 新增 `mcp/features` 作为 feature reactor；
- `mcp/features/spi` 承载唯一稳定扩展契约；
- `mcp/features/encrypt` 和 `mcp/features/mask` 分别拥有自己的 tools、resources 和 workflow 语义；
- `mcp/core` 只保留 registry、dispatch、runtime facade、protocol / session / metadata / execution 等共享平台职责；
- `mcp/bootstrap` 继续只消费聚合后的 tool / resource surface，不直接依赖具体实现类。

规划后的核心技术路线不是“把类搬目录”，也不是“把 provider 搬出去”，而是把当前只在 feature 模块级可插拔的设计，继续收敛成 **tool / resource 粒度直接 SPI 装配** 的架构。

## Technical Context

**Language/Version**: Java 17  
**Primary Dependencies**:
- `shardingsphere-infra-spi` / `ShardingSphereServiceLoader`
- `mcp/core` 当前 controller、registry、protocol、session、metadata、execution 基础设施
- `mcp/bootstrap` MCP Java SDK runtime 暴露层
- Proxy metadata / DistSQL / SQL execution path
**Storage**:
- 进程内 registry 聚合结果
- feature 自己维护的 workflow 状态，或通过 SPI 暴露的共享 workflow store facade 持有的进程内状态
- 无新增外部持久化
**Testing**:
- JUnit 5
- Mockito
- 模块级 Maven 测试与静态检查
- 重点覆盖 `mcp/features/spi`、`mcp/features/encrypt`、`mcp/features/mask`、`mcp/core`、`mcp/bootstrap`
**Target Platform**:
- ShardingSphere MCP runtime
- STDIO 与 Streamable HTTP 两种 transport
**Project Type**: 现有仓库内的多模块 Java 后端重构
**Performance Goals**:
- 启动期 handler 发现与 surface 聚合保持确定性
- tool / resource dispatch 不引入额外 feature 分支扫描
- 不因插件化而增加不必要的 metadata / execution 热路径开销
**Constraints**:
- 不创建新分支
- 不保留 pre-release 兼容层
- `mcp/core` 不允许保留 encrypt / mask 业务分支
- encrypt / mask tool family 必须拆开命名
- URI 需要按插件化 feature ownership 重新设计
- feature 模块只能通过 `mcp/features/spi` 与上层交互
- 在当前固定模块结构下，不额外新增运行时聚合模块
**Scale/Scope**:
- 覆盖 `mcp` reactor 的模块结构、SPI 契约、registry 装配、tool / resource surface、workflow 归属与打包路径
- 不扩展 encrypt / mask 的业务范围，只重构模块边界和首发外部契约

## Constitution Check

*GATE: 通过；本次为架构重构，不引入额外产品边界扩张。*

- **Proxy-first logical abstraction**
  - 本次不改变 Proxy 逻辑视图优先的产品语义，只调整 MCP 模块边界与加载机制。
- **Explicit operator control**
  - 现有 encrypt / mask 的 plan / review / apply / validate 流程仍然保留，只是职责下沉到 feature 模块。
- **Minimal safe automation**
  - 不借本次模块化重构引入数据迁移、回滚或额外自动化能力。
- **Deterministic naming and transparent changes**
  - SPI 聚合必须保持确定性，并继续对重复 tool / URI 做显式失败。
- **Complete verification before completion**
  - validate 语义继续保留在 feature 内，不因为模块拆分而降级为只做注册级检查。
- **Behavior scope note**
  - 003 只处理模块化与首发 contract 设计，不在本 planning 中重新裁定 encrypt / mask 生命周期边界差异。

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
`-- features/
    |-- pom.xml
    |-- spi/
    |-- encrypt/
    `-- mask/
```

**Structure Decision**:
新增 `mcp/features` reactor，并把面向 feature 的稳定契约整体提升到 `mcp/features/spi`。`mcp/core` 只保留共享基础设施和聚合装配逻辑；encrypt / mask 的业务 surface 与 workflow 实现分别下沉到对应 feature 模块，并通过 handler SPI 直接注册。

## Design Focus

### Architectural direction

- 定义 feature 可直接依赖的 `ToolHandler` / `ResourceHandler` / descriptor / runtime facade / workflow seam 契约。
- 把当前位于 `mcp/core` 的 extension-facing contract 提升到 `mcp/features/spi`。
- 保持 `bootstrap -> registry -> controller -> handler` 的整体调用链，但让 registry 聚合来源收敛为“handler SPI 发现”。
- encrypt / mask 各自拥有独立 tool family、resource namespace、workflow service 和 state model。
- `MCPFeatureProvider` 不再作为 tool / resource surface assembly 的主入口；若未来需要 feature metadata，应单独建模。

### Behavior preservation

- 001 中已经定义的 encrypt / mask workflow 行为继续作为业务基线。
- 本次允许重命名 tools 和重设 URI namespace，因为产品尚未发布。
- 行为迁移的目标是“外部 surface 更干净、模块边界更清晰”，不是重新设计 workflow 本身。

### Cleanup expectation

- 删除 `mcp/core` 中 encrypt / mask 特定的 workflow branching 与 hard-coded handler registration source。
- 删除 feature tool / resource 仍靠顶层 provider 手工枚举的现状。
- 删除 `plan_encrypt_mask_rule` / `apply_encrypt_mask_rule` / `validate_encrypt_mask_rule` 这一类跨 feature 共享 tool family。

## Implementation Slices

### Slice 1 - Reactor and SPI extraction

- 新增 `mcp/features/pom.xml`、`mcp/features/spi`、`mcp/features/encrypt`、`mcp/features/mask`。
- 提升 extension-facing contract 到 `mcp/features/spi`。
- 明确 `core -> features/spi` 与 `feature -> features/spi` 的依赖方向。

### Slice 2 - Registry and runtime assembly refactor

- 让 `mcp/core` 通过 `ToolHandler` / `ResourceHandler` SPI 聚合 encrypt / mask 的 tools 与 resources。
- 保留 core 自己的共享 surface，同时把 feature surface 的装配来源从 provider 枚举切换成 handler SPI。
- 保证 duplicate tool name、duplicate / overlapping URI 在启动期显式失败。

### Slice 3 - Encrypt feature migration

- 将 encrypt tools、resources、planning、artifact generation、validation、algorithm recommendation、property template 等能力迁入 `mcp/features/encrypt`。
- 对外发布 encrypt 专属 tool family 与 URI namespace。
- 通过 `META-INF/services` 直接注册 encrypt 的 tools 与 resources。

### Slice 4 - Mask feature migration

- 将 mask tools、resources、planning、validation、algorithm recommendation、property template 等能力迁入 `mcp/features/mask`。
- 对外发布 mask 专属 tool family 与 URI namespace。
- 通过 `META-INF/services` 直接注册 mask 的 tools 与 resources。

### Slice 5 - Core cleanup and verification

- 清理 `mcp/core` 中剩余的 encrypt / mask 分支与 provider-based assembly path。
- 补齐 feature 级、registry 级、bootstrap 暴露级测试。
- 验证只装载单个 feature 时的 discovery surface 和错误场景。

## Verification Strategy

- `./mvnw -pl mcp/features/spi -am test -DskipITs -Dspotless.skip=true`
- `./mvnw -pl mcp/features/encrypt -am test -DskipITs -Dspotless.skip=true`
- `./mvnw -pl mcp/features/mask -am test -DskipITs -Dspotless.skip=true`
- `./mvnw -pl mcp/core -am test -DskipITs -Dspotless.skip=true`
- `./mvnw -pl mcp/bootstrap -am test -DskipITs -Dspotless.skip=true`
- 对触达模块补充 `checkstyle:check -Pcheck` 与必要的 `spotless:apply -Pcheck`

## Complexity Tracking

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| None | N/A | N/A |
