# Implementation Plan: ShardingSphere MCP Native Metadata Shape Without MetadataObject Flattening

**Branch**: `no-branch-switch-requested` | **Date**: 2026-04-05 | **Spec**:
[spec.md](/Users/zhangliang/IdeaProjects/shardingsphere/specs/013-shardingsphere-mcp-native-metadata-shape/spec.md)
**Input**: Feature specification from `/specs/013-shardingsphere-mcp-native-metadata-shape/spec.md`

## Summary

本特性把 MCP metadata 主链路从 “flat `MetadataObject` source model”
收敛为 “native metadata hierarchy + typed payload presentation”。

这轮设计明确接受一个 metadata contract 变化：

- resource URI 与 metadata tool name 保持不变
- capability 与 `execute_query` surface 保持不变
- metadata payload 从 generic `MetadataObject` 改为贴近数据库原貌的 typed objects

不会切换分支，也不会改动 metadata 以外的无关代码。

## Technical Context

**Language/Version**: Java 17 in the MCP subchain  
**Primary Dependencies**: repository-owned MCP core runtime, JDBC `DatabaseMetaData`, ShardingSphere SPI  
**Storage**: in-memory runtime metadata snapshots  
**Testing**: loader/query unit tests, metadata resource tests, metadata tool tests, targeted bootstrap regressions  
**Target Platform**: ShardingSphere MCP standalone runtime on embedded Tomcat / STDIO  
**Project Type**: Java monorepo subproject under `mcp/core` and `mcp/bootstrap`  
**Constraints**: no branch switch; metadata-only scope; no unrelated cleanup; URI/tool names stable;
metadata payload shape intentionally changes; index unsupported semantics stay stable  
**Scale/Scope**: `mcp/core` metadata model, loader, query, resource, tool path and adjacent tests

## Constitution Check

*GATE: Must pass before implementation starts. Re-check after design.*

- **Gate 1 - Smallest safe change**: PASS  
  只收敛 metadata 模型、查询与展现，不扩大到 capability、session、execute 或 transport 重写。
- **Gate 2 - Readability and simplicity**: PASS  
  用真实层级替代 generic flat source model，删除 `parentObjectType` / `parentObjectName`
  这种为抽象补丁式存在的关系表达。
- **Gate 3 - Traceable contract impact**: PASS  
  已明确写出 metadata payload shape 会变化，而 URI、tool name、capability 与 execute surface 不变。
- **Gate 4 - Verification path exists**: PASS  
  loader、query、resource、tool、bootstrap 都有 scoped verification 路径。
- **Gate 5 - Risk is explicit**: PASS  
  最大风险是 metadata payload breaking change，但影响面被限定在 metadata surface，且已显式记录。

## Hard Constraint Checklist

- 不切换分支，只在当前工作树完成 Speckit 设计和后续实现
- 本轮仅允许修改 metadata 相关生产代码、测试代码和 Speckit 文档
- 不允许顺手做无关重构或命名整理
- 必须保持现有 metadata resource URI 与 metadata tool name 不变
- 必须保持 capability 与 `execute_query` surface 不变
- 必须显式更新所有依赖 `MetadataObject` payload 的契约与测试
- 必须使用 scoped tests / style checks 验证 touched modules

## Project Structure

### Documentation (this feature)

```text
specs/013-shardingsphere-mcp-native-metadata-shape/
├── plan.md
└── spec.md
```

### Source Code (repository root)

```text
mcp/core/src/main/java/org/apache/shardingsphere/mcp/metadata/model/*.java
mcp/core/src/main/java/org/apache/shardingsphere/mcp/metadata/query/*.java
mcp/core/src/main/java/org/apache/shardingsphere/mcp/metadata/jdbc/MCPJdbcMetadataLoader.java
mcp/core/src/main/java/org/apache/shardingsphere/mcp/resource/response/MCPMetadataResponse.java
mcp/core/src/main/java/org/apache/shardingsphere/mcp/resource/handler/metadata/*.java
mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/MetadataToolDispatcher.java
mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/MCPToolPayloadResolver.java
mcp/core/src/test/java/org/apache/shardingsphere/mcp/metadata/**/*.java
mcp/core/src/test/java/org/apache/shardingsphere/mcp/resource/**/*.java
mcp/core/src/test/java/org/apache/shardingsphere/mcp/tool/**/*.java
mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/**/*.java
```

**Structure Decision**: 不新增模块；
native metadata hierarchy 继续位于 `mcp/core`，
resource 与 tool 共用同一套 typed metadata model，bootstrap 只更新受影响测试。

## Design Decisions

### 1. Native hierarchy 取代 flat `MetadataObject` 作为源模型

- 新增或重写 `DatabaseMetadata`、`SchemaMetadata`、`TableMetadata`、`ViewMetadata`、
  `ColumnMetadata`、`IndexMetadata`
- `DatabaseMetadataSnapshot` 与 `DatabaseMetadataSnapshots` 直接持有 native hierarchy
- `MetadataObject` 要么删除，要么只保留在明确受限的兼容层

### 2. Query 按路径导航，不再在全局列表上做类型过滤

