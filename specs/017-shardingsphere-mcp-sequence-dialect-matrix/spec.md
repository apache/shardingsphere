# Feature Specification: ShardingSphere MCP Sequence Dialect Matrix

**Feature Branch**: `no-branch-switch-requested`  
**Created**: 2026-04-07  
**Status**: Implemented  
**Input**: User description:
"其他数据库都声明好，按照数据库本身是否支持给我完善好"

## Scope Statement

本轮在已完成的 H2 sequence discovery 基础上，继续把 sequence 的 database-level capability matrix 按方言补齐，
并保证 capability 与 metadata discovery 一致：

- 对原生支持 sequence 的数据库声明 `SEQUENCE`
- 对这些数据库补上最小可行的 sequence metadata loading
- 不支持 sequence 的数据库继续保持未声明
- 不扩大到其他 metadata object types

本轮不新增鉴权、不放松 HTTP 默认边界、不重构 transport，也不恢复旧 metadata tools。

## Problem Statement

当前 sequence 只在 H2 上声明并打通了 discovery 链路，而其他正式支持数据库仍保持默认状态。
这会导致 capability matrix 与数据库真实能力不一致：

1. PostgreSQL / openGauss / SQLServer / Oracle / MariaDB / Firebird 等数据库本身支持 sequence，
   但当前 MCP capability 未声明
2. 如果只补 capability 不补 metadata loader，又会产生新的 contract drift
3. `MCPDatabaseCapabilityProviderTest` 需要反映真实 sequence 支持矩阵

因此这轮必须把“是否声明”与“是否可发现”一起收口。

## User Scenarios & Testing *(mandatory)*

### User Story 1 - capability matrix 反映真实 sequence 支持 (Priority: P1)

作为 reviewer 或接入方，我希望 database capability 按真实数据库能力声明 `SEQUENCE`，
这样模型和客户端可以依据 capability 判断 sequence 是否可用。

**Why this priority**: capability 是 metadata surface 的边界说明；不真实会直接误导调用方。

**Independent Test**: capability provider tests
可以独立验证每个 database type 的 `SEQUENCE` 声明。

**Acceptance Scenarios**:

1. **Given** database type 为 PostgreSQL、openGauss、SQLServer、Oracle、MariaDB、Firebird、H2，
   **When** 读取 database capability，
   **Then** `supportedObjectTypes` 中包含 `SEQUENCE`。
2. **Given** database type 为 MySQL、ClickHouse、Doris、Hive、Presto，
   **When** 读取 database capability，
   **Then** `supportedObjectTypes` 中不包含 `SEQUENCE`。

---

### User Story 2 - 对声明支持的数据库，sequence discovery 至少有对应 loader (Priority: P1)

作为 MCP runtime 使用者，我希望当 capability 声明某个数据库支持 `SEQUENCE` 时，
resource 和 `search_metadata` 至少能通过对应 metadata loader 读取 sequence 名称，
这样 capability 与 discovery 不会自相矛盾。

**Why this priority**: 这轮的重点不是“所有方言都做完整深度 metadata”，
而是“已声明支持的对象类型不能只是空承诺”。

**Independent Test**: `MCPJdbcMetadataLoaderTest` 通过 mock JDBC connections
验证各方言 sequence loading 分支。

**Acceptance Scenarios**:

1. **Given** 受支持的数据库类型与其标准 sequence metadata source，
   **When** `MCPJdbcMetadataLoader` 加载 metadata，
   **Then** metadata catalog 中包含对应 sequence 名称。
2. **Given** 不支持 sequence 的数据库类型，
   **When** 读取 sequence resource，
   **Then** 仍返回 `unsupported`。

### Edge Cases

- MariaDB sequence metadata 优先使用兼容性更好的 `INFORMATION_SCHEMA.TABLES` `TABLE_TYPE='SEQUENCE'`，而不是只依赖较新版本的 `INFORMATION_SCHEMA.SEQUENCES`。
- Oracle 先以当前用户可见的 `USER_SEQUENCES` 作为最小可行 metadata source，避免把大量系统序列默认暴露出来。
- Firebird 以 `RDB$GENERATORS` 读取用户自建 sequence/generator，并过滤系统 generator。
- 不支持 sequence 的数据库不新增假 loader，也不声明 `SEQUENCE`。

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: PostgreSQL、openGauss、SQLServer、Oracle、MariaDB、Firebird、H2 MUST 在 capability 中声明 `SEQUENCE`。
- **FR-002**: MySQL、ClickHouse、Doris、Hive、Presto MUST NOT 在 capability 中声明 `SEQUENCE`。
- **FR-003**: 对于 capability 已声明 `SEQUENCE` 的数据库，`MCPJdbcMetadataLoader` MUST 提供对应的 sequence metadata loading 分支。
- **FR-004**: PostgreSQL 与 openGauss MUST 通过 `information_schema.sequences` 读取 sequence metadata。
- **FR-005**: SQLServer MUST 通过 `sys.sequences` + `sys.schemas` 读取 sequence metadata。
- **FR-006**: Oracle MUST 通过 `USER_SEQUENCES` 读取当前用户 schema 下的 sequence metadata。
- **FR-007**: MariaDB MUST 通过 `INFORMATION_SCHEMA.TABLES` 中 `TABLE_TYPE='SEQUENCE'` 读取 sequence metadata。
- **FR-008**: Firebird MUST 通过 `RDB$GENERATORS` 读取 sequence metadata，并过滤系统 generator。
- **FR-009**: 本轮实现 MUST 保持为增量修改，不引入新的 metadata tool 或新 transport behavior。
- **FR-010**: capability matrix 测试与 Speckit 文档 MUST 与实际实现同步更新。

### Assumptions

- sequence discovery 的 public surface 仍保持 resource-only + `search_metadata`。
- 不支持 sequence 的数据库即便存在“序列相关概念”或“隐藏 sequence column”，也不等同于可公开的 sequence object。
- 本轮优先保证 capability 与 loader 一致，不在没有真实 metadata source 的前提下勉强声明支持。

## Non-Goals

- 不扩展 materialized view、routine、trigger、event、synonym。
- 不为这些数据库新增真实 integration containers。
- 不补深层 sequence detail 字段，只要求可发现 sequence 名称。
- 不修改 remote/hosted 或 auth 方案。

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: `MCPDatabaseCapabilityProviderTest` 反映新的 sequence support matrix 并通过。
- **SC-002**: `MCPJdbcMetadataLoaderTest` 覆盖并通过 PostgreSQL、openGauss、SQLServer、Oracle、MariaDB、Firebird、H2 的 sequence loading 分支。
- **SC-003**: 不支持 sequence 的数据库相关 unsupported tests 保持通过。

## Implementation Notes

- `SEQUENCE` capability 已扩展到 PostgreSQL、openGauss、SQLServer、Oracle、MariaDB、Firebird、H2。
- `MCPJdbcMetadataLoader` 继续保持单点 private-switch 方案，按方言路由到最小可用 sequence metadata 查询。
- MySQL、ClickHouse、Doris、Hive、Presto 继续保持未声明 `SEQUENCE`，避免 capability 与 runtime metadata discovery 漂移。
