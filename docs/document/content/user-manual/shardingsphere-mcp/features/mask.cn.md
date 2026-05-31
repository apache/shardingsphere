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

## 通过自然语言使用

用户在 MCP 客户端中描述脱敏目标即可。
模型会读取表结构、可用脱敏算法和已有规则，再生成可审查的脱敏规则计划。
用户不需要手工拼接工具参数或 JSON-RPC 请求。

示例：

- 检查 `<logic-database>.orders.phone` 当前是否已有脱敏规则。
- 为 `<logic-database>.orders.phone` 规划手机号脱敏，保留前 3 后 4，先预览不要执行。
- 为逻辑表 `order detail` 的 `Phone Number` 列规划手机号脱敏，保留对象名大小写。
- 调整刚才的计划，把替换字符改成 `*`。
- 确认并执行刚才的脱敏规则计划，然后校验结果。

模型或客户端会把这些任务拆成读取资源、规划规则、预览执行和校验结果。
用户需要审查计划中的 DistSQL 和副作用范围，再批准有副作用的执行。

## 规则规划

规则规划是数据脱敏插件的第一阶段。
模型通常先读取算法和已有规则资源，再调用规划工具生成 `plan_id` 和可审查计划。
规划工具不会直接修改数据库；后续预览、执行和校验由[插件工作流](../plugin-workflow/)阶段工具完成。

### 规划输入

规划工具的公共输入如下：

| 参数 | 是否必填 | 作用 |
| --- | --- | --- |
| `database` | 必填 | ShardingSphere-Proxy 暴露的逻辑库名称。 |
| `table` | 必填 | 要配置脱敏规则的逻辑表。 |
| `column` | 必填 | 要配置脱敏规则的逻辑列。 |
| `schema` | 可选 | schema 或 namespace；多 schema 逻辑库建议填写。 |
| `natural_language_intent` | 推荐 | 描述脱敏目标，例如手机号保留位数或替换字符；当未显式填写规则细节时，MCP 会用它推断规划意图。 |
| `operation_type` | 可选 | 规则操作类型；支持 `create`、`alter` 和 `drop`。不填写时由 MCP 根据自然语言和已有规则推断。 |
| `algorithm_type` | 可选 | 脱敏算法类型；如果希望 MCP 基于可用算法给出建议，可以先不填。 |
| `primary_algorithm_properties` | 按算法必填 | 脱敏算法参数，例如保留位数和替换字符。具体参数以算法资源返回值为准。 |

`database`、`schema`、`table` 和 `column` 可以使用普通标识符，也可以使用反引号、双引号或方括号包裹的定界标识符。
MCP 会在规划时保留大小写、空格和其他特殊字符；生成 DistSQL 时使用反引号引用标识符。
生成校验 SQL 时使用目标数据库方言的引用字符，例如 MySQL/MariaDB 使用反引号，PostgreSQL/openGauss 使用双引号。
标识符不能包含 NUL、回车或换行等无法生成可审查 SQL 的字符。

不同操作的输入重点如下：

| 操作 | 输入重点 | 规划结果 |
| --- | --- | --- |
| `create` | 提供目标列、脱敏意图、算法类型和算法参数；如果希望 MCP 推荐算法，可以先只提供自然语言意图。 | 生成新增规则 DistSQL。 |
| `alter` | 提供目标列和要调整的算法或算法参数。 | 生成保留同表其他列规则的修改规则 DistSQL。 |
| `drop` | 至少提供 `database`、`table`、`column` 和 `operation_type=drop`。 | 如果同表还有其他脱敏列，生成保留其他列的 `ALTER MASK RULE`；如果目标表不再保留任何脱敏列，生成 `DROP MASK RULE`。 |

### 规划结果

典型规划结果包括：

- `plan_id`，用于后续预览、执行和校验。
- `status`，取值通常为 `planned` 或 `clarifying`。
- `distsql_artifacts`，包含 `CREATE/ALTER/DROP MASK RULE`。
- `ddl_artifacts`，通常为空。
- `index_plan`，通常为空。

如果返回 `clarifying`，继续使用同一个 `plan_id` 补齐缺失字段。
敏感字段不会明文回显，应通过密钥管理系统、受保护环境变量或运维控制通道取得后再继续。