- `MetadataQueryService` 不再围绕 `List<MetadataObject>` 过滤实现
- database / schema / table / view / column / index 查询改为沿层级导航
- `MetadataQueryCondition` 需要被收敛、降级或重写为只服务少量 list/search 场景

### 3. Resource 与 tool 按请求对象类型返回 typed payload

- list resource / list tool 可以继续保留 `items` envelope
- 但 `items` 中的元素必须是所请求对象的 typed metadata
- detail resource / describe tool 直接返回 detail object，
  不再依赖 “主对象 + 子对象平铺列表” 的拼装方式

### 4. `search_metadata` 使用 purpose-built summary，而不是复用源模型

- 搜索跨多 object type 时允许使用 `MetadataSearchHit` 或等价对象
- summary 中至少需要携带 object type、所属路径和展示所需字段
- 该 summary 只能服务搜索结果展现，不能反过来变成 snapshot / query 的 canonical model

### 5. Native shape 优先于一次性穷举所有 vendor 字段

- 本轮重点是层级和对象边界，而不是一次性透传所有 JDBC metadata 列
- typed nodes 可以预留扩展属性位，但不强行把每种数据库差异抽成一套 universal flat fields

### 6. Metadata payload change 是显式契约变化，不伪装成内部重构

- `spec.md`、相关 contract、tests 和 integration assertions 都要同步更新
- 变更边界限定在 metadata surface，避免 capability / execute / session 跟着漂移

## Branch Checklist

1. `loader_builds_native_metadata_hierarchy`
   Planned verification: `MCPJdbcMetadataLoaderTest` 验证 database / schema / table / view /
   column / index 的 ownership 层级直接存在于 snapshot 中
2. `query_service_navigates_tree_without_flat_scan`
   Planned verification: `MetadataQueryServiceTest` 断言 list / detail 查询通过层级导航完成
3. `resource_handlers_return_typed_metadata_payloads`
   Planned verification: `ResourceHandlerTest` 与 `MCPResourceControllerTest`
   改为断言 typed payload，而不是 `MetadataObject`
4. `metadata_tools_return_typed_or_detail_results`
   Planned verification: `MetadataToolDispatcherTest` 与 `MCPToolPayloadResolverTest`
   覆盖 list / describe / search 三类输出
5. `search_metadata_uses_summary_projection_only`
   Planned verification: 跨 object type 搜索返回 summary result，但 snapshot / query 主路径不依赖它
6. `unsupported_index_and_schema_edge_cases_remain_stable`
   Planned verification: unsupported index 回归与空 schema 语义 dedicated tests 继续通过

## Implementation Strategy

1. 先在 `mcp/core/metadata/model` 中引入 native metadata hierarchy，并改造 snapshot 容器。
2. 重写 `MCPJdbcMetadataLoader`，让它直接产出 hierarchy，而不是先生产 `MetadataObject`。
3. 重构 `MetadataQueryService`，把主路径从 flat filter 切换到层级导航。
4. 重新定义 metadata response 与 tool dispatch 的 payload shaping 规则。
5. 逐个更新 metadata resource handlers 与 metadata tools，使其输出 typed payload。
6. 更新 `specs/001-shardingsphere-mcp` 相关 contract 与所有依赖 `items -> MetadataObject`
   的 core / bootstrap tests。
7. 最后清理 `MetadataObject` 主路径引用，并用 grep 验证没有遗漏。

## Validation Strategy

- **Loader and query verification**
```bash
./mvnw -pl mcp/core -am -DskipITs -Dspotless.skip=true \
  -Dtest=MCPJdbcMetadataLoaderTest,MetadataQueryServiceTest test \
  -Dsurefire.failIfNoSpecifiedTests=false
```

- **Metadata resource verification**
```bash
./mvnw -pl mcp/core -am -DskipITs -Dspotless.skip=true \
  -Dtest=MCPResourceControllerTest,ResourceHandlerTest test \
  -Dsurefire.failIfNoSpecifiedTests=false
```

- **Metadata tool verification**
```bash
./mvnw -pl mcp/core -am -DskipITs -Dspotless.skip=true \
  -Dtest=MetadataToolDispatcherTest,MCPToolPayloadResolverTest test \
  -Dsurefire.failIfNoSpecifiedTests=false
```

- **Bootstrap regression**
```bash
./mvnw -pl mcp/bootstrap -am -DskipITs -Dspotless.skip=true \
  -Dtest=MCPToolCallHandlerTest,StdioTransportIntegrationTest test \
  -Dsurefire.failIfNoSpecifiedTests=false
```

- **Scoped style checks**
```bash
./mvnw -pl mcp/core,mcp/bootstrap -am -Pcheck -DskipITs -DskipTests \
  checkstyle:check spotless:check
```

- **Static grep verification**
```bash
rg -n "MetadataObject|parentObjectType|parentObjectName" \
  /Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java \
  /Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/main/java
```

## Rollout Notes

- 这轮不是把 metadata 搞成一个更大的抽象层，而是反过来把不必要的抽象拆掉。
- 如果后续要把 vendor-specific metadata 列进一步丰富，应建立在 native hierarchy 之上，
  而不是重新引入一个更通用的 flat object。
- 如果最终评审认为 metadata payload breaking change 需要迁移窗口，
  应在 follow-up 里明确兼容策略；本 spec 不假装这是零影响内部重构。
