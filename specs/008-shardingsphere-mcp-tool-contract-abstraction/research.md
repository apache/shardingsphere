# Research: ShardingSphere MCP Tool Contract Abstraction Between Core and Bootstrap

## Decision 1: 把 static tool contract 收口到 core，而不是继续分散在 core 与 bootstrap

- **Decision**: tool 的名称、标题、描述、dispatch 类型和输入参数定义统一下沉到 `mcp/core`，
  `mcp/bootstrap` 不再维护第二份 tool-specific schema 规则。
- **Rationale**:
  - `001` 已经把 MCP public tool surface 定义成 domain contract；
    它本质上属于 core 语义，而不是 transport 语义。
  - 当前 `MCPToolCatalog` 已经承载 tool registry 与 request normalization，
    再让 bootstrap 单独维护 input schema，会形成双重事实来源。
  - 当前 `get_capabilities(database?)` 已经暴露出 contract drift：
    tool 存在且支持可选 `database`，但 bootstrap schema 未显式描述该参数。
- **Alternatives considered**:
  - 维持当前分层: rejected，因为新增或修改 tool contract 仍需双份同步。
  - 把全部 tool 逻辑都上提到 bootstrap: rejected，因为会进一步弱化 core 的 domain ownership。

## Decision 2: 继续保持 `mcp/core` 与 MCP SDK 解耦

- **Decision**: `mcp/core` 继续只依赖 repository-owned DTO 和逻辑模型，
  不直接引用 `io.modelcontextprotocol.*` 类型。
- **Rationale**:
  - `001-shardingsphere-mcp/research.md` 已明确拒绝 “让 `mcp/core` 依赖 SDK types” 的方向。
  - 这次工作是 contract abstraction，不是把 domain contract 重写成 SDK wrapper。
  - core 独立于 SDK 之后，descriptor 也更容易被其他 transport 或文档生成路径复用。
- **Alternatives considered**:
  - 在 core 直接返回 `McpSchema.Tool` / `McpSchema.JsonSchema`: rejected，
    因为会把 domain contract 直接耦合到外部协议类。

## Decision 3: 使用最小充分的 descriptor 模型，而不是引入完整通用 JSON Schema 引擎

- **Decision**: core 采用最小充分抽象：
  `ToolDescriptor`、`ToolDispatchKind`、`ToolInputDefinition`、`ToolFieldDefinition`
  和 `ToolValueDefinition`。
- **Rationale**:
  - 当前 MCP tool 输入只需要 object root、ordered fields、required 标记、
    scalar 类型和 array 类型。
  - 直接引入完整通用 JSON Schema builder 会超出当前需求，增加阅读与维护成本。
  - 使用显式类型对象仍比在 core 中继续拼 `Map<String, Object>` 更可测试、更可读。
- **Alternatives considered**:
  - 直接在 core 中继续拼原始 `Map<String, Object>`: rejected，因为抽象层级不清晰。
  - 引入完整通用 JSON Schema DSL: rejected，因为当前是 over-design。

## Decision 4: bootstrap adapter 必须对具体 tool 名称无感知

- **Decision**: `MCPToolInputSchemaFactory` 只接收 core 输入定义并做递归适配，
  不再通过 `toolName` 判断要生成什么 schema。
- **Rationale**:
  - bootstrap 的职责是 SDK 适配，不是 domain contract 拼装。
  - 一旦 adapter 仍知道具体 tool 名称，contract drift 风险就没有真正消除。
  - tool-name-agnostic adapter 更符合 “同一抽象层级” 与 “最小必要职责”。
- **Alternatives considered**:
  - 保留 `switch(toolName)` 但把大部分分支搬到 helper: rejected，
    因为本质上仍是 bootstrap 维护业务规则。

## Decision 5: 本轮不移动 `MCPToolCallHandler`

- **Decision**: `MCPToolCallHandler` 继续留在 `mcp/bootstrap`，只把 static tool contract
  收口到 core。
- **Rationale**:
  - `MCPToolCallHandler` 直接依赖 `McpSyncServerExchange`、`CallToolResult`
    等 SDK / transport 类型。
  - 当前用户的核心诉求是 “schema factory 只做 adapter”，不是重写整个 call pipeline。
  - 如果把 handler 也一起下沉，会扩大改动范围并模糊本轮最小目标。
- **Alternatives considered**:
  - 同轮把 call handler 一并抽象到 core: rejected，因为会牵涉 session、payload、
    error mapping 和 transport exchange 的边界重划分。

## Decision 6: 所有 supported tool 都必须显式定义，包括零参数和可选参数 tool

- **Decision**: `list_databases()`、`get_capabilities(database?)`、分页 metadata tools、
  `execute_query` 等都必须显式拥有 core descriptor 和输入定义。
- **Rationale**:
  - 零参数 tool 与可选参数 tool 最容易被误判成 “可以靠默认处理”；
    但一旦依赖默认分支，就会再次引入隐式 contract。
  - 显式建模后，`tools/list` 与 core contract 就能一一对应，
    review 时也更容易发现遗漏。
- **Alternatives considered**:
  - 继续让零参数 tool 走空 schema fallback: rejected，因为它让 “已支持 tool”
    与 “未知 tool” 共用同一条表达路径。
