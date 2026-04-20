# Implementation Plan: MCP Runtime Pluginization Completion

**Branch**: `[001-shardingsphere-mcp]` | **Date**: 2026-04-20 | **Spec**: `/.specify/specs/005-mcp-runtime-pluginization/spec.md`
**Input**: Feature specification from `/.specify/specs/005-mcp-runtime-pluginization/spec.md`

## Summary

这次规划不是再做一轮“模块拆分”，而是把 `003-mcp-feature-spi` 补到真正可交付的插件化边界：

- `mcp/bootstrap` 只负责消费启动时 classpath 上能发现到的 handler surface；
- 官方默认 feature 集合改由 `distribution/mcp` 显式打包决定；
- 外部 feature jar 的扩展契约收敛为“启动前放入运行时 classpath”，统一使用 `plugins/`；
- 测试与文档边界同步拆开，避免再把“bootstrap 能发现 feature”和“官方发行包默认带哪些 feature”混成一件事。

本次的核心不是新增更多抽象，而是按 `CODE_OF_CONDUCT.md` 的 **Cleanliness / Consistency / Simplicity / Abstraction** 原则，把当前“编译期耦合 + 运行时看起来像插件”的半插件化状态收束成清晰责任边界。

## Technical Context

**Language/Version**: Java 17  
**Primary Dependencies**:
- `shardingsphere-infra-spi` / `ShardingSphereServiceLoader`
- `mcp/core` 与 `mcp/bootstrap` 的现有 handler registry / transport publishing 链路
- `distribution/mcp` 的打包与启动脚本
- `mcp/features/encrypt` / `mcp/features/mask` 现有 feature jar
**Storage**:
- 无新增持久化
- 仅依赖进程启动时的 classpath 与既有 registry 聚合结果
**Testing**:
- JUnit 5
- Mockito
- 模块级 Maven 测试
- 重点覆盖 `mcp/bootstrap`、`distribution/mcp`、必要的 `mcp/core` registry 场景
**Target Platform**:
- MCP packaged runtime
- STDIO 与 Streamable HTTP 运行模式
**Project Type**: 多模块 Java 后端 + 发行包边界重构
**Performance Goals**:
- 启动期 discovery 保持确定性
- 不引入额外动态 classloader 或热插拔管理成本
- 不增加请求热路径分支
**Constraints**:
- 不切换新分支
- 不新增 runtime 聚合模块
- 不做热插拔 / 热重载
- 官方基线 jar 保留在 `lib/`，用户扩展 jar 统一放到 `plugins/`
- 官方发行包默认 feature 行为不能退化
- 文档必须修正当前“让 bootstrap 依赖 feature” 的指引
**Scale/Scope**:
- 覆盖 `mcp/bootstrap`、`distribution/mcp`、启动脚本、README、相关测试
- 不重做 003 的 feature ownership 拆分
- 不变更 encrypt / mask 业务语义

## Constitution Check

*GATE: 通过；本次只修正运行时装载和发行边界，不改变 Proxy-first、显式 review/apply、V1 无迁移回填等产品边界。*

- **Proxy-first logical abstraction**
  - 不改变 Proxy 作为 MCP 目标运行面的约束。
- **Explicit operator control**
  - 不改变 plan / apply / validate 的业务流程。
- **Minimal safe automation**
  - 不新增任何自动迁移、回滚、热更新能力。
- **Deterministic naming and transparent changes**
  - 插件 discovery 仍需确定性与显式失败。
- **Complete verification before completion**
  - 本次重点是 build / packaging / discovery 边界验证，不降级现有业务校验语义。

## Project Structure

### Documentation (this feature)

```text
specs/005-mcp-runtime-pluginization/
|-- spec.md
|-- plan.md
|-- research.md
`-- tasks.md
```

### Source Code (repository root)

```text
mcp/
|-- bootstrap/
|-- core/
|-- features/
|   |-- encrypt/
|   `-- mask/
`-- README_ZH.md

distribution/
`-- mcp/
    |-- pom.xml
    `-- src/main/bin/start.sh
```

**Structure Decision**:
本次不新增新模块，直接在现有 `mcp/bootstrap` 与 `distribution/mcp` 之间重新划定责任：bootstrap 管“发现并发布 classpath 上的 surface”，distribution 管“官方默认带哪些 feature jar”，外部 jar 则通过现有运行时扩展目录接入。

## Design Focus

### Architectural direction

- 从 build graph 上移除 `bootstrap -> encrypt/mask` 的主依赖。
- 把官方默认 feature 选择权下放到 `distribution/mcp`。
- 明确“真插件化”的定义是 startup-time classpath discovery，而不是 hot-plug。
- 让 bootstrap 测试验证 generic discovery，让 distribution 验证 official bundle。
- 用一个最小 fixture feature 证明发现逻辑依赖 SPI + classpath，而不是依赖 encrypt/mask 的特殊地位。

### Behavior preservation

- 对官方发行包用户来说，encrypt / mask 仍然默认可用。
- 对直接消费 `shardingsphere-mcp-bootstrap` 的维护者来说，feature surface 将改为“谁在 classpath 上，谁被发布”。
- 对外部 feature 开发者来说，新增 feature 不再需要修改 bootstrap 依赖，只需要把 jar 放到 `plugins/` 或加入目标运行时 classpath。

### Cleanup expectation

- 删除 `mcp/bootstrap/pom.xml` 中把 encrypt / mask 当成默认 runtime 构成的一部分。
- 删除 README 中“让 bootstrap 依赖新 feature”的指导。
- 删除 bootstrap 测试里把 encrypt / mask 当作固有内建能力的假设。

## Implementation Slices

### Slice 1 - Build and packaging responsibility transfer

- 调整 `mcp/bootstrap/pom.xml`，移除 encrypt / mask 主依赖。
- 调整 `distribution/mcp/pom.xml`，显式把 encrypt / mask 纳入官方发行包 runtime。
- 如有必要，bootstrap 测试暂时用 `test` scope 保持过渡覆盖，但不恢复主依赖语义。

### Slice 2 - Startup plugin contract clarification

- 明确启动期 classpath discovery 是唯一插件装载语义。
- 统一使用 `plugins/` 作为外部 JDBC driver 与可选 feature jar 的接入路径。
- 明确新增 / 删除 jar 后必须重启进程才能生效。

### Slice 3 - Test boundary refactor

- bootstrap 单测 / 集成测试改成验证“发现 classpath 上已有 feature”。
- 增加一个最小 fixture feature，用来证明 discovery 不依赖 encrypt / mask 特判。
- distribution 或打包层补充默认 bundle 验证。

### Slice 4 - Documentation and onboarding update

- 更新 `mcp/README.md` 与 `mcp/README_ZH.md`。
- 说明 bootstrap、distribution、operator extension 三层职责。
- 说明 direct bootstrap consumer 与 packaged distribution 的行为差异。

## Verification Strategy

- `./mvnw -pl mcp/bootstrap -am test -DskipITs -Dspotless.skip=true`
- `./mvnw -pl mcp/core -am test -DskipITs -Dspotless.skip=true`
- `./mvnw -pl distribution/mcp -am package -DskipTests`
- 按触达范围补充 `./mvnw -pl mcp/bootstrap,distribution/mcp -am checkstyle:check -Pcheck`
- 如添加 fixture plugin 测试，补跑对应的 bootstrap 指定测试类

## Complexity Tracking

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| None | N/A | N/A |
