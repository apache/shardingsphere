+++
title = "自研集成附录"
weight = 9
+++

本页面面向需要自研 MCP 集成、调试协议请求或定位客户端适配问题的开发者。
常规使用可直接参考[快速开始](../quick-start/)、[客户端集成](../client-integration/)和[能力清单](../capabilities/)。

## 通用客户端配置示例

如果使用的 AI 应用没有专门页面，可按客户端自身文档选择 HTTP 或 STDIO 配置方式。

HTTP 示例：

```json
{
  "mcpServers": {
    "shardingsphere-http": {
      "url": "http://127.0.0.1:18088/mcp"
    }
  }
}
```

STDIO 示例：

```json
{
  "mcpServers": {
    "shardingsphere": {
      "command": "/path/to/apache-shardingsphere-mcp/bin/start.sh",
      "args": ["/path/to/apache-shardingsphere-mcp/conf/mcp-stdio.yaml"]
    }
  }
}
```

将 `/path/to/apache-shardingsphere-mcp` 替换为实际发行包目录。

## 协议能力发现

| 入口                                                   | 返回内容                         | 适用场景                        |
|------------------------------------------------------|------------------------------|-----------------------------|
| `tools/list`                                         | 当前 MCP Server 暴露的工具。         | 自研客户端构建可调用动作清单。             |
| `resources/list`                                     | 当前 MCP Server 暴露的静态资源。       | 自研客户端读取固定上下文。               |
| `resources/templates/list`                           | 当前 MCP Server 暴露的资源模板。       | 自研客户端按数据库、模式、表等参数读取上下文。     |
| `prompts/list`                                       | 当前 MCP Server 暴露的提示。         | 自研客户端读取任务引导模板。              |
| `completion/complete`                                | 指定参数的补全候选值。                  | 自研客户端为数据库、模式、表、列等名称提供补全。    |
| `shardingsphere://capabilities`                      | 运行时数据库、连接目标、功能插件和副作用边界。      | 判断当前 MCP Server 可用于哪些数据库任务。 |
| `shardingsphere://databases/{database}/capabilities` | 指定运行时数据库的 SQL、事务、模式和元数据对象能力。 | 判断某个数据库的可用操作和限制。            |

当 client 无法确定 database、schema、table、column、algorithm、storage unit 或 workflow `plan_id` 时，应一次只针对一个参数调用 `completion/complete`。
如果 completion 返回缺少上下文或没有候选值，应遵循返回 meta 中的 `next_actions`；这些动作通常会指向重试补全前需要读取的最近 resource 或 resource template。

## 资源

| 资源 URI 或模板                                                                               | 用途                                                           |
|------------------------------------------------------------------------------------------|--------------------------------------------------------------|
| `shardingsphere://capabilities`                                                          | 查看 MCP Server 的可用任务和副作用提示。                                   |
| `shardingsphere://runtime`                                                               | 查看当前传输方式、运行状态和已配置运行时数据库摘要。                                   |
| `shardingsphere://databases`                                                             | 列出当前 MCP Server 可以访问的运行时数据库；连接 Proxy 时对应 ShardingSphere 逻辑库。 |
| `shardingsphere://databases/{database}`                                                  | 读取一个运行时数据库的详情和元数据摘要。                                         |
| `shardingsphere://databases/{database}/capabilities`                                     | 读取一个运行时数据库的 SQL、事务、模式和元数据对象能力。                               |
| `shardingsphere://databases/{database}/schemas`                                          | 列出一个运行时数据库中的模式或命名空间。                                         |
| `shardingsphere://databases/{database}/schemas/{schema}`                                 | 读取一个模式或命名空间的详情。                                              |
| `shardingsphere://databases/{database}/schemas/{schema}/sequences`                       | 列出一个模式中的序列。                                                  |
| `shardingsphere://databases/{database}/schemas/{schema}/sequences/{sequence}`            | 读取一个序列的详情。                                                   |
| `shardingsphere://databases/{database}/schemas/{schema}/tables`                          | 列出一个模式中的表。                                                   |
| `shardingsphere://databases/{database}/schemas/{schema}/tables/{table}`                  | 读取一个表的详情。                                                    |
| `shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/columns`          | 列出一个表的列。                                                     |
| `shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/columns/{column}` | 读取一个表列的详情。                                                   |
| `shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/indexes`          | 列出一个表的索引。                                                    |
| `shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/indexes/{index}`  | 读取一个表索引的详情。                                                  |
| `shardingsphere://databases/{database}/schemas/{schema}/views`                           | 列出一个模式中的视图。                                                  |
| `shardingsphere://databases/{database}/schemas/{schema}/views/{view}`                    | 读取一个视图的详情。                                                   |
| `shardingsphere://databases/{database}/schemas/{schema}/views/{view}/columns`            | 列出一个视图的列。                                                    |
| `shardingsphere://databases/{database}/schemas/{schema}/views/{view}/columns/{column}`   | 读取一个视图列的详情。                                                  |
| `shardingsphere://workflows/{plan_id}`                                                   | 查看当前治理变更计划、补问信息、变更产物和下一步动作。                                  |

