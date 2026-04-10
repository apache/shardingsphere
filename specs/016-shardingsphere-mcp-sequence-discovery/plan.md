# Implementation Plan: ShardingSphere MCP Sequence Discovery

**Branch**: `no-branch-switch-requested` | **Date**: 2026-04-07 | **Spec**:
[spec.md](/Users/zhangliang/IdeaProjects/shardingsphere/specs/016-shardingsphere-mcp-sequence-discovery/spec.md)
**Input**: Feature specification from `/specs/016-shardingsphere-mcp-sequence-discovery/spec.md`

## Summary

本轮以最小新增方式把 `sequence` 接入当前 MCP 统一 metadata discovery 面：

- metadata model / catalog 新增 sequence
- H2 capability 与 H2 metadata loader 打通 sequence
- resource 新增 sequences list/detail
- `search_metadata` 支持 `sequence`
- contract / README / quickstart 同步更新

不会切换分支，不引入鉴权，不放松默认 loopback 安全基线，也不泛化到更多数据库对象。

## Technical Context

**Language/Version**: Java 17 in the MCP subchain  
**Primary Dependencies**: repository-owned MCP core runtime, JDBC metadata loading, ShardingSphere SPI capability loader  
**Storage**: in-memory metadata catalog populated from configured runtime databases  
**Testing**: scoped `mcp/core`, `mcp/bootstrap`, and `test/e2e/mcp` unit/integration/E2E verification  
**Target Platform**: ShardingSphere MCP standalone runtime on embedded Tomcat / STDIO  
**Project Type**: Java monorepo subproject under `mcp/core`, `mcp/bootstrap`, `test/e2e/mcp`, and `specs/`  
**Constraints**: additive change only; no branch switch; no auth work; no remote runtime relaxation; no old metadata tool revival  
**Scale/Scope**: one new metadata object type plus associated discovery surface

## Constitution Check

*GATE: Must pass before implementation starts. Re-check after design.*

- **Gate 1 - Smallest safe change**: PASS  
  只新增 `sequence` 一种对象，并复用现有 resource-only / tool-only 分层，不做整体重构。
- **Gate 2 - Explicit governance and security**: PASS  
  本轮不改 HTTP 默认边界，也不引入未设计完的 auth；对外范围只增加 sequence discovery。
- **Gate 3 - Testable delivery**: PASS  
  capability、resource、tool、E2E 都有明确验证分支。
- **Gate 4 - Traceable contracts**: PASS  
  public contract、README、quickstart 会与实现同步收敛。
- **Gate 5 - Quality gates**: PASS  
  可以通过 scoped core/bootstrap/e2e tests 与 touched-module style checks 验证。

## Hard Constraint Checklist

- 不切换分支，只在当前工作树完成 Speckit 设计和实现
- 以新增为主，非必要不改动无关现有代码
- 不恢复旧 metadata tools
- 不引入鉴权
- 不放松默认 loopback 安全边界
- 只扩 `sequence`，不顺带扩其他 metadata object
- 必须通过 resources + `search_metadata` 两条 discovery 链路暴露 `sequence`
- 必须让 capability 对实际支持数据库真实声明 `SEQUENCE`
- 必须让不支持 `SEQUENCE` 的数据库返回 `unsupported`
- 必须用 scoped tests 和 style checks 验证 touched modules

## Project Structure

### Documentation (this feature)

```text
specs/016-shardingsphere-mcp-sequence-discovery/
├── plan.md
├── spec.md
└── tasks.md
```

### Source Code (repository root)

