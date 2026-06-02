+++
title = "Client Integration"
weight = 4
+++

Client integration connects ShardingSphere-MCP to MCP-capable desktop clients, IDE extensions, agent platforms, or custom applications.
After configuration, users can inspect metadata, run controlled SQL queries, or start database governance tasks through natural language in the client.

Use client integration when:

- An MCP-capable client, IDE extension, or agent platform needs to connect to ShardingSphere.
- ShardingSphere metadata should be used for query assistance, structure understanding, diagnostics, or governance planning.
- A team needs a unified controlled database access path.
- A custom agent platform needs ShardingSphere metadata, safe SQL, and governance plugin capabilities.

See [Capability Catalog](../capabilities/) for supported tasks and usage boundaries.

## Choose a transport

- HTTP is suitable when the MCP Server is started independently and clients use a fixed endpoint.
- STDIO is suitable when a local client starts ShardingSphere-MCP as a child process.

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

The exact configuration file location and field names may differ by client. Follow the client's own documentation.

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
Replace `/path/to/apache-shardingsphere-mcp` with the actual distribution directory.

In STDIO mode:

- Diagnostics are written to stderr or `logs/mcp.log`.
- `command` and `args` in the client configuration should point to the packaged startup script and STDIO config file.

## Using the integration

After the client is configured with the MCP Server, users describe tasks directly in the client conversation.

Examples:

- Show the tables in `<logic-database>`.
- Inspect columns and indexes for `<table-name>`.
- Run a read-only query and limit the result to 100 rows.
- Plan a data encryption or data masking rule and preview it without execution.

If the client provides a tool approval UI, pay special attention to side-effecting calls such as SQL execution, rule changes, and plugin workflow execution.
For custom clients or protocol debugging, use the [Capability Catalog](../capabilities/) to confirm available capabilities.
