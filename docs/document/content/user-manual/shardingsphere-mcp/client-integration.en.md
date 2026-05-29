+++
title = "Client Integration"
weight = 4
+++

Client integration connects this controlled database access path to desktop clients, IDE extensions, agent platforms, or custom LLM applications.
After integration, the model does not connect to the database directly. It calls the resources, tools, prompts, and completion capabilities exposed by ShardingSphere-MCP through an MCP client.
It is not a replacement for the curl-based smoke test in Quick Start, and it is not a guide for users to hand-write JSON-RPC.
The client manages connection configuration, session headers, completion, and call orchestration.
Users only need to describe the metadata lookup, read-only SQL query, or database governance task they want to complete.

Use client integration when:

- A model client, IDE extension, or agent platform that supports MCP needs to connect to ShardingSphere.
- A model should use ShardingSphere metadata for query assistance, structure understanding, diagnostics, or governance planning.
- A team needs a controlled database access path instead of handing database connection information directly to a model.
- A custom agent platform needs ShardingSphere metadata, safe SQL, and governance plugin capabilities.

See [Capability Catalog](../capabilities/) for the full list of resources, tools, prompts, and completion targets.

## Choose a transport

- HTTP is suitable when the MCP Server is started independently and clients use a fixed endpoint. The client must initialize the session and keep session response headers.
- STDIO is suitable when a local MCP client starts ShardingSphere-MCP as a child process. The client owns the process lifecycle, and stdout carries only MCP protocol frames.

## HTTP configuration

Add the following snippet to the MCP client's server configuration. The exact file location depends on the client.
`url` points to an already running HTTP MCP Server.

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

Add the following snippet to the MCP client's server configuration. The exact file location depends on the client.
`command` points to the packaged startup script, and `args` points to the STDIO configuration file.

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

STDIO mode is for local MCP clients that launch ShardingSphere-MCP as a child process.
It is not a human-oriented interactive shell.
Replace `/path/to/apache-shardingsphere-mcp` with the actual distribution directory.

In STDIO mode:

- stdout is reserved for MCP protocol frames.
- Diagnostics are written to stderr or `logs/mcp.log`.
- `command` and `args` in the client configuration should point to the packaged startup script and STDIO config file.

## Protocol call examples

When using an existing MCP client or agent platform, users usually describe tasks in natural language, such as "show tables in the logical database" or "inspect columns in the orders table".
The client sends MCP protocol requests automatically based on the model's choices. Users do not paste the following JSON into a model chat.
The examples below show protocol messages sent behind the scenes, and are useful for client development, integration debugging, or troubleshooting.

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
