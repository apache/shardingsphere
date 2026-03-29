# Feature Specification: ShardingSphere MCP Tool Contract Abstraction Between Core and Bootstrap

**Feature Branch**: `[008-shardingsphere-mcp-tool-contract-abstraction]`  
**Created**: 2026-03-29  
**Status**: Implementation-aligned draft  
**Input**: User description:
"MCPToolInputSchemaFactory能否再做一层抽象，把创建tool的业务逻辑挪到core模块.
这个类只负责把传递过来的抽象对象转化成为MCP的SDK?先给我设计" and "认可，使用speckit做"

## Scope Statement

本 follow-up 不改变 `001-shardingsphere-mcp` 已固定的 MCP public tool surface，
只收敛 tool contract 的分层方式：

- 把 tool 的静态业务定义收口到 `mcp/core`
- 让 `mcp/bootstrap` 只负责把 core 抽象对象适配成 MCP Java SDK 所需的 `McpSchema.Tool`
  和 `McpSchema.JsonSchema`
- 消除当前 `MCPToolCatalog` 与 `MCPToolInputSchemaFactory` 分别维护 tool contract
  的双重事实来源
- 保持现有 metadata / capability / execute-query runtime 行为与对外参数契约不变

本特性的核心不是新增 tool，也不是改 `tools/call` 执行路径，
而是让 “tool 长什么样” 与 “tool 怎么被 SDK 暴露” 分属正确层级。

## User Scenarios & Testing *(mandatory)*

### User Story 1 - tool 静态契约在 core 中只有一份事实来源 (Priority: P1)

作为 MCP 维护者，我希望 tool 的名称、标题、描述、输入参数定义和 dispatch 类型
都由 `mcp/core` 统一定义，这样新增或修改 tool contract 时不需要在 core 与 bootstrap
之间做双份同步。

**Why this priority**: 当前 `MCPToolCatalog` 已经承载 tool registry 与参数归一化，
但 `MCPToolInputSchemaFactory` 仍用 `switch(toolName)` 单独维护 input schema，
已经出现 contract drift 风险。

**Independent Test**: 通过 core 单元测试可以一次性验证所有 supported tool
都有显式 descriptor，且不再依赖 bootstrap 的默认空 schema 分支。

**Acceptance Scenarios**:

1. **Given** 支持的 tool 集合已经在 core 中注册，**When** 某个 tool 的参数契约变更，
   **Then** 名称、标题、描述和输入参数定义都从同一个 core descriptor 获取。
2. **Given** `list_databases()` 与 `get_capabilities(database?)` 分别代表零参数和可选参数 tool，
   **When** contract 收口完成，**Then** 二者都拥有显式的 core 输入定义，
   而不是依赖 bootstrap fallback。
3. **Given** metadata tool 与 execute-query 仍需做请求归一化，
   **When** 实现完成，**Then** 归一化逻辑继续留在 core 或其相邻 core 逻辑中，
   以便与静态 contract 一起追踪。

---

### User Story 2 - bootstrap 只做 SDK 适配，不再包含 tool-specific schema 业务分支 (Priority: P1)

作为 transport 维护者，我希望 `MCPToolInputSchemaFactory` 只做 core 抽象对象到
MCP SDK schema 的转换，而不是继续知道 `list_tables`、`search_metadata`、
`execute_query` 等具体业务名字。

**Why this priority**: bootstrap 当前依赖 SDK 类型，本应是 transport adapter；
如果它继续维护 tool-specific schema 规则，就会把 domain contract 与 SDK 适配耦合在一起。

**Independent Test**: bootstrap 单元测试可直接验证 descriptor 到 SDK schema 的映射，
且 schema factory 不再按 `toolName` 分支。

**Acceptance Scenarios**:

1. **Given** core 已提供 descriptor 和输入定义，**When** bootstrap 创建
   `SyncToolSpecification`，**Then** input schema 由通用 adapter 递归转换生成，
   而不是通过 `switch(toolName)` 拼装。
2. **Given** 输入定义中包含 string、integer、array 等字段，
   **When** adapter 生成 SDK schema，**Then** 字段类型、描述、required 与字段顺序
   都与 core 定义一致。
3. **Given** 后续新增一个仍可被当前抽象模型表达的新 tool，
   **When** 只更新 core descriptor，**Then** bootstrap adapter 代码不需要增加新的
   tool-specific schema 分支。

---

### User Story 3 - public tool contract 与 runtime 行为保持零损失 (Priority: P1)

作为 MCP 客户端和运行方，我希望这轮重构只改变内部抽象边界，不改变 `tools/list`
暴露出来的 tool surface，也不改变 `tools/call` 的执行和错误语义。

**Why this priority**: 这次工作的价值来自“结构正确化”；只要 public contract 或 runtime
行为回退，它就不再是单纯的抽象收敛。

**Independent Test**: 现有 core / bootstrap 回归继续通过，并新增 descriptor / adapter
测试证明 `tools/list` 和 `tools/call` 对外一致。

**Acceptance Scenarios**:

1. **Given** 当前支持的 11 个 tools，**When** 重构完成，
   **Then** `getSupportedTools()` 返回集合与 title 生成结果保持不变。
2. **Given** `get_capabilities(database?)` 与 `execute_query(database, sql, max_rows?, timeout_ms?)`
   已是既有 public contract，**When** 客户端读取 `tools/list` 或调用 `tools/call`，
   **Then** 对外参数语义不发生变化。
