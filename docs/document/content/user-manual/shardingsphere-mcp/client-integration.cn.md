+++
title = "客户端集成"
weight = 4
+++

客户端集成用于把这条受控数据库访问通路接入桌面客户端、IDE 插件、Agent 平台或自研 LLM 应用。
接入后，大模型不是直接连接数据库，而是通过 MCP 客户端调用 ShardingSphere-MCP 暴露的资源、工具、提示和补全能力。
它不是快速开始中 curl 手工验证流程的替代说明，也不是让用户手工拼 JSON-RPC 的操作手册。
客户端负责连接配置、会话头、补全和调用编排。
用户只需要表达要完成的元数据查询、只读 SQL 查询或数据库治理任务。

适合使用客户端集成的场景：

- 在支持 MCP 的模型客户端、IDE 插件或 Agent 平台中接入 ShardingSphere。
- 让模型基于 ShardingSphere 元数据完成查询辅助、结构理解、问题诊断或治理规划。
- 为团队提供受控数据库访问通路，而不是把数据库连接信息直接交给模型。
- 为自研 Agent 平台集成 ShardingSphere 元数据、安全 SQL 和治理插件能力。

完整的资源、工具、提示和补全说明见[能力清单](../capabilities/)。

## 选择传输方式

- HTTP 适合 MCP Server 独立启动，客户端通过固定端点访问的场景。客户端需要完成会话初始化，并在后续请求中携带会话响应头。
- STDIO 适合本地 MCP 客户端拉起 ShardingSphere-MCP 子进程的场景。客户端负责进程生命周期，stdout 只传输 MCP 协议帧。

## HTTP 配置

将下面片段写入 MCP 客户端的 server 配置；具体文件位置由客户端决定。
`url` 指向已经启动的 HTTP MCP Server。

```json
{
  "mcpServers": {
    "shardingsphere-http": {
      "url": "http://127.0.0.1:18088/mcp"
    }
  }
}
```

HTTP 客户端需要在正常 MCP 调用前完成会话生命周期：

1. 调用 `initialize`。
2. 保存 `MCP-Session-Id` 和 `MCP-Protocol-Version` 响应头。
3. 携带这两个响应头发送 `notifications/initialized`，并预期 HTTP 状态码为 `202`。
4. 后续 MCP 请求继续携带这两个响应头。

关闭会话后，该会话 ID 不能继续复用。

## STDIO 配置

将下面片段写入 MCP 客户端的 server 配置；具体文件位置由客户端决定。
`command` 指向发行包内的启动脚本，`args` 指向 STDIO 配置文件。

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

STDIO 模式适合由本地 MCP 客户端拉起 ShardingSphere-MCP 子进程。
它不是面向人工手输请求的交互式 Shell。
将 `/path/to/apache-shardingsphere-mcp` 替换为实际发行包目录。

STDIO 模式下：

- stdout 只用于 MCP 协议帧。
- 诊断日志写到 stderr 或 `logs/mcp.log`。
- 客户端配置中的 `command` 和 `args` 应指向发行包内的启动脚本和 STDIO 配置文件。

## 协议调用示例

使用现成 MCP 客户端或 Agent 平台时，用户通常通过自然语言提出任务，例如“查看逻辑库有哪些表”或“查询订单表的字段”。
客户端会根据模型选择自动发送 MCP 协议请求，用户不需要把下面的 JSON 粘贴到模型对话框。
下面的示例用于说明客户端背后发送的协议消息，适合客户端开发、集成调试或排查问题。

读取服务状态：

```json
{"jsonrpc":"2.0","id":"runtime-1","method":"resources/read","params":{"uri":"shardingsphere://runtime"}}
```

读取能力目录：

```json
{"jsonrpc":"2.0","id":"capabilities-1","method":"resources/read","params":{"uri":"shardingsphere://capabilities"}}
```

搜索元数据：

```json
{
  "jsonrpc": "2.0",
  "id": "search-1",
  "method": "tools/call",
  "params": {
    "name": "database_gateway_search_metadata",
    "arguments": {
      "database": "<logic-database>",
      "query": "<metadata-keyword>",
      "object_types": ["table"]
    }
  }
}
```

执行只读 SQL：

```json
{
  "jsonrpc": "2.0",
  "id": "query-1",
  "method": "tools/call",
  "params": {
    "name": "database_gateway_execute_query",
    "arguments": {
      "database": "<logic-database>",
      "sql": "SELECT * FROM <table-name> LIMIT 100",
      "max_rows": 100
    }
  }
}
```
