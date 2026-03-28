# Implementation Plan: ShardingSphere MCP HTTP SDK Reuse Without Capability Loss

**Branch**: `006-shardingsphere-mcp-http-sdk-reuse` | **Date**: 2026-03-28 | **Spec**:
[spec.md](/Users/zhangliang/IdeaProjects/shardingsphere/specs/006-shardingsphere-mcp-http-sdk-reuse/spec.md)
**Input**: Feature specification from `/specs/006-shardingsphere-mcp-http-sdk-reuse/spec.md`

## Summary

本 follow-up 收敛 `StreamableHttpMCPServlet` 与官方 MCP Java SDK
`HttpServletStreamableServerTransportProvider` 的职责边界：
把 SDK 已覆盖的 HTTP transport mechanics 交回 SDK，把 loopback `Origin`
校验迁移到官方 `securityValidator` 扩展点，同时保留 ShardingSphere 当前的固定协议版本、
initialize 响应头、follow-up session/protocol contract、managed session cleanup
和零损失兼容行为。

## Technical Context

**Language/Version**: Java 17 in the MCP subchain  
**Primary Dependencies**: MCP Java SDK 1.1.0, embedded Tomcat 11, repository-managed
Jackson 2.16.1, ShardingSphere MCP runtime/session infrastructure  
**Storage**: in-memory SDK transport session map, in-memory ShardingSphere session state,
in-memory metadata refresh and database runtime session state  
**Testing**: JUnit 5, HTTP integration tests, targeted validator tests, scoped Maven verification  
**Target Platform**: Standalone MCP distribution with embedded Tomcat on Linux or macOS  
**Project Type**: Java monorepo subproject with bootstrap runtime and integration tests  
**Constraints**: keep public HTTP contract unchanged; no capability loss; prefer official SDK hooks;
keep the smallest safe change; avoid speculative transport rewrites  
**Scale/Scope**: HTTP transport implementation seam, request validation placement, SDK security hook wiring,
managed session cleanup, compatibility regressions, docs alignment

## Constitution Check

*GATE: Must pass before implementation starts. Re-check after design.*

- **Gate 1 - Smallest safe change**: PASS  
  目标是 transport seam 收敛，不改 public MCP feature surface。
- **Gate 2 - Readability and simplicity**: PASS  
  通过减少重复 transport logic，让 servlet 只保留 contract glue 和 lifecycle bridge。
- **Gate 3 - Runtime behavior remains traceable**: PASS  
  所有保留项都能映射到现有 contract 或零损失兼容矩阵。
- **Gate 4 - Verification path exists**: PASS  
  现有 HTTP integration tests 可复用，并可补 Accept/origin/classloader 回归。
- **Gate 5 - Compatibility risk is bounded**: PASS  
  采用“先补回归，再删冗余”的策略，避免 refactor 造成行为收缩。

## Project Structure

### Documentation (this feature)

```text
specs/006-shardingsphere-mcp-http-sdk-reuse/
├── spec.md
├── research.md
├── plan.md
└── tasks.md
```

### Source Code (repository root)

```text
mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/http/StreamableHttpMCPServlet.java
mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/http/StreamableHttpMCPRequestValidator.java
mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/http/StreamableHttpMCPServer.java
mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/ManagedSessionRegistry.java
mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/MCPSessionCloser.java
mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/http/StreamableHttpMCPRequestValidatorTest.java
mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/http/StreamableHttpRuntimeIntegrationTest.java
mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/http/AbstractProductionRuntimeIntegrationTest.java
docs/mcp/ShardingSphere-MCP-Technical-Design.md
docs/mcp/ShardingSphere-MCP-Detailed-Design.md
```

**Structure Decision**: 不新增模块，继续在 `mcp/bootstrap` 内完成 SDK 复用和
contract glue 收敛；managed session cleanup 继续复用现有 transport-shared 基础设施。

## Design Decisions

### 1. 复用 SDK 原生 transport 能力

- 直接依赖官方 provider 处理 endpoint dispatch、Accept 校验、SDK session map、
  `DELETE` 基础语义和 graceful close。
- 不再在 servlet 中重复承载这些 mechanics。

