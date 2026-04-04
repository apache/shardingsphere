# Implementation Plan: ShardingSphere MCP Error Centralization and Protocol Error Conversion

**Branch**: `no-branch-switch-requested` | **Date**: 2026-04-04 | **Spec**:
[spec.md](/Users/zhangliang/IdeaProjects/shardingsphere/specs/011-shardingsphere-mcp-error-centralization/spec.md)
**Input**: Feature specification from `/specs/011-shardingsphere-mcp-error-centralization/spec.md`

## Summary

本特性把 MCP 内部错误表达统一收敛为 “内部抛异常，协议入口集中转 `MCPErrorResponse`”。

本轮只改错误处理主链路：

- 新增统一异常体系和集中 converter
- metadata / tool dispatch / execute / resource handler 失败时直接抛异常
- tool / resource 总入口统一 catch 并转 `MCPErrorResponse`
- `execute_query` 失败路径改为统一错误响应，不再保留 `ExecuteQueryResponse.error(...)`

不会切换分支，不会改动无关代码。

## Technical Context

**Language/Version**: Java 17 in the MCP subchain  
**Primary Dependencies**: repository-owned MCP core runtime, ShardingSphere SPI, MCP SDK integration already wrapped in bootstrap  
**Storage**: in-memory runtime metadata snapshots, capability builder, JDBC runtime state  
**Testing**: core unit tests for exception throwing and conversion, tool/resource regression, targeted bootstrap tool-call regression  
**Target Platform**: ShardingSphere MCP standalone runtime on embedded Tomcat / STDIO  
**Project Type**: Java monorepo subproject under `mcp/core` and `mcp/bootstrap`  
**Constraints**: no branch switch; no unrelated cleanup; no public error payload drift; no new error codes; only error-handling-related files may change  
**Scale/Scope**: `mcp/core` error model and protocol entrypoints, plus adjacent tests and one bootstrap handler regression

## Constitution Check

*GATE: Must pass before implementation starts. Re-check after design.*

- **Gate 1 - Smallest safe change**: PASS  
  改动只限 MCP 错误处理主链路，不扩大到 transport、URI dispatch、session 或 capability 设计。
- **Gate 2 - Explicit governance and security**: PASS  
  错误码、message 稳定性和 fallback `unavailable` 规则都显式写入规格和 contract。
- **Gate 3 - Testable delivery**: PASS  
  metadata/tool/resource/execute 都有 dedicated verification path。
- **Gate 4 - Traceable contracts**: PASS  
  error payload、error code、入口边界和异常族在 `spec.md`、`plan.md`、contract、tasks 中可追踪。
- **Gate 5 - Quality gates**: PASS  
  已给出 `mcp/core` 与 `mcp/bootstrap` 的 scoped tests / style gate 命令。

## Project Structure

### Documentation (this feature)

```text
specs/011-shardingsphere-mcp-error-centralization/
├── checklists/
│   └── requirements.md
├── contracts/
│   └── mcp-error-contract.md
├── data-model.md
├── plan.md
├── research.md
├── spec.md
└── tasks.md
```

### Source Code (repository root)

```text
mcp/core/src/main/java/org/apache/shardingsphere/mcp/protocol/exception/*.java
mcp/core/src/main/java/org/apache/shardingsphere/mcp/protocol/response/MCPProtocolErrorConverter.java
mcp/core/src/main/java/org/apache/shardingsphere/mcp/metadata/query/MetadataQueryResult.java
mcp/core/src/main/java/org/apache/shardingsphere/mcp/metadata/query/MetadataQueryService.java
mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/MetadataToolDispatcher.java
mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/ToolDispatchResult.java
mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/MCPToolPayloadResolver.java
mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/MCPToolPayloadResult.java
mcp/core/src/main/java/org/apache/shardingsphere/mcp/resource/MCPResourceController.java
mcp/core/src/main/java/org/apache/shardingsphere/mcp/resource/response/MCPResourceResponseFactory.java
mcp/core/src/main/java/org/apache/shardingsphere/mcp/resource/handler/capability/DatabaseCapabilitiesHandler.java
mcp/core/src/main/java/org/apache/shardingsphere/mcp/resource/handler/metadata/AbstractMetadataResourceHandler.java
mcp/core/src/main/java/org/apache/shardingsphere/mcp/execute/MCPJdbcStatementExecutor.java
mcp/core/src/main/java/org/apache/shardingsphere/mcp/execute/MCPJdbcTransactionStatementExecutor.java
mcp/core/src/main/java/org/apache/shardingsphere/mcp/execute/MCPSQLExecutionFacade.java
mcp/core/src/main/java/org/apache/shardingsphere/mcp/protocol/response/ExecuteQueryResponse.java
mcp/core/src/main/java/org/apache/shardingsphere/mcp/session/MCPSessionNotExistedException.java
mcp/core/src/test/java/org/apache/shardingsphere/mcp/**/*
mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/tool/MCPToolCallHandlerTest.java
```

**Structure Decision**: 不新增模块；
集中 converter、异常类和最小必要的 success-only 结果收敛都放在 `mcp/core`，
bootstrap 只改与 tool-call error rendering 相关的测试。

## Design Decisions

### 1. 协议错误转换只保留一个中心

