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

## Using the integration

After the client is configured with the MCP Server, users describe tasks directly in the model conversation.
The client handles session initialization, capability discovery, completion, and tool calls; the model chooses which resources to read or which tools to call.

Examples:

- Show the tables in `<logic-database>`.
- Inspect columns and indexes for `<table-name>`.
- Run a read-only query and limit the result to 100 rows.
- Plan a data encryption or data masking rule and preview it without execution.

If the client provides a tool approval UI, pay special attention to side-effecting calls such as SQL execution, rule changes, and plugin workflow execution.
For custom clients or protocol debugging, use the [Capability Catalog](../capabilities/) to confirm resources, tools, prompts, and completion targets.
