# Contract: ShardingSphere MCP LLM Usability Acceptance

## Purpose

定义 ShardingSphere MCP “模型是否用得舒服” 的最小验收合同：

- 场景包范围
- 指标定义
- 阈值预算
- 回归判定方式

## Suite Profiles

### 1. Minimal Read-Only Baseline

最小稳定回归包，目标是低成本、高可重复。

必须包含 12 个场景：

1. `resource_service_capability_h2`
2. `resource_table_shape_h2`
3. `resource_database_capability_mysql`
4. `tool_list_tables_h2`
5. `tool_describe_table_h2`
6. `tool_list_tables_mysql`
7. `mixed_discover_then_count_h2`
8. `mixed_discover_then_count_mysql`
9. `mixed_search_then_describe_h2`
10. `recovery_invalid_schema_without_database`
11. `recovery_unsafe_write_sql_rejected`
12. `recovery_invalid_tool_arguments_then_fix`

### 2. Extended Boundary Analysis

扩展包用于分析 resource / tool 边界混淆和分页、unsupported 等边缘路径。

第一阶段只定义 contract，不要求立即进入默认 CI。

## Scenario Rules

### Resource-Only Tasks

- 目标：确认模型能优先用 resource 建上下文
- 允许的主要动作：
  - `resources/list`
  - `resources/read`
- 禁止：
  - 用 `execute_query` 替代纯结构信息探索
- 成功条件：
  - 命中预期 resource
  - 最终答案与 capability / metadata 内容一致

### Tool-Only Metadata Tasks

- 目标：确认模型能正确选择 metadata tool
- 允许的主要动作：
  - `list_tables`
  - `describe_table`
  - `search_metadata`
- 成功条件：
  - 首个有效 tool 选择符合场景意图
  - 参数合法
  - 最终答案忠实

### Mixed Discovery + Query Tasks

- 目标：确认模型能在合理 discovery 后进入 `execute_query`
- 成功条件：
  - 有 discovery 动作
  - 有一次合法 `execute_query`
  - 最终结构化答案正确

### Failure-Recovery Tasks

- 目标：确认模型在错误后可恢复，或在 unsafe 场景下被正确阻止
- 成功条件：
  - 错误被正确分类
  - 如果场景允许恢复，则模型能恢复到正确路径
  - 如果场景不允许执行，则必须停在安全拒绝

## Metric Definitions

### `task_success_rate`

- 定义：`通过的场景数 / 总场景数`

### `first_correct_action_rate`

- 定义：`首次 MCP 动作即符合场景意图的场景数 / 需要 MCP 动作的总场景数`

### `invalid_call_rate`

- 定义：`无效动作数 / 总动作数`
- 无效动作包括：
  - 非法参数
  - 禁止能力
  - 错误入口
  - 不满足场景 contract 的动作

### `average_round_trips`

- 定义：`总 MCP 动作数 / 总场景数`

### `query_answer_fidelity`

- 定义：`query 场景中最终结构化答案正确的场景数 / query 场景总数`

### `boundary_confusion_rate`

- 定义：`被判定为 boundary_confusion 的场景数 / 总场景数`
- `boundary_confusion` 典型包括：
  - 该走 resource 却先走无关 tool
  - 该走 tool 却长时间只读无关 resource
  - 在多个相近 metadata tool 之间明显抖动

### `resource_hit_rate`

- 定义：`resource-required 场景中命中预期 resource 的场景数 / resource-required 场景总数`

### `recovery_rate`

- 定义：`出现首轮错误但最终恢复成功的场景数 / 出现首轮错误的总场景数`

## Minimal Baseline Thresholds

`minimal-readonly-baseline` 首轮目标阈值：

- `task_success_rate >= 0.85`
- `first_correct_action_rate >= 0.70`
- `invalid_call_rate <= 0.10`
- `average_round_trips <= 3.50`
- `query_answer_fidelity >= 0.90`
- `boundary_confusion_rate <= 0.15`
- `resource_hit_rate >= 0.80`
- `recovery_rate >= 0.60`
- `unsafe_query_execution_count = 0`

## Regression Budget

candidate run 相比 baseline run 出现以下任一情况，即判定为 regression：

- `task_success_rate` 下降超过 `0.05`
- `first_correct_action_rate` 下降超过 `0.08`
- `invalid_call_rate` 上升超过 `0.05`
- `average_round_trips` 上升超过 `0.50`
- `query_answer_fidelity` 下降超过 `0.05`
- `boundary_confusion_rate` 上升超过 `0.05`

## Required Outputs

每次 suite 执行必须产出：

- `scorecard.json`
- `summary.md`
- `scenario-results.json`
- `comparison.json`（仅在 compare 模式）
- 完整 trace / raw model output artifact

## Failure Classification

最少支持以下失败类型：

- `boundary_confusion`
- `invalid_arguments`
- `unexpected_tool_or_resource`
- `unsafe_query_attempt`
- `final_answer_mismatch`
- `max_turns_exceeded`
- `environment_failure`

## Non-Goals

- 不把这份 contract 用作模型智力排行
- 不把成本、token 和 latency 作为第一阶段阻塞阈值
- 不要求第一阶段覆盖只读之外的业务语义
