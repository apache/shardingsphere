+++
title = "能力清单"
weight = 2
+++

本页从用户任务角度说明 ShardingSphere-MCP 可以读取哪些信息、执行哪些动作，以及不同连接目标下的使用边界。
客户端会自动发现当前 MCP Server 实际可用的能力；用户通常只需要在客户端中描述要完成的数据库任务。

## 能力发现

能力发现用于让客户端确认当前 MCP Server 可以访问哪些数据库、支持哪些任务，以及哪些任务可能产生副作用。

| 发现内容 | 用途 | 用户侧效果 |
| --- | --- | --- |
| 基础能力列表 | 确认当前 MCP Server 可读取的信息和可执行的动作。 | 客户端可以判断是否支持元数据查看、SQL 查询或治理变更。 |
| ShardingSphere 能力摘要 | 汇总运行时数据库、连接目标、功能插件和副作用边界。 | 用户可以询问“这个逻辑库支持哪些治理任务？” |
| 数据库能力摘要 | 确认某个运行时数据库支持的 SQL、事务、schema 和元数据对象能力。 | 用户可以询问“这个库支持只读查询和规则规划吗？” |

能力发现结果代表当前 MCP Server 实际对外可用的内容；实际能否使用某项能力，还取决于 `runtimeDatabases` 连接的是 ShardingSphere-Proxy 还是普通数据库。
客户端会根据连接目标选择可用能力。

### 连接 ShardingSphere-Proxy

适合查看 ShardingSphere 逻辑库结构、读取治理规则状态、执行受控 SQL，或通过功能插件生成可审查的治理变更计划。
此模式下，逻辑元数据、逻辑 SQL、DistSQL、规则状态、算法插件和插件工作流可以被 MCP 能力使用。

使用限制：

- 物理元数据以 Proxy 暴露结果为准，不能等同于每个底层物理库的完整元数据。
- 依赖 ShardingSphere 规则、算法或 DistSQL 的功能只适用于 Proxy 连接。
- 规划类能力生成的是可审查计划，执行前仍需确认业务影响。

### 直接连接数据库

适合把 MCP Server 作为普通数据库的受控访问通路，用于读取 JDBC 元数据、搜索对象、辅助生成查询，或执行受限 SQL。
此模式下，通用数据库元数据和 SQL 工具可用。

使用限制：

- ShardingSphere 规则、算法插件和依赖 DistSQL 的插件工作流不适用。
- 返回的是数据库自身元数据，不包含 ShardingSphere 逻辑规则视图。
- 客户端不能假设直接连接数据库和连接 Proxy 时暴露的资源、工具行为完全一致。

## 资源

资源用于提供运行时状态、数据库列表、表结构、列信息或工作流计划等上下文。
客户端会按任务需要读取对应资源。

| 资源 URI 或模板 | 用途 | 自然语言示例 |
| --- | --- | --- |
| `shardingsphere://capabilities` | 查看 MCP Server 的可用任务和副作用提示。 | “这个 MCP Server 支持哪些数据库任务？” |
| `shardingsphere://runtime` | 查看当前传输方式、运行状态和已配置运行时数据库摘要。 | “当前 MCP Server 配置了哪些逻辑库？” |
| `shardingsphere://databases` | 列出当前 MCP Server 可以访问的运行时数据库；连接 Proxy 时对应 ShardingSphere 逻辑库。 | “列出可以访问的逻辑库。” |
| `shardingsphere://databases/{database}` | 读取一个运行时数据库的详情和元数据摘要。 | “查看 `<logic-database>` 的元数据摘要。” |
| `shardingsphere://databases/{database}/capabilities` | 读取一个运行时数据库的 SQL、事务、schema 和元数据对象能力。 | “`<logic-database>` 支持哪些 SQL 和元数据能力？” |
| `shardingsphere://databases/{database}/schemas` | 列出一个运行时数据库中的 schema 或 namespace。 | “查看 `<logic-database>` 中有哪些 schema。” |
| `shardingsphere://databases/{database}/schemas/{schema}` | 读取一个 schema 或 namespace 的详情。 | “查看 `<schema-name>` 的详情。” |
| `shardingsphere://databases/{database}/schemas/{schema}/sequences` | 列出一个 schema 中的 sequence。 | “列出 `<schema-name>` 中的 sequence。” |
| `shardingsphere://databases/{database}/schemas/{schema}/sequences/{sequence}` | 读取一个 sequence 的详情。 | “查看 `<sequence-name>` 的详情。” |
| `shardingsphere://databases/{database}/schemas/{schema}/tables` | 列出一个 schema 中的表。 | “列出 `<schema-name>` 中的表。” |
| `shardingsphere://databases/{database}/schemas/{schema}/tables/{table}` | 读取一个表的详情。 | “查看 `<table-name>` 的表结构。” |
| `shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/columns` | 列出一个表的列。 | “查看 `<table-name>` 有哪些列。” |
| `shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/columns/{column}` | 读取一个表列的详情。 | “查看 `<column-name>` 的类型和约束。” |
| `shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/indexes` | 列出一个表的索引。 | “查看 `<table-name>` 的索引。” |
| `shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/indexes/{index}` | 读取一个表索引的详情。 | “查看 `<index-name>` 包含哪些列。” |
| `shardingsphere://databases/{database}/schemas/{schema}/views` | 列出一个 schema 中的视图。 | “列出 `<schema-name>` 中的视图。” |
| `shardingsphere://databases/{database}/schemas/{schema}/views/{view}` | 读取一个视图的详情。 | “查看 `<view-name>` 的定义摘要。” |
| `shardingsphere://databases/{database}/schemas/{schema}/views/{view}/columns` | 列出一个视图的列。 | “查看 `<view-name>` 有哪些列。” |
| `shardingsphere://databases/{database}/schemas/{schema}/views/{view}/columns/{column}` | 读取一个视图列的详情。 | “查看视图列 `<column-name>` 的详情。” |
| `shardingsphere://workflows/{plan_id}` | 查看当前治理变更计划、补问信息、变更产物和下一步动作。 | “查看刚才的规则变更计划和下一步动作。” |

