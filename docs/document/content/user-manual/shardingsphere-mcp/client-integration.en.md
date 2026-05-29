+++
title = "Client Integration"
weight = 4
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

## Capability discovery

Official MCP list methods discover protocol-level capabilities. `shardingsphere://capabilities` explains ShardingSphere domain capabilities.
Clients do not need a fixed discovery order; call the method or resource that matches the task.

| Capability | Purpose |
| --- | --- |
| `shardingsphere://capabilities` | Read the ShardingSphere domain capability catalog for resources, tools, prompts, completions, workflow relationships, and side-effect notes. |
| `tools/list` | Discover callable tools. |
| `resources/list` | Discover directly readable resources. |
| `resources/templates/list` | Discover parameterized resource templates when the client needs to construct resource URIs. |
| `prompts/list` | Discover available prompts. |
| `completion/complete` | Get completion candidates for resources, prompts, or arguments. |

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
{"jsonrpc":"2.0","id":"search-1","method":"tools/call","params":{"name":"database_gateway_search_metadata","arguments":{"database":"<logic-database>","query":"<metadata-keyword>","object_types":["table"]}}}
```

Execute read-only SQL:

```json
{"jsonrpc":"2.0","id":"query-1","method":"tools/call","params":{"name":"database_gateway_execute_query","arguments":{"database":"<logic-database>","sql":"SELECT * FROM <table-name> LIMIT 100","max_rows":100}}}
```