### 2. `Origin` 边界优先进入 SDK `securityValidator`

- loopback `Origin` 校验下沉到官方 security hook。
- `StreamableHttpMCPRequestValidator` 优先只保留 session/protocol contract
  和必要兼容逻辑。

### 3. 继续保留 ShardingSphere managed session lifecycle bridge

- initialize 成功后仍要创建 managed session。
- `DELETE` 成功和 server shutdown 时仍要清理 metadata refresh、database runtime
  和 session manager。

### 4. 协议版本 contract 不动

- 固定协议基线仍为 `2025-11-25`。
- initialize 响应头仍要回 `MCP-Protocol-Version`。
- follow-up 仍要校验或回退协商版本。

### 5. 零损失兼容行为先验证再移除

- 当前缺省 `Accept` 兼容先纳入回归矩阵。
- TCCL bridge 只有在 classpath-driver 验证通过后才允许删除。

## Branch Checklist

在实现前固定关键分支与计划测试，避免 refactor 把“代码收敛”做成“能力收缩”：

1. `sdk_native_dispatch_path`
   Planned test: initialize / follow-up / GET / DELETE 继续通过，但 endpoint dispatch 和 Accept 校验主要依赖 SDK provider
2. `loopback_origin_via_sdk_hook`
   Planned test: invalid loopback `Origin` 继续返回 `403`，且承载位置迁移到 SDK security hook
3. `initialize_protocol_header_preserved`
   Planned test: initialize 成功响应继续返回 `MCP-Protocol-Version`
4. `followup_protocol_fallback_preserved`
   Planned test: follow-up 缺失 protocol header 时继续回退到协商版本
5. `delete_cleanup_preserved`
   Planned test: `DELETE` 成功后继续触发 managed session cleanup
6. `shutdown_cleanup_preserved`
   Planned test: server shutdown 仍关闭全部 managed sessions
7. `accept_compatibility_preserved`
   Planned test: 省略或留空 `Accept` 时仍与当前实现兼容
8. `classpath_driver_http_runtime_preserved`
   Planned test: HTTP runtime 在 optional driver / classpath driver 场景下不因 refactor 失败

## Implementation Strategy

1. 先补齐“零损失回归矩阵”，把当前 contract glue 和兼容行为固化为 tests。
2. 引入或下沉 loopback `Origin` SDK `securityValidator` 实现。
3. 收敛 `StreamableHttpMCPServlet`，让 SDK 承担已覆盖的 transport mechanics。
4. 仅保留 ShardingSphere 独有的 protocol/session contract 和 managed session cleanup。
5. 在回归通过前，不删除 `Accept` 兼容层和 classloader bridge。
6. 最后更新设计文档，说明哪些能力已交回 SDK、哪些必须由 ShardingSphere 保留。

## Validation Strategy

- **HTTP integration regression matrix**
```bash
./mvnw -pl mcp/bootstrap -am -DskipITs -Dspotless.skip=true \
  -Dtest=StreamableHttpRuntimeIntegrationTest,ProductionMetadataDiscoveryIntegrationTest,ProductionExecuteQueryIntegrationTest test \
  -Dsurefire.failIfNoSpecifiedTests=false
```
- **Targeted validator and lifecycle coverage**
```bash
./mvnw -pl mcp/bootstrap -am -DskipITs -Dspotless.skip=true \
  -Dtest=StreamableHttpMCPRequestValidatorTest,ManagedSessionRegistryTest,MCPSessionCloserTest test \
  -Dsurefire.failIfNoSpecifiedTests=false
```
- **Scoped style checks for touched module**
```bash
./mvnw -pl mcp/bootstrap -Pcheck -DskipITs -DskipTests checkstyle:check spotless:check
```

## Rollout Notes

- 这是 HTTP transport seam 收敛，不是 public MCP contract 变更。
- `006` 继续继承 `001` 已定义的 Streamable HTTP contract；本 follow-up 只补充
  “SDK 能力复用优先，但 contract 和兼容行为零损失”的实现约束。
- 如果某项当前能力经验证不能由 SDK 直接承接，就保留一层最薄本地 shim，
  而不是为了“纯 SDK”牺牲行为。
