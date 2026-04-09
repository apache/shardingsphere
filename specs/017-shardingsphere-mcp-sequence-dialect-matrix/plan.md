# Implementation Plan: ShardingSphere MCP Sequence Dialect Matrix

**Branch**: `no-branch-switch-requested` | **Date**: 2026-04-07 | **Spec**:
[spec.md](/Users/zhangliang/IdeaProjects/shardingsphere/specs/017-shardingsphere-mcp-sequence-dialect-matrix/spec.md)
**Input**: Feature specification from `/specs/017-shardingsphere-mcp-sequence-dialect-matrix/spec.md`

## Summary

本轮把 sequence support 从 H2 扩展到真实支持该对象的正式数据库方言，并保证 capability 与 metadata loader 同步：

- capability option 为支持 sequence 的方言声明 `SEQUENCE`
- `MCPJdbcMetadataLoader` 按方言增加 sequence 查询
- 更新 capability matrix / loader tests / Speckit 状态

不会新增 transport、auth、tool 或其他对象类型。

## Technical Context

**Language/Version**: Java 17 in the MCP subchain  
**Primary Dependencies**: repository-owned MCP core runtime, JDBC metadata loading, ShardingSphere SPI capability loader  
**Storage**: in-memory metadata catalog populated from configured runtime databases  
**Testing**: scoped `mcp/core` and `test/e2e/mcp` unit/E2E verification  
**Target Platform**: ShardingSphere MCP runtime metadata discovery layer  
**Project Type**: Java monorepo subproject under `mcp/core`, `test/e2e/mcp`, and `specs/`  
**Constraints**: additive change only; no branch switch; no auth work; no remote runtime relaxation; no old metadata tool revival  
**Scale/Scope**: one object type across multiple supported database dialects

## Constitution Check

*GATE: Must pass before implementation starts. Re-check after design.*

- **Gate 1 - Smallest safe change**: PASS  
  只扩 sequence 的方言矩阵，不重构 metadata loader 主体。
- **Gate 2 - Explicit governance and security**: PASS  
  不触及 transport、安全边界和鉴权。
- **Gate 3 - Testable delivery**: PASS  
  capability matrix 与 loader 分支都有 scoped verification。
- **Gate 4 - Traceable contracts**: PASS  
  本轮主要收敛内部 capability/matrix；无需再扩大 public surface。
- **Gate 5 - Quality gates**: PASS  
  `mcp/core` 与 `test/e2e/mcp` 有 scoped test/style 命令。

## Hard Constraint Checklist

- 不切换分支
- 以新增或最小定点修改为主
- capability 声明必须与 loader 能力一致
- 不支持的数据库不得虚假声明 `SEQUENCE`
- 不新增其他 metadata object types
- 不改 transport / auth / remote behavior
- 必须有 scoped tests 与 style checks

## Project Structure

### Documentation (this feature)

```text
specs/017-shardingsphere-mcp-sequence-dialect-matrix/
├── plan.md
├── spec.md
└── tasks.md
```

### Source Code (repository root)

```text
mcp/core/src/main/java/org/apache/shardingsphere/mcp/capability/database/dialect/*.java
mcp/core/src/main/java/org/apache/shardingsphere/mcp/metadata/jdbc/MCPJdbcMetadataLoader.java
mcp/core/src/test/java/org/apache/shardingsphere/mcp/capability/database/MCPDatabaseCapabilityProviderTest.java
mcp/core/src/test/java/org/apache/shardingsphere/mcp/metadata/jdbc/MCPJdbcMetadataLoaderTest.java
specs/017-shardingsphere-mcp-sequence-dialect-matrix/*
```

**Structure Decision**: 不新增模块；沿用 `DatabaseCapabilityOption` 扩展点与 `MCPJdbcMetadataLoader` 的方言分支。

## Design Decisions

### 1. capability 与 loader 一起扩，不做单边声明

- 只有能提供 sequence metadata source 的数据库才声明 `SEQUENCE`
- 避免 capability 比 runtime 真实现更乐观

### 2. sequence metadata loading 继续保持最小 private-switch 方案

- 不引入新的 loader SPI
- `MCPJdbcMetadataLoader` 内部按 `databaseType` 路由 sequence 查询
- 每个方言只返回 schema/name 两个字段

### 3. 数据源按方言最稳定的官方元数据入口选取

- PostgreSQL/openGauss: `information_schema.sequences`
- SQLServer: `sys.sequences` + `sys.schemas`
- Oracle: `USER_SEQUENCES`
- MariaDB: `INFORMATION_SCHEMA.TABLES` with `TABLE_TYPE='SEQUENCE'`
- Firebird: `RDB$GENERATORS` with system flag filter
- H2: `INFORMATION_SCHEMA.SEQUENCES`

## Branch Checklist

1. `capability_matrix_declares_sequence_for_supported_dialects`
   Planned verification: capability provider tests
2. `loader_reads_sequence_names_for_supported_dialects`
   Planned verification: mocked JDBC metadata loader tests per dialect
3. `unsupported_dialects_stay_without_sequence`
   Planned verification: capability provider tests and existing unsupported behavior

## Implementation Strategy

1. 先落 Speckit 文档，冻结本轮 sequence dialect matrix 范围。
2. 更新支持 sequence 的 dialect capability options。
3. 扩展 `MCPJdbcMetadataLoader` 的 sequence 查询路由与 SQL。
4. 在 `MCPJdbcMetadataLoaderTest` 中补各方言 mocked loading tests。
5. 更新 capability matrix provider tests。
6. 跑 scoped tests 和 style checks，最后回填 Speckit 状态。

## Validation Strategy

- **Core capability / loader verification**
```bash
./mvnw -pl mcp/core -am -DskipITs -Dspotless.skip=true \
  -Dtest=MCPDatabaseCapabilityProviderTest,MCPJdbcMetadataLoaderTest test \
  -Dsurefire.failIfNoSpecifiedTests=false
```

- **Capability matrix verification**
```bash
./mvnw -pl mcp/core -am -DskipITs -Dspotless.skip=true \
  -Dtest=MCPDatabaseCapabilityProviderTest test \
  -Dsurefire.failIfNoSpecifiedTests=false
```

- **Scoped style checks**
```bash
./mvnw -pl mcp/core,test/e2e/mcp -am -Pcheck -DskipITs -DskipTests \
  spotless:check checkstyle:check
```
