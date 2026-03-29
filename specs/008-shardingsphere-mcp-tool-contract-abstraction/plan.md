# Implementation Plan: ShardingSphere MCP Tool Contract Abstraction Between Core and Bootstrap

**Branch**: `008-shardingsphere-mcp-tool-contract-abstraction` | **Date**: 2026-03-29 | **Spec**:
[spec.md](/Users/zhangliang/IdeaProjects/shardingsphere/specs/008-shardingsphere-mcp-tool-contract-abstraction/spec.md)
**Input**: Feature specification from `/specs/008-shardingsphere-mcp-tool-contract-abstraction/spec.md`

## Summary

本特性把 MCP tool 的静态契约收口到 `mcp/core`：
用显式 descriptor/input-definition 模型统一承载名称、标题、描述、dispatch 类型与输入参数定义，
让 `mcp/bootstrap` 只负责把这些抽象对象适配成 MCP Java SDK 所需的 schema 和 tool spec。

这轮不改 public tool contract，不改 `tools/call` runtime 执行路径，
也不把 MCP SDK 依赖带进 `mcp/core`。

## Technical Context

**Language/Version**: Java 17 in the MCP subchain  
**Primary Dependencies**: repository-owned MCP core contracts, MCP Java SDK 1.1.0 in bootstrap only  
**Storage**: in-memory tool registry and request normalization rules  
**Testing**: core catalog tests, bootstrap adapter tests, public tool-list compatibility checks, scoped Maven verification  
**Target Platform**: ShardingSphere MCP standalone runtime on embedded Tomcat / STDIO  
**Project Type**: Java monorepo subproject under `mcp/core` and `mcp/bootstrap`  
**Constraints**: no MCP SDK types in `mcp/core`; no public tool surface drift; smallest safe change;
keep handler/session binding in bootstrap; explicit coverage for all supported tools  
**Scale/Scope**: `mcp/core` tool contract model, `MCPToolCatalog`, bootstrap tool schema/spec adapter path

## Constitution Check

*GATE: Must pass before implementation starts. Re-check after design.*

- **Gate 1 - Smallest safe change**: PASS  
  本轮只重构 static tool contract 分层，不改 public MCP tool surface 和 runtime execution path。
- **Gate 2 - Readability and simplicity**: PASS  
  用单一 descriptor 模型替代双份事实来源，减少跨模块追踪与隐式 fallback。
- **Gate 3 - Clear abstraction levels**: PASS  
  core 拥有 domain contract，bootstrap 拥有 SDK adapter，exchange/session 绑定继续留在 transport 层。
- **Gate 4 - Verification path exists**: PASS  
  可以通过 core descriptor 测试、bootstrap adapter 测试与现有 tool-list / tool-call 回归做 scoped 验证。
- **Gate 5 - Compatibility risk is bounded**: PASS  
  `001` 的 public tool contract 保持不变，`MCPToolCallHandler` 不纳入本轮迁移。

## Project Structure

### Documentation (this feature)

```text
specs/008-shardingsphere-mcp-tool-contract-abstraction/
├── spec.md
├── research.md
├── data-model.md
├── plan.md
└── tasks.md
```

### Source Code (repository root)

```text
mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/MCPToolCatalog.java
mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/MCPToolDescriptor.java
mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/MCPToolDispatchKind.java
mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/MCPToolInputDefinition.java
mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/MCPToolFieldDefinition.java
mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/MCPToolValueDefinition.java
mcp/core/src/test/java/org/apache/shardingsphere/mcp/tool/MCPToolCatalogTest.java
mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/tool/MCPToolInputSchemaFactory.java
mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/tool/MCPToolSpecificationFactory.java
mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/tool/MCPToolInputSchemaFactoryTest.java
mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/stdio/StdioTransportIntegrationTest.java
```

**Structure Decision**: 不新增模块；只在 `mcp/core` 新增最小 descriptor 模型，
并让 `mcp/bootstrap` 退化为纯 SDK adapter。

## Design Decisions

### 1. `MCPToolCatalog` 继续作为 registry，但升级为 contract source of truth

- 保留当前 registry 与 request normalization 入口。
- 为每个 tool 显式挂接 `MCPToolDescriptor`。
- 对外暴露 `getToolDescriptors()` 或等价查询入口，供 bootstrap 直接消费 descriptor。

### 2. core 使用最小充分 descriptor 模型

- `MCPToolDescriptor`
- `MCPToolDispatchKind`
- `MCPToolInputDefinition`
- `MCPToolFieldDefinition`
- `MCPToolValueDefinition`

