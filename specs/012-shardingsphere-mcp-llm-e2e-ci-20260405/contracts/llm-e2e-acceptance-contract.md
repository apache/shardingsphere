# Contract: ShardingSphere MCP Minimal LLM E2E Acceptance

## Shared Smoke Goal

一次通过的 LLM smoke run 必须证明：

1. 真实模型参与了决策
2. 真实模型通过 MCP 调用了 discovery tool
3. 真实模型通过 MCP 调用了 `execute_query`
4. 真实数据库结果被整合进最终 JSON

## Required Tool Coverage

通过标准要求 tool trace 至少包含：

- 一个 discovery tool
  - `list_databases`
  - `list_tables`
  - `describe_table`
  - `search_metadata`
  中至少一个
- 一个 `execute_query`

如果模型完全没有 discovery tool，
或者没有 `execute_query`，
必须判定为失败。

## Read-Only SQL Contract

第一阶段 smoke 只允许只读 SQL。

最小 canonical query 为：

```sql
SELECT COUNT(*) AS total_orders FROM orders
```

兼容性要求：

- 可以允许等价的只读单语句查询
- 不允许 `UPDATE`、`DELETE`、`INSERT`
- 不允许 DDL
- 不允许 DCL
- 不允许多语句输入

## Final Structured Answer Contract

模型最终必须返回 JSON。
第一阶段 canonical answer 形状如下：

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

约束如下：

- `database` 必须是 `orders`
- `schema` 必须是 `public`
- `table` 必须是 `orders`
- `totalOrders` 必须是 `2`
- `toolSequence` 必须包含 discovery tool 和 `execute_query`
- 允许 `toolSequence` 出现额外 discovery tool
- 不允许输出额外 prose 作为通过标准

## Failure Classification Contract

runner 至少要区分以下失败类型：

- `missing_required_tool_coverage`
- `unsafe_sql_attempted`
- `invalid_final_json`
- `unexpected_query_result`
- `model_service_unavailable`
- `mcp_runtime_unavailable`

## Artifact Contract

无论成功还是失败，每次 run 都必须输出：

- system prompt
- user prompt
- raw model output
- tool trace
- assertion report
- MCP runtime log

所有 artifact 必须位于唯一的 run 目录下，
不能覆盖其他 run 的结果。
