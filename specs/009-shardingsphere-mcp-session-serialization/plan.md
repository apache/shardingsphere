# Implementation Plan: ShardingSphere MCP Same-Session Execution Serialization

**Branch**: `009-shardingsphere-mcp-session-serialization` | **Date**: 2026-04-01 | **Spec**:
[spec.md](/Users/zhangliang/IdeaProjects/shardingsphere/specs/009-shardingsphere-mcp-session-serialization/spec.md)
**Input**: Feature specification from `/specs/009-shardingsphere-mcp-session-serialization/spec.md`

## Summary

本特性在 `mcp/core` 中引入 per-session execution guard，
把同一 `session` 的 `execute_query` 与 `closeSession()` 串行化，
从而修复当前 transaction resource、savepoint 和 session cleanup 的竞态问题。

本轮不改变 STDIO / HTTP 的 public contract，不引入新的错误码，不把 runtime 退化为全局单线程。

## Technical Context

**Language/Version**: Java 17 in the MCP subchain  
**Primary Dependencies**: repository-owned MCP core runtime, Java concurrency primitives, MCP Java SDK only in bootstrap  
**Storage**: in-memory session registry and in-memory transaction resource map  
**Testing**: core concurrency tests, existing session manager tests, targeted HTTP regression, scoped Maven verification  
**Target Platform**: ShardingSphere MCP standalone runtime on embedded Tomcat / STDIO  
**Project Type**: Java monorepo subproject under `mcp/core` and `mcp/bootstrap`  
**Constraints**: same-session strict serialization; no public contract drift; no transport-specific lock duplication;
smallest safe change; keep metadata/capability paths outside unnecessary hotspots  
**Scale/Scope**: `MCPSessionManager`, `MCPSQLExecutionFacade`, targeted core tests, one transport-side regression

## Constitution Check

*GATE: Must pass before implementation starts. Re-check after design.*

- **Gate 1 - Smallest safe change**: PASS  
  本轮只修复 same-session 执行竞态，不改 transport topology、不改 public MCP contract。
- **Gate 2 - Readability and simplicity**: PASS  
  用一个集中式 per-session guard 代替 scattered transport assumptions 或多处 ad-hoc locking。
- **Gate 3 - Clear abstraction levels**: PASS  
  session lifecycle / execution ordering 留在 core；HTTP / STDIO 继续只是 transport entry 与 cleanup trigger。
- **Gate 4 - Verification path exists**: PASS  
  可以用 core 并发测试验证 race fix，再用 transport regression 证明对外行为不变。
- **Gate 5 - Compatibility risk is bounded**: PASS  
  不新增 client-visible headers、tool arguments 或错误码；主要风险局限于内部执行顺序。

## Project Structure

### Documentation (this feature)

```text
specs/009-shardingsphere-mcp-session-serialization/
├── spec.md
├── research.md
├── data-model.md
├── plan.md
└── tasks.md
```

### Source Code (repository root)

```text
mcp/core/src/main/java/org/apache/shardingsphere/mcp/session/MCPSessionManager.java
mcp/core/src/main/java/org/apache/shardingsphere/mcp/execute/MCPSQLExecutionFacade.java
mcp/core/src/test/java/org/apache/shardingsphere/mcp/session/MCPSessionManagerTest.java
mcp/core/src/test/java/org/apache/shardingsphere/mcp/execute/MCPSQLExecutionFacadeConcurrencyTest.java
mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/StreamableHttpRuntimeIntegrationTest.java
```

**Structure Decision**: 不新增模块；把 session guard 收口到 `mcp/core`，
transport 只继续调用现有 `closeSession()` / `closeAllSessions()` 入口。

## Design Decisions

### 1. `MCPSessionManager` 升级为 session context registry

- 从简单 `Set<String>` 升级到 per-session context registry。
- 每个 live session 都拥有一个稳定的 `SessionExecutionContext`。
- 该 context 至少承载：
  - `sessionId`
  - one `ReentrantLock` or equivalent execution guard
  - lifecycle marker needed by close semantics

