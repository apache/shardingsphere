+++
title = "客户端集成"
weight = 3
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

HTTP 客户端需要先调用 `initialize`，并保存响应头中的：

- `MCP-Session-Id`
- `MCP-Protocol-Version`

后续请求必须继续携带这两个响应头。
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

## 能力发现顺序

建议客户端按下面顺序发现能力：

1. `tools/list`
2. `resources/list`
3. `resources/templates/list`
4. `prompts/list`
5. `completion/complete`

`shardingsphere://capabilities` 是 ShardingSphere 领域目录资源，可作为模型理解可用能力的补充信息，但不替代 MCP 官方列表方法。

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
{"jsonrpc":"2.0","id":"search-1","method":"tools/call","params":{"name":"database_gateway_search_metadata","arguments":{"database":"logic_db","query":"sample","object_types":["table"]}}}
```

执行只读 SQL：

```json
{"jsonrpc":"2.0","id":"query-1","method":"tools/call","params":{"name":"database_gateway_execute_query","arguments":{"database":"logic_db","sql":"SELECT * FROM sample_table LIMIT 100","max_rows":100}}}
```
