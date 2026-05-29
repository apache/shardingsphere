+++
title = "能力清单"
weight = 2
+++

本页说明 ShardingSphere-MCP 的核心能力，以及协议方法、资源 URI、工具和提示之间的关系。
文档用于解释能力语义；客户端应通过 MCP 列表方法和 `shardingsphere://capabilities` 读取当前 MCP Server 实际暴露的内容。

## 能力发现

下面列出的是 MCP 协议方法和 ShardingSphere-MCP 资源 URI。
`tools/list`、`resources/list`、`resources/read`、`prompts/list` 和 `completion/complete` 是 MCP JSON-RPC 方法名。
`shardingsphere://...` 是 ShardingSphere-MCP 资源 URI 前缀。
方法名、工具名和提示名不加 URI 前缀；只有资源 URI 和资源模板使用该前缀。

| 方法或资源 | 类型 | 用途 |
| --- | --- | --- |
| `tools/list` | MCP 协议方法 | 列出可调用工具。 |
| `tools/call` | MCP 协议方法 | 按工具名调用一个工具。 |
| `resources/list` | MCP 协议方法 | 列出不需要参数即可读取的资源描述；它不返回资源内容。 |
| `resources/templates/list` | MCP 协议方法 | 列出带参数的资源 URI 模板；客户端需要先填充模板。 |
| `resources/read` | MCP 协议方法 | 读取一个具体资源 URI 的内容；读取 `shardingsphere://capabilities` 可获得 ShardingSphere 领域能力目录。 |
| `prompts/list` | MCP 协议方法 | 列出可用提示。 |
| `prompts/get` | MCP 协议方法 | 读取一个提示内容，并按提示参数生成消息。 |
| `completion/complete` | MCP 协议方法 | 获取资源、提示或参数的补全候选。 |
| `shardingsphere://capabilities` | ShardingSphere-MCP 资源 URI | 读取 ShardingSphere 领域能力目录。 |

## 能力可用性说明

ShardingSphere-MCP 的协议表面是统一的，但运行时可用性取决于 `runtimeDatabases` 的连接目标。

- 连接 ShardingSphere-Proxy 时，逻辑元数据、逻辑 SQL、DistSQL、加密或脱敏规则以及算法插件能力可用，但物理元数据以 Proxy 暴露结果为准。
- 连接真实数据库时，通用 JDBC 元数据和 SQL 执行能力可用；ShardingSphere 规则、算法插件和依赖 DistSQL 的工作流不适用。
- 客户端应优先读取 `shardingsphere://runtime` 和 `shardingsphere://databases/{database}/capabilities` 判断当前数据库能力，不应假设所有资源和工具在所有连接目标下等价。

## 资源

| 资源 URI 或模板 | 类型 | 用途 |
| --- | --- | --- |
| `shardingsphere://capabilities` | 资源 URI | 查看资源 URI、资源模板、工具、提示、补全、工作流关系和副作用提示。 |
| `shardingsphere://runtime` | 资源 URI | 查看当前传输方式、运行状态和已配置运行时数据库摘要。 |
| `shardingsphere://databases` | 资源 URI | 列出当前 MCP Server 可以访问的运行时数据库；连接 Proxy 时对应 ShardingSphere 逻辑库。 |
| `shardingsphere://databases/{database}` | 资源模板 | 读取一个运行时数据库的详情和元数据摘要。 |
| `shardingsphere://databases/{database}/capabilities` | 资源模板 | 读取一个运行时数据库的 SQL、事务、schema 和元数据对象能力。 |
| `shardingsphere://databases/{database}/schemas` | 资源模板 | 列出一个运行时数据库中的 schema 或 namespace。 |
| `shardingsphere://databases/{database}/schemas/{schema}` | 资源模板 | 读取一个 schema 或 namespace 的详情。 |
| `shardingsphere://databases/{database}/schemas/{schema}/sequences` | 资源模板 | 列出一个 schema 中的 sequence。 |
| `shardingsphere://databases/{database}/schemas/{schema}/sequences/{sequence}` | 资源模板 | 读取一个 sequence 的详情。 |
| `shardingsphere://databases/{database}/schemas/{schema}/tables` | 资源模板 | 列出一个 schema 中的表。 |
| `shardingsphere://databases/{database}/schemas/{schema}/tables/{table}` | 资源模板 | 读取一个表的详情。 |
| `shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/columns` | 资源模板 | 列出一个表的列。 |
| `shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/columns/{column}` | 资源模板 | 读取一个表列的详情。 |
| `shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/indexes` | 资源模板 | 列出一个表的索引。 |
| `shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/indexes/{index}` | 资源模板 | 读取一个表索引的详情。 |
| `shardingsphere://databases/{database}/schemas/{schema}/views` | 资源模板 | 列出一个 schema 中的视图。 |
| `shardingsphere://databases/{database}/schemas/{schema}/views/{view}` | 资源模板 | 读取一个视图的详情。 |
| `shardingsphere://databases/{database}/schemas/{schema}/views/{view}/columns` | 资源模板 | 列出一个视图的列。 |
| `shardingsphere://databases/{database}/schemas/{schema}/views/{view}/columns/{column}` | 资源模板 | 读取一个视图列的详情。 |
| `shardingsphere://workflows/{plan_id}` | 资源模板 | 读取当前会话中的工作流计划、补问信息、变更产物和下一步动作。 |

功能插件提供的资源、工具、提示和补全目标在对应插件页面说明。

## 工具

| 工具 | 用途 | 副作用 |
| --- | --- | --- |
| `database_gateway_search_metadata` | 按名称片段和对象类型搜索运行时数据库元数据，并返回后续资源读取提示。 | 无。 |
| `database_gateway_execute_query` | 执行一个已判定为查询类的 `SELECT` 或 `EXPLAIN ANALYZE`。 | 无；拒绝 DML、DDL、DCL、事务控制、savepoint 和其他有副作用 SQL。 |
| `database_gateway_execute_update` | 预览或执行一个可能修改数据、元数据、规则或事务状态的 SQL。 | 有；必须显式传入 `execution_mode=preview` 或 `execution_mode=execute`。 |
| `database_gateway_apply_workflow` | 预览、执行或导出已规划工作流的人工执行包。 | 取决于 `execution_mode`；`preview` 和 `manual-only` 不修改运行时状态。 |
| `database_gateway_validate_workflow` | 根据可见元数据和生成产物校验计划或执行结果。 | 无。 |

数据加密、数据脱敏等功能插件工具在对应插件页面说明。

## 提示

| 提示 | 用途 |
| --- | --- |
| `inspect_metadata` | 引导模型读取数据库元数据，再选择搜索工具或详情资源。 |
| `safe_sql_execution` | 引导模型区分只读查询和有副作用 SQL，并选择正确 SQL 工具。 |
| `recover_workflow` | 引导模型在工作流失败或 `plan_id` 不可用时恢复或重新规划。 |

数据加密、数据脱敏等功能插件提示在对应插件页面说明。

## 补全目标

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

功能插件的提示补全目标在对应插件页面说明。
