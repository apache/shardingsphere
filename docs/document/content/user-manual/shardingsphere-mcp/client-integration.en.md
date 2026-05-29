+++
title = "Client Integration"
weight = 4
+++

Client integration is for desktop clients, IDE extensions, agent platforms, or custom LLM applications that connect to ShardingSphere-MCP.
It is not a replacement for the curl-based smoke test in Quick Start.
Its value is letting the client manage connection configuration, session headers, capability discovery, completion, and call orchestration while users focus on metadata or database governance tasks.

Use client integration when:

- An MCP client needs to expose ShardingSphere metadata and governance tools to a model.
- The same MCP Server configuration should be reused instead of hand-writing curl requests.
- The client needs to keep HTTP session headers or manage a local ShardingSphere-MCP child process through STDIO.
- The client needs to discover tools, resources, prompts, and completion targets per task instead of hardcoding the full capability catalog.

If you only need to verify that the MCP Server is available, use the curl examples in Quick Start.

## Choose a transport

- HTTP is suitable when the MCP Server is started independently and clients use a fixed endpoint. The client must initialize the session and keep session response headers.
- STDIO is suitable when a local MCP client starts ShardingSphere-MCP as a child process. The client owns the process lifecycle, and stdout carries only MCP protocol frames.

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

An HTTP client must complete the session lifecycle before normal MCP calls:

1. Call `initialize`.
2. Keep the `MCP-Session-Id` and `MCP-Protocol-Version` response headers.
3. Send `notifications/initialized` with both response headers and expect HTTP status code `202`.
4. Include both headers on later MCP requests.

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

MCP list methods discover protocol-level capabilities. `shardingsphere://capabilities` explains ShardingSphere domain capabilities.
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

Execute read-only SQL:

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