```text
mcp/core/src/main/java/org/apache/shardingsphere/mcp/metadata/model/MCPSequenceMetadata.java
mcp/core/src/main/java/org/apache/shardingsphere/mcp/metadata/model/MCPSchemaMetadata.java
mcp/core/src/main/java/org/apache/shardingsphere/mcp/metadata/query/MetadataQueryService.java
mcp/core/src/main/java/org/apache/shardingsphere/mcp/metadata/jdbc/MCPJdbcMetadataLoader.java
mcp/core/src/main/java/org/apache/shardingsphere/mcp/capability/database/DatabaseCapabilityOption.java
mcp/core/src/main/java/org/apache/shardingsphere/mcp/capability/database/MCPDatabaseCapabilityProvider.java
mcp/core/src/main/java/org/apache/shardingsphere/mcp/capability/database/dialect/H2DatabaseCapabilityOption.java
mcp/core/src/main/java/org/apache/shardingsphere/mcp/resource/handler/metadata/SequencesHandler.java
mcp/core/src/main/java/org/apache/shardingsphere/mcp/resource/handler/metadata/SequenceHandler.java
mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/handler/metadata/SearchMetadataToolHandler.java
mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/handler/metadata/SearchMetadataToolService.java
mcp/core/src/main/resources/META-INF/services/org.apache.shardingsphere.mcp.resource.handler.ResourceHandler
mcp/core/src/test/java/org/apache/shardingsphere/mcp/resource/ResourceTestDataFactory.java
mcp/core/src/test/java/org/apache/shardingsphere/mcp/resource/handler/ResourceHandlerRegistryTest.java
mcp/core/src/test/java/org/apache/shardingsphere/mcp/resource/handler/ResourceHandlerTest.java
mcp/core/src/test/java/org/apache/shardingsphere/mcp/resource/MCPResourceControllerTest.java
mcp/core/src/test/java/org/apache/shardingsphere/mcp/tool/MCPToolControllerTest.java
mcp/core/src/test/java/org/apache/shardingsphere/mcp/tool/handler/metadata/SearchMetadataToolServiceTest.java
mcp/core/src/test/java/org/apache/shardingsphere/mcp/jdbc/H2RuntimeTestSupport.java
mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/H2RuntimeTestSupport.java
mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/ProductionMetadataDiscoveryIntegrationTest.java
test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/MetadataDiscoveryE2ETest.java
specs/001-shardingsphere-mcp/contracts/mcp-domain-contract.md
specs/001-shardingsphere-mcp/quickstart.md
specs/001-shardingsphere-mcp/spec.md
mcp/README.md
mcp/README_ZH.md
```

**Structure Decision**: 不新增模块；只在现有 `core`、`bootstrap`、`test/e2e/mcp`、`specs/001` 与 README 上增量接入 `sequence`。

## Design Decisions

### 1. `sequence` 作为首个增量对象，仍然遵守 resource-only discovery

- 不新增 `describe_sequence` 或 `list_sequences` tool
- list/detail 统一通过 sequence resources 暴露
- 搜索统一通过 `search_metadata`

### 2. metadata model 只做必要扩展

- 新增 `MCPSequenceMetadata`
- `MCPSchemaMetadata` 增加 `sequences`
- schema detail 可返回 sequence summaries，schema summary 继续保持轻量

这样不需要改写 catalog 结构，只在既有 schema 聚合点加一个新增集合。

### 3. capability 扩展采用默认方法，而不是重写 provider 流程

- `DatabaseCapabilityOption` 新增默认方法，允许方言声明额外支持的 metadata object types
- `MCPDatabaseCapabilityProvider` 在基线对象外叠加这些扩展对象
- H2 先声明 `SEQUENCE`

这样后续继续接 materialized view / routine 等对象时，可以复用同一能力扩展点。

### 4. metadata loading 先走 H2 最小可用方案

- `MCPJdbcMetadataLoader` 保持现有主流程
- 只新增一段 H2-specific sequence loading
- 从 `INFORMATION_SCHEMA.SEQUENCES` 读取 schema / name 并写入 schema accumulator

不在本轮引入通用 metadata loader SPI，避免为一个对象类型过早设计更厚框架。

### 5. search 和 resource 保持同一对象语义

- sequence resource URI 采用：
  `shardingsphere://databases/{database}/schemas/{schema}/sequences`
  `shardingsphere://databases/{database}/schemas/{schema}/sequences/{sequence}`
- `search_metadata.object_types` 扩到 `sequence`
- 排序与命名规则沿用既有 `MetadataSearchHit`

