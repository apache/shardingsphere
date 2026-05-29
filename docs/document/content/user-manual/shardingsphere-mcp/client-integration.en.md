+++
title = "Client Integration"
weight = 3
+++

MCP clients can connect to ShardingSphere-MCP through Streamable HTTP or STDIO.
Clients should use official MCP capability discovery methods first, then call tools, resources, prompts, or completions for the target task.

## HTTP configuration

```json
{
  "mcpServers": {
    "shardingsphere-http": {
      "url": "http://127.0.0.1:18088/mcp"
    }
  }
}
```

An HTTP client must call `initialize` first and keep these response headers:

- `MCP-Session-Id`
- `MCP-Protocol-Version`

Later requests must include both headers.
After the session is closed, the session id cannot be reused.

## STDIO configuration

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

STDIO mode is for local MCP clients that launch ShardingSphere-MCP as a child process.
It is not a human-oriented interactive shell.

In STDIO mode:

- stdout is reserved for MCP protocol frames.
- Diagnostics are written to stderr or `logs/mcp.log`.
- `command` and `args` in the client configuration should point to the packaged startup script and STDIO config file.

## Capability discovery order

Recommended client discovery order:

1. `tools/list`
2. `resources/list`
3. `resources/templates/list`
4. `prompts/list`
5. `completion/complete`

`shardingsphere://capabilities` is a ShardingSphere domain catalog resource. It can help models understand available capabilities, but it does not replace official MCP list methods.

## Common calls

Read runtime status:

```json
{"jsonrpc":"2.0","id":"runtime-1","method":"resources/read","params":{"uri":"shardingsphere://runtime"}}
```

Read the capability catalog:

```json
{"jsonrpc":"2.0","id":"capabilities-1","method":"resources/read","params":{"uri":"shardingsphere://capabilities"}}
```

Search metadata:

```json
{"jsonrpc":"2.0","id":"search-1","method":"tools/call","params":{"name":"database_gateway_search_metadata","arguments":{"database":"logic_db","query":"orders","object_types":["table"]}}}
```

Execute read-only SQL:

```json
{"jsonrpc":"2.0","id":"query-1","method":"tools/call","params":{"name":"database_gateway_execute_query","arguments":{"database":"logic_db","sql":"SELECT * FROM orders","max_rows":100}}}
```
