# Implementation Plan: ShardingSphere MCP Execute Query Schema Semantics

**Branch**: `no-branch-switch-requested` | **Date**: 2026-04-12 | **Spec**:
[spec.md](/Users/zhangliang/IdeaProjects/shardingsphere/specs/018-shardingsphere-mcp-execution-schema-semantics/spec.md)
**Input**: Feature specification from `/specs/018-shardingsphere-mcp-execution-schema-semantics/spec.md`

## Summary

本特性把 MCP 中 `schema` 的产品语义从“实现里有字段，但 contract 不完整”
收敛成一套可以被 Agent 程序化消费的正式规则：

- `database` 继续是唯一强边界
- `schema` 变成 optional namespace hint
- capability 新增 `schemaExecutionSemantics`
- no-native-schema 数据库把公共 `schema` 归一化为逻辑 `database` 名称
- 不新增 fail-fast 校验，而是通过 capability 和 contract 把边界说清楚

## Technical Context

**Language/Version**: Java 17 in the MCP subchain  
**Primary Dependencies**: repository-owned MCP core runtime, JDBC metadata, ShardingSphere SPI  
**Storage**: in-memory metadata catalog and database capability projections  
**Testing**: capability provider tests, metadata loader tests, JDBC statement executor tests, contract/document alignment checks  
**Target Platform**: ShardingSphere MCP standalone runtime on embedded Tomcat / STDIO  
**Project Type**: Java monorepo subproject under `mcp/core`, plus docs/specs follow-up  
**Constraints**: no branch switch; use scheme A; no new fail-fast schema validation; logical database name is the normalized schema name for no-native-schema databases; `database` remains the only strong execution boundary  
**Scale/Scope**: database capability model, metadata loader normalization, execute-query contract/docs, and adjacent tests

## Constitution Check

*GATE: Must pass before implementation starts. Re-check after design.*

