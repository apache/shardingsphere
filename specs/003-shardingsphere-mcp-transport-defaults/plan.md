# Implementation Plan: ShardingSphere MCP Transport Default Realignment

**Branch**: `003-shardingsphere-mcp-transport-defaults` | **Date**: 2026-03-22 | **Spec**:
[spec.md](/Users/zhangliang/IdeaProjects/shardingsphere/specs/003-shardingsphere-mcp-transport-defaults/spec.md)
**Input**: Feature specification from `/specs/003-shardingsphere-mcp-transport-defaults/spec.md`

## Summary

将 MCP 发行包的默认 transport 从“HTTP-first / 文档漂移状态”收敛为“STDIO-first、HTTP opt-in、双 transport 能力保留”，
并通过统一的启动矩阵、技术文档与验证路径把默认行为重新固定下来。

## Technical Context

**Language/Version**: Java 17 in the MCP subchain  
**Primary Dependencies**: MCP Java SDK, embedded Tomcat, repository-managed Jackson 2.16.1, ShardingSphere `mcp/core` and `mcp/bootstrap` transport wiring  
**Storage**: Packaged transport configuration under `distribution/mcp/conf/mcp.yaml`  
**Testing**: JUnit 5, existing bootstrap integration tests, new transport launch-matrix tests, scoped Maven verification  
**Target Platform**: Standalone MCP distribution on Linux or macOS; default local `stdio` launch profile; optional remote `Streamable HTTP` profile  
**Project Type**: Java monorepo subproject with standalone distribution  
**Constraints**: Do not change HTTP wire contract or STDIO API semantics; keep existing config key names; preserve both transports in build output; keep fail-fast on `both false`  
**Scale/Scope**: Transport defaults, runtime launcher diagnostics, config tests, bootstrap launch tests, README/technical design/quickstart updates

## Constitution Check

*GATE: Must pass before implementation starts. Re-check after design.*

- **Gate 1 - Smallest safe change**: PASS  
  本次变更只调整默认启动组合与文档，不重写 transport 协议层。
- **Gate 2 - Existing contracts stay visible**: PASS  
  HTTP contract 继续继承 `001-shardingsphere-mcp` 的 `/mcp` Streamable HTTP 约束。
- **Gate 3 - Verification path exists**: PASS  
  通过 transport 启动矩阵与现有 STDIO/HTTP integration smoke 即可验证。
- **Gate 4 - Reviewer traceability**: PASS  
  默认值、支持矩阵、文档更新与实现任务都将显式记录在本 spec 包中。

## Project Structure

### Documentation (this feature)

```text
specs/003-shardingsphere-mcp-transport-defaults/
├── spec.md
├── research.md
├── plan.md
├── tasks.md
├── quickstart.md
└── contracts/
    └── transport-launch-contract.md
```

### Source Code (repository root)

```text
distribution/mcp/src/main/resources/conf/mcp.yaml
mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/lifecycle/McpRuntimeLauncher.java
mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/config/McpConfigurationLoaderTest.java
mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/server/StdioTransportIntegrationTest.java
mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/server/StreamableHttpRuntimeIntegrationTest.java
mcp/README.md
mcp/README_ZH.md
docs/mcp/ShardingSphere-MCP-Technical-Design.md
docs/mcp/ShardingSphere-MCP-Detailed-Design.md
specs/001-shardingsphere-mcp/quickstart.md
```

**Structure Decision**: 继续复用现有 transport 实现与配置开关，不新增 transport 层模块；主要工作集中在默认配置、启动诊断、测试矩阵与文档收敛。

## Design Decisions

### 1. 保留双 transport，调整默认 profile

- 默认 profile: `stdio only`
- 远程 profile: `http only`
- 联调 profile: `stdio + http`
- 非法 profile: `both false`

### 2. 不修改 transport contract

- HTTP 继续固定为 `Streamable HTTP`
- STDIO 继续使用现有 `StdioMcpServer`
- session、header、SSE、origin 校验都不随默认值改变

### 3. 增加启动态可观测性

- 启动时输出 effective transport state
- 明确说明是否启动 HTTP listener
- 在 `both false` 前置失败，而非部分启动后回滚

## Branch Checklist

在实现前先固定四个分支与计划测试，避免默认值切换后出现覆盖盲区：

1. `stdio only`
   Planned test: 默认发行配置加载测试 + STDIO 默认启动测试
2. `http only`
   Planned test: HTTP opt-in 启动测试 + `/mcp` session smoke
3. `dual enabled`
   Planned test: 同时启动两条 transport 的 launcher/integration 测试
4. `both false`
   Planned test: fail-fast 配置验证测试

## Implementation Strategy

1. 修改发行包默认 YAML，使默认 profile 变为 `stdio only`。
2. 在 launcher 层输出生效 transport 状态，并保持 `both false` fail-fast。
3. 扩展 bootstrap 测试到四分支启动矩阵。
4. 收敛 README、技术设计和 quickstart，使默认值、推荐用法与实现一致。

## Validation Strategy

- **Config loader**
  `./mvnw -pl mcp/bootstrap -am -DskipITs -Dspotless.skip=true -Dtest=McpConfigurationLoaderTest test -Dsurefire.failIfNoSpecifiedTests=false`
- **Bootstrap transport integration**
  `./mvnw -pl mcp/bootstrap -am -DskipITs -Dspotless.skip=true -Dtest=StdioTransportIntegrationTest,StreamableHttpRuntimeIntegrationTest test -Dsurefire.failIfNoSpecifiedTests=false`
- **Scoped style check for touched modules**
  `./mvnw -pl mcp/bootstrap,distribution/mcp -am checkstyle:check -Pcheck -DskipITs -Dspotless.skip=true`

## Rollout Notes

- 这是 transport 默认值重排，不是 transport 能力收缩。
- 文档应明确写出：默认关闭 HTTP，不等于不支持 HTTP。
- 若后续要把默认 profile 再切回 HTTP-first，应视为新的 feature，而不是在 README 中随意改文案。
