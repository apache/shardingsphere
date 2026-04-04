# Feature Specification: ShardingSphere MCP Error Centralization and Protocol Error Conversion

**Feature Branch**: `[no-branch-switch-requested]`  
**Created**: 2026-04-04  
**Status**: Design draft  
**Input**: User description:
"我不希望使用MetadataQueryResult.error()去返回错误。我希望使用标准的异常去抛出错误。然后在一个统一的地方去catch异常，再转换成为MCPErrorResponse。
举一反三，所有的通过协议去处理错误的地儿，必须都统一在一个集中的地方。转换成为MCPErrorResponse。
其他的地方一旦出了错误，直接抛异常。" and
"用speckit好好设计一下，但是不允许切换分支。可以为这些确定的异常创建独立的类
只改和这些错误处理相关的地方，其他的无关的代码一律不许进行任何小修小改"

## Scope Statement

本 follow-up 只处理 `mcp/core` 与紧邻 `mcp/bootstrap` 测试中的协议错误收敛问题：

- 业务层、查询层、分发层、执行层一律改为抛标准异常，不再返回失败结果对象
- 所有协议层错误转换统一收敛到一个 repository-owned 的集中组件
- 该集中组件只负责把异常转换为 `MCPErrorResponse`
- `execute_query` 失败路径也纳入同一收敛规则，不再通过 `ExecuteQueryResponse.error(...)` 返回协议错误
- 可以为已确定的错误族和高频固定场景创建独立异常类
- 本轮只允许修改与错误处理相关的生产代码、测试代码和 Speckit 文档

本特性的核心不是新增 public tool / resource，也不是顺手做架构清理，
而是把 “内部抛异常” 与 “协议层统一转 `MCPErrorResponse`” 这条边界一次性拉直。

## Problem Statement

当前 MCP 模块在多个层级同时处理错误，已经出现职责混杂：

- `MetadataQueryResult.error(...)`、`ToolDispatchResult.error(...)`、
  `ExecuteQueryResponse.error(...)` 把失败当成返回值
- `MCPToolPayloadResult.error(...)`、`MCPResourceController`、
  `DatabaseCapabilitiesHandler`、`MCPResourceResponseFactory`
  在多个地方直接构造 `MCPErrorResponse`
- `execute_query` 失败时目前仍走专用错误 envelope，而不是统一的 `MCPErrorResponse`
- metadata、tool、resource、execute 四条链路各有自己的失败表达方式，review 与测试都难以追踪

这导致三个直接问题：

1. 业务代码感知了协议错误模型，层级倒挂  
2. 相同错误语义在不同入口重复映射，容易漂移  
3. 测试必须同时覆盖“抛异常”和“错误结果对象”两套风格，维护成本高

## User Scenarios & Testing *(mandatory)*

### User Story 1 - 内部链路只抛异常，不构造协议错误 (Priority: P1)

作为 MCP 维护者，我希望 metadata、tool dispatch 和 SQL execute 的内部代码一旦失败就直接抛异常，
这样协议模型不会继续渗透到业务层。

**Why this priority**: 这是本轮改造的根本目标；如果内部链路仍保留失败结果对象，
集中转换就只完成了一半。

**Independent Test**: core 单元测试可直接验证 metadata 查询、分页校验、
事务控制和 JDBC 执行失败时抛出确定的异常类型，而不是返回失败结果对象。

**Acceptance Scenarios**:

1. **Given** `list_tables` 缺少 `schema`，**When** metadata tool dispatch 执行，
   **Then** 内部必须抛出 `invalid_request` 语义的异常，而不是返回 `ToolDispatchResult.error(...)`。
2. **Given** 当前数据库不支持 index resource，**When** metadata 查询执行，
   **Then** 内部必须抛出 `unsupported` 语义的异常，而不是返回 `MetadataQueryResult.error(...)`。
3. **Given** JDBC query 超时或事务状态非法，**When** execute path 执行，
   **Then** 内部必须抛出对应异常，而不是返回 `ExecuteQueryResponse.error(...)`。

---

### User Story 2 - 协议入口统一 catch 并转换为 MCPErrorResponse (Priority: P1)

