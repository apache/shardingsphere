+++
title = "客户端集成"
weight = 4
+++

MCP 客户端可以通过 Streamable HTTP 或 STDIO 连接 ShardingSphere-MCP。
客户端应优先使用 MCP 官方能力发现方法获取工具、资源、提示和补全列表，再按任务发起调用。

## HTTP 配置

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

```json
{
  "mcpServers": {
    "shardingsphere": {
      "command": "/path/to/apache-shardingsphere-mcp/bin/start.sh",
      "args": ["conf/mcp-stdio.yaml"]
    }
  }
}
```

STDIO 模式适合由本地 MCP 客户端拉起 ShardingSphere-MCP 子进程。
它不是面向人工手输请求的交互式 Shell。

STDIO 模式下：

- stdout 只用于 MCP 协议帧。
- 诊断日志写到 stderr 或 `logs/mcp.log`。
- 客户端配置中的 `command` 和 `args` 应指向发行包内的启动脚本和 STDIO 配置文件。

## 能力发现

MCP 官方列表方法用于发现协议层能力；`shardingsphere://capabilities` 用于理解 ShardingSphere 领域能力。
客户端不需要固定调用顺序，可以按任务需要选择。

| 能力 | 作用 |
| --- | --- |
| `shardingsphere://capabilities` | 读取 ShardingSphere 领域能力目录，了解资源、工具、提示、补全、工作流关系和副作用提示。 |
| `tools/list` | 发现可调用工具。 |
| `resources/list` | 发现可直接读取的资源。 |
| `resources/templates/list` | 发现带参数的资源模板；客户端需要构造资源 URI 时使用。 |
| `prompts/list` | 发现可用提示。 |
| `completion/complete` | 获取资源、提示或参数的补全候选。 |

## 常用调用

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
{"jsonrpc":"2.0","id":"search-1","method":"tools/call","params":{"name":"database_gateway_search_metadata","arguments":{"database":"<logic-database>","query":"<metadata-keyword>","object_types":["table"]}}}
```

执行只读 SQL：

```json
{"jsonrpc":"2.0","id":"query-1","method":"tools/call","params":{"name":"database_gateway_execute_query","arguments":{"database":"<logic-database>","sql":"SELECT * FROM <table-name> LIMIT 100","max_rows":100}}}
```
