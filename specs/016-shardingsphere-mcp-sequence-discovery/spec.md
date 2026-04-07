# Feature Specification: ShardingSphere MCP Sequence Discovery

**Feature Branch**: `no-branch-switch-requested`  
**Created**: 2026-04-07  
**Status**: Implemented  
**Input**: User description:
"继续吧，可以开始写代码了吗？注意：基于现在去做落地，非必要尽量不要修改已有代码，只会对新有的功能和已梳理的功能去进行新增"

## Scope Statement

本轮在现有 V0 基线之上，只新增 `sequence` 这一种 metadata object：

- capability 对支持数据库显式声明 `SEQUENCE`
- metadata catalog / query service 能读取并查询 sequence
- `resources/read` 提供 sequence list/detail 资源
- `search_metadata` 接受 `sequence` 并返回 sequence 命中
- public contract / README / quickstart 与之同步

本轮不放松 HTTP 默认边界，不新增鉴权，不恢复旧 metadata tools，也不一次性扩到 routine、trigger、event 等其他对象。

## Problem Statement

当前 MCP 已经为未来统一搜索面预留了 `MetadataObjectType.SEQUENCE`，
但实际 public surface 仍然把 `sequence` 排除在外：

1. capability 不会声明 `SEQUENCE`
2. metadata loader 不会加载 sequence
3. schema/resource/search 三条 discovery 链路都没有 sequence
4. contract / README 仍把 `sequence` 当作 excluded object

这会导致“对象类型已经在代码里存在，但实际完全不可用”的中间态，
也不利于后续继续扩展 materialized view、routine、trigger 等对象。

## User Scenarios & Testing *(mandatory)*

### User Story 1 - 通过 resource 发现 sequence (Priority: P1)

作为 MCP 调用方，我希望当目标数据库支持 sequence 时，
可以通过 resource list/detail 读取 sequence 元数据，
这样 metadata discovery 仍然保持 resource-only 的统一入口。

**Why this priority**: resource 是当前 metadata discovery 的唯一真源；
如果 sequence 不先接入 resource，统一对象面就会被打破。

**Independent Test**: core resource handler tests 与 HTTP production metadata integration tests
可以独立验证 sequence list/detail/capability 三条链路。

**Acceptance Scenarios**:

1. **Given** H2 runtime schema 中存在 `order_seq`，
   **When** 读取 `shardingsphere://databases/{database}/schemas/{schema}/sequences`，
   **Then** 返回中包含 `order_seq`。
2. **Given** H2 runtime schema 中存在 `order_seq`，
   **When** 读取 `shardingsphere://databases/{database}/schemas/{schema}/sequences/{sequence}`，
   **Then** 返回中包含该 sequence 的 detail。
3. **Given** 目标数据库 capability 支持 sequence，
   **When** 读取 `shardingsphere://databases/{database}/capabilities`，
   **Then** `supportedObjectTypes` 中包含 `SEQUENCE`。
4. **Given** 目标数据库 capability 不支持 sequence，
   **When** 读取 sequence resource，
   **Then** 返回 `unsupported`，而不是静默空结果。

---

### User Story 2 - 通过 `search_metadata` 搜索 sequence (Priority: P1)

作为 MCP tool 调用方，我希望在统一搜索面里直接搜索 sequence，
这样模型或客户端不需要知道对象是否来自 table/view/index 之外的新增类型。

**Why this priority**: `search_metadata` 是 metadata discovery 的统一搜索入口；
如果 sequence 只在 resource 可读而不可搜，统一搜索面仍然是不完整的。

**Independent Test**: core search service tests、tool controller tests 与 E2E / production HTTP tests
可以独立验证 `sequence` object type 的搜索行为。

**Acceptance Scenarios**:

1. **Given** H2 runtime schema 中存在 `order_seq`，
   **When** 调用 `search_metadata` 且 `object_types=["sequence"]`，
   **Then** 返回中包含 `order_seq`。