Workflow resources 和 workflow tools 会包含简短的 `summary` 以及结构化 `next_actions`，让 client 不必先读取所有嵌套字段，也能继续预览、执行、人工执行、校验或恢复。

## 工具

| 工具                                             | 用途                                         | 副作用                                          |
|------------------------------------------------|--------------------------------------------|----------------------------------------------|
| `database_gateway_search_metadata`             | 按名称片段和对象类型搜索运行时数据库元数据，并返回后续资源读取提示。         | 无。                                           |
| `database_gateway_validate_runtime_database` | 校验运行时数据库配置是否可用，用于接入失败时定位连接问题。              | 无。                                           |
| `database_gateway_execute_query`               | 执行一个已判定为查询类的 `SELECT`。           | 无；拒绝 DML、DDL、DCL、事务控制、savepoint 和其他有副作用 SQL。 |
| `database_gateway_execute_explain_query`       | 为一个 classifier 允许的 `SELECT` 执行模型生成的数据库原生 `EXPLAIN`。 | 无；拒绝 `EXPLAIN ANALYZE`、`EXPLAIN PLAN FOR`、多语句和有副作用 SQL。 |
| `database_gateway_execute_update`              | 预览或执行一个可能修改数据、元数据、规则或事务状态的 SQL。            | 有；应先预览并确认。                                   |
| `database_gateway_apply_workflow`              | 预览、执行或导出功能插件生成的治理变更计划。                     | 取决于执行方式；预览和人工执行包不修改运行时状态。                    |
| `database_gateway_validate_workflow`           | 规则变更执行后，按功能插件自身边界校验规则状态或 workflow 执行结果。    | 无。                                           |

功能插件提供的额外工具见对应插件页面。

## 提示

| 提示                   | 用途                               |
|----------------------|----------------------------------|
| `inspect_metadata`   | 引导元数据查看任务先读取数据库元数据，再选择搜索工具或详情资源。 |
| `safe_sql_execution` | 引导 SQL 执行任务区分只读查询和有副作用 SQL。      |
| `recover_workflow`   | 引导规则变更失败后恢复或重新规划。                |

## 补全目标

### 资源补全目标

| 目标                                                                                       | 补全参数                                 |
|------------------------------------------------------------------------------------------|--------------------------------------|
| `shardingsphere://databases/{database}`                                                  | `database`                           |
| `shardingsphere://databases/{database}/schemas`                                          | `database`                           |
| `shardingsphere://databases/{database}/schemas/{schema}`                                 | `database`、`schema`                  |
| `shardingsphere://databases/{database}/schemas/{schema}/tables`                          | `database`、`schema`                  |
| `shardingsphere://databases/{database}/schemas/{schema}/tables/{table}`                  | `database`、`schema`、`table`          |
| `shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/columns`          | `database`、`schema`、`table`          |
| `shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/columns/{column}` | `database`、`schema`、`table`、`column` |
| `shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/indexes`          | `database`、`schema`、`table`          |
| `shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/indexes/{index}`  | `database`、`schema`、`table`、`index`  |
| `shardingsphere://databases/{database}/schemas/{schema}/sequences`                       | `database`、`schema`                  |
| `shardingsphere://databases/{database}/schemas/{schema}/sequences/{sequence}`            | `database`、`schema`、`sequence`       |
| `shardingsphere://workflows/{plan_id}`                                                   | `plan_id`                            |

### 提示补全目标

| 目标                   | 补全参数                |
|----------------------|---------------------|
| `inspect_metadata`   | `database`、`schema` |
| `safe_sql_execution` | `database`、`schema` |
| `recover_workflow`   | `plan_id`           |

## HTTP 调试示例

下面示例仅用于开发者调试 HTTP 传输，不是普通使用流程。

初始化会话：

```bash
curl -i -sS http://127.0.0.1:18088/mcp \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  --data '{"jsonrpc":"2.0","id":"init-1","method":"initialize","params":{"protocolVersion":"2025-11-25","capabilities":{},"clientInfo":{"name":"curl-client","version":"1.0.0"}}}'
```

读取数据库列表：

```bash
curl -sS http://127.0.0.1:18088/mcp \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  -H 'MCP-Session-Id: demo-session-id' \
  -H 'MCP-Protocol-Version: 2025-11-25' \
  --data '{"jsonrpc":"2.0","id":"resource-1","method":"resources/read","params":{"uri":"shardingsphere://databases"}}'
```

调用元数据搜索工具：

```bash
curl -sS http://127.0.0.1:18088/mcp \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  -H 'MCP-Session-Id: demo-session-id' \
  -H 'MCP-Protocol-Version: 2025-11-25' \
  --data '{
    "jsonrpc":"2.0",
    "id":"tool-1",
    "method":"tools/call",
    "params":{
      "name":"database_gateway_search_metadata",
      "arguments":{
        "database":"logic_db",
        "query":"orders",
        "object_types":["table","view"]
      }
    }
  }'
```