- 新增 `MCPProtocolErrorConverter`
- 它是生产代码中唯一允许构造 `MCPErrorResponse` 的运行时组件
- tool 与 resource 入口共用它

### 2. 内部失败结果对象全面退出主路径

- `MetadataQueryResult.error(...)` 退出失败主路径
- `ToolDispatchResult.error(...)` 退出失败主路径
- `ExecuteQueryResponse.error(...)` 删除
- `MCPToolPayloadResult.error(...)` 不再自己生成 `MCPErrorResponse`

### 3. `execute_query` 成功与失败协议彻底分离

- 成功：继续返回 `ExecuteQueryResponse.toPayload()`
- 失败：统一由 tool 入口转成 `MCPErrorResponse`

这样不再有 “success envelope + nested error” 的混合模型。

### 4. 只在固定边界 catch

- `MCPToolPayloadResolver.resolve(...)`
- `MCPResourceController.handle(...)`

其余生产代码不主动把异常再转成 payload。

### 5. 稳定异常体系优先，legacy fallback 只做兜底

- 首选新异常族和叶子异常
- 已存在的 `MCPSessionNotExistedException` 等固定场景可被改造到新体系
- 裸 `IllegalArgumentException` / `IllegalStateException` 只保留过渡兜底映射，不作为新增主路径

### 6. `search_metadata` 保持 skip 语义

- 对 object type 的支持性先判定，再决定是否查询
- 不通过抛 `unsupported` 再在本地吞掉来实现兼容行为

## Branch Checklist

1. `metadata_and_tool_layers_throw_typed_exceptions`
   Planned verification: `MetadataQueryServiceTest` 与 `MetadataToolDispatcherTest` 改为断言异常类型和 message
2. `resource_entry_converts_every_failure_through_one_converter`
   Planned verification: `MCPResourceControllerTest` 与 `ResourceHandlerTest` 继续断言 payload 兼容
3. `execute_query_failure_uses_mcp_error_response_only`
   Planned verification: `MCPToolPayloadResolverTest`、`MCPSQLExecutionFacadeTest`、
   `MCPJdbcStatementExecutorTest`、`MCPJdbcTransactionStatementExecutorTest`
4. `distributed_error_factories_are_removed`
   Planned verification: production grep 不再命中失败结果工厂调用和分散 `new MCPErrorResponse(...)`
5. `search_metadata_skip_behavior_is_preserved`
   Planned verification: metadata tool regression继续返回跨数据库搜索结果，不因 unsupported index 整体失败

## Implementation Strategy

1. 先新增异常基类、family exceptions、必要的 leaf exceptions 和 `MCPProtocolErrorConverter`。
2. 改 tool / resource 总入口，让它们成为唯一 catch-and-convert 边界。
3. 改 metadata query 与 metadata tool dispatch，让失败直接抛异常。
4. 改 execute 链，让 SQL / transaction 失败抛异常，删除 `ExecuteQueryResponse.error(...)`。
5. 清理 success-only 结果模型上的失败接口。
6. 跑 scoped tests 和 style gate，最后用 `rg` 验证旧失败工厂和分散 `MCPErrorResponse` 构造已退出主路径。

## Validation Strategy

- **Metadata and tool error-path verification**
```bash
./mvnw -pl mcp/core -am -DskipITs -Dspotless.skip=true \
  -Dtest=MetadataQueryServiceTest,MetadataToolDispatcherTest,MCPToolPayloadResolverTest test \
  -Dsurefire.failIfNoSpecifiedTests=false
```

- **Execute-path verification**
```bash
./mvnw -pl mcp/core -am -DskipITs -Dspotless.skip=true \
  -Dtest=MCPSQLExecutionFacadeTest,MCPSQLExecutionFacadeConcurrencyTest,MCPJdbcStatementExecutorTest,MCPJdbcTransactionStatementExecutorTest test \
  -Dsurefire.failIfNoSpecifiedTests=false
```

- **Resource-path verification**
```bash
./mvnw -pl mcp/core -am -DskipITs -Dspotless.skip=true \
  -Dtest=MCPResourceControllerTest,ResourceHandlerTest,MCPResponseTest test \
  -Dsurefire.failIfNoSpecifiedTests=false
```

- **Bootstrap regression**
```bash
./mvnw -pl mcp/bootstrap -am -DskipITs -Dspotless.skip=true \
  -Dtest=MCPToolCallHandlerTest test \
  -Dsurefire.failIfNoSpecifiedTests=false
```

- **Scoped style checks**
```bash
./mvnw -pl mcp/core,mcp/bootstrap -am -Pcheck -DskipITs -DskipTests \
  checkstyle:check spotless:check
```

- **Static grep verification**
```bash
rg -n "MetadataQueryResult\\.error\\(|ToolDispatchResult\\.error\\(|ExecuteQueryResponse\\.error\\(|new MCPErrorResponse\\(" \
  /Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java \
  /Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/main/java
```

## Rollout Notes

- 这轮只修协议错误收敛，不做错误 message 国际化或错误目录重组。
- 如果后续需要把更多 MCP 内部异常统一成同一体系，应另开 follow-up，
  不在本轮继续扩大到无关路径。
- 若某个老异常暂时无法改造成新体系，必须在 converter 中留下显式映射，而不是靠默认 fallback。