2. **Given** H2 runtime schema 中存在 `order_seq`，
   **When** 调用 `search_metadata` 且省略 `object_types`，
   **Then** 默认搜索面也能命中 `order_seq`。
3. **Given** 调用方传入 `object_types=["materialized_view"]`，
   **When** 调用 `search_metadata`，
   **Then** 仍然返回 `invalid_request`。

### Edge Cases

- sequence 当前只为 H2 打通；其他数据库保持现状，不做泛化承诺。
- 不支持 sequence 的数据库 capability 不得虚假声明 `SEQUENCE`。
- `schema` summary 仍保持轻量；只有 detail resource 才暴露 sequence 列表。
- 本轮不会为 sequence 新增专门 tool，仍然统一走 resource + `search_metadata`。

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: 系统 MUST 将 `sequence` 纳入当前统一 metadata discovery surface 的可扩展对象之一。
- **FR-002**: 公共 resources MUST 新增：
  `shardingsphere://databases/{database}/schemas/{schema}/sequences` 与
  `shardingsphere://databases/{database}/schemas/{schema}/sequences/{sequence}`。
- **FR-003**: 当目标 database capability 支持 `SEQUENCE` 时，
  sequence list/detail resources MUST 返回真实 metadata。
- **FR-004**: 当目标 database capability 不支持 `SEQUENCE` 时，
  sequence resources MUST 返回 `unsupported`。
- **FR-005**: `search_metadata.object_types` MUST 接受 `sequence`，
  并将其视为合法 public object type。
- **FR-006**: `search_metadata` 在未显式指定 `object_types` 时，
  对支持 `SEQUENCE` 的数据库 MUST 将 sequence 纳入默认搜索面。
- **FR-007**: H2 runtime metadata loader MUST 从 `INFORMATION_SCHEMA.SEQUENCES`
  读取 sequence，并写入 metadata catalog。
- **FR-008**: H2 database capability MUST 在 `supportedObjectTypes` 中声明 `SEQUENCE`。
- **FR-009**: 本轮实现 MUST 以新增为主，不引入新的 metadata tool，
  不改动 HTTP 默认安全基线，也不引入鉴权。
- **FR-010**: README、quickstart 与 domain contract MUST 明确 public surface 已支持 sequence。

### Assumptions

- 当前 resource-only metadata discovery 方向已确认，不恢复 `describe_*` / `list_*` tools。
- `sequence` 是统一搜索面的首个新增对象类型，其它对象以后续切片继续扩展。
- H2 是本轮最小可验证数据库；跨方言 sequence 发现不在本轮承诺范围内。

## Non-Goals

- 不实现 routine、trigger、event、synonym、materialized view。
- 不引入 OAuth、API Key、ACL、rate limiting 或 remote hosted 鉴权。
- 不把 sequence 扩展成 SQL execution 语义的一部分。
- 不重构整个 metadata loader 为通用插件框架。

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: `resources/read` 支持并通过读取
  `shardingsphere://databases/{database}/schemas/{schema}/sequences`
  与 `.../sequences/{sequence}` 的 core / HTTP tests。
- **SC-002**: H2 database capability resource 中包含 `SEQUENCE`。
- **SC-003**: `search_metadata` 在 core / HTTP / E2E tests 中能命中 `order_seq`。
- **SC-004**: `rg -n "sequence" mcp/README.md mcp/README_ZH.md specs/001-shardingsphere-mcp/contracts/mcp-domain-contract.md`
  能看到 sequence 已进入 public surface，而 `materialized_view` 仍未进入 public object type 白名单。

## Implementation Notes

- `MCPSequenceMetadata` 已作为新增 metadata model 接入 schema detail。
- H2 capability 通过 `DatabaseCapabilityOption#isSequenceSupported()` 声明 `SEQUENCE`。
- H2 runtime 通过 `INFORMATION_SCHEMA.SEQUENCES` 加载 `order_seq`，并打通了 resource 与 `search_metadata`。
- `materialized_view` 等仍然保持 `invalid_request` / 未公开状态，没有随本轮一并放开。
