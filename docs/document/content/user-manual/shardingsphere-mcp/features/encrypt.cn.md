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

## 通过自然语言使用

用户在 MCP 客户端中描述加密目标即可。
模型会读取表结构、可用加密算法和已有规则，再生成可审查的加密规则计划。
用户不需要手工拼接工具参数或 JSON-RPC 请求。

示例：

- 检查 `<logic-database>.orders.status` 当前是否已有加密规则。
- 为 `<logic-database>.orders.status` 规划可逆加密，需要支持等值查询，先预览不要执行。
- 为逻辑表 `order detail` 的 `Phone Number` 列规划可逆加密，保留对象名大小写。
- 使用 AES 算法继续刚才的计划，密钥通过受保护渠道提供。
- 确认并执行刚才的加密规则计划，然后校验结果。

模型或客户端会把这些任务拆成读取资源、规划规则、预览执行和校验结果。
用户需要审查计划中的 DistSQL、DDL、索引建议和副作用范围，再批准有副作用的执行。

## 规则规划

规则规划是数据加密插件的第一阶段。
模型通常先读取算法和已有规则资源，再调用规划工具生成 `plan_id` 和可审查计划。
规划工具不会直接修改数据库；后续预览、执行和校验由[插件工作流](../plugin-workflow/)阶段工具完成。

### 规划输入

规划工具的公共输入如下：

| 参数 | 是否必填 | 作用 |
| --- | --- | --- |
| `database` | 必填 | ShardingSphere-Proxy 暴露的逻辑库名称。 |
| `table` | 必填 | 要配置加密规则的逻辑表。 |
| `column` | 必填 | 要配置加密规则的逻辑列。 |
| `schema` | 可选 | schema 或 namespace；多 schema 逻辑库建议填写。 |
| `natural_language_intent` | 推荐 | 描述是否需要可逆加密、等值查询或模糊查询；当未显式填写规则细节时，MCP 会用它推断规划意图。 |
| `operation_type` | 可选 | 规则操作类型；支持 `create`、`alter` 和 `drop`。不填写时由 MCP 根据自然语言和已有规则推断。 |
| `algorithm_type` | 可选 | 主加密算法类型；如果希望 MCP 基于可用算法给出建议，可以先不填。 |
| `primary_algorithm_properties` | 按算法必填 | 主加密算法参数，例如 AES 密钥。具体参数以算法资源返回值为准。 |
| `allow_index_ddl` | 可选 | 是否允许为辅助查询列生成物理索引计划。 |

`database`、`schema`、`table` 和 `column` 可以使用普通标识符，也可以使用反引号、双引号或方括号包裹的定界标识符。
MCP 会在规划时保留普通标识符的原始写法，并保留显式定界标识符；当未定界标识符会与 DistSQL 语法冲突时，生成的 DistSQL 会添加反引号。
生成物理 DDL、索引计划和校验 SQL 时，仅当未定界标识符会与 SQL 语法冲突时使用目标数据库方言的引用字符，例如 MySQL/MariaDB 使用反引号，PostgreSQL/openGauss 使用双引号。
标识符内容不能包含反引号、NUL、回车或换行等无法生成可审查 SQL 的字符。

不同操作的输入重点如下：

| 操作 | 输入重点 | 规划结果 |
| --- | --- | --- |
| `create` | 提供目标列、加密意图、算法类型和算法参数；如果希望 MCP 推荐算法，可以先只提供自然语言意图。 | 生成新增规则 DistSQL，并在需要时生成物理派生列 DDL 和索引建议。 |
| `alter` | 提供目标列和要调整的算法、查询能力或算法参数。 | 生成保留同表其他列规则的修改规则 DistSQL，并按需更新 DDL 或索引建议。 |
| `drop` | 至少提供 `database`、`table`、`column` 和 `operation_type=drop`。 | 如果同表还有其他加密列，生成保留其他列的 `ALTER ENCRYPT RULE`；如果目标表不再保留任何加密列，生成 `DROP ENCRYPT RULE`。 |

### 规划结果

典型规划结果包括：

- `plan_id`，用于后续预览、执行和校验。
- `status`，取值通常为 `planned` 或 `clarifying`。
- `derived_column_plan`，说明派生列命名。
- `ddl_artifacts`，可能包含物理列 DDL。
- `distsql_artifacts`，包含 `CREATE/ALTER/DROP ENCRYPT RULE`。
- `index_plan`，可能包含辅助查询索引建议。

如果返回 `clarifying`，继续使用同一个 `plan_id` 补齐缺失字段。
敏感字段不会明文回显，应通过密钥管理系统、受保护环境变量或运维控制通道取得后再继续。

## 派生列与索引计划

派生列与索引计划是规则规划的输出内容，不是需要用户单独调用的能力。
加密规则可能需要物理派生列来保存密文或支持查询。
MCP 会根据逻辑列、用户意图和已有规则生成派生列建议，并把最终命名写入 `derived_column_plan`。