- **Gate 1 - Simplicity**: PASS  
  本轮不靠增加新校验链路修补语义，而是补齐 capability 与 contract，
  符合 [CODE_OF_CONDUCT.md](/Users/zhangliang/IdeaProjects/shardingsphere/CODE_OF_CONDUCT.md#L8)
  到 [CODE_OF_CONDUCT.md](/Users/zhangliang/IdeaProjects/shardingsphere/CODE_OF_CONDUCT.md#L14)
  的 Readability / Cleanliness / Consistency / Simplicity / Abstraction 原则。
- **Gate 2 - Smallest safe change**: PASS  
  只收敛 schema 语义，不扩大到 auth、statement classification 或 transport redesign。
- **Gate 3 - Honest capability**: PASS  
  V1 只定义 `FIXED_TO_DATABASE` 与 `BEST_EFFORT`，
  不抢跑承诺尚未可验证的 strict behavior。
- **Gate 4 - Traceable contract impact**: PASS  
  已明确这是 capability + metadata + `execute_query` contract 的产品收敛，
  不是纯内部重构。
- **Gate 5 - Verification path exists**: PASS  
  capability、metadata loader、statement executor 和文档对齐都有 scoped verification 路径。

## Hard Constraint Checklist

- 使用方案 A，通过 capability 显式声明 execution-time schema semantics
- 对没有独立 schema 概念的数据库，公共 `schema` 名称使用逻辑 `database` 名称
- 不新增因 schema 不能严格生效而直接 fail-fast 的请求校验
- `database` 继续是唯一强执行边界
- `schema` 只定义为 optional namespace hint
- `defaultSchemaSemantics` 与 `schemaExecutionSemantics` 必须并存，不互相替代
- `execute_query` contract、tool descriptor、PRD、技术设计与代码实现必须对齐
- 只做 schema 语义相关最小修改，不夹带无关重构

## Project Structure

### Documentation (this feature)

```text
specs/018-shardingsphere-mcp-execution-schema-semantics/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── execute-query-contract.md
└── tasks.md
```

### Source Code (repository root)

```text
mcp/core/src/main/java/org/apache/shardingsphere/mcp/capability/database/SchemaExecutionSemantics.java
mcp/core/src/main/java/org/apache/shardingsphere/mcp/capability/database/SchemaSemantics.java
mcp/core/src/main/java/org/apache/shardingsphere/mcp/capability/database/MCPDatabaseCapabilityOption.java
mcp/core/src/main/java/org/apache/shardingsphere/mcp/capability/database/MCPDatabaseCapability.java
mcp/core/src/main/java/org/apache/shardingsphere/mcp/capability/database/MCPDatabaseCapabilityProvider.java
mcp/core/src/main/java/org/apache/shardingsphere/mcp/capability/database/dialect/*.java
mcp/core/src/main/java/org/apache/shardingsphere/mcp/resource/response/MCPDatabaseCapabilityResponse.java
mcp/core/src/main/java/org/apache/shardingsphere/mcp/metadata/jdbc/MCPJdbcMetadataLoader.java
mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/request/SQLExecutionRequest.java
mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/handler/execute/ExecuteSQLToolHandler.java
mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/handler/execute/MCPJdbcStatementExecutor.java
mcp/core/src/test/java/org/apache/shardingsphere/mcp/capability/database/MCPDatabaseCapabilityProviderTest.java
mcp/core/src/test/java/org/apache/shardingsphere/mcp/resource/response/MCPDatabaseCapabilityResponseTest.java
mcp/core/src/test/java/org/apache/shardingsphere/mcp/metadata/jdbc/MCPJdbcMetadataLoaderTest.java
mcp/core/src/test/java/org/apache/shardingsphere/mcp/tool/handler/execute/ExecuteSQLToolHandlerTest.java
mcp/core/src/test/java/org/apache/shardingsphere/mcp/tool/handler/execute/MCPJdbcStatementExecutorTest.java
docs/mcp/ShardingSphere-MCP-PRD.md
docs/mcp/ShardingSphere-MCP-Technical-Design.md
docs/mcp/ShardingSphere-MCP-Detailed-Design.md
specs/001-shardingsphere-mcp/spec.md
specs/001-shardingsphere-mcp/data-model.md
specs/001-shardingsphere-mcp/contracts/mcp-domain-contract.md
```

**Structure Decision**: 不新增模块；
schema 语义能力继续放在 `mcp/core` 的 capability 子域，
metadata 与 execute 的对齐工作分别在 loader 与 statement executor 内完成，
同时补齐 `001` 规格和产品文档。

## Design Decisions

### 1. `database` 不变，`schema` 明确降到 namespace hint

- `database` 仍然是唯一强边界
- `schema` 只作用于 database 内部命名空间意图
- `schema` 不再被隐含理解为第二路由键

### 2. capability 新增 `schemaExecutionSemantics`

- 新增独立 execution-time 维度
- V1 只定义 `FIXED_TO_DATABASE` 与 `BEST_EFFORT`
- 不引入 `STRICT`，避免过度承诺

### 3. no-native-schema 数据库统一用逻辑 `database` 名称暴露 `schema`

- metadata 层不再暴露空字符串 schema
- 统一的 `schema` 名称首先服务 MCP 公共 contract
- 该名字不自动成为底层 SQL qualifier 的产品承诺

### 4. `MCPJdbcStatementExecutor` 与 capability 语义对齐

- `FIXED_TO_DATABASE` 路径不能继续把 public schema label 当成独立 selector
- `BEST_EFFORT` 路径保留当前 “尝试应用 schema” 的思路，
  但产品承诺仍然是 best effort

### 5. 文档与契约要先于实现对齐

- `001` 的 `execute_query` contract 必须正式纳入 `schema`
- PRD、technical design、detailed design 与 tool descriptor 必须统一改口径

## Branch Checklist

1. `capability_publishes_execution_schema_semantics`  
   Planned verification: `MCPDatabaseCapabilityProviderTest` 与 capability response coverage
2. `no_native_schema_metadata_is_normalized_to_logical_database_name`  
   Planned verification: `MCPJdbcMetadataLoaderTest`
3. `execute_query_schema_is_documented_as_optional_namespace_hint`  
   Planned verification: `specs/001` contract、tool descriptor 与 docs diff review
4. `fixed_to_database_does_not_require_new_fail_fast_validation`  
   Planned verification: `MCPJdbcStatementExecutorTest`
5. `explicit_sql_qualification_remains_the_deterministic_selector`  
   Planned verification: contract/docs consistency review plus executor tests where applicable

## Implementation Strategy

1. 新增 `SchemaExecutionSemantics` 枚举，并扩展 capability option / capability model。
2. 为各数据库方言补齐 `schemaExecutionSemantics` 默认值；
   `DATABASE_AS_SCHEMA` 方言统一落到 `FIXED_TO_DATABASE`。
3. 扩展 capability response，把 `schemaExecutionSemantics` 暴露给调用方。
4. 调整 `MCPJdbcMetadataLoader`，
   让没有独立 schema 概念的数据库把公共 `schema` 归一化为逻辑 `database` 名称。
5. 调整 `ExecuteSQLToolHandler`、`SQLExecutionRequest` 和 `001` contract，
   正式把 `schema` 定义成 optional namespace hint。
6. 调整 `MCPJdbcStatementExecutor`，
   使其应用 schema 的行为与 capability 语义对齐，
   尤其避免在 `FIXED_TO_DATABASE` 路径中把 public label 当成独立 selector。
7. 更新 PRD、technical design、detailed design 与 Speckit 文档，
   统一对外话术。
8. 用 scoped tests 验证 capability、metadata loader 与 executor 行为一致。

## Validation Strategy

- **Capability verification**
```bash
./mvnw -pl mcp/core -am -DskipITs -Dspotless.skip=true \
  -Dtest=MCPDatabaseCapabilityProviderTest test \
  -Dsurefire.failIfNoSpecifiedTests=false
```

- **Metadata normalization verification**
```bash
./mvnw -pl mcp/core -am -DskipITs -Dspotless.skip=true \
  -Dtest=MCPJdbcMetadataLoaderTest test \
  -Dsurefire.failIfNoSpecifiedTests=false
```

- **Execute-query schema semantics verification**
```bash
./mvnw -pl mcp/core -am -DskipITs -Dspotless.skip=true \
  -Dtest=MCPJdbcStatementExecutorTest,MCPSQLExecutionFacadeTest test \
  -Dsurefire.failIfNoSpecifiedTests=false
```

- **Scoped style checks**
```bash
./mvnw -pl mcp/core -am -Pcheck -DskipITs -DskipTests \
  checkstyle:check spotless:check
```

## Rollout Notes

- 这不是把 `schema` 能力做强，而是把当前真实能力诚实说清楚。
- 如果后续要引入 strict schema switching，
  应开新的 follow-up feature，而不是在本轮先把 capability 写超前。
- 由于用户已明确不接受新增 fail-fast 校验，
  本轮实现必须始终以 “契约健全优先于校验升级” 为原则。
