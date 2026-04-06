# Data Model: ShardingSphere MCP LLM Usability Evaluation

## 1. `LLMUsabilityEvaluationSuite`

表示一整套可执行评估合同。

### Fields

- `suiteId`: suite 唯一标识，例如 `minimal-readonly-baseline`
- `suiteVersion`: suite 版本
- `profile`: 执行 profile，例如 `minimal` 或 `extended`
- `scenarios`: scenario 列表
- `regressionBudget`: 回归预算
- `outputFormat`: 结果输出形状，至少包含 JSON 和 Markdown

### Relationships

- 一个 suite 包含多个 `LLMUsabilityScenario`
- 一个 suite 产出一个 `LLMUsabilityScorecard`

## 2. `LLMUsabilityScenario`

表示单个评估任务。

### Fields

- `scenarioId`: 场景唯一标识
- `dimension`: `resource`、`tool`、`query`、`mixed`、`recovery`
- `runtimeKind`: `h2` 或 `mysql`
- `systemPrompt`: 系统提示
- `userPrompt`: 用户提示
- `requiredResources`: 必须命中的 resource URI 或 URI pattern
- `requiredTools`: 必须调用的 tool 名称
- `allowedTools`: 允许调用的 tool 集合
- `forbiddenTools`: 禁止调用的 tool 集合
- `expectedAnswer`: 最终结构化答案
- `maxTurns`: 场景允许的最大 turn 数
- `classificationRules`: 失败分类规则

### Relationships

- 一个 scenario 产出一个 `LLMUsabilityScenarioResult`
- 一个 scenario 在执行时会生成一个 `LLMUsabilityTrace`

### Mapping to Existing Assets

- 可以复用或包装现有 `LLME2EScenario`
- `expectedAnswer` 可以复用 `LLMStructuredAnswer`

## 3. `LLMUsabilityActionRecord`

表示一次关键交互动作。

### Fields

- `turnIndex`: 第几轮
- `actionKind`: `resource_list`、`resource_read`、`tool_call`、`final_answer`
- `targetName`: tool name 或 resource URI
- `argumentsSummary`: 参数摘要
- `valid`: 是否有效
- `latencyMillis`: 该动作耗时
- `resultSummary`: 返回摘要
- `classificationHint`: 如 `boundary_confusion_candidate`

### Relationships

- 多个 action record 组成一条 `LLMUsabilityTrace`

### Mapping to Existing Assets

- `tool_call` 路径可复用现有 `MCPToolTraceRecord`
- 需要补齐 resource 读取和 validity 分类

## 4. `LLMUsabilityTrace`

表示一个 scenario 的完整执行轨迹。

### Fields

- `scenarioId`
- `actions`
- `rawModelMessages`
- `finalAnswerRaw`
- `environmentNotes`

### Relationships

- 一个 trace 对应一个 scenario result

## 5. `LLMUsabilityScenarioResult`

表示单个场景的最终判定。

### Fields

- `scenarioId`
- `success`
- `failureType`
- `message`
- `firstCorrectAction`
- `invalidCallCount`
- `roundTripCount`
- `resourceHit`
- `queryAnswerFidelity`
- `recoveredAfterError`
- `trace`

### Derived Properties

- `boundaryConfusion`: 是否属于入口或能力边界混淆
- `degradedSuccess`: 是否最终成功但过程明显绕路

## 6. `LLMUsabilityDimensionScore`

表示某个维度的聚合结果。

### Fields

- `dimensionName`
- `scenarioCount`
- `successRate`
- `firstCorrectActionRate`
- `invalidCallRate`
- `averageRoundTrips`
- `notes`

### Dimensions

- `resource`
- `tool`
- `query`
- `recovery`

## 7. `LLMUsabilityScorecard`

表示整个 suite 的最终评估结果。

### Fields

- `suiteId`
- `runId`
- `overallSuccessRate`
- `firstCorrectActionRate`
- `invalidCallRate`
- `averageRoundTrips`
- `queryAnswerFidelity`
- `boundaryConfusionRate`
- `resourceHitRate`
- `recoveryRate`
- `dimensionScores`
- `scenarioResults`
- `summary`

### Output Shapes

- JSON：供后续自动比较
- Markdown：供 reviewer 快速阅读

## 8. `LLMUsabilityRegressionBudget`

表示候选结果相对基线的允许波动。

### Fields

- `minTaskSuccessRate`
- `minFirstCorrectActionRate`
- `maxInvalidCallRate`
- `maxAverageRoundTrips`
- `minQueryAnswerFidelity`
- `maxBoundaryConfusionRate`
- `minResourceHitRate`
- `minRecoveryRate`

### Purpose

- 把 “退步了多少算不可接受” 明确写死在 contract 中

## 9. `LLMUsabilityComparisonResult`

表示 candidate 与 baseline 的比较结果。

### Fields

- `baselineRunId`
- `candidateRunId`
- `regressionDetected`
- `regressedMetrics`
- `improvedMetrics`
- `stableMetrics`
- `decision`

### Purpose

- 让 reviewer 不必手工比较两份原始日志
