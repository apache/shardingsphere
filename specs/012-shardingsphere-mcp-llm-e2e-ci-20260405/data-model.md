# Data Model: ShardingSphere MCP Minimal LLM-Driven E2E Validation

## Core Runtime Entities

### LLME2EScenario

- **Purpose**: 描述一次 LLM smoke 的固定验收 contract。
- **Fields**:
  - `scenarioId`
  - `systemPrompt`
  - `userPrompt`
  - `expectedStructuredAnswer`
  - `allowedToolNames`
  - `requiredToolNames`
- **Validation rules**:
  - 第一阶段固定要求 `list_tables -> describe_table -> execute_query` 三个 tool。
  - 第一阶段只允许只读 SQL。
  - 一个 scenario 只验证一个稳定 demo contract。

### LLME2EConfiguration

- **Purpose**: 描述本地模型服务的最小接入参数。
- **Fields**:
  - `enabled`
  - `baseUrl`
  - `modelName`
  - `apiKey`
  - `readyTimeoutSeconds`
  - `requestTimeout`
  - `maxTurns`
  - `artifactRoot`
  - `runId`
- **Validation rules**:
  - 默认 profile 必须适配 GitHub-hosted CPU runner。
  - 默认 profile 固定为 `Ollama + qwen3:1.7b`。
  - 默认 profile 不依赖外部闭源模型 secret。
  - `response_format=json_object` 必须可被默认 provider 正常接受。

### MCPRuntimeTarget

- **Purpose**: 表示本次 smoke 使用的 MCP runtime 目标。
- **Fields**:
  - `runtimeKind`
  - `endpoint`
  - `logicalDatabase`
  - `schema`
  - `table`
  - `expectedRowCount`
  - `requiresDocker`
- **Validation rules**:
  - 第一阶段固定绑定两条场景：
    - H2: `logic_db.public.orders`
    - MySQL: `logic_db.<detected-schema>.orders`
  - `expectedRowCount` 稳定为 `2`。
  - runtime 必须来自真实 JDBC-backed runtime，而不是测试 fixture。

### ToolTraceRecord

- **Purpose**: 记录模型与 MCP 之间的每次工具交互。
- **Fields**:
  - `sequence`
  - `toolName`
  - `arguments`
  - `structuredContent`
- **Validation rules**:
  - 顺序必须保留。
  - 必须能区分 discovery tool 与 `execute_query`。
  - `structuredContent` 必须保留足够的诊断信息，用于重建最终断言。

### StructuredFinalAnswer

- **Purpose**: 模型最终返回给 runner 的结构化 JSON。
- **Fields**:
  - `database`
  - `schema`
  - `table`
  - `query`
  - `totalOrders`
  - `toolSequence`
- **Validation rules**:
  - 第一阶段必须严格匹配目标库表。
  - `totalOrders` 必须等于 `2`。
  - `toolSequence` 必须与观察到的 `list_tables -> describe_table -> execute_query` 顺序一致。

### LLME2EArtifactBundle

- **Purpose**: 汇总一次 run 的全部复盘材料。
- **Fields**:
  - `scenarioId`
  - `systemPrompt`
  - `userPrompt`
  - `finalAnswerJson`
  - `rawModelOutputs`
  - `toolTrace`
  - `assertionReport`
  - `mcpInteractionLogLines`
- **Validation rules**:
  - 每次 run 写入的 artifact 目录必须唯一。
  - 失败和成功都必须生成 assertion report。
  - 即使模型失败，也必须保留 raw output。

## Relationships

- `LLME2EScenario` 约束 `LLME2EConfiguration` 的请求格式和 `StructuredFinalAnswer` 的预期形状。
- `MCPRuntimeTarget` 为 `ToolTraceRecord` 和 `StructuredFinalAnswer` 提供对照基线。
- `LLME2EArtifactBundle` 收集 prompt、trace、final answer 和日志，作为 workflow artifact 上传。

## Canonical Flow

```text
Start production bootstrap runtime harness
  -> wait for MCP endpoint ready
  -> load LLME2EScenario + LLME2EConfiguration
  -> send prompts to model
  -> model decides MCP tool calls
  -> runner executes tool calls and appends tool results back to model conversation
  -> model returns StructuredFinalAnswer
  -> runner validates trace + SQL safety + final JSON
  -> write LLME2EArtifactBundle under target/llm-e2e/<run-id>/
```

## Invariants

- discovery tool 必须先于最终 `execute_query` 断言出现。
- `execute_query` 只能是只读 SQL。
- 最终 JSON 不能绕过 tool trace 独立成立。
- artifact 输出目录必须隔离，不能复用固定共享目录。

## First-Stage Expected Answer

第一阶段 H2 canonical answer 形状：

```json
{
  "database": "logic_db",
  "schema": "public",
  "table": "orders",
  "query": "SELECT COUNT(*) AS total_orders FROM orders",
  "totalOrders": 2,
  "toolSequence": [
    "list_tables",
    "describe_table",
    "execute_query"
  ]
}
```

第一阶段 MySQL canonical answer 允许 `schema` 为空字符串，
或使用 JDBC metadata 暴露出的 schema 值。

不允许 toolSequence 缺少 `execute_query`，
也不允许偏离默认 smoke 要求的调用顺序。
