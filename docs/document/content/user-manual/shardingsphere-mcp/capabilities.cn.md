+++
title = "功能介绍"
weight = 2
+++

本页列出 ShardingSphere-MCP 对外暴露的协议能力。
运行时实际可用能力以 `shardingsphere://capabilities` 以及 MCP 官方列表方法返回值为准。

## 发现入口

| 协议能力 | 用途 |
| --- | --- |
| `tools/list` | 发现可调用工具。 |
| `resources/list` | 发现可直接读取的资源。 |
| `resources/templates/list` | 发现带参数的资源模板。 |
| `prompts/list` | 发现可用提示。 |
| `completion/complete` | 获取资源、提示或参数的补全候选。 |
| `resources/read` 读取 `shardingsphere://capabilities` | 读取 ShardingSphere 领域能力目录。 |

## 静态资源

| 资源 | 用途 |
| --- | --- |
| `shardingsphere://capabilities` | 查看资源、资源模板、工具、提示、补全、工作流关系和副作用提示。 |
| `shardingsphere://runtime` | 查看当前传输方式、运行状态和已配置逻辑库摘要。 |
| `shardingsphere://databases` | 列出当前 MCP Server 可以访问的 ShardingSphere 逻辑库。 |
| `shardingsphere://features/encrypt/algorithms` | 列出当前 ShardingSphere-Proxy 可见的数据加密算法插件。 |
| `shardingsphere://features/mask/algorithms` | 列出当前 ShardingSphere-Proxy 可见的数据脱敏算法插件。 |

## 资源模板

| 资源模板 | 用途 |
| --- | --- |
| `shardingsphere://databases/{database}` | 读取一个逻辑库的详情和元数据摘要。 |
| `shardingsphere://databases/{database}/capabilities` | 读取一个逻辑库的 SQL、事务、schema 和元数据对象能力。 |
| `shardingsphere://databases/{database}/schemas` | 列出一个逻辑库中的 schema 或 namespace。 |
| `shardingsphere://databases/{database}/schemas/{schema}` | 读取一个 schema 或 namespace 的详情。 |
| `shardingsphere://databases/{database}/schemas/{schema}/sequences` | 列出一个 schema 中的 sequence。 |
| `shardingsphere://databases/{database}/schemas/{schema}/sequences/{sequence}` | 读取一个 sequence 的详情。 |
| `shardingsphere://databases/{database}/schemas/{schema}/tables` | 列出一个 schema 中的逻辑表。 |
| `shardingsphere://databases/{database}/schemas/{schema}/tables/{table}` | 读取一个逻辑表的详情。 |
| `shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/columns` | 列出一个逻辑表的列。 |
| `shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/columns/{column}` | 读取一个逻辑表列的详情。 |
| `shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/indexes` | 列出一个逻辑表的索引。 |
| `shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/indexes/{index}` | 读取一个逻辑表索引的详情。 |
| `shardingsphere://databases/{database}/schemas/{schema}/views` | 列出一个 schema 中的视图。 |
| `shardingsphere://databases/{database}/schemas/{schema}/views/{view}` | 读取一个视图的详情。 |
| `shardingsphere://databases/{database}/schemas/{schema}/views/{view}/columns` | 列出一个视图的列。 |
| `shardingsphere://databases/{database}/schemas/{schema}/views/{view}/columns/{column}` | 读取一个视图列的详情。 |
| `shardingsphere://workflows/{plan_id}` | 读取当前会话中的工作流计划、补问信息、变更产物和下一步动作。 |
| `shardingsphere://features/encrypt/databases/{database}/rules` | 列出一个逻辑库中的数据加密规则。 |
| `shardingsphere://features/encrypt/databases/{database}/tables/{table}/rules` | 读取一个逻辑表的数据加密规则。 |
| `shardingsphere://features/mask/databases/{database}/rules` | 列出一个逻辑库中的数据脱敏规则。 |
| `shardingsphere://features/mask/databases/{database}/tables/{table}/rules` | 读取一个逻辑表的数据脱敏规则。 |

