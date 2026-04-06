# Implementation Plan: ShardingSphere MCP Direct Typed Metadata Model Without MetadataObject

**Branch**: `no-branch-switch-requested` | **Date**: 2026-04-05 | **Spec**:
[spec.md](/Users/zhangliang/IdeaProjects/shardingsphere/specs/013-shardingsphere-mcp-native-metadata-shape/spec.md)
**Input**: Feature specification from `/specs/013-shardingsphere-mcp-native-metadata-shape/spec.md`

## Summary

本特性把 MCP metadata 主链路从
“flat `MetadataObject` + rebuild hierarchy”
进一步收敛为
“`MCPDatabaseMetadata` typed tree 直接作为 source of truth”。

这轮设计明确接受一个 metadata contract 变化：

- resource URI 与 metadata tool name 保持不变
- capability 与 `execute_query` surface 保持不变
- metadata payload 从 flat object 彻底切换为具体 typed objects

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
  仍然只收敛 metadata 模型、查询与展现，不扩大到 capability、session、execute 或 transport 重写。
- **Gate 2 - Readability and simplicity**: PASS  
  让 `MCPDatabaseMetadata` 直接成为源模型，删除 `MetadataObject` 这种多余中间层，符合
  [CODE_OF_CONDUCT.md](/Users/zhangliang/IdeaProjects/shardingsphere/CODE_OF_CONDUCT.md#L8) 到
  [CODE_OF_CONDUCT.md](/Users/zhangliang/IdeaProjects/shardingsphere/CODE_OF_CONDUCT.md#L12) 的
  readability / cleanliness / simplicity / abstraction 原则。
- **Gate 3 - Traceable contract impact**: PASS  
  已明确写出 metadata payload shape 会变化，而 URI、tool name、capability 与 execute surface 不变。
- **Gate 4 - Verification path exists**: PASS  
  loader、snapshot、query、resource、tool、bootstrap 都有 scoped verification 路径。
- **Gate 5 - Risk is explicit**: PASS  
  最大风险是 metadata payload breaking change 与 loader build 路径调整，但影响面被限定在 metadata surface。

## Hard Constraint Checklist

- 不切换分支，只在当前工作树完成 Speckit 设计和后续实现
- 本轮仅允许修改 metadata 相关生产代码、测试代码和 Speckit 文档
- 不允许顺手做无关重构或命名整理
- 必须保持现有 metadata resource URI 与 metadata tool name 不变
- 必须保持 capability 与 `execute_query` surface 不变
- 必须彻底删除 `MetadataObject`，不能只把它保留成兼容壳
- 必须让 `DatabaseMetadataSnapshot` 直接持有完整 `MCPDatabaseMetadata`
- 必须删除 `DatabaseMetadataSnapshots` 中的 `metadataObjects` 聚合缓存
- 必须用 scoped tests / style checks 验证 touched modules

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
typed metadata hierarchy 继续位于 `mcp/core`；
resource 与 tool 共用同一套 `MCP*Metadata` model；
bootstrap 只更新受影响测试。

## Design Decisions

### 1. `MCPDatabaseMetadata` 成为唯一 metadata source of truth

- `DatabaseMetadataSnapshot` 不再拆分保存 `databaseType`、`databaseVersion`、`schemas`
  与 `List<MetadataObject>`
- snapshot 直接持有一个完整 `MCPDatabaseMetadata`
- `DatabaseMetadataSnapshots` 只负责 logical database 的 snapshot/cached typed tree 管理

### 2. `MetadataObject` 彻底删除，而不是降级保留

- `MetadataObject` 不再作为 loader 中间形态
- `MetadataObject` 不再作为 snapshot 兼容字段
- `MetadataObject` 不再出现在 resource / tool / query / tests
- `MetadataObjectType` 继续保留为 object category enum

### 3. loader 直接构建 typed tree

- `MCPJdbcMetadataLoader` 直接积累 schema/table/view/column/index 具体节点
- table columns/indexes 直接归属到 `MCPTableMetadata`
- view columns 直接归属到 `MCPViewMetadata`
- schema 去重和 index 去重逻辑原样保留，但载体从 flat record 改为 typed accumulators

### 4. `MetadataHierarchyBuilder` 退出主路径

- 理想状态是删除该类
- 如果实现阶段临时保留，则它只能处理 typed node 组装，
  不能再承担 `MetadataObject -> hierarchy` rebuild 职责

### 5. query/resource/tool 与 snapshot 同构

- `MetadataQueryService` 直接围绕 `MCPDatabaseMetadata` 导航
- resource handlers 直接返回 `MCPDatabaseMetadata` / `MCPSchemaMetadata` /
  `MCPTableMetadata` / `MCPViewMetadata` / `MCPColumnMetadata` / `MCPIndexMetadata`
- `describe_table` / `describe_view` 直接返回 detail object
- `search_metadata` 允许使用 `MetadataSearchHit` 作为投影结果，但不是 canonical model

### 6. 兼容边界明确收敛

- metadata payload breaking change 作为显式契约变化记录
- capability surface、tool names、resource URI、unsupported index 语义保持稳定
- 不把这轮扩大成 capability/session/transport 的模型统一行动

## Branch Checklist

1. `snapshot_holds_complete_mcp_database_metadata`
   Planned verification: `DatabaseMetadataSnapshotsTest` 验证 snapshot 不再暴露 `metadataObjects`
   且直接持有完整 `MCPDatabaseMetadata`
2. `loader_builds_typed_tree_directly`
   Planned verification: `MCPJdbcMetadataLoaderTest` 验证 schema/table/view/column/index
   的 ownership 层级直接在 loader 产物中成立
3. `metadata_object_removed_from_main_codepath`
   Planned verification: `rg -n "\\bMetadataObject\\b" mcp/core mcp/bootstrap`
   结果只允许为 `0`
4. `query_service_navigates_typed_snapshot`
   Planned verification: `MetadataQueryServiceTest` 断言 list / detail 查询通过 typed tree 导航完成
5. `resource_and_tool_payloads_use_concrete_objects`
   Planned verification: `ResourceHandlerTest`、`MCPResourceControllerTest`、
   `MetadataToolDispatcherTest`、`MCPToolPayloadResolverTest`
6. `unsupported_index_and_schema_edge_cases_remain_stable`
   Planned verification: unsupported index 回归与空 schema 语义 dedicated tests 继续通过

## Implementation Strategy

1. 先重定义 `DatabaseMetadataSnapshot`，
   让它只持有 `MCPDatabaseMetadata`，并删除镜像字段与 `getMetadataObjects()` 依赖。
2. 重构 `DatabaseMetadataSnapshots`，
   删除全局 `metadataObjects` 聚合缓存，使其只维护 typed snapshots 与必要查找入口。
3. 重写 `MCPJdbcMetadataLoader`，
   让它直接构建 `MCPDatabaseMetadata` typed tree，
   用 typed accumulator 取代 `MetadataObjectAccumulator`。
4. 删除 `MetadataObject` 的主代码路径引用，并移除或重写 `MetadataHierarchyBuilder`。
5. 重构 `MetadataQueryService`，
   把所有 list / detail / search 的源数据切换到 typed snapshot。
6. 更新 metadata response、resource handlers、tool dispatch 与 payload resolver，
   确保对外结果只使用具体 metadata 对象或 `MetadataSearchHit`。
7. 更新 core / bootstrap tests 与测试夹具，
   移除所有手写 `new MetadataObject(...)` 的路径。
8. 最后用 grep、scoped tests、style checks 确认 `MetadataObject` 已彻底退出代码库主路径。

## Validation Strategy

- **Loader and snapshot verification**
```bash
./mvnw -pl mcp/core -am -DskipITs -Dspotless.skip=true \
  -Dtest=MCPJdbcMetadataLoaderTest,DatabaseMetadataSnapshotsTest test \
  -Dsurefire.failIfNoSpecifiedTests=false
```

- **Query verification**
```bash
./mvnw -pl mcp/core -am -DskipITs -Dspotless.skip=true \
  -Dtest=MetadataQueryServiceTest test \
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
  -Dtest=MCPToolCallHandlerTest,ProductionMetadataDiscoveryIntegrationTest,StdioTransportIntegrationTest test \
  -Dsurefire.failIfNoSpecifiedTests=false
```

- **Scoped style checks**
```bash
./mvnw -pl mcp/core,mcp/bootstrap -am -Pcheck -DskipITs -DskipTests \
  checkstyle:check spotless:check
```

- **Static grep verification**
```bash
rg -n "\\bMetadataObject\\b" \
  /Users/zhangliang/IdeaProjects/shardingsphere/mcp/core \
  /Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap
```

## Rollout Notes

- 这轮不是把 `MetadataObject` 藏到更里面，而是把它彻底删掉。
- 如果实现后 `DatabaseMetadataSnapshot` 也只剩透传职责，
  后续可以进一步评估是否继续收敛成直接使用 `MCPDatabaseMetadata` map；
  但本轮先以最小安全变化完成 typed source-of-truth 迁移。
- 如果最终评审认为 metadata payload breaking change 需要迁移窗口，
  应在 follow-up 里明确兼容策略；本 spec 不假装这是零影响内部重构。
