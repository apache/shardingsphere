+++
title = "Claude Code"
weight = 2
+++

This page explains how to connect Claude Code to an already running ShardingSphere-MCP HTTP Server, or let Claude Code start a local STDIO MCP Server. ShardingSphere-MCP is the Apache ShardingSphere MCP Server that exposes database metadata access, controlled SQL queries, and database governance capabilities to MCP-capable AI clients and platforms.

## Applicable Scenarios

- Use this page when you want to connect an already running ShardingSphere-MCP HTTP Server to Claude Code CLI.
- Use this page when you want Claude Code to start ShardingSphere-MCP locally through STDIO in a local development environment.
- After integration, Claude Code can inspect logic databases, inspect table structures, run controlled read-only queries, or call `database_gateway_validate_runtime_database` for preflight validation.

## Prerequisites

- Prepare the ShardingSphere-MCP distribution and database configuration by following [Quick Start](../../quick-start/).
- Claude Code CLI is available.
- For HTTP transport, the Claude Code environment can reach `http://127.0.0.1:18088/mcp`, or the actual MCP Server address you configured.

## Integration Steps

### Choose an integration method

- Choose HTTP when ShardingSphere-MCP is already started independently, or when multiple applications need to share the same service.
- Choose STDIO when ShardingSphere-MCP is only used in a local development environment and Claude Code should start it on demand.

### Configure the integration

Use an HTTP MCP Server:

```bash
claude mcp add --transport http shardingsphere http://127.0.0.1:18088/mcp
```

To make the MCP Server available across all Claude Code projects for the current user, use user scope:

```bash
claude mcp add --transport http --scope user shardingsphere http://127.0.0.1:18088/mcp
```

You can also create `.mcp.json` in the project root:

```json
{
  "mcpServers": {
    "shardingsphere": {
      "type": "http",
      "url": "http://127.0.0.1:18088/mcp"
    }
  }
}
```

If you want Claude Code to start ShardingSphere-MCP as a local process, use STDIO:

```bash
claude mcp add --transport stdio shardingsphere -- \
  /path/to/apache-shardingsphere-mcp/bin/start.sh \
  /path/to/apache-shardingsphere-mcp/conf/mcp-stdio.yaml
```

Replace `/path/to/apache-shardingsphere-mcp` with the actual distribution directory.

### Verify the integration

Recognition succeeds when:

- Run `claude mcp list` and confirm that `shardingsphere` appears in the MCP Server list.
- Run the following command in Claude Code:

```text
/mcp
```

Invocation succeeds when:

- In a Claude Code conversation, run a minimal validation task such as:
  - Show the tables in `<logic-database>`.
  - Show columns and indexes for the `orders` table.
  - Run `database_gateway_validate_runtime_database` against a configured runtime database.
- If the tools are listed and the validation query succeeds, the integration is working.

## Notes

- When using STDIO, Claude Code must be able to access the local ShardingSphere-MCP distribution and the matching configuration file.
- A single `shardingsphere` server name should map to only one integration method. If you need both HTTP and STDIO, use different server names.
- See the [Capability Catalog](../../capabilities/) for the supported task surface and usage boundaries.
- When SQL execution or rule changes are involved, review the preview content before confirming execution.
- If you want to integrate ShardingSphere-MCP through the Anthropic platform API, use [Anthropic MCP Connector](../anthropic-mcp-connector/) instead.

## References

### Related documents

- [Quick Start](../../quick-start/)
- [Capability Catalog](../../capabilities/)
- [Configuration](../../configuration/)
- [Anthropic MCP Connector](../anthropic-mcp-connector/)

### Official references

- [Connect Claude Code to tools via MCP](https://code.claude.com/docs/en/mcp)
