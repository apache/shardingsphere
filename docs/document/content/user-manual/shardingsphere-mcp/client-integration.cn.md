+++
title = "Client 集成"
weight = 3
+++

MCP client 可以通过 Streamable HTTP 或 STDIO 连接 ShardingSphere-MCP。
Client 应优先使用 MCP 官方 discovery 方法发现 public surface，再按任务调用 tools、resources、prompts 或 completions。

## HTTP 配置形态

```json
{
  "mcpServers": {
    "shardingsphere-http": {
      "url": "http://127.0.0.1:18088/mcp"
    }
  }
}
```

HTTP client 需要先调用 `initialize`，并保存响应 header 中的：

- `MCP-Session-Id`
- `MCP-Protocol-Version`

后续请求必须继续携带这两个 header。
关闭 session 后，该 session id 不能继续复用。

## STDIO 配置形态

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

STDIO 模式适合由本地 MCP client 拉起 ShardingSphere-MCP 子进程。
它不是面向人工手输请求的交互式 Shell。

STDIO 模式下：

- stdout 只用于 MCP 协议帧。
- 诊断日志写到 stderr 或 `logs/mcp.log`。
- client 配置中的 `command` 和 `args` 应指向发行包内的启动脚本和 STDIO 配置文件。

## Discovery 顺序

建议 client 按下面顺序发现能力：

1. `tools/list`
2. `resources/list`
3. `resources/templates/list`
4. `prompts/list`
5. `completion/complete`

`shardingsphere://capabilities` 是 ShardingSphere 领域目录资源，可作为模型理解可用能力的补充信息，但不替代 MCP 官方 list 方法。

## 常用调用

读取 runtime 状态：

```json
{"jsonrpc":"2.0","id":"runtime-1","method":"resources/read","params":{"uri":"shardingsphere://runtime"}}
```

读取能力目录：

```json
{"jsonrpc":"2.0","id":"capabilities-1","method":"resources/read","params":{"uri":"shardingsphere://capabilities"}}
```

搜索 metadata：

```json
{"jsonrpc":"2.0","id":"search-1","method":"tools/call","params":{"name":"database_gateway_search_metadata","arguments":{"database":"logic_db","query":"orders","object_types":["table"]}}}
```

执行只读 SQL：

```json
{"jsonrpc":"2.0","id":"query-1","method":"tools/call","params":{"name":"database_gateway_execute_query","arguments":{"database":"logic_db","sql":"SELECT * FROM orders","max_rows":100}}}
```