## Branch Checklist

1. `h2_capability_declares_sequence`
   Planned verification: database capability resource / provider-visible behavior includes `SEQUENCE`
2. `h2_metadata_catalog_loads_sequence`
   Planned verification: H2-backed tests can discover `order_seq` from metadata catalog-driven resources
3. `sequence_resources_are_available_for_supported_databases`
   Planned verification: core resource tests and production HTTP tests read sequences list/detail successfully
4. `sequence_resources_fail_on_unsupported_databases`
   Planned verification: core resource controller/handler tests return `unsupported` for non-supporting databases
5. `search_metadata_supports_sequence`
   Planned verification: tool controller, search service, HTTP integration, and E2E tests find `order_seq`
6. `public_contract_mentions_sequence`
   Planned verification: README / contract / quickstart / spec grep and review

## Implementation Strategy

1. 先落 Speckit 文档，冻结本轮 sequence 范围、分支和验证口径。
2. 新增 `MCPSequenceMetadata`，并把 `MCPSchemaMetadata` 扩到能承载 sequence summaries/details。
3. 为 capability 增加“额外支持对象类型”的默认扩展点，并让 H2 声明 `SEQUENCE`。
4. 修改 `MCPJdbcMetadataLoader`，仅为 H2 增量加载 `INFORMATION_SCHEMA.SEQUENCES`。
5. 在 `MetadataQueryService`、resource handlers、resource SPI 注册中新增 sequence list/detail 读取。
6. 在 `search_metadata` handler/service 中把 `sequence` 纳入 public object type 和默认搜索面。
7. 更新 core/bootstrap/e2e 测试与 H2 fixture，确保 `order_seq` 贯通整条 discovery 链路。
8. 更新 contract / README / quickstart / spec，最后回填 Speckit 状态。

## Validation Strategy

- **Core metadata / resource / tool verification**
```bash
./mvnw -pl mcp/core -am -DskipITs -Dspotless.skip=true \
  -Dtest=ResourceHandlerRegistryTest,ResourceHandlerTest,MCPResourceControllerTest,MCPToolControllerTest,SearchMetadataToolServiceTest test \
  -Dsurefire.failIfNoSpecifiedTests=false
```

- **Bootstrap production HTTP verification**
```bash
./mvnw -pl mcp/bootstrap -am -DskipITs -Dspotless.skip=true \
  -Dtest=ProductionMetadataDiscoveryIntegrationTest test \
  -Dsurefire.failIfNoSpecifiedTests=false
```

- **E2E metadata verification**
```bash
./mvnw -pl test/e2e/mcp -am -DskipITs -Dspotless.skip=true \
  -Dtest=MetadataDiscoveryE2ETest test \
  -Dsurefire.failIfNoSpecifiedTests=false
```

- **Scoped style checks**
```bash
./mvnw -pl mcp/core,mcp/bootstrap,test/e2e/mcp -am -Pcheck -DskipITs -DskipTests \
  checkstyle:check spotless:check
```

- **Static contract grep**
```bash
rg -n "sequence|SEQUENCE|materialized_view" \
  /Users/zhangliang/IdeaProjects/shardingsphere/specs/001-shardingsphere-mcp/contracts/mcp-domain-contract.md \
  /Users/zhangliang/IdeaProjects/shardingsphere/specs/001-shardingsphere-mcp/quickstart.md \
  /Users/zhangliang/IdeaProjects/shardingsphere/specs/001-shardingsphere-mcp/spec.md \
  /Users/zhangliang/IdeaProjects/shardingsphere/mcp/README.md \
  /Users/zhangliang/IdeaProjects/shardingsphere/mcp/README_ZH.md
```

## Rollout Notes

- 本轮只把 sequence 作为首个增量对象落地，不意味着所有扩展对象策略已经最终定型。
- 后续若继续扩 object types，优先复用 capability 扩展点和 resource-only public surface。
- remote/hosted 和鉴权仍然保持后续独立设计，不与本轮耦合。
