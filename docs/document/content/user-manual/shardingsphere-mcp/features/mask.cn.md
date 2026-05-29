+++
title = "Mask"
weight = 2
+++

Mask MCP 功能插件帮助 MCP 客户端把脱敏需求规划成 ShardingSphere-Proxy 可执行的 DistSQL 和校验步骤。
脱敏规则直接作用于逻辑列，不生成加密功能使用的物理派生列。

## 前置条件

- 当前版本只支持连接 ShardingSphere-Proxy 暴露的逻辑库。
- `runtimeDatabases` 应指向 Proxy 逻辑库，而不是底层物理存储库。
- 目标逻辑表和逻辑列应能通过 JDBC 元数据发现。

## 可调用能力

规划工具：

- `database_gateway_plan_mask_rule`

通用工作流工具：

- `database_gateway_apply_workflow`
- `database_gateway_validate_workflow`

资源：

- `shardingsphere://features/mask/algorithms`
- `shardingsphere://features/mask/databases/{database}/rules`
- `shardingsphere://features/mask/databases/{database}/tables/{table}/rules`

## 最小输入

创建或修改脱敏规则时，推荐至少提供：

- `database`
- `table`
- `column`
- `natural_language_intent`，或显式 `operation_type=create|alter`
- `algorithm_type`，如果希望 MCP 推荐算法可以先省略
- `primary_algorithm_properties`
- `schema`，多 schema 逻辑库建议显式提供

删除脱敏规则时，最小输入是：

- `database`
- `table`
- `column`
- `operation_type=drop`

## 规划脱敏规则

```json
{
  "jsonrpc": "2.0",
  "id": "mask-plan-1",
  "method": "tools/call",
  "params": {
    "name": "database_gateway_plan_mask_rule",
    "arguments": {
      "database": "logic_db",
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

## 删除脱敏规则

```json
{
  "jsonrpc": "2.0",
  "id": "mask-drop-1",
  "method": "tools/call",
  "params": {
    "name": "database_gateway_plan_mask_rule",
    "arguments": {
      "database": "logic_db",
      "table": "orders",
      "column": "phone",
      "operation_type": "drop"
    }
  }
}
```

如果同一张表仍有其他脱敏列，MCP 会生成保留同表其他规则的 `ALTER MASK RULE`。
只有目标表不再剩余任何 mask 列时，才会生成 `DROP MASK RULE`。

## 限制

- 仅支持 ShardingSphere-Proxy 逻辑库。
- 支持 `create`、`alter`、`drop`。
- 不生成物理派生列。
- 不提供自动回滚能力。
- 规划输入只接受标准未加引号的逻辑标识符。