模型目标是表达当前 MCP tool 输入需要，而不是引入完整通用 JSON Schema DSL。

### 3. `MCPToolInputSchemaFactory` 只做 adapter

- 入参改为 `MCPToolInputDefinition` 或等价 core 抽象对象。
- 内部做递归转换，生成 SDK root object schema、properties、required 列表。
- 不再知道 `list_tables`、`search_metadata`、`execute_query` 等业务名字。

### 4. `MCPToolSpecificationFactory` 基于 descriptor 构建 tool spec

- 不再通过 `toolName` 分别去 catalog 取 title、去 schema factory 取 schema。
- 改为直接遍历 descriptor：
  - `name`
  - `title`
  - `description`
  - `inputDefinition`
- `callHandler` 绑定方式保持现状。

### 5. 显式补齐零参数与可选参数 tool

- `list_databases()` 需要显式空字段输入定义。
- `get_capabilities(database?)` 需要显式 optional `database` 字段。
- 共享分页字段的 metadata tools 可在 core 复用 builder/helper，
  但复用也必须发生在 core，而不是 bootstrap。

### 6. `MCPToolCallHandler` 本轮不迁移

- 它继续保留在 bootstrap。
- `McpSyncServerExchange`、`CallToolResult`、payload 组装和 session binding 维持现状。
- 如后续仍想继续抽象，可在本轮 contract 收口完成后另开 follow-up。

## Branch Checklist

1. `zero_argument_tool_descriptor_is_explicit`
   Planned verification: `list_databases()` 拥有显式 descriptor 与空字段输入定义
2. `optional_argument_tool_descriptor_is_explicit`
   Planned verification: `get_capabilities(database?)` 在 core 中有 optional `database`
3. `tool_name_specific_schema_branch_removed_from_bootstrap`
   Planned verification: `MCPToolInputSchemaFactory` 不再按 `toolName` 分支
4. `core_remains_sdk_free`
   Planned verification: `mcp/core/pom.xml` 不新增 MCP SDK 依赖，core 代码不引用 `io.modelcontextprotocol.*`
5. `request_normalization_stays_traceable`
   Planned verification: metadata / execution request normalization 继续由 core 承接
6. `public_tool_listing_behavior_preserved`
   Planned verification: `tools/list` 仍暴露相同 tool 集合与 title，并显式体现 core descriptor 输入定义

## Implementation Strategy

1. 先在 `mcp/core` 定义最小 descriptor 模型，避免 bootstrap 先行倒逼 core 设计。
2. 把 `MCPToolCatalog` 的 tool 定义迁移到 descriptor 模型上，同时保留现有 request normalization。
3. 让 `MCPToolSpecificationFactory` 直接消费 descriptor。
4. 把 `MCPToolInputSchemaFactory` 改成纯 adapter，并移除 tool-name-specific schema 分支。
5. 用 core 与 bootstrap 两层测试分别锁定 descriptor coverage 和 SDK mapping。
6. 最后用一条 public listing 回归确认 `tools/list` 对外兼容。

## Validation Strategy

- **Core descriptor coverage**
```bash
./mvnw -pl mcp/core -am -DskipITs -Dspotless.skip=true \
  -Dtest=MCPToolCatalogTest test \
  -Dsurefire.failIfNoSpecifiedTests=false
```
- **Bootstrap schema adapter coverage**
```bash
./mvnw -pl mcp/bootstrap -am -DskipITs -Dspotless.skip=true \
  -Dtest=MCPToolInputSchemaFactoryTest test \
  -Dsurefire.failIfNoSpecifiedTests=false
```
- **Public tool listing compatibility**
```bash
./mvnw -pl mcp/bootstrap -am -DskipITs -Dspotless.skip=true \
  -Dtest=StdioTransportIntegrationTest test \
  -Dsurefire.failIfNoSpecifiedTests=false
```
- **Scoped style checks**
```bash
./mvnw -pl mcp/core,mcp/bootstrap -am -Pcheck -DskipITs -DskipTests \
  checkstyle:check spotless:check
```

## Rollout Notes

- 这是 `001` public contract 内部的一次分层收敛，不是新的 protocol 设计。
- 如果 descriptor 模型已经能表达新增 tool，就不应再往 bootstrap 加 schema 分支。
- 如果未来需要进一步抽象 call handler，应单独评估 session / payload / error mapping 边界，
  不与本轮 contract 收口混做。
