+++
title = "数据加密"
weight = 1
+++

数据加密 MCP 功能插件帮助 MCP 客户端把加密需求规划成 ShardingSphere-Proxy 可执行的 DDL、DistSQL、索引计划和校验步骤。
它不在 MCP Server 内实现加密算法，而是面向 ShardingSphere 逻辑库生成和执行加密规则变更。

## 前置条件

- 当前版本只支持连接 ShardingSphere-Proxy 暴露的逻辑库。
- `runtimeDatabases` 应指向 Proxy 逻辑库，而不是底层物理存储库。
- 直连真实数据库时，本功能不适用；真实数据库通常不识别 ShardingSphere 加密 DistSQL，也不能暴露 Proxy 中可见的加密算法插件和规则状态。
- 目标逻辑表和逻辑列应能通过 Proxy 暴露的 JDBC 元数据发现；这些信息不保证等同于底层物理库的完整元数据。

## 可调用能力

| 能力 | 怎么调用 | 什么时候用 |
| --- | --- | --- |
| `database_gateway_plan_encrypt_rule` | 通过 `tools/call` 调用。 | 用户提出创建、调整或删除加密规则需求时，用它生成 `plan_id`、DistSQL、校验步骤，以及适用场景下的 DDL 或索引建议。 |
| `database_gateway_apply_workflow` | 通过 `tools/call` 调用，并传入规划阶段返回的 `plan_id`。 | 先预览计划，再在审查后执行，或导出人工执行包。 |
| `database_gateway_validate_workflow` | 通过 `tools/call` 调用，并传入同一个 `plan_id`。 | 自动执行或人工执行完成后，校验规则状态、逻辑元数据和 SQL 可执行性。 |
| `shardingsphere://features/encrypt/algorithms` | 通过 `resources/read` 读取。 | 规划前查看 Proxy 当前可见的加密算法类型和参数要求。 |
| `shardingsphere://features/encrypt/databases/{database}/rules` | 填充 `{database}` 后通过 `resources/read` 读取。 | 规划修改前查看逻辑库已有加密规则。 |
| `shardingsphere://features/encrypt/databases/{database}/tables/{table}/rules` | 填充 `{database}` 和 `{table}` 后通过 `resources/read` 读取。 | 只关心单表加密规则，或需要保留同表其他列规则时读取。 |
| `plan_encrypt_rule` | 通过 `prompts/get` 获取提示。 | 客户端希望先引导模型读取表结构、算法和已有规则，再调用规划工具时使用。 |
| `plan_encrypt_rule` 补全 | 通过 `completion/complete` 获取候选值。 | 为 `database`、`schema`、`table`、`column`、`algorithm_type`、`assisted_query_algorithm_type`、`like_query_algorithm_type` 或 `plan_id` 补全。 |

## 最小输入

创建或修改加密规则时，规划工具主要使用以下输入：

| 参数 | 是否必填 | 作用 |
| --- | --- | --- |
| `database` | 必填 | ShardingSphere-Proxy 暴露的逻辑库名称。 |
| `table` | 必填 | 要配置加密规则的逻辑表。 |
| `column` | 必填 | 要配置加密规则的逻辑列。 |
| `schema` | 可选 | schema 或 namespace；多 schema 逻辑库建议填写。 |
| `natural_language_intent` | 推荐 | 描述是否需要可逆加密、等值查询或模糊查询；当未显式填写规则细节时，MCP 会用它推断规划意图。 |
| `operation_type` | 可选 | 规则操作类型；支持 `create`、`alter` 和 `drop`。不填写时由 MCP 根据自然语言和现有规则推断。 |
| `algorithm_type` | 可选 | 主加密算法类型；如果希望 MCP 基于可用算法给出建议，可以先不填。 |
| `primary_algorithm_properties` | 按算法必填 | 主加密算法参数，例如 AES 密钥。具体参数以算法资源返回值为准。 |
| `allow_index_ddl` | 可选 | 是否允许为辅助查询列生成物理索引计划。 |

删除加密规则时，至少提供：

- `database`
- `table`
- `column`
- `operation_type=drop`

## 规划加密规则

规划加密规则就是调用 `database_gateway_plan_encrypt_rule`。
它只生成可审查的计划，不直接修改数据库。

```json
{
  "jsonrpc": "2.0",
  "id": "encrypt-plan-1",
  "method": "tools/call",
  "params": {
    "name": "database_gateway_plan_encrypt_rule",
    "arguments": {
      "database": "<logic-database>",
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

- `*_cipher` 用于保存密文，是加密规则的默认派生列。
- 如果需要等值查询，会生成 `*_assisted_query`，并在允许索引 DDL 时生成相应索引计划。
- 如果需要模糊查询，会生成 `*_like_query`，用于支持 LIKE 查询场景。
- 如果默认列名冲突，系统会追加数字后缀，并把最终命名写回 `derived_column_plan`。
- 校验阶段只检查规则、逻辑元数据和生成产物，不替代人工审查真实物理表结构。

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
      "database": "<logic-database>",
      "table": "orders",
      "column": "status",
      "operation_type": "drop"
    }
  }
}
```

如果同一个表上还有其他加密列，MCP 会生成 `ALTER ENCRYPT RULE` 并保留这些同表规则。
只有目标表不再保留任何加密列时，MCP 才会生成 `DROP ENCRYPT RULE`。
删除加密规则只移除规则本身，不会恢复历史明文数据；不再需要的物理派生列或索引仍需人工清理。

## 限制

- 仅支持 ShardingSphere-Proxy 逻辑库。
- MCP 根据 Proxy 暴露的逻辑元数据生成派生列、索引和列类型建议；它不会直接检查每个物理库。执行前应结合真实物理库表结构审查生成的 DDL。
- 不处理已有数据迁移或回填。
- 不提供自动回滚。
- 规划器只接受普通未加引号的逻辑库、schema、表和列名，用于降低自动生成 SQL 的歧义；这不是 ShardingSphere SQL 能力限制。
