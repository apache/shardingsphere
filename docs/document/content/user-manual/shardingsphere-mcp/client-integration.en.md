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

- An MCP client needs to expose ShardingSphere metadata and governance tools to a model.
- The same MCP Server configuration should be reused instead of hand-writing curl requests.
- The client needs to keep HTTP session headers or manage a local ShardingSphere-MCP child process through STDIO.
- The client needs to discover tools, resources, prompts, and completion targets per task instead of hardcoding the full capability catalog.

If you only need to verify that the MCP Server is available, use the curl examples in Quick Start.

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

## Capability catalog

See [Capability Catalog](../capabilities/) for the capability catalog and method semantics.
This page only explains how to configure the MCP Server on the client side and where the call examples are used.

## JSON-RPC call examples

The following JSON snippets are request examples sent by an MCP client or custom LLM application after session initialization.
Regular users usually do not send them directly. They are mainly useful for custom clients, debugging, or troubleshooting client integration.

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