## 工具

| 工具 | 用途 | 副作用 |
| --- | --- | --- |
| `database_gateway_search_metadata` | 按名称片段和对象类型搜索逻辑库元数据，并返回后续资源读取提示。 | 无。 |
| `database_gateway_execute_query` | 执行一个已判定为查询类的 `SELECT` 或 `EXPLAIN ANALYZE`。 | 无；拒绝 DML、DDL、DCL、事务控制、savepoint 和其他有副作用 SQL。 |
| `database_gateway_execute_update` | 预览或执行一个可能修改数据、元数据、规则或事务状态的 SQL。 | 有；必须显式传入 `execution_mode=preview` 或 `execution_mode=execute`。 |
| `database_gateway_apply_workflow` | 预览、执行或导出已规划工作流的人工执行包。 | 取决于 `execution_mode`；`preview` 和 `manual-only` 不修改运行时状态。 |
| `database_gateway_validate_workflow` | 根据可见元数据和生成产物校验计划或执行结果。 | 无。 |
| `database_gateway_plan_encrypt_rule` | 规划数据加密规则变更，生成可审查的 DDL、DistSQL、索引计划和校验步骤。 | 无；只生成计划。 |
| `database_gateway_plan_mask_rule` | 规划数据脱敏规则变更，生成可审查的 DistSQL 和校验步骤。 | 无；只生成计划。 |

## 提示

| 提示 | 用途 |
| --- | --- |
| `inspect_metadata` | 引导模型读取逻辑库元数据，再选择搜索工具或详情资源。 |
| `safe_sql_execution` | 引导模型区分只读查询和有副作用 SQL，并选择正确 SQL 工具。 |
| `recover_workflow` | 引导模型在工作流失败或 `plan_id` 不可用时恢复或重新规划。 |
| `plan_encrypt_rule` | 引导模型在规划数据加密规则前读取逻辑元数据、可用算法和已有规则。 |
| `plan_mask_rule` | 引导模型在规划数据脱敏规则前读取逻辑元数据、可用算法和已有规则。 |

## 补全目标

| 目标类型 | 目标 | 补全参数 |
| --- | --- | --- |
| resource | `shardingsphere://databases/{database}` | `database` |
| resource | `shardingsphere://databases/{database}/schemas` | `database` |
| resource | `shardingsphere://databases/{database}/schemas/{schema}` | `database`、`schema` |
| resource | `shardingsphere://databases/{database}/schemas/{schema}/tables` | `database`、`schema` |
| resource | `shardingsphere://databases/{database}/schemas/{schema}/tables/{table}` | `database`、`schema`、`table` |
| resource | `shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/columns` | `database`、`schema`、`table` |
| resource | `shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/columns/{column}` | `database`、`schema`、`table`、`column` |
| resource | `shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/indexes` | `database`、`schema`、`table` |
| resource | `shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/indexes/{index}` | `database`、`schema`、`table`、`index` |
| resource | `shardingsphere://databases/{database}/schemas/{schema}/sequences` | `database`、`schema` |
| resource | `shardingsphere://databases/{database}/schemas/{schema}/sequences/{sequence}` | `database`、`schema`、`sequence` |
| resource | `shardingsphere://workflows/{plan_id}` | `plan_id` |
| resource | `shardingsphere://features/encrypt/algorithms` | `algorithm_type`、`assisted_query_algorithm_type`、`like_query_algorithm_type` |
| resource | `shardingsphere://features/mask/algorithms` | `algorithm_type` |
| prompt | `inspect_metadata` | `database`、`schema` |
| prompt | `safe_sql_execution` | `database`、`schema` |
| prompt | `recover_workflow` | `plan_id` |
| prompt | `plan_encrypt_rule` | `database`、`schema`、`table`、`column`、`algorithm_type`、`assisted_query_algorithm_type`、`like_query_algorithm_type`、`plan_id` |
| prompt | `plan_mask_rule` | `database`、`schema`、`table`、`column`、`algorithm_type`、`plan_id` |