## 执行与校验

规划工具返回 `plan_id` 后，模型或客户端再使用插件工作流阶段工具处理执行和校验。
建议先预览，确认 DistSQL 和副作用范围后再执行。

| 阶段 | 用户表达 | 模型或客户端动作 |
| --- | --- | --- |
| 预览 | “先预览刚才的脱敏规则计划，不要执行。” | 使用 `database_gateway_apply_workflow` 和 `execution_mode=preview` 生成预览结果。 |
| 执行 | “确认执行刚才的计划。” | 用户审查后，使用 `database_gateway_apply_workflow` 和 `execution_mode=review-then-execute` 执行。 |
| 人工执行 | “导出人工执行包，不要自动执行。” | 使用 `database_gateway_apply_workflow` 和 `execution_mode=manual-only` 返回人工执行包。 |
| 校验 | “校验刚才的脱敏规则是否生效。” | 使用 `database_gateway_validate_workflow` 校验规则状态、逻辑元数据和 SQL 可执行性。 |

校验重点：

- `rule_validation`
- `logical_metadata_validation`
- `sql_executability_validation`

插件工作流的状态、执行模式和敏感输入处理方式见[插件工作流](../plugin-workflow/)。

## MCP 能力参考

本节用于自研客户端、协议调试或理解模型背后的 MCP 调用。
普通用户通常只需要使用自然语言描述任务。

| MCP 能力 | 类型 | 调用入口 | 适用阶段 | 结果 |
| --- | --- | --- | --- | --- |
| `database_gateway_plan_mask_rule` | 工具 | `tools/call` | 规划创建、修改或删除脱敏规则。 | 返回 `plan_id`、规划状态、DistSQL 和校验步骤。 |
| `database_gateway_apply_workflow` | 阶段工具 | `tools/call`，传入 `plan_id`。 | 规划完成后预览、执行或导出人工执行包。 | 返回预览产物、执行结果或人工执行包。 |
| `database_gateway_validate_workflow` | 阶段工具 | `tools/call`，传入同一个 `plan_id`。 | 自动执行或人工执行完成后校验结果。 | 返回规则状态、逻辑元数据和 SQL 可执行性校验结果。 |
| `shardingsphere://features/mask/algorithms` | 资源 | `resources/read` | 规划前查看 Proxy 当前可见的脱敏算法。 | 返回算法类型和参数要求。 |
| `shardingsphere://features/mask/databases/{database}/rules` | 资源模板 | 填充 `{database}` 后通过 `resources/read` 读取。 | 规划修改前查看逻辑库已有脱敏规则。 | 返回逻辑库级脱敏规则。 |
| `shardingsphere://features/mask/databases/{database}/tables/{table}/rules` | 资源模板 | 填充 `{database}` 和 `{table}` 后通过 `resources/read` 读取。 | 只关心单表规则，或需要保留同表其他列规则时读取。 | 返回表级脱敏规则。 |
| `plan_mask_rule` | 提示 | `prompts/get` | 客户端希望引导模型先读取表结构、算法和已有规则时使用。 | 返回规划脱敏规则的模型提示。 |
| `plan_mask_rule` 补全 | 补全目标 | `completion/complete` | 客户端填写规划参数时使用。 | 返回 `database`、`schema`、`table`、`column`、算法类型或 `plan_id` 候选值。 |

## 限制

### 支持范围

- 仅支持 ShardingSphere-Proxy 逻辑库。
- 直连真实数据库时，本功能不适用。

### MCP 插件边界

- MCP Server 不实现脱敏算法，也不替代用户判断脱敏策略是否满足业务合规要求。
- 规划结果是可审查的变更计划；是否执行仍需要用户确认。
- 删除脱敏规则只移除规则本身；后续通过 Proxy 查询该列时不再应用该脱敏规则。

### Proxy 可见元数据边界

- 逻辑列和规则校验以 Proxy 可见信息为准。
- 直连真实数据库只能执行普通 SQL，不代表脱敏规则状态。

### ShardingSphere 功能边界

- 不提供自动回滚。

### 规划器输入限制

- 标识符不能包含 NUL、回车或换行等无法生成可审查 SQL 的字符。