### 2. `MCPSQLExecutionFacade` 成为 same-session guard 的统一入口

- `execute(ExecutionRequest)` 外层包一层 session guard。
- guard 持有期间覆盖：
  - statement classification
  - transaction-control / savepoint execution
  - plain JDBC execution
  - metadata refresh follow-up on successful DDL / DCL
- 这样 transaction state、connection reuse 与 refresh visibility 都遵守相同顺序。

### 3. `closeSession()` 与执行路径共享同一 guard

- `closeSession(sessionId)` 需要先获取该 session guard。
- 在 guard 内完成：
  - transaction cleanup / rollback
  - session removal
- 等待中的执行请求在获取锁后必须重新确认 session 仍然有效。

### 4. 采用 blocking serialization，而不是引入新的 busy-fail contract

- 同一 `session` 的第二个请求等待前一个完成。
- 不新增 busy / conflict API branch。
- 不同 `session` 继续自然并发。

### 5. metadata / capability tools 不进入本轮 guard

- `MCPToolPayloadResolver` 中的 metadata / capability 分支继续直通。
- 只有 `execute_query` 进入 guarded execution path。
- 这样避免把本次 race fix 扩大成全工具热点。

## Branch Checklist

1. `same_session_begin_is_serialized`
   Planned verification: 同一 session 并发 `BEGIN` 不再产生双重建连或未跟踪连接
2. `same_session_query_and_commit_are_serialized`
   Planned verification: query 与 `COMMIT` 顺序稳定，避免共享连接并发访问
3. `same_session_query_and_close_are_serialized`
   Planned verification: `closeSession()` 等待 in-flight execution 完成后再 cleanup/remove
4. `cross_session_concurrency_is_preserved`
   Planned verification: 不同 session 并发请求不被全局锁阻塞
5. `transport_contract_remains_unchanged`
   Planned verification: HTTP / STDIO public contract 不新增参数、header 或错误码
6. `guard_logic_exists_only_once_in_core`
   Planned verification: transport 层不新增第二份 same-session locking implementation

## Implementation Strategy

1. 先改 `MCPSessionManager`，让 session registry 能承载 per-session context 和 guard。
2. 在 `MCPSQLExecutionFacade` 外层接入 guarded execution。
3. 把 `closeSession()` / `closeAllSessions()` 切到同一 guard 语义。
4. 用 core 并发测试锁定竞态修复，再用 transport regression 证明兼容性。

## Validation Strategy

- **Core session manager verification**
```bash
./mvnw -pl mcp/core -am -DskipITs -Dspotless.skip=true \
  -Dtest=MCPSessionManagerTest test \
  -Dsurefire.failIfNoSpecifiedTests=false
```

- **Core concurrency regression**
```bash
./mvnw -pl mcp/core -am -DskipITs -Dspotless.skip=true \
  -Dtest=MCPSQLExecutionFacadeConcurrencyTest test \
  -Dsurefire.failIfNoSpecifiedTests=false
```

- **Targeted transport regression**
```bash
./mvnw -pl mcp/bootstrap -am -DskipITs -Dspotless.skip=true \
  -Dtest=StreamableHttpRuntimeIntegrationTest test \
  -Dsurefire.failIfNoSpecifiedTests=false
```

- **Scoped style checks**
```bash
./mvnw -pl mcp/core,mcp/bootstrap -am -Pcheck -DskipITs -DskipTests \
  checkstyle:check spotless:check
```

## Rollout Notes

- 本轮修复的是 same-session race，不是 transport capability 扩展。
- 如果后续需要把 STDIO 升级为 multi-session，应另开 follow-up；本轮 guard 设计应天然可复用到多 session 场景。
- 如果未来证明 blocking serialization 对某个 transport 线程模型不够友好，
  可再单独演进到 per-session serial executor，但不应在本轮扩大范围。
