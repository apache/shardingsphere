# Data Model: ShardingSphere MCP Minimal LLM-Driven E2E Validation

## Core Runtime Entities

### LLME2EScenario

- **Purpose**: 描述一次 LLM smoke 的固定验收 contract。
- **Fields**:
  - `scenarioId`
  - `systemPromptResource`
  - `userPromptResource`
  - `requiredTools`
  - `forbiddenSqlPatterns`
  - `expectedStructuredAnswer`
  - `maxTurns`
  - `maxToolCalls`
- **Validation rules**:
  - 第一阶段固定要求至少一个 discovery tool 和一个 `execute_query`。
  - 第一阶段只允许只读 SQL。
  - 一个 scenario 只验证一个稳定 demo contract。

### ModelServiceProfile

- **Purpose**: 描述本地模型服务的最小接入参数。
- **Fields**:
  - `baseUrl`
  - `modelName`
  - `apiKeyPlaceholder`
  - `startupWaitTimeout`
  - `requestTimeout`
  - `responseFormat`
  - `thinkingMode`
- **Validation rules**:
  - 默认 profile 必须适配 GitHub-hosted CPU runner。
  - 默认 profile 不依赖外部闭源模型 secret。
  - `responseFormat` 必须支持结构化 JSON 或等价的 deterministic final answer contract。

### MCPDemoRuntimeTarget

- **Purpose**: 表示本次 smoke 使用的 MCP runtime 目标。
- **Fields**:
  - `endpoint`
  - `logicalDatabase`
  - `schema`
  - `table`
  - `expectedRowCount`
  - `packagedConfigPath`
- **Validation rules**:
  - 第一阶段固定绑定 `orders.public.orders`。
  - `expectedRowCount` 稳定为 `2`。
  - runtime 必须来自打包 distribution，而不是测试 fixture。

### ToolTraceRecord

- **Purpose**: 记录模型与 MCP 之间的每次工具交互。
- **Fields**:
  - `sequence`
  - `toolName`
  - `arguments`
  - `responseDigest`
  - `timestamp`
  - `sessionId`
- **Validation rules**:
  - 顺序必须保留。
  - 必须能区分 discovery tool 与 `execute_query`。
  - response 只保留摘要时，也必须保留足够的诊断信息。

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
  - `toolSequence` 必须包含 discovery tool 和 `execute_query`。

### LLME2EArtifactBundle

- **Purpose**: 汇总一次 run 的全部复盘材料。
- **Fields**:
  - `runId`
  - `artifactDirectory`
  - `systemPrompt`
  - `userPrompt`
  - `rawModelOutput`
  - `toolTrace`
  - `assertionReport`
  - `mcpLogPath`
  - `environmentSummary`
- **Validation rules**:
  - 每次 run 的 `artifactDirectory` 必须唯一。
  - 失败和成功都必须生成 assertion report。
  - 即使模型失败，也必须保留 raw output。

## Relationships

- `LLME2EScenario` 约束 `ModelServiceProfile` 的请求格式和 `StructuredFinalAnswer` 的预期形状。
- `MCPDemoRuntimeTarget` 为 `ToolTraceRecord` 和 `StructuredFinalAnswer` 提供对照基线。
- `LLME2EArtifactBundle` 收集 prompt、trace、final answer 和日志，作为 workflow artifact 上传。

## Canonical Flow

```text
Start packaged MCP distribution
  -> wait for MCP endpoint ready
  -> load LLME2EScenario + ModelServiceProfile
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

第一阶段建议的 canonical answer 形状：

```json
{
  "database": "orders",
  "schema": "public",
  "table": "orders",
  "query": "SELECT COUNT(*) AS total_orders FROM orders",
  "totalOrders": 2,
  "toolSequence": [
    "list_databases",
    "list_tables",
    "execute_query"
  ]
}
```

允许 toolSequence 中出现额外 discovery tool，
但不允许缺少 `execute_query` 或完全没有 discovery tool。
