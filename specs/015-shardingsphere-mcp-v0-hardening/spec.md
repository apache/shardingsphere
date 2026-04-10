# Feature Specification: ShardingSphere MCP V0 Surface Hardening

**Feature Branch**: `no-branch-switch-requested`  
**Created**: 2026-04-07  
**Status**: Implemented  
**Input**: User description:
"做V0吧。使用speckit做好设计和实施，但不允许切换分支"

## Scope Statement

本轮 V0 只做当前 MCP 可提交前必须立即收口的四类问题：

- 统一 public surface 口径，明确 metadata discovery 以 `resource` 为唯一真源
- 修正 `execute_query` 的结果集截断语义
- 收紧 tool 参数校验，避免无效输入被静默放大为更宽泛的查询
- 收紧 HTTP 默认安全边界，非 loopback 绑定必须显式声明远程访问意图

本轮不恢复已删除的 metadata tools，不新增 public resource URI，不切换分支，也不夹带无关重构。

## Problem Statement

当前 MCP 主链路已经可运行，但还有四个会直接影响提交质量的问题：

1. 文档和 contract 仍然把已经删除的 metadata tools 当作 V1 public surface  
2. `execute_query` 在 `max_rows` 场景下无法稳定标记 `truncated=true`  
3. tool 参数校验过宽，非法 `object_types` 会被静默过滤，甚至扩大查询范围  
4. HTTP 端虽然默认示例绑定 loopback，但配置层缺少显式远程访问意图，默认安全基线不够明确

这四类问题如果不先收口，reviewer 会同时看到：

- public contract 与实现不一致
- 行为正确性缺口
- 请求校验不严格
- 安全边界说不清

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Public surface 与实现一致 (Priority: P1)

作为 ShardingSphere MCP 的 reviewer 或接入方，我希望公开文档、contract 和 quickstart 只描述当前真实可用的入口，
这样不会再被已经删除的 metadata tools 误导。

**Why this priority**: 这是提交前的最小真实性要求；如果 public surface 还在漂移，后续测试通过也很难获得可信评审。

**Independent Test**: 文档与 contract review 应能确认 metadata discovery 统一走 resource，
tools 只保留 `search_metadata` 与 `execute_query`。

**Acceptance Scenarios**:

1. **Given** 接入方阅读 README、quickstart 与 domain contract，
   **When** 查找 metadata discovery 入口，
   **Then** 只能看到 resource-based discovery，而不是旧的 `list_*` / `describe_*` / `get_capabilities` tool 承诺。
2. **Given** 接入方阅读 capability 说明，
   **When** 判断 service/database capability 的访问方式，
   **Then** 文档必须明确 capability 通过 `resources/read` 获取。

---

### User Story 2 - `execute_query` 截断语义正确 (Priority: P1)

作为 MCP query 使用者，我希望当结果被 `max_rows` 限制时，
返回体能稳定标记 `truncated=true`，这样我可以知道结果并不完整。

**Why this priority**: 这属于 public behavior correctness，错误的 `truncated=false` 会直接误导上层 agent 或 client。

**Independent Test**: `MCPJdbcStatementExecutorTest` 与 HTTP production execute integration tests
可以独立验证超限与未超限两条分支。

**Acceptance Scenarios**:

1. **Given** 查询真实结果行数大于 `max_rows`，
   **When** 调用 `execute_query`，
   **Then** 返回行数等于 `max_rows` 且 `truncated=true`。
2. **Given** 查询真实结果行数小于等于 `max_rows`，
   **When** 调用 `execute_query`，
   **Then** 返回完整结果且 `truncated=false`。

---

### User Story 3 - 非法 tool 输入必须显式失败 (Priority: P1)

作为 MCP tool 调用方，我希望缺失或非法的输入立即得到 `invalid_request`，
而不是被静默兼容成另一个更宽的查询。

**Why this priority**: 这是 request validation 的基本要求，也是 MCP 官方工具语义最看重的边界之一。

**Independent Test**: `MCPToolControllerTest` 与 `SearchMetadataToolServiceTest`
可独立验证必填参数为空、非法 `object_types`、以及合法过滤路径。

**Acceptance Scenarios**:

1. **Given** `search_metadata` 缺失 `query` 或提供空白 `query`，
   **When** 调用 tool，
   **Then** 返回 `invalid_request`。
2. **Given** `execute_query` 缺失 `database`、`sql` 或提供空白值，
   **When** 调用 tool，
   **Then** 返回 `invalid_request`。
3. **Given** `search_metadata.object_types` 包含超出当前 public search surface 的值，
   **When** 调用 tool，
   **Then** 返回 `invalid_request`，而不是退化成更宽的搜索。
4. **Given** `search_metadata.object_types` 包含合法值，
   **When** 调用 tool，
   **Then** 查询范围必须严格受该过滤器约束。

---

### User Story 4 - HTTP 默认边界要求显式远程访问意图 (Priority: P1)

作为运行方，我希望当前内置 HTTP runtime 在未引入完整认证授权前，
默认只接受 loopback 绑定；如果需要非 loopback 绑定，必须显式设置 `allowRemoteAccess`，这样本地调试和默认发行包不会被误用成可直接对外暴露的服务。

**Why this priority**: 这轮不做完整 authn/authz，但必须先把默认风险面收紧。

**Independent Test**: YAML swapper / loader tests
可以独立验证非 loopback `bindHost` 在未显式允许时被拒绝，并在 `allowRemoteAccess: true` 时通过。

