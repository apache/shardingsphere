+++
title = "Client Integration"
weight = 4
+++

Client integration connects ShardingSphere-MCP to MCP-capable AI applications, IDE extensions, or agent platforms.
After configuration, users can inspect metadata, run controlled SQL queries, or start database governance tasks through natural language in the application.

Use client integration when:

- An AI application, IDE extension, or agent platform needs to connect to ShardingSphere.
- ShardingSphere metadata should be used for query assistance, structure understanding, diagnostics, or governance planning.
- A team needs a unified controlled database access path.
- An AI application needs ShardingSphere metadata, controlled SQL, and rule change capabilities.

See [Capability Catalog](../capabilities/) for supported tasks and usage boundaries.

## Typical Clients

- [Codex](./codex/): use ShardingSphere-MCP in Codex CLI or IDE extension.
- [Claude Code](./claude-code/): use ShardingSphere-MCP in Claude Code project or user configuration.

## Choose a Transport

- HTTP is suitable when the MCP Server is started independently and AI applications use a fixed endpoint.
- STDIO is suitable when a local AI application starts ShardingSphere-MCP as a child process.

## HTTP Configuration

Add the following snippet to the AI application's MCP Server configuration. The exact file location depends on the application.
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

Configuration file locations and field names may differ between AI applications. Follow the documentation of the application you use.
For Codex and Claude Code examples, see the corresponding pages in this section.

## STDIO Configuration

Add the following snippet to the AI application's MCP Server configuration. The exact file location depends on the application.
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

Replace `/path/to/apache-shardingsphere-mcp` with the actual distribution directory.

In STDIO mode:

- Diagnostics are written to stderr or `logs/mcp.log`.
- `command` and `args` in the configuration should point to the packaged startup script and STDIO config file.

## Using the Integration

After the AI application is configured with the MCP Server, users describe tasks directly in the conversation.

Examples:

- Show the tables in `<logic-database>`.
- Inspect columns and indexes for `<table-name>`.
- Run a read-only query and limit the result to 100 rows.
- Plan a data encryption or data masking rule and preview it without execution.

When SQL execution, rule changes, or rule change plan execution is involved, review the preview content before confirming execution.
For custom integration or protocol debugging, see the [Developer Appendix](../developer-appendix/).