插件提供的资源、工具、提示和补全目标在对应插件页面说明。

## 工具

工具用于执行动作，例如搜索元数据、执行 SQL，或处理插件工作流阶段。
有副作用的动作应先预览或审查。

| 工具 | 用途 | 自然语言示例 | 副作用 |
| --- | --- | --- | --- |
| `database_gateway_search_metadata` | 按名称片段和对象类型搜索运行时数据库元数据，并返回后续资源读取提示。 | “查找名字包含 `order` 的表。” | 无。 |
| `database_gateway_validate_proxy_connectivity` | 在正式接入前校验已配置的运行时数据库，包括驱动加载、JDBC 连通性、metadata 可读性和数据库可见性。 | “先检查已配置的 `logic_db` 能不能接入，再注册。” | 无。 |
| `database_gateway_execute_query` | 执行一个已判定为查询类的 `SELECT` 或 `EXPLAIN ANALYZE`。 | “查询 `orders` 表前 10 行。” | 无；拒绝 DML、DDL、DCL、事务控制、savepoint 和其他有副作用 SQL。 |
| `database_gateway_execute_update` | 预览或执行一个可能修改数据、元数据、规则或事务状态的 SQL。 | “预览这条变更 SQL，先不要执行。” | 有；应先预览并确认。 |
| `database_gateway_apply_workflow` | 预览、执行或导出功能插件生成的治理变更计划。 | “先预览刚才的加密规则计划。” | 取决于执行方式；预览和人工执行包不修改运行时状态。 |
| `database_gateway_validate_workflow` | 插件工作流执行后，根据可见元数据和生成产物校验结果。 | “校验刚才的脱敏规则是否生效。” | 无。 |

插件工具在对应插件页面说明。

### Proxy 预检结果

`database_gateway_validate_proxy_connectivity` 返回固定结构的校验结果，顶层字段包括：

- `response_mode`
- `status`
- `database`
- `checks`
- `category`
- `recovery`

常见失败分类包括 `missing_jdbc_driver`、`authentication_failed`、`authorization_failed`、`connection_timeout`、`invalid_configuration`、`database_unavailable`、`connection_failed` 和 `database_not_visible`。
`recovery` 字段沿用运行时数据库连接失败的 secret-safe 恢复风格。
该工具只接受已配置的 `database` 名称。JDBC URL、用户名、密码和驱动类名等连接细节保留在运行时配置中。

## 提示

提示用于任务引导，例如先读取哪些信息、如何处理 SQL 执行边界、如何从失败中恢复。
用户通常不需要直接使用提示。

| 提示 | 用途 |
| --- | --- |
| `inspect_metadata` | 引导元数据查看任务先读取数据库元数据，再选择搜索工具或详情资源。 |
| `safe_sql_execution` | 引导 SQL 执行任务区分只读查询和有副作用 SQL。 |
| `recover_workflow` | 引导插件工作流失败后恢复或重新规划。 |

插件提示在对应插件页面说明。

## 补全目标

补全目标用于帮助客户端补齐数据库、schema、表、列等名称。
例如用户只输入部分对象名时，客户端可以给出候选值。

### 资源补全目标

| 目标 | 补全参数 |
| --- | --- |
| `shardingsphere://databases/{database}` | `database` |
| `shardingsphere://databases/{database}/schemas` | `database` |
| `shardingsphere://databases/{database}/schemas/{schema}` | `database`、`schema` |
| `shardingsphere://databases/{database}/schemas/{schema}/tables` | `database`、`schema` |
| `shardingsphere://databases/{database}/schemas/{schema}/tables/{table}` | `database`、`schema`、`table` |
| `shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/columns` | `database`、`schema`、`table` |
| `shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/columns/{column}` | `database`、`schema`、`table`、`column` |
| `shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/indexes` | `database`、`schema`、`table` |
| `shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/indexes/{index}` | `database`、`schema`、`table`、`index` |
| `shardingsphere://databases/{database}/schemas/{schema}/sequences` | `database`、`schema` |
| `shardingsphere://databases/{database}/schemas/{schema}/sequences/{sequence}` | `database`、`schema`、`sequence` |
| `shardingsphere://workflows/{plan_id}` | `plan_id` |

### 提示补全目标

| 目标 | 补全参数 |
| --- | --- |
| `inspect_metadata` | `database`、`schema` |
| `safe_sql_execution` | `database`、`schema` |
| `recover_workflow` | `plan_id` |

插件提示补全目标在对应插件页面说明。