作为 MCP 客户端和维护者，我希望所有通过协议暴露的错误最终都统一映射为 `MCPErrorResponse`，
并且只在集中入口进行转换，这样 public error surface 稳定且可追踪。

**Why this priority**: 用户明确要求 “所有通过协议处理错误的地儿，必须统一在一个集中的地方转换”。

**Independent Test**: tool 和 resource 入口测试可独立验证 unsupported tool、
unsupported resource URI、database capability 不存在、query failed 等场景都返回同一错误 payload 形状。

**Acceptance Scenarios**:

1. **Given** 请求一个不存在的 tool，**When** `MCPToolPayloadResolver.resolve(...)` 处理，
   **Then** 集中错误转换必须返回 `MCPErrorResponse` payload，且 `isError=true`。
2. **Given** 请求一个不存在的 resource URI，**When** `MCPResourceController.handle(...)` 处理，
   **Then** 集中错误转换必须返回 `MCPErrorResponse` payload。
3. **Given** `execute_query` 失败，**When** tool 协议层处理，
   **Then** 最终返回也必须是 `MCPErrorResponse`，而不是 `ExecuteQueryResponse` 的错误 envelope。

---

### User Story 3 - 固定错误语义有稳定异常类型，映射规则可回归 (Priority: P1)

作为 MCP 维护者，我希望 `invalid_request`、`not_found`、`unsupported`、
`timeout`、`transaction_state_error`、`query_failed`、`unavailable`
这些确定错误语义有稳定的异常类型和集中映射规则，
这样 review 和测试可以直接按类型追踪行为。

**Why this priority**: 用户已经明确允许为确定错误创建独立类；
如果仍依赖裸 `IllegalStateException` / `IllegalArgumentException` 猜测语义，
改造仍然不稳定。

**Independent Test**: converter 单元测试和各入口回归测试可验证不同异常类型稳定映射到预期 `error_code`。

**Acceptance Scenarios**:

1. **Given** `InvalidPageTokenException`，**When** 协议入口集中转换，
   **Then** payload 中的 `error_code` 必须是 `invalid_request`。
2. **Given** `DatabaseCapabilityNotFoundException`，**When** 协议入口集中转换，
   **Then** payload 中的 `error_code` 必须是 `not_found`。
3. **Given** 未知运行时异常，**When** 协议入口集中转换，
   **Then** payload 中的 `error_code` 必须退化为 `unavailable`，且不泄漏栈信息。

### Edge Cases

- `search_metadata` 当前对“某种 object type 在该数据库不支持”采用跳过语义，不能因为统一异常而把整次搜索错误化。
- `execute_query` 的成功 payload 形状必须保持现状；只允许清理失败路径。
- `MCPToolCallHandler` 与 `MCPResourceSpecificationFactory` 继续消费 `Map<String, Object>` payload，不引入新的 transport model。
- 不允许为了这轮错误收敛去顺手调整命名、排序、循环写法、注释或无关测试。
- 不允许新增 public error code，也不允许改变现有 `MCPErrorResponse` 的字段形状。

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: 系统 MUST 在 `mcp/core` 中提供一个 repository-owned 的集中错误转换组件，
  负责把异常转换为 `MCPErrorResponse`。
- **FR-002**: 生产代码中除该集中组件外，其他运行时错误路径 MUST NOT 直接构造 `MCPErrorResponse`。
- **FR-003**: `MCPToolPayloadResolver.resolve(...)` MUST 作为 tool 协议入口捕获内部异常，
  并委托集中错误转换组件生成错误 payload。
- **FR-004**: `MCPResourceController.handle(...)` MUST 作为 resource 协议入口捕获内部异常，
  并委托同一集中错误转换组件生成错误 payload。
- **FR-005**: metadata、tool dispatch、resource handler、execute 链路中的内部失败
  MUST 通过抛异常表达，而不是返回失败结果对象。
- **FR-006**: `ExecuteQueryResponse` MUST 收敛为 success-only 响应模型；
  `execute_query` 失败时 MUST 由协议入口直接返回 `MCPErrorResponse`。
- **FR-007**: `MetadataQueryResult`、`ToolDispatchResult`、`MCPToolPayloadResult`
  SHOULD 从失败承载模型收敛为 success-only 模型，或从失败主路径中移除。