- `*_cipher` 用于保存密文，是加密规则的默认派生列。
- 如果需要等值查询，会生成 `*_assisted_query`，并在允许索引 DDL 时生成相应索引计划。
- 如果需要模糊查询，会生成 `*_like_query`，用于支持 LIKE 查询场景。
- 如果默认列名冲突，系统会追加数字后缀，并把最终命名写回 `derived_column_plan`。

## 执行与校验

规划工具返回 `plan_id` 后，模型或客户端再使用插件工作流阶段工具处理执行和校验。
建议先预览，确认 DistSQL、DDL、索引计划和副作用范围后再执行。

| 阶段 | 用户表达 | 模型或客户端动作 |
| --- | --- | --- |
| 预览 | “先预览刚才的加密规则计划，不要执行。” | 使用 `database_gateway_apply_workflow` 和 `execution_mode=preview` 生成预览结果。 |
| 执行 | “确认执行刚才的计划。” | 用户审查后，使用 `database_gateway_apply_workflow` 和 `execution_mode=review-then-execute` 执行。 |
| 人工执行 | “导出人工执行包，不要自动执行。” | 使用 `database_gateway_apply_workflow` 和 `execution_mode=manual-only` 返回人工执行包。 |
| 校验 | “校验刚才的加密规则是否生效。” | 使用 `database_gateway_validate_workflow` 校验规则状态、逻辑元数据和 SQL 可执行性。 |

校验重点：

- `ddl_validation`
- `rule_validation`
- `logical_metadata_validation`
- `sql_executability_validation`

插件工作流的状态、执行模式和敏感输入处理方式见[插件工作流](../plugin-workflow/)。

## MCP 能力参考

本节用于自研客户端、协议调试或理解模型背后的 MCP 调用。
普通用户通常只需要使用自然语言描述任务。

| MCP 能力 | 类型 | 调用入口 | 适用阶段 | 结果 |
| --- | --- | --- | --- | --- |
| `database_gateway_plan_encrypt_rule` | 工具 | `tools/call` | 规划创建、修改或删除加密规则。 | 返回 `plan_id`、规划状态、DistSQL、校验步骤，以及适用场景下的 DDL、派生列和索引建议。 |
| `database_gateway_apply_workflow` | 阶段工具 | `tools/call`，传入 `plan_id`。 | 规划完成后预览、执行或导出人工执行包。 | 返回预览产物、执行结果或人工执行包。 |
| `database_gateway_validate_workflow` | 阶段工具 | `tools/call`，传入同一个 `plan_id`。 | 自动执行或人工执行完成后校验结果。 | 返回规则状态、逻辑元数据和 SQL 可执行性校验结果。 |
| `shardingsphere://features/encrypt/algorithms` | 资源 | `resources/read` | 规划前查看 Proxy 当前可见的加密算法。 | 返回算法类型和参数要求。 |
| `shardingsphere://features/encrypt/databases/{database}/rules` | 资源模板 | 填充 `{database}` 后通过 `resources/read` 读取。 | 规划修改前查看逻辑库已有加密规则。 | 返回逻辑库级加密规则。 |
| `shardingsphere://features/encrypt/databases/{database}/tables/{table}/rules` | 资源模板 | 填充 `{database}` 和 `{table}` 后通过 `resources/read` 读取。 | 只关心单表规则，或需要保留同表其他列规则时读取。 | 返回表级加密规则。 |
| `plan_encrypt_rule` | 提示 | `prompts/get` | 客户端希望引导模型先读取表结构、算法和已有规则时使用。 | 返回规划加密规则的模型提示。 |
| `plan_encrypt_rule` 补全 | 补全目标 | `completion/complete` | 客户端填写规划参数时使用。 | 返回 `database`、`schema`、`table`、`column`、算法类型或 `plan_id` 候选值。 |

## 限制

### 支持范围

- 仅支持 ShardingSphere-Proxy 逻辑库。
- 直连真实数据库时，本功能不适用。

### MCP 插件边界

- MCP Server 不实现加密算法，也不替代用户判断加密策略是否满足业务安全要求。
- 规划结果是可审查的变更计划；是否执行仍需要用户确认。
- 删除加密规则只移除规则本身，不会恢复历史明文数据；不再需要的物理派生列或索引仍需人工清理。

### Proxy 可见元数据边界

- MCP 根据 Proxy 暴露的逻辑元数据生成派生列、索引和列类型建议；它不会直接检查每个物理库。
- 执行前应结合真实物理库表结构审查生成的 DDL。

### ShardingSphere 功能边界

- 不处理已有数据迁移或回填。
- 不提供自动回滚。

### 规划器输入限制

- 标识符不能包含 NUL、回车或换行等无法生成可审查 SQL 的字符。