3. **Given** `MCPToolCallHandler` 仍负责 session / exchange 绑定，
   **When** 本轮重构完成，**Then** tool dispatch 类型、执行路径和错误 surface 保持当前行为。

### Edge Cases

- 不允许 `mcp/core` 为了表达 descriptor 而直接依赖 MCP Java SDK 类型。
- 不允许继续依赖 “unknown tool -> empty object schema” 这种默认分支来表达已支持 tool。
- `list_databases()` 这类零参数 tool 需要显式输入定义，而不是隐式空处理。
- `get_capabilities(database?)` 这类可选参数 tool 需要显式描述 optional field。
- `search_metadata.object_types` 这类数组字段需要通过抽象模型稳定表达，而不是在 bootstrap 中硬编码。
- 本轮不把 `MCPToolCallHandler`、`McpSyncServerExchange` 或 `CallToolResult` 下沉到 core。

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: 系统 MUST 在 `mcp/core` 中定义 repository-owned 的 tool descriptor 抽象，
  用于表达 tool 名称、标题、描述、dispatch 类型和输入参数定义。
- **FR-002**: 每个 supported tool MUST 都拥有显式 descriptor；
  不允许仅靠 bootstrap 默认分支表达已支持 tool。
- **FR-003**: `MCPToolCatalog` MUST 成为静态 tool contract 的单一事实来源，
  同时继续作为 tool registry 和参数归一化入口。
- **FR-004**: `MCPToolInputSchemaFactory` MUST 只负责把 core 输入定义转换成
  MCP SDK 的 schema 对象，不再包含基于 `toolName` 的业务分支。
- **FR-005**: `mcp/core` MUST NOT 引入 `io.modelcontextprotocol.*` 依赖或类型。
- **FR-006**: 当前 `001-shardingsphere-mcp/contracts/mcp-domain-contract.md`
  已定义的 public tool set、参数命名与参数可选性 MUST 保持不变。
- **FR-007**: `list_databases()` 与 `get_capabilities(database?)` MUST 都拥有显式的
  core 输入定义；bootstrap MUST NOT 再依赖默认空 schema 处理它们。
- **FR-008**: metadata tool request normalization 与 execute-query request normalization
  MUST 继续留在 core 或紧邻 core 的逻辑中，以保持 contract 与归一化规则可追踪。
- **FR-009**: 本轮重构 MUST 保持 `MCPToolCallHandler` 在 bootstrap 中，
  不把 transport exchange / session binding 下沉到 core。
- **FR-010**: bootstrap 生成的 `tools/list` 结果 MUST 直接反映 core descriptor 中的输入定义。
- **FR-011**: 当新增的 tool 可由当前抽象模型表达时，系统 SHOULD 只需要修改 core descriptor
  与 core 归一化逻辑，而不需要新增 bootstrap schema 分支。
- **FR-012**: 本特性 MUST 提供 core descriptor 覆盖测试、bootstrap adapter 映射测试
  和至少一条 public listing 兼容验证路径。

### Key Entities *(include if feature involves data)*

- **ToolDescriptor**: core 中的单个 tool 静态契约定义，包含名称、标题、描述、dispatch 类型和输入定义。
- **ToolDispatchKind**: tool 的执行分类，如 metadata、capability、execution。
- **ToolInputDefinition**: 根对象级别的输入定义，维护有序字段列表和 additional-properties 语义。
- **ToolFieldDefinition**: 单个输入字段定义，包含字段名、字段值定义和 required 标记。
- **ToolValueDefinition**: 字段值定义，当前覆盖 scalar 与 array 两类值形态。
- **SDK tool schema adapter**: bootstrap 中把 `ToolInputDefinition` 转成 `McpSchema.JsonSchema`
  的纯适配器。

### Assumptions

- `MCPToolCatalog` 继续作为现有 tool registry 入口，不另外新增并行 registry。
- public tool contract 继续继承 `001-shardingsphere-mcp` 的 domain contract。
- `MCPToolCallHandler` 本轮保持 transport-facing 角色，不做跨模块下沉。
- 当前 tool 输入模型以 object root + scalar / array 字段为主，本轮不引入完整通用 JSON Schema 引擎。

## Non-Goals

- 不新增 public tools，不修改 tool 名称、title 规则、参数命名或错误码 surface。
- 不把 `MCPToolCallHandler`、`SyncToolSpecification` 或 `McpSchema.CallToolResult`
  等 transport / SDK 类型移动到 core。
- 不把这轮抽象收敛扩展成新的 SPI 机制或新的 tool execution pipeline。
- 不为了未来潜在场景预埋完整通用 JSON Schema 功能，而超出当前 MCP tool 输入需要。

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% 的当前 supported tools 在 core 中拥有显式 descriptor，
  且没有任何一个依赖 bootstrap 默认 fallback schema。
- **SC-002**: bootstrap 中用于创建 input schema 的 tool-name-specific `switch` / `if`
  分支数量降为 `0`。
- **SC-003**: `get_capabilities` 在 `tools/list` 中显式暴露可选 `database` 参数定义。
- **SC-004**: 100% 的 descriptor 到 SDK schema 映射由独立 adapter 测试覆盖。
- **SC-005**: `mcp/core` 继续不依赖 MCP SDK，而 `mcp/bootstrap` 继续作为唯一 SDK 适配层。
