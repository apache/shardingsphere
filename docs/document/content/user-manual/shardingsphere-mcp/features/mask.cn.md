+++
title = "数据脱敏"
weight = 2
+++

数据脱敏 MCP 功能插件帮助 MCP 客户端把脱敏需求规划成 ShardingSphere-Proxy 可执行的 DistSQL 和校验步骤。
脱敏规则直接作用于逻辑列，不生成加密功能使用的物理派生列。

## 前置条件

- 当前版本只支持连接 ShardingSphere-Proxy 暴露的逻辑库。
- `runtimeDatabases` 应指向 Proxy 逻辑库，而不是底层物理存储库。
- 直连真实数据库时，本功能不适用；真实数据库通常不识别 ShardingSphere 脱敏 DistSQL，也不能暴露 Proxy 中可见的脱敏算法插件和规则状态。
- 目标逻辑表和逻辑列应能通过 Proxy 暴露的 JDBC 元数据发现；这些信息不保证等同于底层物理库的完整元数据。

## 可调用能力

| 能力 | 怎么调用 | 什么时候用 |
| --- | --- | --- |
| `database_gateway_plan_mask_rule` | 通过 `tools/call` 调用。 | 用户提出创建或调整脱敏规则需求时，用它生成 `plan_id`、DistSQL 和校验步骤。 |
| `database_gateway_apply_workflow` | 通过 `tools/call` 调用，并传入规划阶段返回的 `plan_id`。 | 先预览计划，再在审查后执行，或导出人工执行包。 |
| `database_gateway_validate_workflow` | 通过 `tools/call` 调用，并传入同一个 `plan_id`。 | 自动执行或人工执行完成后，校验规则状态、逻辑元数据和 SQL 可执行性。 |
| `shardingsphere://features/mask/algorithms` | 通过 `resources/read` 读取。 | 规划前查看 Proxy 当前可见的脱敏算法类型和参数要求。 |
| `shardingsphere://features/mask/databases/{database}/rules` | 填充 `{database}` 后通过 `resources/read` 读取。 | 规划修改前查看逻辑库已有脱敏规则。 |
| `shardingsphere://features/mask/databases/{database}/tables/{table}/rules` | 填充 `{database}` 和 `{table}` 后通过 `resources/read` 读取。 | 只关心单表脱敏规则，或需要保留同表其他列规则时读取。 |
| `plan_mask_rule` | 通过 `prompts/get` 获取提示。 | 客户端希望先引导模型读取表结构、算法和已有规则，再调用规划工具时使用。 |
| `plan_mask_rule` 补全 | 通过 `completion/complete` 获取候选值。 | 为 `database`、`schema`、`table`、`column`、`algorithm_type` 或 `plan_id` 补全。 |

## 最小输入

创建或修改脱敏规则时，规划工具主要使用以下输入：

| 参数 | 是否必填 | 作用 |
| --- | --- | --- |
| `database` | 必填 | ShardingSphere-Proxy 暴露的逻辑库名称。 |
| `table` | 必填 | 要配置脱敏规则的逻辑表。 |
| `column` | 必填 | 要配置脱敏规则的逻辑列。 |
| `schema` | 可选 | schema 或 namespace；多 schema 逻辑库建议填写。 |
| `natural_language_intent` | 推荐 | 描述脱敏目标，例如手机号保留位数或替换字符；当未显式填写规则细节时，MCP 会用它推断规划意图。 |
| `operation_type` | 可选 | 规则操作类型；当前文档只说明 `create` 和 `alter`。不填写时由 MCP 根据自然语言和现有规则推断。 |
| `algorithm_type` | 可选 | 脱敏算法类型；如果希望 MCP 基于可用算法给出建议，可以先不填。 |
| `primary_algorithm_properties` | 按算法必填 | 脱敏算法参数，例如保留位数和替换字符。具体参数以算法资源返回值为准。 |

## 规划脱敏规则

规划脱敏规则就是调用 `database_gateway_plan_mask_rule`。
它只生成可审查的计划，不直接修改数据库。

```json
{
  "jsonrpc": "2.0",
  "id": "mask-plan-1",
  "method": "tools/call",
  "params": {
    "name": "database_gateway_plan_mask_rule",
    "arguments": {
      "database": "<logic-database>",
      "table": "orders",
      "column": "phone",
      "natural_language_intent": "把 phone 当作手机号做脱敏，保留前3后4",
      "algorithm_type": "KEEP_FIRST_N_LAST_M",
      "primary_algorithm_properties": {
        "first-n": "3",
        "last-m": "4",
        "replace-char": "*"
      }
    }
  }
}
```

典型结果：

- 返回 `plan_id`。
- `status` 为 `planned` 或 `clarifying`。
- `distsql_artifacts` 包含 `CREATE/ALTER MASK RULE`。
- `ddl_artifacts` 通常为空。
- `index_plan` 通常为空。

如果自然语言没有说清算法或缺少算法属性，MCP 会返回 `clarifying`。
此时应继续使用同一个 `plan_id` 补齐 `clarification_questions` 中要求的字段。

## 执行与校验

规划工具返回 `plan_id` 后，再使用通用工作流工具处理执行和校验。

执行前先预览：

```json
{
  "name": "database_gateway_apply_workflow",
  "arguments": {
    "plan_id": "${PLAN_ID}",
    "execution_mode": "preview"
  }
}
```

确认变更产物后执行：

```json
{
  "name": "database_gateway_apply_workflow",
  "arguments": {
    "plan_id": "${PLAN_ID}",
    "execution_mode": "review-then-execute"
  }
}
```

校验：

```json
{
  "name": "database_gateway_validate_workflow",
  "arguments": {
    "plan_id": "${PLAN_ID}"
  }
}
```

校验重点：

- `rule_validation`
- `logical_metadata_validation`
- `sql_executability_validation`

## 限制

- 仅支持 ShardingSphere-Proxy 逻辑库。
- 逻辑列和规则校验以 Proxy 可见信息为准；直连真实数据库只能执行普通 SQL，不代表脱敏规则状态。
- 规划器只接受普通未加引号的逻辑库、schema、表和列名，用于降低自动生成 SQL 的歧义；这不是 ShardingSphere SQL 能力限制。
