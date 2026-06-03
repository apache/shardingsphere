+++
title = "Claude Code"
weight = 2
+++

This page explains how to connect Claude Code to an already running ShardingSphere-MCP HTTP Server, or let Claude Code start a local STDIO MCP Server.

## Prerequisites

- Prepare the ShardingSphere-MCP distribution and database configuration by following [Quick Start](../../quick-start/).
- Claude Code CLI is available.
- For HTTP transport, the Claude Code environment can reach `http://127.0.0.1:18088/mcp`, or the actual MCP Server address you configured.

## Add an HTTP MCP Server

Run the following command from the project where you use Claude Code:

```bash
claude mcp add --transport http shardingsphere http://127.0.0.1:18088/mcp
```

Verify that it has been added:

```bash
claude mcp list
```

Run the following command in Claude Code:

```text
/mcp
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

## Use the STDIO MCP Server

If you want Claude Code to start ShardingSphere-MCP as a local process, use STDIO:

```bash
claude mcp add --transport stdio shardingsphere -- \
  /path/to/apache-shardingsphere-mcp/bin/start.sh \
  /path/to/apache-shardingsphere-mcp/conf/mcp-stdio.yaml
```

Replace `/path/to/apache-shardingsphere-mcp` with the actual distribution directory.

## Usage

Describe database tasks directly in Claude Code, for example:

- Show the tables in `<logic-database>`.
- Show columns and indexes for the `orders` table.
- Query the first 10 rows from the `orders` table.
- Plan reversible encryption for `orders.status` and preview it without execution.

When SQL execution or rule changes are involved, review the preview content before confirming execution.

## Reference

- [Connect Claude Code to tools via MCP](https://code.claude.com/docs/en/mcp)