- **FR-008**: 系统 MUST 提供稳定的异常族，至少覆盖：
  `invalid_request`、`not_found`、`unsupported`、`timeout`、
  `transaction_state_error`、`query_failed`、`unavailable`。
- **FR-009**: 系统 SHOULD 为高频固定错误场景提供命名异常类，
  至少包括 unsupported tool、unsupported resource URI、
  database capability 不存在、invalid page token 这些场景。
- **FR-010**: 集中错误转换 MUST 保持当前稳定 public message 语义；
  对于本轮已存在的确定消息文本，除非设计明确需要，消息内容不得漂移。
- **FR-011**: `search_metadata` MUST 保持当前“跳过不支持的 object type，继续返回其他命中项”的语义，
  不得依赖抛异常再吞掉异常来实现。
- **FR-012**: 本轮实现 MUST 只修改与错误处理相关的生产代码、测试代码和本规格文档，
  不得夹带无关小修小改。
- **FR-013**: 本特性 MUST 提供 core 层 dedicated tests，
  覆盖异常抛出、集中转换、payload 兼容和 `execute_query` 失败收敛。
- **FR-014**: 本特性 MUST 保持 `MCPErrorResponse` payload 形状为
  `error_code` + `message`，并保持工具、资源两条协议 surface 的可观测兼容性。

### Key Entities *(include if feature involves data)*

- **MCPProtocolException**: 内部抛出的抽象运行时异常基类，定义协议错误语义，但不负责生成 payload。
- **Code-Family Exceptions**: `MCPInvalidRequestException`、`MCPNotFoundException`、
  `MCPUnsupportedException`、`MCPTimeoutException`、`MCPTransactionStateException`、
  `MCPQueryFailedException`、`MCPUnavailableException` 这些按 `MCPErrorCode` 对齐的异常族。
- **Named Leaf Exceptions**: 针对固定场景的命名异常，例如 `UnsupportedToolException`、
  `UnsupportedResourceUriException`、`DatabaseCapabilityNotFoundException`、
  `InvalidPageTokenException`。
- **MCPProtocolErrorConverter**: 集中错误转换组件，负责把异常映射为 `MCPError`
  和 `MCPErrorResponse` payload。
- **Success-Only Result Models**: metadata、tool dispatch、execute 成功场景下保留的轻量结果对象，
  不再承载失败语义。

### Assumptions

- `MCPToolPayloadResolver` 与 `MCPResourceController` 继续分别作为 tool / resource 的总入口。
- `MCPToolCallHandler` 的 `isError` 标记与 text content 组装行为继续保留。
- `MCPErrorCode.CONFLICT` 在当前错误收敛中不是新增目标；
  本轮只覆盖现有稳定使用的错误族和 `unavailable` 兜底。
- `MCPSessionNotExistedException` 这类现有异常可以被纳入新的异常体系，或在转换器中做明确映射，
  但不得继续依赖模糊的 `IllegalStateException` 猜测。

## Non-Goals

- 不新增 public tool、resource 或 error code。
- 不改变 resource URI、tool name、HTTP status contract 或 STDIO API surface。
- 不在本轮做 URI dispatch、session serialization、transport simplification 等其他内部重构。
- 不以“错误收敛”为理由修改无关命名、提取方法、调整集合类型或整理测试结构。
- 不把本轮扩展成全模块异常统一框架；范围限定在 MCP 协议错误主链路。

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: `mcp/core` 生产代码中的 `MetadataQueryResult.error(...)`、
  `ToolDispatchResult.error(...)`、`ExecuteQueryResponse.error(...)`
  调用数降为 `0`。
- **SC-002**: `mcp/core` 运行时主链路中除集中转换组件外，`new MCPErrorResponse(...)`
  的生产代码调用点降为 `0`。
- **SC-003**: tool、resource、`execute_query` 的失败结果 100% 统一映射为 `MCPErrorResponse` payload。
- **SC-004**: `search_metadata` 的 unsupported-object-type 兼容语义保持零漂移。
- **SC-005**: 相关 scoped tests 能证明异常类型、错误码映射和 payload 形状都稳定回归。
