# Implementation Plan: ShardingSphere MCP HTTP Transport Simplification After SDK Reuse

**Branch**: `007-shardingsphere-mcp-http-transport-simplification` | **Date**: 2026-03-28 | **Spec**:
[spec.md](/Users/zhangliang/IdeaProjects/shardingsphere/specs/007-shardingsphere-mcp-http-transport-simplification/spec.md)
**Input**: Feature specification from `/specs/007-shardingsphere-mcp-http-transport-simplification/spec.md`

## Summary

本 follow-up 不重新讨论 `006` 的 SDK reuse 方向，而是把 HTTP transport 的实现继续收敛到更简单的最终形态：
保留 MCP SDK 作为 transport core，保留必须由 ShardingSphere 自己承接的 protocol/runtime glue，
同时消除只服务单点调用、且没有稳定抽象价值的 helper 文件。

## Technical Context

**Language/Version**: Java 17 in the MCP subchain  
**Primary Dependencies**: MCP Java SDK 1.1.0, embedded Tomcat 11, Jackson 2.16.1,
ShardingSphere MCP runtime/session infrastructure  
**Storage**: in-memory SDK transport sessions plus in-memory ShardingSphere managed session state  
**Testing**: existing HTTP integration regressions, validator tests, lifecycle tests, scoped Maven verification  
**Target Platform**: Standalone MCP runtime on embedded Tomcat  
**Project Type**: Java monorepo subproject under `mcp/bootstrap`  
**Constraints**: zero public behavior drift; prefer fewer abstractions; do not depend on SDK internal helpers;
only keep standalone types that represent stable seams  
**Scale/Scope**: `mcp/bootstrap` HTTP transport structure, helper consolidation, docs alignment, no protocol redesign

## Constitution Check

*GATE: Must pass before implementation starts. Re-check after design.*

- **Gate 1 - Smallest safe change**: PASS  
  只做结构简化与边界收口，不改 public MCP contract。
- **Gate 2 - Readability and simplicity**: PASS  
  目标是减少文件跳转和 one-off helper，而不是继续制造细碎抽象。
- **Gate 3 - Clear abstraction levels**: PASS  
  保留 SDK core、独立本地策略类、ShardingSphere glue 三层划分。
- **Gate 4 - Verification path exists**: PASS  
  `006` 已建立的零损失回归矩阵可直接复用。
- **Gate 5 - Compatibility risk is bounded**: PASS  
  所有“能不能合并/删除”的判断都以现有回归和已确认 contract 为界。

## Project Structure

### Documentation (this feature)

```text
specs/007-shardingsphere-mcp-http-transport-simplification/
├── spec.md
├── research.md
├── plan.md
└── tasks.md
```

### Source Code (repository root)

```text
mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/http/StreamableHttpMCPServlet.java
mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/http/StreamableHttpMCPRequestValidator.java
mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/http/LoopbackOriginSecurityValidator.java
mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/http/StreamableHttpRuntimeIntegrationTest.java
mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/http/StreamableHttpMCPRequestValidatorTest.java
mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/http/ProductionMetadataDiscoveryIntegrationTest.java
mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/http/ProductionExecuteQueryIntegrationTest.java
docs/mcp/ShardingSphere-MCP-Technical-Design.md
docs/mcp/ShardingSphere-MCP-Detailed-Design.md
```

**Structure Decision**: 不新增模块；仅在 `mcp/bootstrap` 下重新收敛 helper 形态，并同步文档表达。

## Design Decisions

### 1. 保持 SDK core，不回退 `006`

- 继续以 `HttpServletStreamableServerTransportProvider` 作为 transport core。
- 不重新把 routing / session / SSE / `DELETE` mechanics 拉回本地实现。

### 2. 保留独立的本地策略类，合并 one-off helper

- 本地策略类如 `LoopbackOriginSecurityValidator` 可以继续独立。
- 只服务单次包装或单次 normalize 的 helper 优先合并回 servlet 或更贴近调用点的局部结构。

### 3. 不为复用而强迫走 SDK hook

- loopback `Origin` 保持独立策略类，但由 servlet 前置校验直接使用。
- delegate 的 `securityValidator` 使用 `NOOP`，避免重复执行同一规则。
- package-private 的 SDK helper 不作为承载方案。
- `DefaultServerTransportSecurityValidator` 只有在零损失验证完全成立时才可替代当前 loopback 语义；
  当前默认按“不替代”处理。

### 4. ShardingSphere-specific glue 不删，只压缩组织形式

- initialize protocol header
- follow-up protocol/session contract
- missing/blank `Accept` compatibility
- managed session cleanup
- classloader guard

这些仍保留在本地，只优化结构和落位。

### 5. `Accept` 继续采用最薄本地 shim，而不是误判为 SDK native

- 当前 SDK 1.1.0 没有提供可自定义的 Accept 校验策略 hook。
- 因此 missing/blank `Accept` 的零损失兼容只能继续通过本地预处理承接。
- 本轮优化目标是把这层 shim 收敛到最简单的位置，而不是错误地把它标记成“已完全内化到 SDK”。

## Branch Checklist

1. `single_use_helper_removed_or_inlined`
   Planned verification: 当前 one-off helper 数量下降，且保留类都有明确 seam 理由
2. `single_security_validation_path_preserved`
   Planned verification: loopback `Origin` 继续保留独立策略类，且请求只走一条本地校验路径
3. `protocol_contract_preserved`
   Planned verification: initialize protocol header、follow-up protocol fallback/mismatch 继续通过
4. `accept_compatibility_preserved`
   Planned verification: missing/blank `Accept` 行为不变
5. `managed_session_cleanup_preserved`
   Planned verification: `DELETE` 和 shutdown cleanup 继续通过
6. `classpath_driver_behavior_preserved`
   Planned verification: metadata / execute-query 的 classpath-driver 回归继续通过

## Implementation Strategy

1. 先列出现有 HTTP 生产类的 keep / inline / retain 理由矩阵。
2. 合并 one-off helper，避免因局部“瘦身”造成整体结构变碎。
3. 保留真正代表稳定本地概念边界的独立类型。
4. 复用 `006` 的回归矩阵验证零损失。
5. 明确 `Accept` 兼容在当前 SDK 下仍需本地 shim。
6. 更新设计文档，写清楚“哪些还可继续内化到 SDK，哪些必须保留在 ShardingSphere”。

## Validation Strategy

- **HTTP regression matrix**
```bash
./mvnw -pl mcp/bootstrap -am -DskipITs -Dspotless.skip=true \
  -Dtest=StreamableHttpRuntimeIntegrationTest,ProductionMetadataDiscoveryIntegrationTest,ProductionExecuteQueryIntegrationTest test \
  -Dsurefire.failIfNoSpecifiedTests=false
```
- **Validator and lifecycle coverage**
```bash
./mvnw -pl mcp/bootstrap -am -DskipITs -Dspotless.skip=true \
  -Dtest=StreamableHttpMCPRequestValidatorTest,ManagedSessionRegistryTest,MCPSessionCloserTest test \
  -Dsurefire.failIfNoSpecifiedTests=false
```
- **Scoped style checks**
```bash
./mvnw -pl mcp/bootstrap -Pcheck -DskipITs -DskipTests checkstyle:check spotless:check
```

## Rollout Notes

- 这是 `006` 之后的结构简化 follow-up，不改 public protocol。
- 目标不是“更多类”，而是“更少不必要的类”。
- 如某个 helper 不能清楚说明其独立存在的 seam 价值，应默认合并。