**Acceptance Scenarios**:

1. **Given** `transport.http.bindHost` 为 `127.0.0.1`、`localhost` 或 `::1`，
   **When** 装配启动配置，
   **Then** 配置通过。
2. **Given** `transport.http.bindHost` 为 `0.0.0.0` 或其他非 loopback host，
   且 `transport.http.allowRemoteAccess` 为 `false`，
   **When** 装配启动配置，
   **Then** 启动前即返回配置错误，明确说明非 loopback 绑定需要显式允许远程访问。
3. **Given** `transport.http.bindHost` 为 `0.0.0.0` 或其他非 loopback host，
   且 `transport.http.allowRemoteAccess` 为 `true`，
   **When** 装配启动配置，
   **Then** 配置通过，并由 HTTP listener 绑定到该地址。

### Edge Cases

- `search_metadata` 合法的 `object_types` 仍允许 `index`；对不支持 index 的数据库仍保持现有 skip / unsupported 语义，不扩大成无关行为变化。
- `execute_query` 的 `max_rows <= 0` 仍表示“不做显式返回行数限制”。
- loopback 绑定下原有 `Origin` 校验继续保留；本轮只收紧非 loopback 配置入口，不改 session / protocol 行为。
- `allowRemoteAccess` 只表达产品意图，不提供认证、授权或 TLS。

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: MCP public metadata discovery surface MUST 以 resources 为唯一真源；
  已删除的 `list_*`、`describe_*`、`get_capabilities` tools MUST NOT 再出现在 README、quickstart 或 domain contract 中。
- **FR-002**: 文档 MUST 明确 service-level capability 与 database-level capability 通过 `resources/read` 获取。
- **FR-003**: `execute_query` 在实际结果行数超过 `max_rows` 时 MUST 返回 `truncated=true`。
- **FR-004**: `execute_query` 在实际结果行数未超过 `max_rows` 时 MUST 返回 `truncated=false`。
- **FR-005**: tool controller MUST 对 required string arguments 执行 presence + non-blank 校验，
  并对非法输入返回 `invalid_request`。
- **FR-006**: `search_metadata.object_types` MUST 只接受当前 public search surface 支持的
  `database`、`schema`、`table`、`view`、`column`、`index`；
  当调用方显式提供其他值时，系统 MUST 返回 `invalid_request`。
- **FR-007**: 合法 `object_types` 过滤语义 MUST 保持不变。
- **FR-008**: HTTP transport configuration MUST allow loopback `bindHost` values by default:
  `127.0.0.1`、`localhost`、`::1`。
- **FR-009**: 非 loopback `transport.http.bindHost` MUST 在 `transport.http.allowRemoteAccess` 不是 `true` 时于配置装配阶段失败；
  当该字段显式为 `true` 时，配置装配 MUST 允许非 loopback `bindHost`。
- **FR-010**: 本轮实现 MUST 只修改与上述四个 V0 问题直接相关的生产代码、测试代码、README 与 Speckit 文档。

### Assumptions

- 当前版本不恢复旧 metadata tools；resource-only 是确认过的对外方向。
- 当前版本优先确保默认安全边界明确，不在本轮引入新的 remote HTTP 放开开关。
- 本轮允许对 `specs/001-shardingsphere-mcp/` 下的 public contract 与 quickstart 做对齐更新。

## Non-Goals

- 不恢复 `list_databases`、`list_schemas`、`list_tables`、`list_views`、`list_columns`、`list_indexes`。
- 不恢复 `describe_table`、`describe_view`、`get_capabilities` 的 tool 形态。
- 不引入 OAuth、ACL、rate limiting、审计持久化等更完整的 production security 方案。
- 不补厚 metadata 模型，不改 capability 字段设计，不扩大到 prompts / subscriptions / remotes。

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: `rg -n "list_databases|list_schemas|list_tables|list_views|list_columns|list_indexes|describe_table|describe_view|get_capabilities" specs/001-shardingsphere-mcp/contracts/mcp-domain-contract.md specs/001-shardingsphere-mcp/quickstart.md mcp/README_ZH.md mcp/README.md`
  不再把这些已删除 tools 描述成 public tool surface。
- **SC-002**: `MCPJdbcStatementExecutorTest` 与 HTTP execute integration tests
  覆盖并通过 `truncated=true/false` 两条分支。
- **SC-003**: `MCPToolControllerTest` 覆盖并通过 required string blank 校验与非法 `object_types` 校验。
- **SC-004**: `YamlHttpTransportConfigurationSwapperTest` 与 `MCPConfigurationLoaderTest`
  覆盖并通过 loopback、未允许 remote 的 non-loopback、已允许 remote 的 non-loopback 分支。

## Implementation Notes

- public metadata discovery 已收口为 resource-only；README、quickstart 与 domain contract 不再声明已删除的 metadata tools。
- `search_metadata` 的 public `object_types` 已限制为 `database`、`schema`、`table`、`view`、`column`、`index`，并对超出范围的值返回 `invalid_request`。
- `execute_query` 通过 “多取一行” 修正了 `truncated` 判定，HTTP 与 core 测试都已覆盖超限分支。
- `transport.http.bindHost` 默认限制为 loopback host；非 loopback host 必须同时设置 `transport.http.allowRemoteAccess: true`，避免当前 V0 被误配成对外暴露服务。
