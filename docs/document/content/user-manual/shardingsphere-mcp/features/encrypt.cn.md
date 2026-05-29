+++
title = "Encrypt"
weight = 1
+++

Encrypt MCP 功能插件帮助 MCP 客户端把加密需求规划成 ShardingSphere-Proxy 可执行的 DDL、DistSQL、索引计划和校验步骤。
它不在 MCP Server 内实现加密算法，而是面向 ShardingSphere 逻辑库生成和执行加密规则变更。

## 前置条件

- 当前版本只支持连接 ShardingSphere-Proxy 暴露的逻辑库。
- `runtimeDatabases` 应指向 Proxy 逻辑库，而不是底层物理存储库。
- 目标逻辑表和逻辑列应能通过 JDBC 元数据发现。
- 当前功能插件不处理存量数据迁移或回填。

## 可调用能力

规划工具：

- `database_gateway_plan_encrypt_rule`

通用工作流工具：

- `database_gateway_apply_workflow`
- `database_gateway_validate_workflow`

资源：

- `shardingsphere://features/encrypt/algorithms`
- `shardingsphere://features/encrypt/databases/{database}/rules`
- `shardingsphere://features/encrypt/databases/{database}/tables/{table}/rules`

## 最小输入

创建或修改加密规则时，推荐至少提供：

- `database`
- `table`
- `column`
- `natural_language_intent`，或显式 `operation_type=create|alter`
- `algorithm_type`，如果希望 MCP 推荐算法可以先省略
- `primary_algorithm_properties`
- `schema`，多 schema 逻辑库建议显式提供

删除加密规则时，最小输入是：

- `database`
- `table`
- `column`
- `operation_type=drop`

## 规划加密规则

```json
{
  "jsonrpc": "2.0",
  "id": "encrypt-plan-1",
  "method": "tools/call",
  "params": {
    "name": "database_gateway_plan_encrypt_rule",
    "arguments": {
      "database": "logic_db",
      "table": "orders",
      "column": "status",
      "natural_language_intent": "给 status 做可逆加密，需要等值查询，不需要模糊查询",
      "algorithm_type": "AES",
      "primary_algorithm_properties": {
        "aes-key-value": "${AES_KEY_VALUE}"
      }
    }
  }
}
```

典型结果：

- 返回 `plan_id`。
- `status` 为 `planned` 或 `clarifying`。
- `derived_column_plan` 说明派生列命名。
- `ddl_artifacts` 可能包含物理列 DDL。
- `distsql_artifacts` 包含 `CREATE/ALTER ENCRYPT RULE`。
- `index_plan` 可能包含辅助查询索引。

如果返回 `clarifying`，继续使用同一个 `plan_id` 补齐缺失字段。
敏感字段不会明文回显，应通过密钥管理系统、受保护环境变量或运维控制通道取得后再继续。

## 派生列规则

- 默认生成 `*_cipher` 派生列。
- 如果需要等值查询，会生成 `*_assisted_query` 及相应索引计划。
- 如果需要模糊查询，会生成 `*_like_query`。
- 如果默认列名冲突，系统会追加数字后缀，并把最终命名写回 `derived_column_plan`。

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

- `ddl_validation`
- `rule_validation`
- `logical_metadata_validation`
- `sql_executability_validation`

## 删除加密规则

```json
{
  "jsonrpc": "2.0",
  "id": "encrypt-drop-1",
  "method": "tools/call",
  "params": {
    "name": "database_gateway_plan_encrypt_rule",
    "arguments": {
      "database": "logic_db",
      "table": "orders",
      "column": "status",
      "operation_type": "drop"
    }
  }
}
```

如果同一张表仍有其他加密列，MCP 会生成保留同表其他规则的 `ALTER ENCRYPT RULE`。
只有目标表不再剩余任何 encrypt 列时，才会生成 `DROP ENCRYPT RULE`。

## 限制

- 仅支持 ShardingSphere-Proxy 逻辑库。
- 支持 `create`、`alter`、`drop`。
- `drop` 只删除规则，不自动清理物理派生列和索引。
- 不处理存量数据迁移或回填。
- 不提供自动回滚能力。
- 规划输入只接受标准未加引号的逻辑标识符。
